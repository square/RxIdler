package com.squareup.rx.idler;

import androidx.test.espresso.IdlingResource;
import rx.Scheduler;

/** A RxJava {@link Scheduler} that is also an Espresso {@link IdlingResource}. */
public abstract class IdlingResourceScheduler extends Scheduler implements IdlingResource {
}
