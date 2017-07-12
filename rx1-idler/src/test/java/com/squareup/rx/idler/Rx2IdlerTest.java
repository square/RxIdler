package com.squareup.rx.idler;

import org.junit.Test;
import rx.schedulers.TestScheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public final class Rx2IdlerTest {
  @Test public void nullWrapArgumentsFails() {
    try {
      RxIdler.wrap(null, "Bob");
      fail();
    } catch (NullPointerException e) {
      assertEquals("scheduler == null", e.getMessage());
    }
    try {
      RxIdler.wrap(new TestScheduler(), null);
      fail();
    } catch (NullPointerException e) {
      assertEquals("name == null", e.getMessage());
    }
  }
}
