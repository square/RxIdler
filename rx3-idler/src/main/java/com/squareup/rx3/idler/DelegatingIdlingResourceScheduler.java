package com.squareup.rx3.idler;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
final class DelegatingIdlingResourceScheduler extends IdlingResourceScheduler {
  private final Scheduler delegate;
  private final String name;
  private final AtomicInteger work = new AtomicInteger();
  @Nullable private ResourceCallback callback;

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
      private final CompositeDisposable disposables = new CompositeDisposable(delegateWorker);

      @Override public Disposable schedule(Runnable action) {
        if (disposables.isDisposed()) {
          return Disposable.disposed();
        }
        ScheduledWork work = createWork(action, 0L, 0L);
        Disposable disposable = delegateWorker.schedule(work);
        ScheduledWorkDisposable workDisposable = new ScheduledWorkDisposable(work, disposable);
        disposables.add(workDisposable);
        return workDisposable;
      }

      @Override public Disposable schedule(Runnable action, long delayTime, TimeUnit unit) {
        if (disposables.isDisposed()) {
          return Disposable.disposed();
        }
        ScheduledWork work = createWork(action, delayTime, 0L);
        Disposable disposable = delegateWorker.schedule(work, delayTime, unit);
        disposables.add(disposable);
        ScheduledWorkDisposable workDisposable = new ScheduledWorkDisposable(work, disposable);
        disposables.add(workDisposable);
        return workDisposable;
      }

      @Override
      public Disposable schedulePeriodically(Runnable action, long initialDelay, long period,
          TimeUnit unit) {
        if (disposables.isDisposed()) {
          return Disposable.disposed();
        }
        ScheduledWork work = createWork(action, initialDelay, period);
        Disposable disposable =
            delegateWorker.schedulePeriodically(work, initialDelay, period, unit);
        disposables.add(disposable);
        ScheduledWorkDisposable workDisposable = new ScheduledWorkDisposable(work, disposable);
        disposables.add(workDisposable);
        return workDisposable;
      }

      @Override public void dispose() {
        disposables.dispose();
      }

      @Override public boolean isDisposed() {
        return disposables.isDisposed();
      }
    };
  }

  void startWork() {
    work.incrementAndGet();
  }

  void stopWork() {
    if (work.decrementAndGet() == 0 && callback != null) {
      callback.onTransitionToIdle();
    }
  }

  ScheduledWork createWork(Runnable action, long delay, long period) {
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

  final class ScheduledWork extends AtomicInteger implements Runnable {
    static final int STATE_IDLE = 0; // --> STATE_RUNNING, STATE_DISPOSED
    static final int STATE_SCHEDULED = 1; // --> STATE_RUNNING, STATE_DISPOSED
    static final int STATE_RUNNING = 2; // --> STATE_IDLE, STATE_COMPLETED, STATE_DISPOSED
    static final int STATE_COMPLETED = 3; // --> STATE_DISPOSED
    static final int STATE_DISPOSED = 4;

    final Runnable delegate;

    private final boolean isPeriodic;

    ScheduledWork(Runnable delegate, int startingState, boolean isPeriodic) {
      super(startingState);
      this.delegate = delegate;
      this.isPeriodic = isPeriodic;
    }

    @Override public void run() {
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
                delegate.run();
              } finally {
                // Change state with a CAS to ensure we don't overwrite a disposed state.
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

          case STATE_DISPOSED:
            return; // Nothing to do.
        }
      }
    }

    void dispose() {
      for (;;) {
        int state = get();
        if (state == STATE_DISPOSED) {
          return; // Nothing to do.
        } else if (compareAndSet(state, STATE_DISPOSED)) {
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

  static final class ScheduledWorkDisposable implements Disposable {
    private final ScheduledWork work;
    private final Disposable delegate;

    ScheduledWorkDisposable(ScheduledWork work, Disposable delegate) {
      this.delegate = delegate;
      this.work = work;
    }

    @Override public void dispose() {
      work.dispose();
      delegate.dispose();
    }

    @Override public boolean isDisposed() {
      return work.get() == ScheduledWork.STATE_DISPOSED;
    }
  }
}
