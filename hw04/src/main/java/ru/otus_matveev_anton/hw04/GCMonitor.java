package ru.otus_matveev_anton.hw04;

import com.sun.management.GarbageCollectionNotificationInfo;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Matveev.AV1 on 27.04.2017.
 */
public class GCMonitor implements NotificationListener {

    private final Queue<GCStats> logs = new ConcurrentLinkedQueue<>();
    private final int rateSeconds;
    private long lastTime;

    public GCMonitor(int rateSeconds) {
        this.rateSeconds = rateSeconds;
        start();
    }

    private static class GCStats {
        private final String gcName;
        private final long time;
        private final long durationMs;

        GCStats(String gcName, long time, long durationMs) {
            this.gcName = gcName;
            this.time = time;
            this.durationMs = durationMs;
        }
    }

    private void start() {
        List<GarbageCollectorMXBean> gcbeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gcbean : gcbeans) {
            NotificationEmitter emitter = (NotificationEmitter) gcbean;
            emitter.addNotificationListener(this, null, null);
        }
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        lastTime = System.currentTimeMillis();
        service.scheduleAtFixedRate(GCMonitor.this::log, rateSeconds, rateSeconds, TimeUnit.SECONDS);
    }

    private static class Vals {
        int countCollects = 0, duration = 0;
    }

    private void log() {
        Map<String, Vals> statMap = new HashMap<>();

        long curTime = System.currentTimeMillis();
        Vals curVals;
        GCStats curStat;

        while (true) {
            curStat = logs.peek();
            if (curStat == null || curStat.time >= curTime) {
                break;
            }
            logs.poll();
            statMap.putIfAbsent(curStat.gcName, new Vals());

            curVals = statMap.get(curStat.gcName);
            curVals.countCollects++;
            curVals.duration += curStat.durationMs;
        }
        statMap.forEach((k, v) -> {
            System.out.printf("%tT %-15s : %-3d сбоорок за %-3d секунд, продолжительность %-6d мс. (%-3d c.)%n", new Date(curTime), k, v.countCollects, (curTime - lastTime) / 1000, v.duration, v.duration / 1000);
        });
        lastTime = curTime;
    }

    @Override
    public void handleNotification(Notification notification, Object handback) {
        if (notification.getType().equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {
            GarbageCollectionNotificationInfo gcInfo = (GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData()));
            logs.add(new GCStats(gcInfo.getGcName(), gcInfo.getGcInfo().getStartTime(), gcInfo.getGcInfo().getDuration()));
        }
    }
}
