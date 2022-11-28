package me.java;

import me.java.scrapers.NikeScraper;
import me.java.scrapers.Scraper;

import java.util.HashMap;

public class ScrapeManager {

    private final HashMap<String, Class> scrapers;
    public static final ScrapeManager INSTANCE = new ScrapeManager();

    protected ScrapeManager() {
        this.scrapers = new HashMap<>();
        this.registerScrapers();
    }

    private void addScraper(String url, Class scraper) {
        this.scrapers.put(url, scraper);
    }

    private void registerScrapers() {
        this.addScraper("nike.com", NikeScraper.class);
    }

    public synchronized Scraper newScraper(String url) {
        if (this.scrapers.isEmpty()) {
            this.registerScrapers();
        }
        Class scraper = null;

        for (String key : this.scrapers.keySet()) {
            if (url.contains(key)) {
                scraper = this.scrapers.get(key);
                break;
            }
        }

        if (scraper == null) {
            return null;
        }
        try {
            return (Scraper) scraper.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



}
