package io.ebean.meta;

import java.util.List;

/**
 * Provides access to the meta data in Database such as query execution statistics.
 */
public interface MetaInfoManager {

  /**
   * Return the metrics for the database instance.
   * <p>
   * This will reset the metrics (reset counters back to zero etc) and
   * will only return the non-empty metrics.
   * </p>
   */
  ServerMetrics collectMetrics();

  /**
   * Collect query plans.
   */
  List<MetaQueryPlan> collectQueryPlans(QueryPlanRequest request);

  /**
   * Visit the metrics resetting and collecting/reporting as desired.
   */
  void visitMetrics(MetricVisitor visitor);

  /**
   * Run a visit collecting all the metrics and returning BasicMetricVisitor
   * which holds all the metrics in simple lists.
   */
  BasicMetricVisitor visitBasic();

  /**
   * Just reset all the metrics. Maybe only useful for testing purposes.
   */
  void resetAllMetrics();

  /**
   * Collect and return the ObjectGraphNode statistics.
   * <p>
   * These show query executions based on an origin point and relative path.
   * This is used to look at the amount of lazy loading occurring for a given
   * query origin point and highlight potential for tuning a query.
   * </p>
   *
   * @param reset Set to true to reset the underlying statistics after collection.
   */
  List<MetaOrmQueryNode> collectNodeStatistics(boolean reset);

}
