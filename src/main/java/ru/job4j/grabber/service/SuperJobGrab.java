package ru.job4j.grabber.service;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import ru.job4j.grabber.store.Store;
import ru.job4j.grabber.utils.DateTimeParser;

public class SuperJobGrab implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        var store = (Store) context.getJobDetail().getJobDataMap().get("store");
        var parser = new HabrCareerParse(new DateTimeParser());
        for (var post : parser.fetch()) {
            store.save(post);
        }
    }

}
