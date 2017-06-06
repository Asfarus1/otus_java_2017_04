package ru.otus_matveev_anton.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
}
