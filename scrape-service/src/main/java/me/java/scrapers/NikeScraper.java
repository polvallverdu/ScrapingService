package me.java.scrapers;

import org.jsoup.nodes.Document;
import us.codecraft.xsoup.XElements;
import us.codecraft.xsoup.Xsoup;

public class NikeScraper extends Scraper {

    @Override
    public boolean process(Document content) {
        this.content = content;
        this.url = content.location();

        String title = this.getFromXpath("/html/body/div[4]/div/div/div[2]/div/div[4]/div[2]/div[2]/div/div/div[1]/div/div[2]/div/h1");
        this.description = this.getFromXpath("/html/body/div[4]/div/div/div[2]/div/div[4]/div[2]/div[2]/div/div/div[1]/div/div[2]/div/h2");
        this.name = title + " - " + this.description;

        // TODO: FIX THIS
        // for size in grid:
        //      s = size[1].text
        //      self.sizes[s] = "disabled" not in size[0].attrib
        var grid = this.getElement("/html/body/div[ ]/div/div/div[2]/div/div[4]/div[2]/div[2]/div/div/div[3]/form/div[1]/fieldset/div");
        for (var size : grid.getElements()) {
            var s = size.select("div[1]").text();
            this.sizes.put(s, !size.select("div[1]").attr("class").contains("disabled"));
        }

        this.price = this.getFromXpath("/html/body/div[4]/div/div/div[2]/div/div[4]/div[2]/div[2]/div/div/div[1]/div/div[2]/div/div/div/div/div[2]");
        if (this.price.equals("")) {
            this.price = this.getFromXpath("/html/body/div[4]/div/div/div[2]/div/div[4]/div[2]/div[2]/div/div/div[1]/div/div[2]/div/div/div/div/div");
        }

        this.originalPrice = this.getFromXpath("/html/body/div[4]/div/div/div[2]/div/div[4]/div[2]/div[2]/div/div/div[1]/div/div[2]/div/div/div/div/div[2]/text()");
        if (this.originalPrice.equals("")) {
            this.originalPrice = price;
        }

        // TODO: Format price
        this.brand = "Nike Sportswear";


        return true;
    }

    private String getFromXpath(String xpath) {
        return this.getElement(xpath).get();
    }

    private XElements getElement(String xpath) {
        return Xsoup.compile(xpath).evaluate(content);
    }

}
