package io.ebean.cache;

/**
 * The statistics collected per cache.
 * <p>
 * These can be monitored to review the effectiveness of a particular cache.
 * </p>
 * <p>
 * Depending on the cache implementation not all the statistics may be collected.
 * </p>
 */
public class ServerCacheStatistics {

  protected String cacheName;

  protected int maxSize;

  protected int size;

  protected long hitCount;

  protected long missCount;

  protected long putCount;

  protected long removeCount;

  protected long clearCount;

  protected long evictCount;

  @Override
  public String toString() {
    //noinspection StringBufferReplaceableByString
    StringBuilder sb = new StringBuilder(80);
    sb.append(cacheName);
    sb.append(" maxSize:").append(maxSize);
    sb.append(" size:").append(size);
    sb.append(" hitRatio:").append(getHitRatio());
    sb.append(" hit:").append(hitCount);
    sb.append(" miss:").append(missCount);
    sb.append(" put:").append(putCount);
    sb.append(" remove:").append(removeCount);
    sb.append(" clear:").append(clearCount);
    sb.append(" evict:").append(evictCount);
    return sb.toString();
  }

  /**
   * Returns an int from 0 to 100 (percentage) for the hit ratio.
   * <p>
   * A hit ratio of 100 means every get request against the cache hits an entry.
   * </p>
   */
  public int getHitRatio() {
    long totalCount = hitCount + missCount;
    if (totalCount == 0) {
      return 0;
    } else {
      return (int) (hitCount * 100 / totalCount);
    }
  }

  /**
   * Return the name of the cache.
   */
  public String getCacheName() {
    return cacheName;
  }

  /**
   * Set the name of the cache.
   */
  public void setCacheName(String cacheName) {
    this.cacheName = cacheName;
  }

  /**
   * Return the hit count. The number of successful gets.
   */
  public long getHitCount() {
    return hitCount;
  }

  /**
   * Set the hit count.
   */
  public void setHitCount(long hitCount) {
    this.hitCount = hitCount;
  }

  /**
   * Return the miss count. The number of gets that returned null.
   */
  public long getMissCount() {
    return missCount;
  }

  /**
   * Set the miss count.
   */
  public void setMissCount(long missCount) {
    this.missCount = missCount;
  }

  /**
   * Return the size of the cache.
   */
  public int getSize() {
    return size;
  }

  /**
   * Set the size of the cache.
   */
  public void setSize(int size) {
    this.size = size;
  }

  /**
   * Return the maximum size of the cache.
   * <p>
   * Can be used in conjunction with the size to determine if the cache use is
   * being potentially limited by its maximum size.
   * </p>
   */
  public int getMaxSize() {
    return maxSize;
  }

  /**
   * Set the maximum size of the cache.
   */
  public void setMaxSize(int maxSize) {
    this.maxSize = maxSize;
  }

  /**
   * Set the put insert count.
   */
  public void setPutCount(long putCount) {
    this.putCount = putCount;
  }

  /**
   * Return the put insert count.
   */
  public long getPutCount() {
    return putCount;
  }

  /**
   * Set the remove count.
   */
  public void setRemoveCount(long removeCount) {
    this.removeCount = removeCount;
  }

  /**
   * Return the remove count.
   */
  public long getRemoveCount() {
    return removeCount;
  }

  /**
   * Set the clear count.
   */
  public void setClearCount(long clearCount) {
    this.clearCount = clearCount;
  }

  /**
   * Return the clear count.
   */
  public long getClearCount() {
    return clearCount;
  }

  /**
   * Set the count of entries evicted due to idle time.
   */
  public void setEvictCount(long evictCount) {
    this.evictCount = evictCount;
  }

  /**
   * Return the count of entries evicted due to idle time.
   */
  public long getEvictCount() {
    return evictCount;
  }

}
