package me.java.playwrightservice.services;

import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import me.java.playwrightservice.utils.Queues;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Paths;

public class BrowserService extends Service {

    private final boolean headless = true;

    private void consume() {
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

        while(true) {
            String url;
            try {
                url = Queues.BROWSER_QUEUE.take().url();
            } catch (InterruptedException e) {
                break;
            }

            boolean bb = true;

            String content = "<html></html>";
            if (bb) {
                page.navigate(url);
                content = page.content();
                //page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("example.png")));
            } else {
                try {
                    HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
                    conn.setInstanceFollowRedirects(true);
                    int status = conn.getResponseCode();

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuffer ccontent = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        ccontent.append(inputLine);
                    }
                    in.close();
                    content = ccontent.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }



            try {
                Queues.PROCESS_QUEUE.put(new Queues.ProcessData(url, content));
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
        return new Thread(this::consume);
    }
}
