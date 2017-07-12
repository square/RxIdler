package com.squareup.rx2.idler;

import io.reactivex.schedulers.TestScheduler;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public final class Rx2IdlerTest {
  @Test public void createNullArgumentsFlow() {
    try {
      Rx2Idler.create(null);
      fail();
    } catch (NullPointerException e) {
      assertEquals("name == null", e.getMessage());
    }
  }

  @Test public void nullWrapArgumentsFails() {
    try {
      Rx2Idler.wrap(null, "Bob");
      fail();
    } catch (NullPointerException e) {
      assertEquals("scheduler == null", e.getMessage());
    }
    try {
      Rx2Idler.wrap(new TestScheduler(), null);
      fail();
    } catch (NullPointerException e) {
      assertEquals("name == null", e.getMessage());
    }
  }
}
