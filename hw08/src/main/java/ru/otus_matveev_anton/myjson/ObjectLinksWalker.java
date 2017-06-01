package ru.otus_matveev_anton.myjson;

import java.util.Set;

/**
 * Created by Matveev.AV1 on 01.06.2017.
 */
@FunctionalInterface
interface ObjectLinksWalker {
    FunctionPart visitLink(Object obj, Set<Object> links);
}
