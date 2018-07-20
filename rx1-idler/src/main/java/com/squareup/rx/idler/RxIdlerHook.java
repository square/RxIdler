package com.squareup.rx.idler;

import android.support.annotation.RestrictTo;
import android.support.test.espresso.IdlingRegistry;
import rx.Scheduler;
import rx.plugins.RxJavaSchedulersHook;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
final class RxIdlerHook extends RxJavaSchedulersHook {
  @Override public Scheduler getComputationScheduler() {
    Scheduler delegate = createComputationScheduler();
    IdlingResourceScheduler scheduler = RxIdler.wrap(delegate, "RxJava 1.x Computation Scheduler");
    IdlingRegistry
            .getInstance()
            .register(scheduler);
    return scheduler;
  }

  @Override public Scheduler getIOScheduler() {
    Scheduler delegate = createIoScheduler();
    IdlingResourceScheduler scheduler = RxIdler.wrap(delegate, "RxJava 1.x IO Scheduler");
    IdlingRegistry
            .getInstance()
            .register(scheduler);
    return scheduler;
  }

  @Override public Scheduler getNewThreadScheduler() {
    Scheduler delegate = createNewThreadScheduler();
    IdlingResourceScheduler scheduler = RxIdler.wrap(delegate, "RxJava 1.x New Thread Scheduler");
    IdlingRegistry
            .getInstance()
            .register(scheduler);
    return scheduler;
  }
}
