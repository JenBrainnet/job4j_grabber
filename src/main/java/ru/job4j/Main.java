package ru.job4j;

import org.apache.log4j.Logger;
import ru.job4j.grabber.service.Config;
import ru.job4j.grabber.service.ScheduleManager;
import ru.job4j.grabber.service.SuperJobGrab;
import ru.job4j.grabber.service.Web;
import ru.job4j.grabber.store.JdbcStore;

import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {

    private final static Logger LOGGER = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        var config = new Config();
        config.load("application.properties");
        try (var connection = DriverManager.getConnection(
                config.get("db.url"),
                config.get("db.username"),
                config.get("db.password"));
            var scheduler = new ScheduleManager()) {
            var store = new JdbcStore(connection);
            scheduler.init();
            scheduler.load(
                    Integer.parseInt(config.get("rabbit.interval")),
                    SuperJobGrab.class,
                    store
            );
            new Web(store).start(Integer.parseInt(config.get("server.port")));
            Thread.currentThread().join();
        } catch (SQLException | InterruptedException e) {
            LOGGER.error("When create a connection", e);
        }
    }

}
