package ru.otus_matveev_anton.db.my_orm;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class MyOrmConfig {

    private boolean showSql;

    private String createPattern;

    private final Connection connection;

    private MapperFactory factory;

    private PrintStream log;

    public MyOrmConfig(String... configFiles) throws DBException {
        Properties props = new Properties();
        Class clazz = this.getClass();
        try {
            for (String file : configFiles) {
                try (InputStream is = clazz.getResourceAsStream(file)) {
                    props.load(is);
                }
            }

            Class.forName(props.getProperty("driver"));
            this.connection = DriverManager.getConnection(props.getProperty("url"), props);

            createPattern = props.getProperty("createPattern");
            if (createPattern == null) {
                throw new IllegalArgumentException("cfg must have createPattern property");
            }
            showSql = "true".equalsIgnoreCase(props.getProperty("showSql"));
        }catch (IOException|SQLException|ClassNotFoundException e){
            throw new DBException(e);
        }

        this.factory = new MapperFactoryImpl(this);
        this.log = System.out;
    }

    public MapperFactory getFactory() {
        return factory;
    }

    Connection getConnection() {
        return connection;
    }

    PrintStream getLog() {
        return log;
    }

    boolean isShowSql() {
        return showSql;
    }

    String getCreatePattern() {
        return createPattern;
    }

    @Override
    protected void finalize() throws Throwable {
        close();
    }

    public void close(){
        try {
            if (connection!= null){
                connection.close();
            }
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }
}
