package ru.job4j.grabber.service;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import ru.job4j.grabber.model.Post;
import ru.job4j.grabber.utils.DateTimeParser;

import java.io.IOException;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class HabrCareerParse implements Parse {

    private static final Logger LOGGER = Logger.getLogger(HabrCareerParse.class);
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PREFIX = "/vacancies?page=";
    private static final String SUFFIX = "&q=Java%20developer&type=all";
    private static final int PAGES = 5;
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @Override
    public List<Post> fetch() {
        var result = new ArrayList<Post>();
        try {
            for (int pageNumber = 1; pageNumber <= PAGES; pageNumber++) {
                String fullLink = "%s%s%d%s".formatted(SOURCE_LINK, PREFIX, pageNumber, SUFFIX);
                var document = Jsoup.connect(fullLink).get();
                var rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> result.add(createPost(row)));
            }
        } catch (IOException e) {
            LOGGER.error("When load page", e);
        }
        return result;
    }

    private Post createPost(Element row) {
        var dataElement = row.select(".vacancy-card__date").first();
        var timeElement = dataElement.child(0);
        String dateTime = timeElement.attr("datetime");
        long time = dateTimeParser.parse(dateTime)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        var titleElement = row.select(".vacancy-card__title").first();
        var linkElement = titleElement.child(0);
        String vacancyName = titleElement.text();
        String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
        String description = retrieveDescription(link);

        var post = new Post();
        post.setTitle(vacancyName);
        post.setLink(link);
        post.setDescription(description);
        post.setTime(time);
        return post;
    }

    private String retrieveDescription(String link) {
        StringBuilder result = new StringBuilder();
        String ln = System.lineSeparator();
        try {
            var document = Jsoup.connect(link).get();
            var salaryElement = document.select(".vacancy-header__salary").first();
            if (salaryElement != null) {
                result.append("Зарплата: ")
                        .append(salaryElement.text())
                        .append(ln);
            }
            var companyElement = document.select(".vacancy-company__title").first();
            if (companyElement != null) {
                result.append("Компания: ")
                        .append(companyElement.text())
                        .append(ln);
            }
            var tags = document.select(".chip-with-icon__text");
            if (!tags.isEmpty()) {
                result.append("Требования и условия: ");
                result.append(String.join(", ", tags.eachText()));
                result.append(ln);
            }
            var descriptionElement = document.select(".vacancy-description__text").first();
            if (descriptionElement != null) {
                result.append("Описание вакансии:").append(ln);
                Set<String> unique = descriptionElement.select("p, li, h2, h3")
                        .stream()
                        .map(Element::text)
                        .filter(t -> !t.isBlank())
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                unique.forEach(element -> result.append(element).append(ln));
            }
        } catch (IOException e) {
            LOGGER.error("When retrieve description", e);
        }
        return result.toString();
    }

}
