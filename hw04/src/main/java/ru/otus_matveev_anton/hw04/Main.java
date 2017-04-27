package ru.otus_matveev_anton.hw04;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * Created by Matveev.AV1 on 26.04.2017.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Starting pid: " + ManagementFactory.getRuntimeMXBean().getName());

        GCMonitor monitor = new GCMonitor(60);

        MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName("ru.otus_matveev_anton:type=Benchmark");
        Benchmark mBean = new Benchmark();
        beanServer.registerMBean(mBean, name);

        mBean.setCountAddedElemPerIter(3);
        mBean.setCountRemovedElemPerIter(1);
        mBean.run();

    }
}
