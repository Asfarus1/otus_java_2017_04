package ru.otus_matveev_anton.db;

import java.sql.*;

public class Executor {
    private final Connection connection;

    public Executor(Connection connection) {
        this.connection = connection;
    }

    public int ExecuteUpdate(String update, Object... args) {
        try (PreparedStatement stmt = connection.prepareStatement(update)){
            setArgs(stmt, args);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public <T> T ExecuteQuery(String query, ResultHandler<T> handler){
        try (Statement stmt = connection.createStatement();
             ResultSet resultSet = stmt.executeQuery(query)){
            return handler.handle(resultSet);
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public long ExecuteWithReturningKey(String insert, Object... args){
        try (PreparedStatement stmt = connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)){
            setArgs(stmt, args);
            stmt.executeUpdate();
            if (connection.getMetaData().supportsGetGeneratedKeys()) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return -1;
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    private void setArgs(PreparedStatement stmt, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null){
                stmt.setNull(i, Types.NULL);
            }else if (args[i] instanceof String){
                stmt.setString(i, (String) args[i]);
            }else if (args[i] instanceof Short){
                stmt.setShort(i, (Short) args[i]);
            }else if (args[i] instanceof Integer){
                stmt.setInt(i,(Integer) args[i]);
            }else if (args[i] instanceof Long){
                stmt.setLong(i,(Long) args[i]);
            }else {
                throw new IllegalArgumentException("args type must be one of Long, String, Short, Integer");
            }
        }
    }
}
