package io.ebean;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Background thread pool service for executing of tasks asynchronously.
 * <p>
 * This service is used internally by Ebean for executing background tasks such
 * as the {@link Query#findFutureList()} and also for executing background tasks
 * periodically.
 * </p>
 * <p>
 * This service has been made available so you can use it for your application
 * code if you want. It can be useful for some server caching implementations
 * (background population and trimming of the cache etc).
 * </p>
 *
 * @author rbygrave
 */
public interface BackgroundExecutor {

  /**
   * Execute a task in the background.
   */
  void execute(Runnable r);

  /**
   * Execute a task periodically with a fixed delay between each execution.
   * <p>
   * For example, execute a runnable every minute.
   * </p>
   * <p>
   * The delay is the time between executions no matter how long the task took.
   * That is, this method has the same behaviour characteristics as
   * {@link ScheduledExecutorService#scheduleWithFixedDelay(Runnable, long, long, TimeUnit)}
   * </p>
   */
  void executePeriodically(Runnable r, long delay, TimeUnit unit);

  /**
   * Schedules a Runnable for one-shot action that becomes enabled after the given delay.
   *
   * @return a ScheduledFuture representing pending completion of the task and
   *         whose get() method will return null upon completion
   */
  ScheduledFuture<?> schedule(Runnable r, long delay, TimeUnit unit);

  /**
   * Schedules a Callable for one-shot action that becomes enabled after the given delay.
   *
   * @return a ScheduledFuture that can be used to extract result or cancel
   */
  <V> ScheduledFuture<V> schedule(Callable<V> c, long delay, TimeUnit unit);


}
