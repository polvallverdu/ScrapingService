package me.java.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.java.services.Service;
import me.java.utils.Queues;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import us.codecraft.xsoup.Xsoup;

public class ScrapperService extends Service {

    private void consumer() {
        while (true) {
            Queues.ProcessData processData;
            try {
                processData = Queues.PROCESS_QUEUE.take();
            } catch (InterruptedException e) {
                break;
            }

            Document document = Jsoup.parse(processData.content());
            //String title = Xsoup.compile("//*[@id=\"pdp_product_title\"]").evaluate(document).get();

            // TODO: Process html

            JsonObject finalData = JsonParser.parseString("{\"link\": \"" + processData.url() + "\"}").getAsJsonObject();
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
