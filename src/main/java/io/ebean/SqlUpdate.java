package io.ebean;

/**
 * A SqlUpdate for executing insert update or delete statements.
 * <p>
 * Provides a simple way to execute raw SQL insert update or delete statements
 * without having to resort to JDBC.
 * </p>
 * <p>
 * Supports the use of positioned or named parameters and can automatically
 * notify Ebean of the table modified so that Ebean can maintain its cache.
 * </p>
 * <p>
 * Note that {@link #setAutoTableMod(boolean)} and
 * Ebean#externalModification(String, boolean, boolean, boolean)} can be to
 * notify Ebean of external changes and enable Ebean to maintain it's "L2"
 * server cache.
 * </p>
 *
 * <pre>{@code
 *
 *   // example using 'positioned' parameters
 *
 *   String sql = "insert into audit_log (group, title, description) values (?, ?, ?);
 *
 *   int rows =
 *     DB.sqlUpdate(sql)
 *       .setParams("login", "new user", "user rob was created")
 *       .executeNow();
 *
 * }</pre>
 *
 * <pre>{@code
 *
 *   // example using 'named' parameters
 *
 *   String sql = "update topic set post_count = :count where id = :id";
 *
 *   int rows =
 *     DB.sqlUpdate(sql)
 *       .setParameter("id", 1)
 *       .setParameter("count", 50)
 *       .execute();
 *
 *   String msg = "There were " + rows + " rows updated";
 *
 * }</pre>
 * <p>
 * <h3>Example: Using setNextParameter()</h3>
 * <pre>{@code
 *
 *  String sql = "insert into audit_log (id, description, modified_description) values (?,?,?)";
 *
 *  SqlUpdate insert = DB.sqlUpdate(sql);
 *
 *  try (Transaction txn = DB.beginTransaction()) {
 *    txn.setBatchMode(true);
 *
 *    insert.setNextParameter(10000);
 *    insert.setNextParameter("hello");
 *    insert.setNextParameter("rob");
 *    insert.execute();
 *
 *    insert.setNextParameter(10001);
 *    insert.setNextParameter("goodbye");
 *    insert.setNextParameter("rob");
 *    insert.execute();
 *
 *    insert.setNextParameter(10002);
 *    insert.setNextParameter("chow");
 *    insert.setNextParameter("bob");
 *    insert.execute();
 *
 *    txn.commit();
 *  }
 * }</pre>
 * <p>
 * An alternative to the batch mode on the transaction is to use addBatch() and executeBatch() like:
 * </p>
 * <pre>{@code
 *
 *   try (Transaction txn = DB.beginTransaction()) {
 *
 *     insert.setNextParameter(10000);
 *     insert.setNextParameter("hello");
 *     insert.setNextParameter("rob");
 *     insert.addBatch();
 *
 *     insert.setNextParameter(10001);
 *     insert.setNextParameter("goodbye");
 *     insert.setNextParameter("rob");
 *     insert.addBatch();
 *
 *     insert.setNextParameter(10002);
 *     insert.setNextParameter("chow");
 *     insert.setNextParameter("bob");
 *     insert.addBatch();
 *
 *     int[] rows = insert.executeBatch();
 *
 *     txn.commit();
 *   }
 *
 * }</pre>
 *
 * @see Update
 * @see SqlQuery
 * @see CallableSql
 */
public interface SqlUpdate {

  /**
   * Execute the update returning the number of rows modified.
   * <p>
   * Note that if the transaction has batch mode on then this update will use JDBC batch and may not execute until
   * later - at commit time or a transaction flush. In this case this method returns -1 indicating that the
   * update has been batched for later execution.
   * </p>
   * <p>
   * After you have executed the SqlUpdate you can bind new variables using
   * {@link #setParameter(String, Object)} etc and then execute the SqlUpdate
   * again.
   * </p>
   * <p>
   * For JDBC batch processing refer to
   * {@link Transaction#setBatchMode(boolean)} and
   * {@link Transaction#setBatchSize(int)}.
   * </p>
   *
   * @see Ebean#execute(SqlUpdate)
   */
  int execute();

  /**
   * Execute the statement now regardless of the JDBC batch mode of the transaction.
   */
  int executeNow();

  /**
   * Execute when addBatch() has been used to batch multiple bind executions.
   *
   * @return The row counts for each of the batched statements.
   */
  int[] executeBatch();

  /**
   * Add the statement to batch processing to then later execute via executeBatch().
   */
  void addBatch();

  /**
   * Return the generated key value.
   */
  Object getGeneratedKey();

  /**
   * Execute and return the generated key. This is effectively a short cut for:
   * <p>
   * <pre>{@code
   *
   *   sqlUpdate.execute();
   *   Object key = sqlUpdate.getGeneratedKey();
   *
   * }</pre>
   *
   * @return The generated key value
   */
  Object executeGetKey();

  /**
   * Return true if eBean should automatically deduce the table modification
   * information and process it.
   * <p>
   * If this is true then cache invalidation and text index management are aware
   * of the modification.
   * </p>
   */
  boolean isAutoTableMod();

  /**
   * Set this to false if you don't want eBean to automatically deduce the table
   * modification information and process it.
   * <p>
   * Set this to false if you don't want any cache invalidation or text index
   * management to occur. You may do this when say you update only one column
   * and you know that it is not important for cached objects or text indexes.
   * </p>
   */
  SqlUpdate setAutoTableMod(boolean isAutoTableMod);

  /**
   * Return the label that can be seen in the transaction logs.
   */
  String getLabel();

  /**
   * Set a descriptive text that can be put into the transaction log.
   * <p>
   * Useful when identifying the statement in the transaction log.
   * </p>
   */
  SqlUpdate setLabel(String label);

  /**
   * Set to true when we want to use getGeneratedKeys with this statement.
   */
  SqlUpdate setGetGeneratedKeys(boolean getGeneratedKeys);

  /**
   * Return the sql statement.
   */
  String getSql();

  /**
   * Return the generated sql that has named parameters converted to positioned parameters.
   */
  String getGeneratedSql();

  /**
   * Return the timeout used to execute this statement.
   */
  int getTimeout();

  /**
   * Set the timeout in seconds. Zero implies no limit.
   * <p>
   * This will set the query timeout on the underlying PreparedStatement. If the
   * timeout expires a SQLException will be throw and wrapped in a
   * PersistenceException.
   * </p>
   */
  SqlUpdate setTimeout(int secs);

  /**
   * Set one of more positioned parameters.
   * <p>
   * This is a convenient alternative to multiple setParameter() calls.
   *
   * <pre>{@code
   *
   *   String sql = "insert into audit_log (id, name, version) values (?,?,?)";
   *
   *   DB.sqlUpdate(sql)
   *       .setParams(UUID.randomUUID(), "Hello", 1)
   *       .executeNow();
   *
   *
   *   // is the same as ...
   *
   *   DB.sqlUpdate(sql)
   *       .setParameter(1, UUID.randomUUID())
   *       .setParameter(2, "Hello")
   *       .setParameter(3, 1)
   *       .executeNow();
   *
   * }</pre>
   *
   */
  SqlUpdate setParams(Object... values);

  /**
   * Set the next positioned parameter.
   *
   * @param value The value to bind
   */
  SqlUpdate setNextParameter(Object value);

  /**
   * Set a parameter via its index position.
   */
  SqlUpdate setParameter(int position, Object value);

  /**
   * Set a null parameter via its index position.
   */
  SqlUpdate setNull(int position, int jdbcType);

  /**
   * Set a null valued parameter using its index position.
   */
  SqlUpdate setNullParameter(int position, int jdbcType);

  /**
   * Set a named parameter value.
   */
  SqlUpdate setParameter(String name, Object param);

  /**
   * Set a named parameter that has a null value. Exactly the same as
   * {@link #setNullParameter(String, int)}.
   */
  SqlUpdate setNull(String name, int jdbcType);

  /**
   * Set a named parameter that has a null value.
   */
  SqlUpdate setNullParameter(String name, int jdbcType);

}
