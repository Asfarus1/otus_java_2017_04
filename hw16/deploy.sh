#!/usr/bin/env bash

mvn clean package
java -jar app/target/app.jar
cp frontend/target/frontend.war ~/apps/jetty-distribution-9.4.6.v20170531/webapps/frontend1.war
cp frontend/target/frontend.war ~/apps/jetty-distribution-9.4.6.v20170531/webapps/frontend2.war
