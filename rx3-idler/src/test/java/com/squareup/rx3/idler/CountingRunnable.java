package com.squareup.rx3.idler;

import java.util.concurrent.atomic.AtomicInteger;

final class CountingRunnable implements Runnable {
  private final AtomicInteger count = new AtomicInteger();

  int count() {
    return count.get();
  }

  @Override public void run() {
    count.incrementAndGet();
  }
}
