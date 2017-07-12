package com.squareup.rx.idler;

import java.util.concurrent.atomic.AtomicInteger;
import rx.functions.Action0;

final class CountingAction implements Action0 {
  private final AtomicInteger count = new AtomicInteger();

  int count() {
    return count.get();
  }

  @Override public void call() {
    count.incrementAndGet();
  }
}
