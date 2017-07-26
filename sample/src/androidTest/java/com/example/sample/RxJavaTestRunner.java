package com.example.sample;

import android.support.test.runner.AndroidJUnitRunner;

import com.squareup.rx.idler.RxIdler;
import com.squareup.rx2.idler.Rx2Idler;

public class RxJavaTestRunner extends AndroidJUnitRunner {

    @Override
    public void onStart() {
        // for RxJava 1.x
        rx.plugins.RxJavaPlugins.getInstance().registerSchedulersHook(RxIdler.hooks());

        // for RxJava 2.x; set handler for each Scheduler you use
        io.reactivex.plugins.RxJavaPlugins.setInitIoSchedulerHandler(
                Rx2Idler.create("RxJava 2.x IO Scheduler"));

        io.reactivex.plugins.RxJavaPlugins.setInitComputationSchedulerHandler(
                Rx2Idler.create("RxJava 2.x Computation Scheduler"));

        io.reactivex.plugins.RxJavaPlugins.setInitNewThreadSchedulerHandler(
                Rx2Idler.create("RxJava 2.x New Thread Scheduler"));

        super.onStart();
    }
}
