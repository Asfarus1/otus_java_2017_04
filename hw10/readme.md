Группа 2017-04.1

Автор

Anton Matveev (Антон Матвеев)

m.a.v.norm@mail.ru

ДЗ 10:
1. (взять из дз9)
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

<T extends DataSet> void save(T dataSet){…}
<T extends DataSet> T load(long id, Class<T> clazz){...} (edited)
где DataSet -- базовый класс для датасетов, в котором есть поле long id

2. Оформить решение в виде DBService (interface DBService, class DBServiceImpl, UsersDAO, UsersDataSet, Executor)
3. Не меняя интерфейс DBSerivice сделать DBServiceHibernateImpl на Hibernate.
4. Добавить в UsersDataSet
 
class AddressDataSet{
 private String street;
 private int index;
 } (OneToOne)
  
 и телефон 
 class PhoneDataSet{ 
  private int code;
   private String number;
  } (OneToMany)
  
   Добавить соответствущие датасеты и DAO. 