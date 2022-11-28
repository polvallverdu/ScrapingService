package me.java.service;

import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import me.java.queue.RabbitMQClient;
import me.java.services.Service;
import me.java.utils.Queues;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class BrowserService extends Service {

    private final boolean headless = true;

    private void worker() {
        System.out.println("Starting worker");
        var playwright = Playwright.create();
        var browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless));

        var ctx = browser.newContext();
        ctx.route("**/*", (route) -> {
            //System.out.println(route.request().url() + " - " + route.request().resourceType());
            String resourceType = route.request().resourceType();
            //if (resourceType.equals("script") || resourceType.equals("document") || resourceType.equals("xhr") || resourceType.equals("fetch")) {
            if (resourceType.equals("document") || resourceType.equals("xhr")) {
                route.resume();
            } else {
                route.abort();
            }
        });

        Page page = ctx.newPage();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {return;}

        System.out.println("Worker started");
        while(true) {
            Queues.BrowserData data;
            try {
                data = Queues.BROWSER_QUEUE.take();
            } catch (InterruptedException e) {
                break;
            }
            System.out.println("Starting job with url: " + data.url());

            String content;
            try {
                HttpsURLConnection conn = (HttpsURLConnection) new URL(data.url()).openConnection();
                conn.setInstanceFollowRedirects(true);
                int status = conn.getResponseCode();

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder rawContent = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    rawContent.append(inputLine);
                }
                in.close();
                content = rawContent.toString();
            } catch (Exception e) {
                // TODO: ERROR HANDLING
                try {
                    RabbitMQClient.INSTANCE.sendNACK(data.deliveryTag());
                } catch (Exception ignored) {}
                e.printStackTrace();
                continue;
            }

            System.out.println("Finished job with url: " + data.url());

            try {
                Queues.FINISH_BROWSER_QUEUE.put(new Queues.FinishedBrowserData(data.deliveryTag(), data.url(), content, data.toProcess()));
            } catch (InterruptedException e) {
                break;
            }
        }

        page.close();
        browser.close();
        playwright.close();
    }

    @Override
    protected Thread createThread() {
        return new Thread(this::worker);
    }
}
