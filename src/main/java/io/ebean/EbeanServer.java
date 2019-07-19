package io.ebean;

/**
 * Provides the API for fetching and saving beans to a particular Database.
 * <p>
 * Effectively this is an alias for {@link Database} which is now the new
 * and improved name for EbeanServer.
 * <p>
 * The preference is to use DB and Database rather than Ebean and EbeanServer.
 */
public interface EbeanServer extends Database {

}
