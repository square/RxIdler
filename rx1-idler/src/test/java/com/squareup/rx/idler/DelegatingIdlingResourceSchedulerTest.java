package com.squareup.rx.idler;

import android.support.test.espresso.IdlingResource;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
import org.junit.Test;
import rx.Scheduler;
import rx.functions.Action0;
import rx.schedulers.TestScheduler;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class DelegatingIdlingResourceSchedulerTest {
  private final TestScheduler delegate = new TestScheduler();
  private final IdlingResourceScheduler scheduler = RxIdler.wrap(delegate, "Bob");
  private final AtomicInteger idleCount = new AtomicInteger();

  @Before public void setUp() {
    scheduler.registerIdleTransitionCallback(new IdlingResource.ResourceCallback() {
      @Override public void onTransitionToIdle() {
        idleCount.incrementAndGet();
      }
    });
  }

  @Test public void name() {
    assertEquals("Bob", scheduler.getName());
  }

  @Test public void creatingWorkerReportsIdle() {
    assertIdle(0);
    scheduler.createWorker();
    assertIdle(0);
  }

  @Test public void scheduledWorkReportsBusy() {
    Scheduler.Worker worker = scheduler.createWorker();
    worker.schedule(new CountingAction());
    assertBusy();
  }

  @Test public void scheduledWorkUnsubscribedReportsIdle() {
    Scheduler.Worker worker = scheduler.createWorker();
    worker.schedule(new CountingAction()).unsubscribe();
    assertIdle(1);
  }

  @Test public void scheduleWithZeroDelayReportsBusy() {
    Scheduler.Worker worker = scheduler.createWorker();
    worker.schedule(new CountingAction(), 0, SECONDS);
    assertBusy();
  }

  @Test public void scheduleWithNonZeroDelayReportsIdle() {
    Scheduler.Worker worker = scheduler.createWorker();
    worker.schedule(new CountingAction(), 1, SECONDS);
    assertIdle(0);
  }

  @Test public void schedulePeriodicallyWithZeroDelayReportsBusy() {
    Scheduler.Worker worker = scheduler.createWorker();
    worker.schedulePeriodically(new CountingAction(), 0, 1, SECONDS);
    assertBusy();
  }

  @Test public void schedulePeriodicallyWithNonZeroDelayReportsIdle() {
    Scheduler.Worker worker = scheduler.createWorker();
    worker.schedulePeriodically(new CountingAction(), 1, 1, SECONDS);
    assertIdle(0);
  }

  @Test public void betweenPeriodicSchedulesReportsIdle() {
    Scheduler.Worker worker = scheduler.createWorker();
    CountingAction action = new CountingAction();
    worker.schedulePeriodically(action, 0, 1, SECONDS);
    delegate.triggerActions();
    assertEquals(1, action.count());
    delegate.advanceTimeBy(500, MILLISECONDS);
    assertIdle(1);
    delegate.advanceTimeBy(1000, MILLISECONDS);
    assertIdle(2);
  }

  @Test public void runningWorkReportsBusy() {
    Scheduler.Worker worker = scheduler.createWorker();
    worker.schedule(new Action0() {
      @Override public void call() {
        assertBusy();
      }
    });
    delegate.triggerActions();
  }

  @Test public void unsubscribingScheduledWorksReportsIdle() {
    Scheduler.Worker worker = scheduler.createWorker();
    worker.schedule(new CountingAction());
    worker.unsubscribe();
    assertIdle(1);
  }

  @Test public void unsubscribingScheduledWorkWhileRunningWorkReportsBusy() {
    final Scheduler.Worker worker = scheduler.createWorker();
    worker.schedule(new Action0() {
      @Override public void call() {
        worker.unsubscribe();
        assertBusy();
      }
    });
    delegate.triggerActions();
  }

  @Test public void scheduleWorkAfterUnsubscribedReportsIdle() {
    Scheduler.Worker worker = scheduler.createWorker();
    worker.unsubscribe();
    worker.schedule(new CountingAction());
    assertIdle(0);
  }

  private void assertBusy() {
    assertFalse(scheduler.isIdleNow());
  }

  private void assertIdle(int count) {
    assertTrue(scheduler.isIdleNow());
    assertEquals(count, idleCount.get());
  }
}
