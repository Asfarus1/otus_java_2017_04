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
import ru.otus_matveev_anton.message_system_client.JsonSocketClient;
import ru.otus_matveev_anton.messages.CachePropsDataSet;
import ru.otus_matveev_anton.my_cache.CacheEngine;
import ru.otus_matveev_anton.my_cache.CacheEngineImplMBean;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;


public class MainMyOrmWithCache {
    private final static Logger log = LogManager.getLogger(MainMyOrmWithCache.class);

    private Random rnd = new Random();
    private AtomicLong seq = new AtomicLong(0);
    private DBServiceWithCache dbService;
    private MessageSystemClient<String> msClient;
    private Addressee addresseeFrontend;

    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException, InterruptedException {
        MyOrmConfig config = new MyOrmConfig("/connection.cfg", "/MyOrmConf.cfg");
        MapperFactory factory = new MapperFactoryImpl(config);
        DBService dbServiceMyOrm  = new DBServiceMyOrmImpl(factory);
        DBServiceWithCache dbServiceWithCache = new DBServiceWithCache(dbServiceMyOrm);
        new MainMyOrmWithCache(dbServiceWithCache).runTest();
    }

    private MainMyOrmWithCache(DBServiceWithCache dbService) {
        this.dbService = dbService;
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private void runTest() throws InterruptedException {

        initMS();

        UserDataSet user;
        try {
            while (true){
                user = getRandomUser();
                dbService.saveUser(user);
                Thread.sleep(rnd.nextInt(2_000));
                user = dbService.getUser(rnd.nextInt(seq.intValue() * 3));
                System.out.println("got user " + user);
                Thread.sleep(rnd.nextInt(2_000));
            }
        }finally {
            dbService.shutdown();
        }
    }

    private void initMS() {
        msClient = JsonSocketClient.newInstance();
        try {
            msClient.init();
        } catch (IOException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
        addresseeFrontend = new AddresseeImpl(SpecialAddress.ANYONE, "frontend");

        CacheEngine<Long, UserDataSet> cacheEngine = dbService.getCache();
        CacheEngineImplMBean cacheBean = (CacheEngineImplMBean)cacheEngine;

        msClient.addMessageReceiveListener(message ->{
                Object data = message.getData();
                if (data instanceof CachePropsDataSet){
                    CachePropsDataSet propsDataSet = (CachePropsDataSet) data;
                    cacheBean.setEternal(propsDataSet.isEternal());
                    cacheBean.setIdleTimeS(propsDataSet.getIdleTimeS());
                    cacheBean.setLifeTimeS(propsDataSet.getLifeTimeS());
                    cacheBean.setMaxElements(propsDataSet.getMaxElements());
                    cacheBean.setTimeThresholdS(propsDataSet.getTimeThresholdS());
                }
                return false;
            }
        );
        cacheEngine.addCacheStatsChangedListener(b -> msClient.sendMessage(addresseeFrontend, b));
    }

    private UserDataSet getRandomUser(){
        UserDataSet user = new UserDataSet();
        user.setAge(rnd.nextInt(100));
        user.setName("user" + seq.incrementAndGet());
        return user;
    }
}
