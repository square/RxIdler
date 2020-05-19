package com.squareup.rx3.idler;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.TestScheduler;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
import org.junit.Test;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class DelegatingIdlingResourceSchedulerTest {
  private final TestScheduler delegate = new TestScheduler();
  private final IdlingResourceScheduler scheduler = Rx3Idler.wrap(delegate, "Bob");
  private final AtomicInteger idleCount = new AtomicInteger();

  @Before public void setUp() {
    scheduler.registerIdleTransitionCallback(idleCount::incrementAndGet);
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
    worker.schedule(new CountingRunnable());
    assertBusy();
  }

  @Test public void scheduledWorkUnsubscribedReportsIdle() {
    Scheduler.Worker worker = scheduler.createWorker();
    worker.schedule(new CountingRunnable()).dispose();
    assertIdle(1);
  }

  @Test public void scheduleWithZeroDelayReportsBusy() {
    Scheduler.Worker worker = scheduler.createWorker();
    worker.schedule(new CountingRunnable(), 0, SECONDS);
    assertBusy();
  }

  @Test public void scheduleWithNonZeroDelayReportsIdle() {
    Scheduler.Worker worker = scheduler.createWorker();
    worker.schedule(new CountingRunnable(), 1, SECONDS);
    assertIdle(0);
  }

  @Test public void schedulePeriodicallyWithZeroDelayReportsBusy() {
    Scheduler.Worker worker = scheduler.createWorker();
    worker.schedulePeriodically(new CountingRunnable(), 0, 1, SECONDS);
    assertBusy();
  }

  @Test public void schedulePeriodicallyWithNonZeroDelayReportsIdle() {
    Scheduler.Worker worker = scheduler.createWorker();
    worker.schedulePeriodically(new CountingRunnable(), 1, 1, SECONDS);
    assertIdle(0);
  }

  @Test public void betweenPeriodicSchedulesReportsIdle() {
    Scheduler.Worker worker = scheduler.createWorker();
    CountingRunnable action = new CountingRunnable();
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
    worker.schedule(this::assertBusy);
    delegate.triggerActions();
  }

  @Test public void unsubscribingScheduledWorksReportsIdle() {
    Scheduler.Worker worker = scheduler.createWorker();
    worker.schedule(new CountingRunnable());
    worker.dispose();
    assertIdle(1);
  }

  @Test public void unsubscribingScheduledWorkWhileRunningWorkReportsBusy() {
    final Scheduler.Worker worker = scheduler.createWorker();
    worker.schedule(() -> {
      worker.dispose();
      assertBusy();
    });
    delegate.triggerActions();
  }

  @Test public void scheduleWorkAfterUnsubscribedReportsIdle() {
    Scheduler.Worker worker = scheduler.createWorker();
    worker.dispose();
    worker.schedule(new CountingRunnable());
    assertIdle(0);
  }

  @Test public void finishingWorkWithoutRegisteredCallbackDoesNotCrash() {
    IdlingResourceScheduler scheduler = Rx3Idler.wrap(delegate, "Bob");
    Scheduler.Worker worker = scheduler.createWorker();
    worker.schedule(new CountingRunnable());
    delegate.triggerActions();
  }

  private void assertBusy() {
    assertFalse(scheduler.isIdleNow());
  }

  private void assertIdle(int count) {
    assertTrue(scheduler.isIdleNow());
    assertEquals(count, idleCount.get());
  }
}
