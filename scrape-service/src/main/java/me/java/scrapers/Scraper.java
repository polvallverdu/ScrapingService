package me.java.scrapers;

import com.google.gson.JsonObject;
import org.jsoup.nodes.Document;

import java.util.HashMap;

public abstract class Scraper {

    protected Document content;

    protected String url;
    protected String brand;
    protected String name;
    protected String price;
    protected String originalPrice;
    protected String description;
    protected HashMap<String, Boolean> sizes = new HashMap<>();

    public Scraper() {
    }

    public Scraper(String url, String brand, String name, String price, String originalPrice, String description) {
        this.url = url;
        this.brand = brand;
        this.name = name;
        this.price = price;
        this.originalPrice = originalPrice;
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public String getBrand() {
        return brand;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getOriginalPrice() {
        return originalPrice;
    }

    public String getDescription() {
        return description;
    }

    public Scraper setUrl(String url) {
        this.url = url;
        return this;
    }

    public Scraper setBrand(String brand) {
        this.brand = brand;
        return this;
    }

    public Scraper setName(String name) {
        this.name = name;
        return this;
    }

    public Scraper setPrice(String price) {
        this.price = price;
        return this;
    }

    public Scraper setOriginalPrice(String originalPrice) {
        this.originalPrice = originalPrice;
        return this;
    }

    public Scraper setDescription(String description) {
        this.description = description;
        return this;
    }

    public abstract boolean process(Document content);

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("url", this.url);
        jsonObject.addProperty("brand", this.brand);
        jsonObject.addProperty("name", this.name);
        jsonObject.addProperty("price", this.price);
        jsonObject.addProperty("originalPrice", this.originalPrice);
        jsonObject.addProperty("description", this.description);

        JsonObject sizes = new JsonObject();
        for (String size : this.sizes.keySet()) {
            sizes.addProperty(size, this.sizes.get(size));
        }
        jsonObject.add("sizes", sizes);

        return jsonObject;
    }
}
