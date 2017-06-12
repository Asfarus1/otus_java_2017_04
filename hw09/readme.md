Группа 2017-04.1

Автор

Anton Matveev (Антон Матвеев)

m.a.v.norm@mail.ru

ДЗ 9:
Создать в базе таблицу:
<pre>
|-------+--------------+------+-----+---------+----------------+
| Field | Type         | Null | Key | Default | Extra          |
+-------+--------------+------+-----+---------+----------------+
| id    | bigint(20)   | NO   | PRI | NULL    | auto_increment |
| name  | varchar(255) | YES  |     | NULL    |                |
| age   | int(3)       | NO   |     | 0       |                |
+-------+--------------+------+-----+---------+----------------+
</pre>
Разметьте класс User, аннотациями JPA так, чтобы он соответствовал таблице. 
Написать Executor, который сохраняет объект <T extends DataSet> базу и читает объект класса <T extends DataSet> из базы по id.

<pre>
<T extends DataSet> void save(T dataSet){…}
<T extends DataSet> T load(long id, Class<T> clazz){...}
</pre>
где DataSet -- базовый класс для датасетов, в котором есть поле long id
