package ru.otus_matveev_anton.db;

/**
 * Created by Matveev.AV1 on 08.06.2017.
 */

public interface ORMfunc {
    <T extends DataSet> void save(T dataSet);

    <T extends DataSet> T get(long id);
}
