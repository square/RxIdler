package com.example.sample;

import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;
import rx.Observable;
import rx.functions.Action1;

public class UserManager {

    private static final int TIME_DELAY_MILLISECONDS = 3000;

    /**
     * a normal rx {@link Observable#delay(long, TimeUnit)} will not work with RxIdler (yet)
     * see: https://www.reddit.com/r/androiddev/comments/6mzw22/rxidler/dk5vjv6/
     */
    public rx.Observable<User> getUser() {
        return Observable.just(buildUser())
                .doOnNext(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        doSomeLongRunningTask();
                    }
                });
    }

    public io.reactivex.Observable<User> getUsersRx2() {
        return io.reactivex.Observable.just(buildUser())
                .doOnNext(new Consumer<User>() {
                    @Override
                    public void accept(User user) throws Exception {
                        doSomeLongRunningTask();
                    }
                });
    }

    private User buildUser() {
        return new User("Alice", "Jones");
    }

    private void doSomeLongRunningTask() {
        try {
            Thread.sleep(TIME_DELAY_MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
