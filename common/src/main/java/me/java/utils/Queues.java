package me.java.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Queues {

    public record BrowserData(Long deliveryTag, String url, boolean toProcess) {}
    public record ProcessData(Long deliveryTag, String url, String content) {}
    public record FinishedBrowserData(Long deliveryTag, String url, String content, boolean toProcess) {}
    public record FinishedProcessData(Long deliveryTag, String data) {}

    public static final BlockingQueue<BrowserData> BROWSER_QUEUE = new LinkedBlockingQueue<>();
    public static final BlockingQueue<ProcessData> PROCESS_QUEUE = new LinkedBlockingQueue<>();
    public static final BlockingQueue<FinishedBrowserData> FINISH_BROWSER_QUEUE = new LinkedBlockingQueue<>();
    public static final BlockingQueue<FinishedProcessData> FINISH_PROCESS_QUEUE = new LinkedBlockingQueue<>();

}
