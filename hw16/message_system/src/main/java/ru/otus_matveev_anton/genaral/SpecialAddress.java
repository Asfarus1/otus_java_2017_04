package ru.otus_matveev_anton.genaral;

public enum SpecialAddress implements Address {
    ANYONE,
    ALL,
    GENERATE_NEW,//only for adding new client
    MESSAGE_SERVER//for message server only
}
