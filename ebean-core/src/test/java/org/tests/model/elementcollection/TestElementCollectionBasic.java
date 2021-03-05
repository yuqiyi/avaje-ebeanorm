package org.tests.model.elementcollection;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Transaction;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestElementCollectionBasic extends BaseTestCase {

  private List<String> eventLog() {
    return EcPersonPersistAdapter.eventLog();
  }

  @Test
  public void insertThen_UpdateWhenNotChanged_expect_noChanges() {

    EcPerson person = new EcPerson("Nothing021");
    person.getPhoneNumbers().add("021 1234");
    person.getPhoneNumbers().add("021 4321");

    LoggedSqlCollector.start();
    Ebean.save(person);

    List<String> sql = LoggedSqlCollector.current();
    assertThat(eventLog()).containsOnly("preInsert", "postInsert");
    assertThat(sql).hasSize(4);

    final EcPerson found = Ebean.find(EcPerson.class)
      .setId(person.getId())
      //.setLabel("findById")
      .findOne();

    found.getPhoneNumbers().size();

    sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(2);
    assertSql(sql.get(0)).contains("from ec_person t0 where t0.id = ?");
    assertSql(sql.get(1)).contains("from ec_person_phone t0 where");

    // save when not actually changed
    Ebean.save(found);

    sql = LoggedSqlCollector.stop();
    assertThat(sql).isEmpty();
    assertThat(eventLog()).isEmpty();
  }

  @Test
  public void test() {

    eventLog();
    LoggedSqlCollector.start();

    EcPerson person = new EcPerson("Fiona021");
    person.getPhoneNumbers().add("021 1234");
    person.getPhoneNumbers().add("021 4321");
    Ebean.save(person);

    assertThat(eventLog()).containsExactly("preInsert", "postInsert");

    List<String> sql = LoggedSqlCollector.current();
    if (isPersistBatchOnCascade()) {
      assertThat(sql).hasSize(4);
      assertSql(sql.get(0)).contains("insert into ec_person");
      assertSql(sql.get(1)).contains("insert into ec_person_phone");
      assertSqlBind(sql, 2, 3);
    } else {
      assertThat(sql).hasSize(3);
      assertSql(sql.get(0)).contains("insert into ec_person");
      assertSql(sql.get(1)).contains("insert into ec_person_phone");
      assertSql(sql.get(2)).contains("insert into ec_person_phone");
    }

    EcPerson person1 = new EcPerson("Fiona09");
    person1.getPhoneNumbers().add("09 1234");
    person1.getPhoneNumbers().add("09 4321");
    Ebean.save(person1);

    assertThat(eventLog()).containsExactly("preInsert", "postInsert");

    LoggedSqlCollector.current();

    List<EcPerson> found =
      Ebean.find(EcPerson.class).where()
        .startsWith("name", "Fiona0")
        .order().asc("id")
        .findList();

    List<String> phoneNumbers0 = found.get(0).getPhoneNumbers();
    List<String> phoneNumbers1 = found.get(1).getPhoneNumbers();
    phoneNumbers0.size();

    assertThat(phoneNumbers0).containsExactly("021 1234", "021 4321");
    assertThat(phoneNumbers1).containsExactly("09 1234", "09 4321");

    sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(2);
    assertSql(sql.get(0)).contains("select t0.id, t0.name, t0.version from ec_person t0 where");
    assertSql(sql.get(1)).contains("select t0.owner_id, t0.phone from ec_person_phone t0 where");

    List<EcPerson> found2 =
      Ebean.find(EcPerson.class)
        .fetch("phoneNumbers")
        .where()
        .startsWith("name", "Fiona0")
        .order().asc("id")
        .findList();

    assertThat(found2).hasSize(2);

    sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(1);
    assertSql(sql.get(0)).contains("select t0.id, t0.name, t0.version, t1.phone from ec_person t0 left join ec_person_phone t1");

    EcPerson foundFirst = found2.get(0);

    jsonToFrom(foundFirst);
    updateBasic(foundFirst);

    LoggedSqlCollector.stop();
  }

  private void updateBasic(EcPerson bean) {

    bean.setName("Fiona021-mod-0");
    Ebean.save(bean);

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(1);
    assertSql(sql.get(0)).contains("update ec_person");

    assertThat(eventLog()).containsExactly("preUpdate", "postUpdate");

    updateBasicInBatch(bean);
  }

  private void updateBasicInBatch(EcPerson bean) {

    try (Transaction txn = Ebean.beginTransaction()) {
      txn.setBatchMode(true);
      bean.setName("Fiona021-mod-0-batch");
      Ebean.save(bean);
      txn.commit();
    }

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(2);
    assertSql(sql.get(0)).contains("update ec_person");

    assertThat(eventLog()).containsExactly("preUpdate", "postUpdate");

    updateBoth(bean);
  }

  private void updateBoth(EcPerson bean) {

    bean.setName("Fiona021-mod-both");
    bean.getPhoneNumbers().add("01-22123");
    Ebean.save(bean);

    List<String> sql = LoggedSqlCollector.current();
    if (isPersistBatchOnCascade()) {
      assertThat(sql).hasSize(7);
      assertSql(sql.get(0)).contains("update ec_person set name=?, version=? where id=? and version=?");
      assertSql(sql.get(1)).contains("delete from ec_person_phone where owner_id=?");
      assertSqlBind(sql.get(2));
      assertThat(sql.get(3)).contains("insert into ec_person_phone (owner_id,phone) values (?,?)");
      assertSqlBind(sql, 4, 6);

    } else {
      assertThat(sql).hasSize(5);
      assertSql(sql.get(0)).contains("update ec_person set name=?, version=? where id=? and version=?");
      assertSql(sql.get(1)).contains("delete from ec_person_phone where owner_id=?");
      assertSql(sql.get(2)).contains("insert into ec_person_phone (owner_id,phone) values (?,?)");
      assertThat(sql.get(3)).contains("insert into ec_person_phone (owner_id,phone) values (?,?)");
      assertThat(sql.get(4)).contains("insert into ec_person_phone (owner_id,phone) values (?,?)");
    }

    assertThat(eventLog()).containsExactly("preUpdate", "postUpdate");

    updateBothInBatch(bean);
  }

  private void updateBothInBatch(EcPerson bean) {

    try (Transaction txn = Ebean.beginTransaction()) {
      txn.setBatchMode(true);
      bean.setName("Fiona021-mod-both-batch");
      bean.getPhoneNumbers().add("01-22123");
      Ebean.save(bean);
      txn.commit();
    }

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(9);
    assertSql(sql.get(0)).contains("update ec_person set name=?, version=? where id=? and version=?");
    assertSql(sql.get(2)).contains("delete from ec_person_phone where owner_id=?");
    assertSqlBind(sql, 3);
    assertThat(sql.get(4)).contains("insert into ec_person_phone (owner_id,phone) values (?,?)");
    assertSqlBind(sql, 5, 8);

    assertThat(eventLog()).containsExactly("preUpdate", "postUpdate");

    updateNothing(bean);
  }

  private void updateNothing(EcPerson bean) {

    Ebean.save(bean);

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(0);

    assertThat(eventLog()).isEmpty();

    updateOnlyCollectionInBatch(bean);
  }

  private void updateOnlyCollectionInBatch(EcPerson bean) {

    try (Transaction txn = Ebean.beginTransaction()) {
      txn.setBatchMode(true);
      bean.getPhoneNumbers().add("01-4321");
      Ebean.save(bean);
      txn.commit();
    }

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(8);
    assertSql(sql.get(0)).contains("delete from ec_person_phone where owner_id=?");
    assertSqlBind(sql, 1);
    assertSql(sql.get(2)).contains("insert into ec_person_phone (owner_id,phone) values (?,?)");
    assertSqlBind(sql, 3, 7);

    assertThat(eventLog()).containsExactly("preUpdate", "postUpdate");

    updateOnlyCollection(bean);
  }

  private void updateOnlyCollection(EcPerson bean) {

    bean.getPhoneNumbers().add("01-4321");
    Ebean.save(bean);

    List<String> sql = LoggedSqlCollector.current();
    if (isPersistBatchOnCascade()) {
      assertThat(sql).hasSize(9);
      assertSql(sql.get(0)).contains("delete from ec_person_phone where owner_id=?");
      assertSqlBind(sql.get(1));
      assertSql(sql.get(2)).contains("insert into ec_person_phone (owner_id,phone) values (?,?)");
      assertSqlBind(sql, 3, 8);

    } else {
      assertThat(sql).hasSize(7);
      assertSql(sql.get(0)).contains("delete from ec_person_phone where owner_id=?");
      assertSql(sql.get(1)).contains("insert into ec_person_phone (owner_id,phone) values (?,?)");
      assertSql(sql.get(2)).contains("insert into ec_person_phone (owner_id,phone) values (?,?)");
      assertThat(sql.get(3)).contains("insert into ec_person_phone (owner_id,phone) values (?,?)");
      assertThat(sql.get(4)).contains("insert into ec_person_phone (owner_id,phone) values (?,?)");
      assertThat(sql.get(5)).contains("insert into ec_person_phone (owner_id,phone) values (?,?)");
      assertThat(sql.get(6)).contains("insert into ec_person_phone (owner_id,phone) values (?,?)");
    }

    assertThat(eventLog()).containsExactly("preUpdate", "postUpdate");

    delete(bean);
  }

  private void delete(EcPerson bean) {

    Ebean.delete(bean);

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(2);
    assertSql(sql.get(0)).contains("delete from ec_person_phone where owner_id = ?");
    assertSql(sql.get(1)).contains("delete from ec_person where id=? and version=?");

    assertThat(eventLog()).containsExactly("preDelete", "postDelete");
  }

  private void jsonToFrom(EcPerson foundFirst) {

    String asJson = Ebean.json().toJson(foundFirst);
    EcPerson fromJson = Ebean.json().toBean(EcPerson.class, asJson);
    assertThat(fromJson.getPhoneNumbers()).containsAll(foundFirst.getPhoneNumbers());
  }
}
