package ru.otus_matveev_anton.db.my_orm;

import java.io.PrintStream;
import java.sql.*;
import java.util.Arrays;

public class Executor {
    private final MyOrmConfig myOrmConfig;
    private final Connection connection;

    public Executor(MyOrmConfig myOrmConfig) {
        this.myOrmConfig = myOrmConfig;
        this.connection = myOrmConfig.getConnection();
    }

    void ExecuteUpdate(String update, Object... args) {
        try (PreparedStatement stmt = connection.prepareStatement(update)){
            printQuery(update, args);
            setArgs(stmt, args);
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new DBException(e);
        }
    }

    <T> T ExecuteQuery(String query, ResultHandler<T> handler, Object... args) {
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            printQuery(query, args);
            setArgs(stmt, args);
            ResultSet resultSet = stmt.executeQuery();
            return handler.handle(resultSet);
        } catch (Exception e) {
            throw new DBException(e);
        }
    }

    long ExecuteWithReturningKey(String insert, Object... args){
        try (PreparedStatement stmt = connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)){
            printQuery(insert, args);
            setArgs(stmt, args);
            stmt.executeUpdate();
            if (connection.getMetaData().supportsGetGeneratedKeys()) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return -1;
        } catch (Exception e) {
            throw new DBException(e);
        }
    }

    private void setArgs(PreparedStatement stmt, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null){
                stmt.setNull(i + 1, Types.NULL);
            }else if (args[i] instanceof String){
                stmt.setString(i + 1, (String) args[i]);
            }else if (args[i] instanceof Short){
                stmt.setShort(i + 1, (Short) args[i]);
            }else if (args[i] instanceof Integer){
                stmt.setInt(i + 1,(Integer) args[i]);
            }else if (args[i] instanceof Long){
                stmt.setLong(i + 1,(Long) args[i]);
            }else {
                throw new IllegalArgumentException("args type must be one of Long, String, Short, Integer");
            }
        }
    }

    private void printQuery(String query, Object... args){
        if (myOrmConfig.isShowSql()){
            PrintStream log = myOrmConfig.getLog();
            log.println("sql:" + query);
            if (args.length > 0){
                log.print("args:");
                Arrays.stream(args).forEach((a)->log.printf("%s;",a));
                log.println();
            }
            log.println("---");
        }
    }
}
