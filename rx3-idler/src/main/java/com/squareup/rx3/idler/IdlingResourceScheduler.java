package com.squareup.rx3.idler;

import androidx.test.espresso.IdlingResource;
import io.reactivex.rxjava3.core.Scheduler;

/** A RxJava {@link Scheduler} that is also an Espresso {@link IdlingResource}. */
public abstract class IdlingResourceScheduler extends Scheduler implements IdlingResource {
}
