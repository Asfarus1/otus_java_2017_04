package ru.otus_matveev_anton.hw04;

import com.sun.management.GarbageCollectionNotificationInfo;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matveev.AV1 on 27.04.2017.
 */
public class GCMonitor implements NotificationListener{

    private LocalTime startTime;

    public GCMonitor() {
        startTime = LocalTime.now();
    }

    private List<GCStats> logs = new ArrayList<>();

    public static class GCStats{
        private final String gcName;
        private final long time;
        private final long durationMs;

        public GCStats(String gcName, long time, long durationMs) {
            this.gcName = gcName;
            this.time = time;
            this.durationMs = durationMs;
        }
    }

    @Override
    public void handleNotification(Notification notification, Object handback) {
        if (notification.getType().equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)){
            GarbageCollectionNotificationInfo gcInfo = (GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData()));
            logs.add(new GCStats(gcInfo.getGcName(), gcInfo.getGcInfo().getStartTime(), gcInfo.getGcInfo().getDuration()));
        }
    }
}
