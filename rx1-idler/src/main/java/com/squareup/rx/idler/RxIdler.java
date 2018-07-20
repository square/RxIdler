package com.squareup.rx.idler;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import rx.Scheduler;
import rx.plugins.RxJavaSchedulersHook;

/**
 * Utilities for connecting RxJava's {@link Scheduler} to Espresso's {@link IdlingResource}.
 */
public final class RxIdler {
  /**
   * An {@link RxJavaSchedulersHook} which wraps
   * <p>
   * <pre><code>
   * RxJavaPlugins.getInstance().registerSchedulersHook(RxIdler.hooks());
   * </code></pre>
   */
  @CheckResult @NonNull
  public static RxJavaSchedulersHook hooks() {
    return new RxIdlerHook();
  }

  /**
   * Wraps the supplied {@link Scheduler} into one which also implements {@link IdlingResource}.
   * You must {@linkplain IdlingRegistry#register(IdlingResource...) register} the
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

  private RxIdler() {
    throw new AssertionError("No instances");
  }
}
