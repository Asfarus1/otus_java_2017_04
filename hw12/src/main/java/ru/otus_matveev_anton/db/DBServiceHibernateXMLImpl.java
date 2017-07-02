package ru.otus_matveev_anton.db;

import org.hibernate.cfg.Configuration;

public class DBServiceHibernateXMLImpl extends DBServiceHibernate {

    @Override
    Configuration configure() {
        Configuration cfg = new Configuration();
        cfg.configure("/hibernate.cfg.xml");
        return cfg;
    }
}
