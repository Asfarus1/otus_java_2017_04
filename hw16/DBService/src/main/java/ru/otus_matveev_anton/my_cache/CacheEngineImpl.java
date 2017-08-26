package ru.otus_matveev_anton.my_cache;

import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.ref.SoftReference;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

public class CacheEngineImpl<K, V> implements CacheEngine<K, V>, CacheEngineImplMBean {

    private static final int CHANGE_STATS_MONITORING_DELAY = 2000;
    private final long DEFAULT_THRESHOLD_TIME_S = 10;

    private int maxElements;
    private long lifeTimeS;
    private long idleTimeS;
    private volatile boolean isEternal;
    private long timeThresholdS = DEFAULT_THRESHOLD_TIME_S;

    private volatile boolean isStatsChanged;
    private volatile boolean isPropsChanged;

    private final Map<K, SoftReference<MyElement<K, V>>> elements = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = Executors.newScheduledThreadPool(2);
    private final String name;

    private final AtomicLong hit = new AtomicLong(0);
    private final AtomicLong miss = new AtomicLong(0);

    private final List<CacheStatsChangedListener> changedStatsListeners = new CopyOnWriteArrayList<>();
    private final List<CachePropsChangedListener> changedPropsListeners = new CopyOnWriteArrayList<>();

    private final Runnable taskForCacheCleaning = () -> {
        final long fLifeTimeS = getLifeTimeS() * 1000;
        final long fIdleTimeS = getIdleTimeS() * 1000;

        Predicate<Map.Entry<K, MyElement<K, V>>> test = e -> e.getValue() == null;

        if (!isEternal) {
            if (fLifeTimeS > 0) {
                test.and(e -> System.currentTimeMillis() > e.getValue().getCreationTime() + fLifeTimeS);
            }
            if (fIdleTimeS > 0) {
                removeIf(e -> System.currentTimeMillis() > e.getValue().getLastAccessTime() + fIdleTimeS);
            }
        }

        prepareTimer();
        onChange();
    };

    private final static Logger log = LogManager.getLogger(CacheEngineImpl.class);

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
            ObjectName oName = new ObjectName("ru.otus_matveev_anton.my_cache:type=my_cache_" + name);
            beanServer.registerMBean(this, oName);

            log.info("create cache {} PID={}", this, ManagementFactory.getRuntimeMXBean().getName());

        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        prepareTimer();
        isStatsChanged = true;
        isPropsChanged = true;
        cleaner.schedule(this::onChange, CHANGE_STATS_MONITORING_DELAY, TimeUnit.MILLISECONDS);
    }

    @Override
    public void addCacheStatsChangedListener(CacheStatsChangedListener listener) {
        changedStatsListeners.add(listener);
    }

    @Override
    public void addCachePropsChangedListener(CachePropsChangedListener listener) {
        changedPropsListeners.add(listener);
    }

    @Override
    public void setDataChanged() {
        isPropsChanged = true;
        isStatsChanged = true;
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private void onChange() {
        if (isPropsChanged) {
            isPropsChanged = false;
            isStatsChanged = false;
            for (CachePropsChangedListener listener : changedPropsListeners) {
                listener.onChange(this);
            }
        }
        if (isStatsChanged) {
            isStatsChanged = false;
            for (CacheStatsChangedListener listener : changedStatsListeners) {
                listener.onChange(this);
            }
        }
        cleaner.schedule(this::onChange, CHANGE_STATS_MONITORING_DELAY, TimeUnit.MILLISECONDS);
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
        cleaner.schedule(taskForCacheCleaning, timeThresholdS > 1 ? timeThresholdS : DEFAULT_THRESHOLD_TIME_S, TimeUnit.SECONDS);
    }

    private void removeIf(Predicate<Pair<K, MyElement<K, V>>> test) {
        elements.entrySet().stream()
                .map(e -> new Pair<>(e.getKey(), e.getValue().get()))
                .filter(test)
                .forEach((e) -> {
                            elements.remove(e.getKey());
                            log.debug("removed elem {}", e);
                            isStatsChanged = true;
                        }
                );
    }

    public void put(MyElement<K, V> element) {

        if (maxElements != 0 && elements.size() >= maxElements) {
            Pair<K, MyElement<K, V>> first = elements.entrySet().stream()
                    .map(e -> new Pair<>(e.getKey(), e.getValue().get()))
                    .sorted(Comparator.comparingLong(
                            e -> {
                                MyElement<K, V> el = e.getValue();
                                return el == null ? 0 : el.getCreationTime();
                            }
                    )).findFirst().orElse(null);
            if (first != null) {
                elements.remove(first.getKey());
                log.debug("removed elem {}", first.getValue());
            }
        }

        K key = element.getKey();
        elements.put(key, new SoftReference<>(element));
        log.debug("added elem {}", element);
        isStatsChanged = true;
    }

    public MyElement<K, V> get(K key) {
        SoftReference<MyElement<K, V>> ref = elements.get(key);
        MyElement<K, V> element = ref == null ? null : ref.get();

        if (element != null) {
            hit.incrementAndGet();
            element.setAccessed();
        } else {
            miss.incrementAndGet();
        }
        isStatsChanged = true;
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
    public void setTimeThresholdS(long timeThresholdS) {
        if (timeThresholdS < 2) throw new IllegalArgumentException("time threshold value can`t be less then 2");
        this.timeThresholdS = timeThresholdS;
        isPropsChanged = true;
    }

    public int getMaxElements() {
        return maxElements;
    }

    public void setMaxElements(int maxElements) {
        if (maxElements < 0) throw new IllegalArgumentException("max elements value can`t be less then 0");
        if (maxElements < this.maxElements) {
            elements.clear();
        }
        isPropsChanged = true;
        this.maxElements = maxElements;
    }

    public long getLifeTimeS() {
        return lifeTimeS;
    }

    public void setLifeTimeS(long lifeTimeS) {
        if (maxElements < 0) throw new IllegalArgumentException("life time value can`t be less then 0");
        this.lifeTimeS = lifeTimeS;
        isPropsChanged = true;
    }

    public long getIdleTimeS() {
        return idleTimeS;
    }

    public void setIdleTimeS(long idleTimeS) {
        if (idleTimeS < 0) throw new IllegalArgumentException("idle time value can`t be less then 0");
        this.idleTimeS = idleTimeS;
        isPropsChanged = true;
    }

    public boolean isEternal() {
        return isEternal;
    }

    public void setEternal(boolean eternal) {
        isEternal = eternal;
        isPropsChanged = true;
    }

    public long getTimeThresholdS() {
        return timeThresholdS;
    }

    @Override
    public int getSize() {
        return elements.size();
    }
}
