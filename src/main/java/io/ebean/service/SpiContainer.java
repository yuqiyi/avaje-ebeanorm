package io.ebean.service;

import io.ebean.EbeanServer;
import io.ebean.config.ServerConfig;

/**
 * Creates the Database implementations. This is used internally by the EbeanServerFactory and is not currently
 * exposed as public API.
 */
public interface SpiContainer {

  /**
   * Create the EbeanServer for a given configuration.
   *
   * @param configuration The configuration information for this server.
   */
  EbeanServer createServer(ServerConfig configuration);

  /**
   * Create an EbeanServer just using the name.
   * <p>
   * In this case the dataSource parameters etc will be defined on the global
   * avaje.properties file.
   * </p>
   */
  EbeanServer createServer(String name);

  /**
   * Shutdown any Ebean wide resources such as clustering.
   */
  void shutdown();
}
