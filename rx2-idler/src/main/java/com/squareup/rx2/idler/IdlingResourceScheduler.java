package com.squareup.rx2.idler;

import androidx.test.espresso.IdlingResource;
import io.reactivex.Scheduler;

/** A RxJava {@link Scheduler} that is also an Espresso {@link IdlingResource}. */
public abstract class IdlingResourceScheduler extends Scheduler implements IdlingResource {
}
