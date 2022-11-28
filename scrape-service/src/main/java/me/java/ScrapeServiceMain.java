package me.java;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.java.queue.RabbitMQClient;
import me.java.queue.RedisClient;
import me.java.service.ScraperService;
import me.java.utils.Queues;

import java.io.IOException;
import java.util.ArrayList;

public class ScrapeServiceMain {

    private static ArrayList<Long> deliveryTags = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        ScraperService browserService = new ScraperService();
        browserService.setThreads(10);
        RabbitMQClient.INSTANCE.setPrefetchCount(10);
        RabbitMQClient.INSTANCE.registerQueueListener(RabbitMQClient.RabbitMQQueues.PROCESS_QUEUE, (consumerTag, message) -> {
            Long deliveryTag = message.getEnvelope().getDeliveryTag();
            deliveryTags.add(deliveryTag);

            JsonObject jsonObject = JsonParser.parseString(new String(message.getBody())).getAsJsonObject();
            System.out.println("Received message: " + jsonObject);
            String contentUUID = jsonObject.get("uuid").getAsString();

            String content = RedisClient.INSTANCE.get(contentUUID, false);

            try {
                Queues.PROCESS_QUEUE.put(new Queues.ProcessData(deliveryTag, jsonObject.get("url").getAsString(), content));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        while (true) {
            Queues.FinishedProcessData finishData;

            try {
                finishData = Queues.FINISH_PROCESS_QUEUE.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                stop();
                System.exit(-1);
                break;
            }

            RabbitMQClient.INSTANCE.publishJob(RabbitMQClient.RabbitMQQueues.FINISH_PROCESS_QUEUE, finishData.data());

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