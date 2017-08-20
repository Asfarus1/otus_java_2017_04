package ru.otus_matveev_anton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus_matveev_anton.db.DBService;
import ru.otus_matveev_anton.db.DBServiceMyOrmImpl;
import ru.otus_matveev_anton.db.DBServiceWithCache;
import ru.otus_matveev_anton.db.data_sets.UserDataSet;
import ru.otus_matveev_anton.db.my_orm.MapperFactory;
import ru.otus_matveev_anton.db.my_orm.MapperFactoryImpl;
import ru.otus_matveev_anton.db.my_orm.MyOrmConfig;
import ru.otus_matveev_anton.genaral.Addressee;
import ru.otus_matveev_anton.genaral.AddresseeImpl;
import ru.otus_matveev_anton.genaral.MessageSystemClient;
import ru.otus_matveev_anton.genaral.SpecialAddress;
import ru.otus_matveev_anton.json_message_system.JsonSocketClient;
import ru.otus_matveev_anton.messages.CacheFullDataSet;
import ru.otus_matveev_anton.messages.CacheGetCurrentProps;
import ru.otus_matveev_anton.messages.CachePropsDataSet;
import ru.otus_matveev_anton.messages.CacheStatsDataSet;
import ru.otus_matveev_anton.my_cache.CacheEngine;
import ru.otus_matveev_anton.my_cache.CacheEngineImplMBean;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;


public class MainMyOrmWithCache implements MainMyOrmWithCacheMBean {
    private final static Logger log = LogManager.getLogger(MainMyOrmWithCache.class);

    private Random rnd = new Random();
    private AtomicLong seq = new AtomicLong(0);
    private DBServiceWithCache dbService;
    private MessageSystemClient<String> msClient;
    private Addressee addresseeFrontend;
    private boolean isRunning = true;

    public static void main(String[] args) throws Exception {
        MyOrmConfig config = new MyOrmConfig("/connection.cfg", "/MyOrmConf.cfg");
        MapperFactory factory = new MapperFactoryImpl(config);
        DBService dbServiceMyOrm = new DBServiceMyOrmImpl(factory);
        DBServiceWithCache dbServiceWithCache = new DBServiceWithCache(dbServiceMyOrm);
        MainMyOrmWithCache emulDBService = new MainMyOrmWithCache(dbServiceWithCache);

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName("ru.otus_matveev_anton:type=MainMyOrmWithCache");
        mbs.registerMBean(emulDBService, name);

        emulDBService.runTest();
    }

    private MainMyOrmWithCache(DBServiceWithCache dbService) {
        this.dbService = dbService;
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private void runTest() throws InterruptedException {

        initMS();

        UserDataSet user;
        try {
            while (isRunning) {
                user = getRandomUser();
                dbService.saveUser(user);
                Thread.sleep(rnd.nextInt(2_000));
                user = dbService.getUser(rnd.nextInt(seq.intValue() * 3));
                log.debug("got user " + user);
                Thread.sleep(rnd.nextInt(2_000));
            }
        } finally {
            dbService.shutdown();
            msClient.close();
            log.info("Bye.");
        }
    }

    private void initMS() {
        msClient = JsonSocketClient.newInstance();
        msClient.init();
        addresseeFrontend = new AddresseeImpl(SpecialAddress.ALL, "frontend");

        CacheEngine<Long, UserDataSet> cacheEngine = dbService.getCache();
        CacheEngineImplMBean cacheBean = (CacheEngineImplMBean) cacheEngine;

        msClient.addMessageReceiveListener(message -> {
            if (log.isDebugEnabled()){
                log.debug("on message receive: {}", message.toPackedData());
            }
                    Object data = message.getData();
                    if (data instanceof CacheGetCurrentProps) {
                        cacheEngine.setDataChanged();
                    } else if (data instanceof CachePropsDataSet) {
                        CachePropsDataSet propsDataSet = (CachePropsDataSet) data;
                        cacheBean.setEternal(propsDataSet.isEternal());
                        cacheBean.setIdleTimeS(propsDataSet.getIdleTimeS());
                        cacheBean.setLifeTimeS(propsDataSet.getLifeTimeS());
                        cacheBean.setMaxElements(propsDataSet.getMaxElements());
                        cacheBean.setTimeThresholdS(propsDataSet.getTimeThresholdS());
                    } else {
                        return false;
                    }
                    return false;
                }
        );
        cacheEngine.addCacheStatsChangedListener(b -> {
                    CacheStatsDataSet ds = new CacheStatsDataSet();
                    ds.setHitCount(b.getHitCount());
                    ds.setMissCount(b.getMissCount());
                    ds.setSize(b.getSize());
                    msClient.sendMessage(addresseeFrontend, ds);
                }
        );
        cacheEngine.addCachePropsChangedListener(b -> {
                    CacheFullDataSet ds = new CacheFullDataSet();
                    ds.setEternal(b.isEternal());
                    ds.setIdleTimeS(b.getIdleTimeS());
                    ds.setLifeTimeS(b.getLifeTimeS());
                    ds.setMaxElements(b.getMaxElements());
                    ds.setTimeThresholdS(b.getTimeThresholdS());
                    ds.setHitCount(b.getHitCount());
                    ds.setMissCount(b.getMissCount());
                    ds.setSize(b.getSize());
                    msClient.sendMessage(addresseeFrontend, ds);
                }
        );
    }

    private UserDataSet getRandomUser() {
        UserDataSet user = new UserDataSet();
        user.setAge(rnd.nextInt(100));
        user.setName("user" + seq.incrementAndGet());
        return user;
    }

    @Override
    public boolean getRunning() {
        return isRunning;
    }

    @Override
    public void setRunning(boolean running) {
        this.isRunning = running;
    }
}
