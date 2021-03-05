package org.tests.model.basic.xtra;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Transaction;
import io.ebean.annotation.IgnorePlatform;
import io.ebean.annotation.Platform;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestInsertBatchThenUpdate extends BaseTestCase {

  @Test
  @IgnorePlatform({Platform.SQLSERVER, Platform.HANA}) // has generated IDs
  public void test() {

    LoggedSqlCollector.start();
    try (Transaction txn = Ebean.beginTransaction()) {
      txn.setBatchMode(true);

      LoggedSqlCollector.start();

      EdParent parent = new EdParent();
      parent.setName("MyComputer");

      EdChild child = new EdChild();
      child.setName("Harddisk 123");
      child.setParent(parent);
      ArrayList<EdChild> children = new ArrayList<>();
      children.add(child);
      parent.setChildren(children);

      Ebean.save(parent);

      // get Id or any generated property and that invokes a flush
      parent.getId();

      // going to get an update now
      parent.setName("MyDesk");
      Ebean.save(parent);

      Ebean.commitTransaction();

      // insert statements for EdExtendedParent
      List<String> loggedSql = LoggedSqlCollector.stop();
      assertThat(loggedSql).hasSize(6);
      assertThat(loggedSql.get(0)).contains("insert into td_parent");
      assertThat(loggedSql.get(2)).contains("insert into td_child ");
      assertThat(loggedSql.get(4)).contains("update td_parent set parent_name=? where parent_id=?");
    }
  }


  @Test
  @IgnorePlatform(Platform.HANA)
  public void test_noFlushOn_getterOfNonGeneratedProperty() {

    LoggedSqlCollector.start();
    try (Transaction txn = Ebean.beginTransaction()) {
      txn.setBatchMode(true);

      LoggedSqlCollector.start();

      EdParent parent = new EdParent();
      parent.setName("MyComputer");

      EdChild child = new EdChild();
      child.setName("Harddisk 123");
      child.setParent(parent);
      ArrayList<EdChild> children = new ArrayList<>();
      children.add(child);
      parent.setChildren(children);

      Ebean.save(parent);

      // no getter call on a generated property
      // so no flush here
      String existing = parent.getName();

      parent.setName(existing+" - additional");
      // the first and second save of parent merge into a single insert
      Ebean.save(parent);

      // flush
      Ebean.commitTransaction();

      // insert statements for EdExtendedParent
      List<String> loggedSql = LoggedSqlCollector.stop();
      assertThat(loggedSql).hasSize(4);
      assertThat(loggedSql.get(0)).contains("insert into td_parent");
      assertThat(loggedSql.get(2)).contains("insert into td_child ");
    }
  }

}
