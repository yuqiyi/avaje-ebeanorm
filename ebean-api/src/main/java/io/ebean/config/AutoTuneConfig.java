package io.ebean.config;

/**
 * Defines the AutoTune behaviour for a Database.
 */
public class AutoTuneConfig {

  private AutoTuneMode mode = AutoTuneMode.DEFAULT_OFF;

  private String queryTuningFile = "ebean-autotune.xml";

  private boolean queryTuning;

  private boolean queryTuningAddVersion;

  private boolean profiling;

  private String profilingFile = "ebean-profiling";

  private int profilingBase = 5;

  private double profilingRate = 0.01;

  private int profilingUpdateFrequency;

  private int garbageCollectionWait = 100;

  private boolean skipGarbageCollectionOnShutdown;

  private boolean skipProfileReportingOnShutdown;

  public AutoTuneConfig() {
  }

  /**
   * Return true if we are profiling or query tuning.
   *
   * If we are not doing either then we don't need a CallStack.
   */
  public boolean isActive() {
    return profiling || queryTuning;
  }

  /**
   * Return the name of the file that holds the query tuning information.
   */
  public String getQueryTuningFile() {
    return queryTuningFile;
  }

  /**
   * Set the name of the file that holds the query tuning information.
   */
  public void setQueryTuningFile(String queryTuningFile) {
    this.queryTuningFile = queryTuningFile;
  }

  /**
   * Return the name of the file that profiling information is written to.
   */
  public String getProfilingFile() {
    return profilingFile;
  }

  /**
   * Set the name of the file that profiling information is written to.
   */
  public void setProfilingFile(String profilingFile) {
    this.profilingFile = profilingFile;
  }

  /**
   * Return the frequency in seconds the profiling should be collected and automatically applied to the tuning.
   */
  public int getProfilingUpdateFrequency() {
    return profilingUpdateFrequency;
  }

  /**
   * Set the frequency in seconds the profiling should be collected and automatically applied to the tuning.
   */
  public void setProfilingUpdateFrequency(int profilingUpdateFrequency) {
    this.profilingUpdateFrequency = profilingUpdateFrequency;
  }

  /**
   * Return the mode used when autoTune has not been explicit defined on a
   * query.
   */
  public AutoTuneMode getMode() {
    return mode;
  }

  /**
   * Set the mode used when autoTune has not been explicit defined on a query.
   */
  public void setMode(AutoTuneMode mode) {
    this.mode = mode;
  }

  /**
   * Return true if the queries are being tuned.
   */
  public boolean isQueryTuning() {
    return queryTuning;
  }

  /**
   * Set to true if the queries should be tuned by autoTune.
   */
  public void setQueryTuning(boolean queryTuning) {
    this.queryTuning = queryTuning;
  }

  /**
   * Return true if the version property should be added when the query is
   * tuned.
   * <p>
   * If this is false then the version property will be added when profiling
   * detects that the bean is possibly going to be modified.
   * </p>
   */
  public boolean isQueryTuningAddVersion() {
    return queryTuningAddVersion;
  }

  /**
   * Set to true to force the version property to be always added by the query
   * tuning.
   * <p>
   * If this is false then the version property will be added when profiling
   * detects that the bean is possibly going to be modified.
   * </p>
   * <p>
   * Generally this is not expected to be turned on.
   * </p>
   */
  public void setQueryTuningAddVersion(boolean queryTuningAddVersion) {
    this.queryTuningAddVersion = queryTuningAddVersion;
  }

  /**
   * Return true if profiling information should be collected.
   */
  public boolean isProfiling() {
    return profiling;
  }

  /**
   * Set to true if profiling information should be collected.
   * <p>
   * The profiling information is collected and then used to generate the tuned
   * queries for autoTune.
   * </p>
   */
  public void setProfiling(boolean profiling) {
    this.profiling = profiling;
  }

  /**
   * Return the base number of queries to profile before changing to profile
   * only a percentage of following queries (profileRate).
   */
  public int getProfilingBase() {
    return profilingBase;
  }

  /**
   * Set the based number of queries to profile.
   */
  public void setProfilingBase(int profilingBase) {
    this.profilingBase = profilingBase;
  }

  /**
   * Return the rate (%) of queries to be profiled after the 'base' amount of
   * profiling.
   */
  public double getProfilingRate() {
    return profilingRate;
  }

  /**
   * Set the rate (%) of queries to be profiled after the 'base' amount of
   * profiling.
   */
  public void setProfilingRate(double profilingRate) {
    this.profilingRate = profilingRate;
  }

  /**
   * Return the time in millis to wait after a system gc to collect profiling
   * information.
   * <p>
   * The profiling information is collected on object finalise. As such we
   * generally don't want to trigger GC (let the JVM do its thing) but on
   * shutdown the autoTune manager will trigger System.gc() and then wait
   * (default 100 millis) to hopefully collect profiling information -
   * especially for short run unit tests.
   * </p>
   */
  public int getGarbageCollectionWait() {
    return garbageCollectionWait;
  }

  /**
   * Set the time in millis to wait after a System.gc() to collect profiling information.
   */
  public void setGarbageCollectionWait(int garbageCollectionWait) {
    this.garbageCollectionWait = garbageCollectionWait;
  }

  /**
   * Return true if triggering garbage collection should be skipped on shutdown.
   * You might set this when System.GC() slows a application shutdown too much.
   */
  public boolean isSkipGarbageCollectionOnShutdown() {
    return skipGarbageCollectionOnShutdown;
  }

  /**
   * Set to true if triggering garbage collection should be skipped on shutdown.
   * You might set this when System.GC() slows a application shutdown too much.
   */
  public void setSkipGarbageCollectionOnShutdown(boolean skipGarbageCollectionOnShutdown) {
    this.skipGarbageCollectionOnShutdown = skipGarbageCollectionOnShutdown;
  }

  /**
   * Return true if profile reporting should be skipped on shutdown.
   */
  public boolean isSkipProfileReportingOnShutdown() {
    return skipProfileReportingOnShutdown;
  }

  /**
   * Set to true if profile reporting should be skipped on shutdown.
   */
  public void setSkipProfileReportingOnShutdown(boolean skipProfileReportingOnShutdown) {
    this.skipProfileReportingOnShutdown = skipProfileReportingOnShutdown;
  }

  /**
   * Load the settings from the properties file.
   */
  public void loadSettings(PropertiesWrapper p) {

    queryTuning = p.getBoolean("autoTune.queryTuning", queryTuning);
    queryTuningAddVersion = p.getBoolean("autoTune.queryTuningAddVersion", queryTuningAddVersion);
    queryTuningFile = p.get("autoTune.queryTuningFile", queryTuningFile);

    skipGarbageCollectionOnShutdown = p.getBoolean("autoTune.skipGarbageCollectionOnShutdown", skipGarbageCollectionOnShutdown);
    skipProfileReportingOnShutdown = p.getBoolean("autoTune.skipProfileReportingOnShutdown", skipProfileReportingOnShutdown);

    mode = p.getEnum(AutoTuneMode.class, "autoTune.mode", mode);

    profiling = p.getBoolean("autoTune.profiling", profiling);
    profilingBase = p.getInt("autoTune.profilingBase", profilingBase);
    profilingRate = p.getDouble("autoTune.profilingRate", profilingRate);
    profilingFile = p.get("autoTune.profilingFile", profilingFile);
    profilingUpdateFrequency = p.getInt("autoTune.profilingUpdateFrequency", profilingUpdateFrequency);
  }
}
