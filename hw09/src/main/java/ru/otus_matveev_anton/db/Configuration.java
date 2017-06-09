package ru.otus_matveev_anton.db;

import ru.otus_matveev_anton.db.orm.MapperFactory;
import ru.otus_matveev_anton.db.orm.MapperFactoryImpl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.util.Properties;

public class Configuration {

    private boolean showSql;

    private String createPattern;

    private final Connection connection;

    private final static String defaltPathToCfg = "/orm.cfg";

    private MapperFactory factory;

    private PrintStream log;

    public Configuration(Connection connection) {
        this(connection, null);
    }

    public Configuration(Connection connection, String pathToCfg) {
        this.connection = connection;
        Properties props = new Properties();

        try (InputStream is = (pathToCfg == null) ? this.getClass().getResourceAsStream(defaltPathToCfg) : new FileInputStream(pathToCfg)){
            props.load(is);
        }catch (IOException ex){
            throw new DBException(ex);
        }

        createPattern = props.getProperty("createPattern");
        if (createPattern == null){
            throw new IllegalArgumentException("cfg must have createPattern property");
        }
        setShowSql("true".equalsIgnoreCase(props.getProperty("showSql")));

        this.factory = new MapperFactoryImpl(this);
    }

    MapperFactory getFactory() {
        return factory;
    }

    public void setFactory(MapperFactory factory) {
        this.factory = factory;
    }

    Connection getConnection() {
        return connection;
    }

    PrintStream getLog() {
        return log;
    }

    public void setLog(PrintStream log) {
        this.log = log;
    }

    boolean isShowSql() {
        return showSql;
    }

    public void setShowSql(boolean showSql) {
        this.showSql = showSql;
    }

    public String getCreatePattern() {
        return createPattern;
    }

    public Dao getSimpleDao(){
        return new SimpleDAO(this);
    }
}
