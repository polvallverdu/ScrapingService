package me.java.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.java.ScrapeManager;
import me.java.scrapers.Scraper;
import me.java.services.Service;
import me.java.utils.Queues;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class ScraperService extends Service {

    private void consumer() {
        while (true) {
            Queues.ProcessData processData;
            try {
                processData = Queues.PROCESS_QUEUE.take();
            } catch (InterruptedException e) {
                break;
            }

            Document document = Jsoup.parse(processData.content());

            Scraper scraper = ScrapeManager.INSTANCE.newScraper(processData.url());
            if (scraper == null) {
                // TODO: ERROR URL NOT FOUND
            }
            if (!scraper.process(document)) {
                // TODO: ERROR BAD DOCUMENT
            }

            JsonObject finalData = scraper.toJson();
            try {
                Queues.FINISH_PROCESS_QUEUE.put(new Queues.FinishedProcessData(processData.deliveryTag(), finalData.toString()));
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    @Override
    protected Thread createThread() {
        return new Thread(this::consumer);
    }
}
