package io.ebean.config;

/**
 * Represents the key used for encryption.
 * <p>
 * For simple cases this often represent a simple String key but depending on
 * the encryption method this could contain other details.
 */
@FunctionalInterface
public interface EncryptKey {

  /**
   * Return the string key value.
   */
  String getStringValue();
}
