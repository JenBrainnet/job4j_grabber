package ru.job4j.grabber.service;

import org.apache.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import ru.job4j.grabber.store.Store;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class ScheduleManager implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(ScheduleManager.class);
    private Scheduler scheduler;

    public void init() {
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
        } catch (SchedulerException se) {
            LOGGER.error("When init scheduler", se);
        }
    }

    public void load(int period, Class<SuperJobGrab> task, Store store) {
        try {
            var data = new JobDataMap();
            data.put("store", store);
            var job = newJob(task)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(period)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException se) {
            LOGGER.error("When init job", se);
        }
    }

    @Override
    public void close() {
        if (scheduler != null) {
            try {
                scheduler.shutdown();
            } catch (SchedulerException se) {
                LOGGER.error("When shutdown scheduler", se);
            }
        }
    }
}
