package com.squareup.rx.idler;

import androidx.annotation.RestrictTo;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action0;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
final class DelegatingIdlingResourceScheduler extends IdlingResourceScheduler {
  private final Scheduler delegate;
  private final String name;
  private final AtomicInteger work = new AtomicInteger();
  private ResourceCallback callback;

  DelegatingIdlingResourceScheduler(Scheduler delegate, String name) {
    this.delegate = delegate;
    this.name = name;
  }

  @Override public String getName() {
    return name;
  }

  @Override public boolean isIdleNow() {
    return work.get() == 0;
  }

  @Override public void registerIdleTransitionCallback(ResourceCallback callback) {
    this.callback = callback;
  }

  @Override public Worker createWorker() {
    final Worker delegateWorker = delegate.createWorker();
    return new Worker() {
      private final CompositeSubscription subscriptions = new CompositeSubscription(delegateWorker);

      @Override public Subscription schedule(Action0 action) {
        if (subscriptions.isUnsubscribed()) {
          return Subscriptions.unsubscribed();
        }
        ScheduledWork work = createWork(action, 0L, 0L);
        Subscription subscription = delegateWorker.schedule(work);
        ScheduledWorkSubscription workSubscription =
            new ScheduledWorkSubscription(work, subscription);
        subscriptions.add(workSubscription);
        return workSubscription;
      }

      @Override public Subscription schedule(Action0 action, long delayTime, TimeUnit unit) {
        if (subscriptions.isUnsubscribed()) {
          return Subscriptions.unsubscribed();
        }
        ScheduledWork work = createWork(action, delayTime, 0L);
        Subscription subscription = delegateWorker.schedule(work, delayTime, unit);
        subscriptions.add(subscription);
        ScheduledWorkSubscription workSubscription =
            new ScheduledWorkSubscription(work, subscription);
        subscriptions.add(workSubscription);
        return workSubscription;
      }

      @Override
      public Subscription schedulePeriodically(Action0 action, long initialDelay, long period,
          TimeUnit unit) {
        if (subscriptions.isUnsubscribed()) {
          return Subscriptions.unsubscribed();
        }
        ScheduledWork work = createWork(action, initialDelay, period);
        Subscription subscription =
            delegateWorker.schedulePeriodically(work, initialDelay, period, unit);
        subscriptions.add(subscription);
        ScheduledWorkSubscription workSubscription =
            new ScheduledWorkSubscription(work, subscription);
        subscriptions.add(workSubscription);
        return workSubscription;
      }

      @Override public void unsubscribe() {
        subscriptions.unsubscribe();
      }

      @Override public boolean isUnsubscribed() {
        return subscriptions.isUnsubscribed();
      }
    };
  }

  void startWork() {
    work.incrementAndGet();
  }

  void stopWork() {
    if (work.decrementAndGet() == 0) {
      callback.onTransitionToIdle();
    }
  }

  ScheduledWork createWork(Action0 action, long delay, long period) {
    if (action instanceof ScheduledWork) {
      // Unwrap any re-scheduled work. We want each scheduler to get its own state machine.
      action = ((ScheduledWork) action).delegate;
    }
    boolean immediate = delay == 0;
    if (immediate) {
      startWork();
    }
    int startingState = immediate ? ScheduledWork.STATE_SCHEDULED : ScheduledWork.STATE_IDLE;
    return new ScheduledWork(action, startingState, period > 0L);
  }

  final class ScheduledWork extends AtomicInteger implements Action0 {
    static final int STATE_IDLE = 0; // --> STATE_RUNNING, STATE_UNSUBSCRIBED
    static final int STATE_SCHEDULED = 1; // --> STATE_RUNNING, STATE_UNSUBSCRIBED
    static final int STATE_RUNNING = 2; // --> STATE_IDLE, STATE_COMPLETED, STATE_UNSUBSCRIBED
    static final int STATE_COMPLETED = 3; // --> STATE_UNSUBSCRIBED
    static final int STATE_UNSUBSCRIBED = 4;

    final Action0 delegate;
    final boolean isPeriodic;

    ScheduledWork(Action0 delegate, int startingState, boolean isPeriodic) {
      super(startingState);
      this.delegate = delegate;
      this.isPeriodic = isPeriodic;
    }

    @Override public void call() {
      for (;;) {
        int state = get();
        switch (state) {
          case STATE_IDLE:
          case STATE_SCHEDULED:
            if (compareAndSet(state, STATE_RUNNING)) {
              if (state == STATE_IDLE) {
                startWork();
              }
              try {
                delegate.call();
              } finally {
                // Change state with a CAS to ensure we don't overwrite an unsubscribed state.
                compareAndSet(STATE_RUNNING, isPeriodic ? STATE_IDLE : STATE_COMPLETED);
                stopWork();
              }
              return; // CAS success, we're done.
            }
            break; // CAS failed, retry.

          case STATE_RUNNING:
            throw new IllegalStateException("Already running");

          case STATE_COMPLETED:
            throw new IllegalStateException("Already completed");

          case STATE_UNSUBSCRIBED:
            return; // Nothing to do.
        }
      }
    }

    void unsubscribe() {
      for (;;) {
        int state = get();
        if (state == STATE_UNSUBSCRIBED) {
          return; // Nothing to do.
        } else if (compareAndSet(state, STATE_UNSUBSCRIBED)) {
          // If idle, startWork() hasn't been called so we don't need a matching stopWork().
          // If running, startWork() was called but the try/finally ensures a stopWork() call.
          // If completed, both startWork() and stopWork() have been called.
          if (state == STATE_SCHEDULED) {
            stopWork(); // Scheduled but not running means we called startWork().
          }
          return;
        }
      }
    }
  }

  static final class ScheduledWorkSubscription implements Subscription {
    private final ScheduledWork work;
    private final Subscription delegate;

    ScheduledWorkSubscription(ScheduledWork work, Subscription delegate) {
      this.delegate = delegate;
      this.work = work;
    }

    @Override public void unsubscribe() {
      work.unsubscribe();
      delegate.unsubscribe();
    }

    @Override public boolean isUnsubscribed() {
      return work.get() == ScheduledWork.STATE_UNSUBSCRIBED;
    }
  }
}
