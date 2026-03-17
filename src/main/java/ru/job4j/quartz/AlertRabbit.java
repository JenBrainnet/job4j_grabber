package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class AlertRabbit {

    public static void main(String[] args) {
        try {
            int interval = Integer.parseInt(getConfig().getProperty("rabbit.interval"));
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();

            JobDetail job = newJob(Rabbit.class)
                    .usingJobData("param1", "Hello, Rabbit!")
                    .usingJobData("param2", 42)
                    .build();

            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(interval)
                    .repeatForever();

            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();

            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException se) {
            se.printStackTrace();
        }
    }

    public static class Rabbit implements Job {

        @Override
        public void execute(JobExecutionContext context) {
            String param1 = context.getJobDetail().getJobDataMap().getString("param1");
            int param2 = context.getJobDetail().getJobDataMap().getInt("param2");
            System.out.println("Rabbit runs here with param1: " + param1 + " and param2: " + param2);
        }

    }

    private static Properties getConfig() {
        Properties properties = new Properties();
        try (InputStream in =
                     AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            properties.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

}
