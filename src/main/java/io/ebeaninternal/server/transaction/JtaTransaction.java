package io.ebeaninternal.server.transaction;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import java.sql.SQLException;

/**
 * Jta based transaction.
 */
public class JtaTransaction extends JdbcTransaction {

  private final UserTransaction userTransaction;

  private boolean commmitted;

  private boolean newTransaction;

  /**
   * Create the JtaTransaction.
   */
  public JtaTransaction(String id, boolean explicit, UserTransaction utx, DataSource ds, TransactionManager manager) {
    super(id, explicit, null, manager);
    userTransaction = utx;
    try {
      newTransaction = userTransaction.getStatus() == Status.STATUS_NO_TRANSACTION;
      if (newTransaction) {
        userTransaction.begin();
      }
    } catch (Exception e) {
      throw new PersistenceException(e);
    }

    try {
      // Open JDBC Connection
      this.connection = ds.getConnection();
      if (connection == null) {
        throw new PersistenceException("The DataSource returned a null connection.");
      }
      if (connection.getAutoCommit()) {
        connection.setAutoCommit(false);
      }

    } catch (SQLException e) {
      throw new PersistenceException(e);
    }
  }

  /**
   * Commit the transaction.
   */
  @Override
  public void commit() {
    if (commmitted) {
      throw new PersistenceException("This transaction has already been committed.");
    }
    try {
      try {
        if (newTransaction) {
          userTransaction.commit();
        }
        notifyCommit();
      } finally {
        close();
      }
    } catch (Exception e) {
      throw new PersistenceException(e);
    }
    commmitted = true;
  }

  @Override
  public void rollback() {
    rollback(null);
  }

  /**
   * Rollback the transaction.
   */
  @Override
  public void rollback(Throwable e) {
    if (!commmitted) {
      try {
        try {
          if (userTransaction != null) {
            if (newTransaction) {
              userTransaction.rollback();
            } else {
              userTransaction.setRollbackOnly();
            }
          }
          notifyRollback(e);
        } finally {
          closeConnection();
        }
      } catch (Exception ex) {
        throw new PersistenceException(ex);
      }
    }

  }

  /**
   * Close the underlying connection.
   */
  private void closeConnection() throws SQLException {
    if (connection != null) {
      connection.close();
      connection = null;
    }
  }

}
