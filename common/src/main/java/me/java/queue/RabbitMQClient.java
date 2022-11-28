package me.java.queue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

public class RabbitMQClient {

    public static class RabbitMQQueues {
        public static final String BROWSER_QUEUE = "browser_queue";
        public static final String PROCESS_QUEUE = "process_queue";
        public static final String FINISH_BROWSER_QUEUE = "finish_browser_queue";
        public static final String FINISH_PROCESS_QUEUE = "finish_process_queue";
    }

    public static final RabbitMQClient INSTANCE;

    static {
        try {
            INSTANCE = new RabbitMQClient(Dotenv.load().get("RABBITMQ_URI"));
        } catch (URISyntaxException | NoSuchAlgorithmException | KeyManagementException | IOException | TimeoutException e) {
            e.printStackTrace();
            System.exit(-1);
            throw new RuntimeException(e);
        }
    }

    private final Connection connection;
    private final Channel channel;

    public RabbitMQClient(String uri) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(new URI(uri));
        factory.setVirtualHost("/");

        this.connection = factory.newConnection();
        this.channel = this.connection.createChannel();
        this.declareQueues();
    }

    private void declareQueues() throws IOException {
        this.channel.queueDeclare(RabbitMQQueues.BROWSER_QUEUE, true, false, false, null);
        this.channel.queueDeclare(RabbitMQQueues.PROCESS_QUEUE, true, false, false, null);
        this.channel.queueDeclare(RabbitMQQueues.FINISH_BROWSER_QUEUE, true, false, false, null);
        this.channel.queueDeclare(RabbitMQQueues.FINISH_PROCESS_QUEUE, true, false, false, null);
    }

    public void registerQueueListener(String queue, DeliverCallback callback) throws IOException {
        this.channel.basicConsume(queue, false, callback, consumerTag -> {});
    }

    public void unregisterQueueListener(String queue) throws IOException {
        this.channel.basicCancel(queue);
    }

    public void publishJob(String queue, String message) throws IOException {
        this.channel.basicPublish("", queue, null, message.getBytes());
    }

    public void setPrefetchCount(int count) throws IOException {
        this.channel.basicQos(count <= 0 ? 1 : count, false);
    }

    public void sendACK(Long deliveryTag) throws IOException {
        this.channel.basicAck(deliveryTag, false);
    }

    public void sendNACK(Long deliveryTag) throws IOException {
        this.channel.basicReject(deliveryTag, true);
    }

    public void close() throws IOException, TimeoutException {
        this.channel.close();
        this.connection.close();
    }

}
