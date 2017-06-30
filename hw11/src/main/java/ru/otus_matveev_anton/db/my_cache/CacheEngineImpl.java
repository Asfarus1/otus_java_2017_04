package ru.otus_matveev_anton.db.my_cache;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

public class CacheEngineImpl<K, V> implements CacheEngine<K, V>, CacheEngineImplMBean {

    private final int DEFAULT_THRESHOLD_TIME_S = 10;

    private int maxElements;
    private long lifeTimeS;
    private long idleTimeS;
    private volatile boolean isEternal;
    private int timeThresholdS = DEFAULT_THRESHOLD_TIME_S;

    private final Map<K, MyElement<K, V>> elements = new ConcurrentHashMap<>();
    private Timer timer = new Timer();
    private ScheduledExecutorService cleaner = Executors.newScheduledThreadPool(1);
    private final String name;

    private AtomicLong hit = new AtomicLong(0);
    private AtomicLong miss = new AtomicLong(0);

    private final Runnable taskForCacheCleanind = () -> {
            final long fLifeTimeS = getLifeTimeS() * 1000;
            final long fIdleTimeS = getIdleTimeS() * 1000;

            if (!isEternal) {
                if (fLifeTimeS > 0) {
                    removeIf(e -> System.currentTimeMillis() > e.getCreationTime() + fLifeTimeS);
                }
                if (fIdleTimeS > 0) {
                    removeIf(e -> System.currentTimeMillis() > e.getLastAccessTime() + fIdleTimeS);
                }
            }

            prepareTimer();
    };

    private PrintStream log = System.out;

    public CacheEngineImpl(String name, String... configFiles) {
        Properties props = new Properties();
        Class clazz = this.getClass();
        this.name = name;
        try {
            for (String file : configFiles) {
                try (InputStream is = clazz.getResourceAsStream(file)) {
                    props.load(is);
                }
            }


        String propValue = props.getProperty("max_elements");
        if (propValue != null) {
            maxElements = Integer.valueOf(propValue);
        }

        propValue = props.getProperty("life_time_seconds");
        if (propValue != null) {
            lifeTimeS = Integer.valueOf(propValue);
        }

        propValue = props.getProperty("idle_time_seconds");
        if (propValue != null) {
            idleTimeS = Integer.valueOf(propValue);
        }

        propValue = props.getProperty("time_threshold_seconds");
        if (propValue != null) {
            setTimeThresholdS(Integer.valueOf(propValue));
        }

        propValue = props.getProperty("is_eternal");
        isEternal = propValue != null && Boolean.valueOf(propValue) || idleTimeS == 0 && lifeTimeS == 0;


        MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
        ObjectName oName = new ObjectName("ru.otus_matveev_anton.db.my_cache:type=my_cache_" + name);
        beanServer.registerMBean(this, oName);

        log.printf("create cache %s PID=%s%n", this, ManagementFactory.getRuntimeMXBean().getName());

        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        prepareTimer();
    }

    @Override
    public String toString() {
        return "CacheEngineImpl{" +
                "name='" + name + '\'' +
                ", maxElements=" + maxElements +
                ", lifeTimeS=" + lifeTimeS +
                ", idleTimeS=" + idleTimeS +
                ", isEternal=" + isEternal +
                ", timeThresholdS=" + timeThresholdS +
                '}';
    }

    private void prepareTimer() {
        cleaner.schedule(taskForCacheCleanind, timeThresholdS > 1 ? timeThresholdS : DEFAULT_THRESHOLD_TIME_S, TimeUnit.SECONDS);
    }

    private void removeIf(Predicate<MyElement<K,V>> test){
        elements.values().stream().filter(test).forEach((e)->{
            elements.remove(e.getKey());
            log.printf("removed elem %s%n", e);
        });
    }

    public void put(MyElement<K, V> element) {

        if (maxElements != 0 && elements.size() >= maxElements) {
            MyElement<K, V> first = elements.values().parallelStream().sorted(Comparator.comparingLong(MyElement::getCreationTime)).findFirst().orElse(null);
            if (first != null){
                elements.remove(first.getKey());
                log.printf("removed elem %s%n", first);
            }
        }

        K key = element.getKey();
        elements.put(key, element);
        log.printf("added elem %s%n", element);
    }

    public MyElement<K, V> get(K key) {
        MyElement<K, V> element = elements.get(key);
        if (element != null) {
            hit.incrementAndGet();
            element.setAccessed();
        } else {
            miss.incrementAndGet();
        }
        return element;
    }

    public long getHitCount() {
        return hit.longValue();
    }

    public long getMissCount() {
        return miss.longValue();
    }

    @Override
    public void dispose() {
        cleaner.shutdown();
    }

//Getters and setters
    public int getTimeThresholdMs() {
        return timeThresholdS;
    }

    public void setTimeThresholdS(int timeThresholdS) {
        if (timeThresholdS < 2) throw new IllegalArgumentException("time threshold value can`t be less then 2");
        this.timeThresholdS = timeThresholdS;
    }

    public int getMaxElements() {
        return maxElements;
    }

    public void setMaxElements(int maxElements) {
        if (maxElements < 0) throw new IllegalArgumentException("max elements value can`t be less then 0");
        if (maxElements < this.maxElements){
            elements.clear();
        }
        this.maxElements = maxElements;
    }

    public long getLifeTimeS() {
        return lifeTimeS;
    }

    public void setLifeTimeS(long lifeTimeS) {
        if (maxElements < 0) throw new IllegalArgumentException("life time value can`t be less then 0");
        this.lifeTimeS = lifeTimeS;
    }

    public long getIdleTimeS() {
        return idleTimeS;
    }

    public void setIdleTimeS(long idleTimeS) {
        if (idleTimeS < 0) throw new IllegalArgumentException("idle time value can`t be less then 0");
        this.idleTimeS = idleTimeS;
    }

    public boolean isEternal() {
        return isEternal;
    }

    public void setEternal(boolean eternal) {
        isEternal = eternal;
    }

    public int getTimeThresholdS() {
        return timeThresholdS;
    }

    @Override
    public int getSize() {
        return elements.size();
    }
}
