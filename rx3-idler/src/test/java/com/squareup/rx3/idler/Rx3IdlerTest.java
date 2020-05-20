package com.squareup.rx3.idler;

import io.reactivex.rxjava3.schedulers.TestScheduler;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public final class Rx3IdlerTest {
  @Test public void createNullArgumentsFlow() {
    try {
      Rx3Idler.create(null);
      fail();
    } catch (NullPointerException e) {
      assertEquals("name == null", e.getMessage());
    }
  }

  @Test public void nullWrapArgumentsFails() {
    try {
      Rx3Idler.wrap(null, "Bob");
      fail();
    } catch (NullPointerException e) {
      assertEquals("scheduler == null", e.getMessage());
    }
    try {
      Rx3Idler.wrap(new TestScheduler(), null);
      fail();
    } catch (NullPointerException e) {
      assertEquals("name == null", e.getMessage());
    }
  }
}
