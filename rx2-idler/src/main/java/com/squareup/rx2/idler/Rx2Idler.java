package com.squareup.rx2.idler;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.IdlingResource;
import io.reactivex.Scheduler;
import io.reactivex.functions.Function;
import java.util.concurrent.Callable;

/**
 * Factory methods for connecting RxJava's {@link Scheduler} to Espresso's {@link IdlingResource}.
 * <p>
 * <pre><code>
 * RxJavaPlugins.setInitComputationSchedulerHandler(
 *     Rx2Idler.create("RxJava 2.x Computation Scheduler"));
 * </code></pre>
 */
public final class Rx2Idler {
  /**
   * Returns a function which wraps the supplied {@link Scheduler} in one which notifies Espresso as
   * to whether it is currently executing work or not.
   * <p>
   * Note: Work scheduled in the future does not mark the idling resource as busy.
   */
  @SuppressWarnings("ConstantConditions") // Public API guarding.
  @CheckResult @NonNull
  public static Function<Callable<Scheduler>, Scheduler> create(@NonNull final String name) {
    if (name == null) throw new NullPointerException("name == null");
    return new Function<Callable<Scheduler>, Scheduler>() {
      @Override public Scheduler apply(Callable<Scheduler> delegate) throws Exception {
        IdlingResourceScheduler scheduler =
            new DelegatingIdlingResourceScheduler(delegate.call(), name);
        Espresso.registerIdlingResources(scheduler);
        return scheduler;
      }
    };
  }

  /**
   * Wraps the supplied {@link Scheduler} into one which also implements {@link IdlingResource}.
   * You must {@linkplain Espresso#registerIdlingResources(IdlingResource...) register} the
   * returned instance with Espresso before it will be used. Only work scheduled on the returned
   * instance directly will be registered.
   */
  @SuppressWarnings("ConstantConditions") // Public API guarding.
  @CheckResult @NonNull
  public static IdlingResourceScheduler wrap(@NonNull Scheduler scheduler, @NonNull String name) {
    if (scheduler == null) throw new NullPointerException("scheduler == null");
    if (name == null) throw new NullPointerException("name == null");
    return new DelegatingIdlingResourceScheduler(scheduler, name);
  }

  private Rx2Idler() {
    throw new AssertionError("No instances");
  }
}
