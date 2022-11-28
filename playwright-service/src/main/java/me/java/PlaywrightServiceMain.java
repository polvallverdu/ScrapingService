package me.java;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.java.queue.RabbitMQClient;
import me.java.queue.RedisClient;
import me.java.service.BrowserService;
import me.java.utils.Queues;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class PlaywrightServiceMain {

    private static ArrayList<Long> deliveryTags = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        BrowserService browserService = new BrowserService();
        browserService.setThreads(5);
        RabbitMQClient.INSTANCE.setPrefetchCount(5);
        RabbitMQClient.INSTANCE.registerQueueListener(RabbitMQClient.RabbitMQQueues.BROWSER_QUEUE, (consumerTag, message) -> {
            Long deliveryTag = message.getEnvelope().getDeliveryTag();
            deliveryTags.add(deliveryTag);

            JsonObject jsonObject = JsonParser.parseString(new String(message.getBody())).getAsJsonObject();
            System.out.println("Received message: " + jsonObject);

            try {
                Queues.BROWSER_QUEUE.put(new Queues.BrowserData(deliveryTag, jsonObject.get("url").getAsString(), jsonObject.get("toProcess").getAsBoolean()));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        while (true) {
            Queues.FinishedBrowserData finishData;

            try {
                finishData = Queues.FINISH_BROWSER_QUEUE.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                stop();
                System.exit(-1);
                break;
            }

            String uuid = UUID.randomUUID().toString();
            RedisClient.INSTANCE.add(uuid, finishData.content(), 5*60l);

            RabbitMQClient.INSTANCE.publishJob(finishData.toProcess() ? RabbitMQClient.RabbitMQQueues.PROCESS_QUEUE : RabbitMQClient.RabbitMQQueues.FINISH_BROWSER_QUEUE, "{\"url\":\"" + finishData.url() + "\", \"uuid\": \"" + uuid + "\"}");

            deliveryTags.remove(finishData.deliveryTag());
            RabbitMQClient.INSTANCE.sendACK(finishData.deliveryTag());
        }
    }

    private static void stop() {
        deliveryTags.forEach((deliveryTag) -> {
            try {
                RabbitMQClient.INSTANCE.sendNACK(deliveryTag);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        try {
            RabbitMQClient.INSTANCE.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}