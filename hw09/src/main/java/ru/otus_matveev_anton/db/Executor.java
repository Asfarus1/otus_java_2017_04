package ru.otus_matveev_anton.db;

import java.sql.*;

public class Executor {
    private final Connection connection;

    public Executor(Connection connection) {
        this.connection = connection;
    }

    public int ExecuteUpdate(String update) {
        try (Statement stmt = connection.createStatement()){
            return stmt.executeUpdate(update);
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

    public long ExecuteWithReturningKey(String insert){
        try (PreparedStatement stmt = connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)){
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()){
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }
}
