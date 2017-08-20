mvn clean package
java -jar app/target/app.jar
copy frontend\target\frontend.war %CATALINA_HOME%\webapps\frontend1.war
copy frontend\target\frontend.war %CATALINA_HOME%\webapps\frontend2.war