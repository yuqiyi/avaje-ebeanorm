package io.ebean.config;

/**
 * Determine keys used for encryption and decryption.
 */
@FunctionalInterface
public interface EncryptKeyManager {

  /**
   * Initialise the EncryptKeyManager.
   * <p>
   * This gives the EncryptKeyManager the opportunity to get keys etc.
   * </p>
   */
  default void initialise() {}

  /**
   * Return the key used to encrypt and decrypt a property mapping to the given
   * table and column.
   */
  EncryptKey getEncryptKey(String tableName, String columnName);
}
