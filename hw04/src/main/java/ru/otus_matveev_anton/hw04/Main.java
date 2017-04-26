package ru.otus_matveev_anton.hw04;

import com.sun.management.GarbageCollectionNotificationInfo;

import javax.management.MBeanServer;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * Created by Matveev.AV1 on 26.04.2017.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Starting pid: " + ManagementFactory.getRuntimeMXBean().getName());
        installGCMonitoring();
        int size = 5 * 1000 * 1000;
        //int size = 50 * 1024 * 1024;//for OOM with -Xms512m
        //int size = 50 * 1024 * 102; //for small dump

        MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName("ru.otus_matveev_anton:type=Benchmark");
        Benchmark mBean = new Benchmark();
        beanServer.registerMBean(mBean, name);

        mBean.setSize(size);
        mBean.setMemoryGrowthRate(1000);
        mBean.run();
    }

    private static void installGCMonitoring() {
        List<GarbageCollectorMXBean> gcbeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gcbean : gcbeans) {
            NotificationEmitter emitter = (NotificationEmitter) gcbean;
            System.out.println(gcbean.getName());

            NotificationListener listener = (notification, handback) -> {
                if (notification.getType().equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {
                    GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData());

                    long duration = info.getGcInfo().getDuration();
                    String gctype = info.getGcAction();

                    System.out.println(gctype + ": - "
                            + info.getGcInfo().getId() + ", "
                            + info.getGcName()
                            + " (from " + info.getGcCause() + ") " + duration + " milliseconds");

                }
            };

            emitter.addNotificationListener(listener, null, null);
        }
    }
}
