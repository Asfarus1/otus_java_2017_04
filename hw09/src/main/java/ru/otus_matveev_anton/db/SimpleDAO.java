package ru.otus_matveev_anton.db;

public class SimpleDAO implements Dao{
    private Configuration configuration;

    SimpleDAO(Configuration configuration) {
        this.configuration = configuration;
    }

    public <T extends DataSet> T load(long id, Class<T> clazz){
       return configuration.getFactory().get(clazz).get(id);
    }

    public <T extends DataSet> void save(T dataSet, Class<T> clazz){
        try {
            configuration.getFactory().get(clazz).save(dataSet);
        } catch (Exception e) {
           throw new DBException(e);
        }
    }

    public <T extends DataSet> void createTableIfNotExists(Class<T> clazz){
        String createTableQuery;
        try {
            createTableQuery = configuration.getFactory().createTableQuery(clazz);
        } catch (Exception e) {
            throw new DBException(e);
        }
        Executor executor = new Executor(configuration);
        executor.ExecuteUpdate(createTableQuery);
    }
}
