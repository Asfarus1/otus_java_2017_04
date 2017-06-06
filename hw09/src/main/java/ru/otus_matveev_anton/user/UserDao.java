package ru.otus_matveev_anton.user;

import ru.otus_matveev_anton.db.Executor;

import java.math.BigInteger;
import java.sql.Connection;

public class UserDao {
    private Connection connection;

    public UserDao(Connection connection) {
        this.connection = connection;
    }

    public void save(UserDataSet user) {
        Executor executor = new Executor(connection);
        String update = String.format("INSERT INTO users (name, age) values(%s, %d)"
                , user.getName()
                , user.getAge()
        );
        executor.ExecuteUpdate(update);
    }

    public UserDataSet get(BigInteger id){
        Executor executor = new Executor(connection);
        String query = "SELECT id, name, age FROM users WHERE id=".concat(id.toString());

        return executor.ExecuteQuery(query, resultSet-> {
            UserDataSet result = null;
            if (resultSet.next()){
                result = new UserDataSet();
                result.setId(resultSet.getLong("id"));
                result.setName(resultSet.getString("name"));
                result.setAge(resultSet.getShort("age"));
            }
            return result;
        });
    }
}
