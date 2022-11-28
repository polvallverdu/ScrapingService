package me.java.playwrightservice.utils;

import com.google.gson.JsonObject;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Queues {

    public record BrowserData(String url) {}
    public record ProcessData(String url, String content) {}
    public record FinishedData(JsonObject finalData) {}

    public static final BlockingQueue<BrowserData> BROWSER_QUEUE = new LinkedBlockingQueue<>();
    public static final BlockingQueue<ProcessData> PROCESS_QUEUE = new LinkedBlockingQueue<>();
    public static final BlockingQueue<FinishedData> FINISH_QUEUE = new LinkedBlockingQueue<>();

}
