package ru.job4j.grabber.service;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import ru.job4j.grabber.model.Post;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final Logger LOGGER = Logger.getLogger(HabrCareerParse.class);
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PREFIX = "/vacancies?page=";
    private static final String SUFFIX = "&q=Java%20developer&type=all";

    @Override
    public List<Post> fetch() {
        var result = new ArrayList<Post>();
        try {
            int pageNumber = 1;
            String fullLink = "%s%s%d%s".formatted(SOURCE_LINK, PREFIX, pageNumber, SUFFIX);
            var connection = Jsoup.connect(fullLink);
            var document = connection.get();
            var rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                var dataElement = row.select(".vacancy-card__date").first();
                var timeElement = dataElement.child(0);
                String dateTime = timeElement.attr("datetime");
                long time = OffsetDateTime.parse(dateTime).toInstant().toEpochMilli();
                var titleElement = row.select(".vacancy-card__title").first();
                var linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                var post = new Post();
                post.setTitle(vacancyName);
                post.setLink(link);
                post.setTime(time);
                result.add(post);
                System.out.println(post);
            });

        } catch (IOException e) {
            LOGGER.error("When load page", e);
        }
        return result;
    }

}
