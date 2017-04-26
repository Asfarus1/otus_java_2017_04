package ru.otus_matveev_anton;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * Created by Matveev.AV1 on 26.04.2017.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Starting pid: " + ManagementFactory.getRuntimeMXBean().getName());

        int size = 5 * 1000 * 1000;
        //int size = 50 * 1024 * 1024;//for OOM with -Xms512m
        //int size = 50 * 1024 * 102; //for small dump

        MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName("ru.otus_matveev_anton:type=BenchMark");
        BenchMark mBean = new BenchMark();
        beanServer.registerMBean(mBean, name);

        mBean.setSize(size);
        mBean.setMemoryGrowthRate(1000);
        mBean.run();
    }
}
