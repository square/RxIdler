RxIdler
=======

An `IdlingResource` for Espresso which wraps an RxJava `Scheduler`.


Usage
-----

Set the wrapping functions as the delegate for handling scheduler initialization to RxJava:

 *  RxJava 2.x:
 
    ```java
    RxJavaPlugins.setInitComputationSchedulerHandler(
        Rx2Idler.create("RxJava 2.x Computation Scheduler"));
    ```

 *  RxJava 1.x:
    
    ```java
    RxJavaPlugins.getInstance().registerSchedulersHook(RxIdler.hooks());
    ```

When that `Scheduler` is first accessed via `Schedulers`, the RxIdler function will wrap it with an
Espresso `IdlingResource` and side-effect by registering it to the `Espresso` class.

This code is most frequently put in a custom test runner's `onStart()` method:
```java
public final class MyTestRunner extends AndroidJUnitRunner {
  @Override public void onStart() {
    RxJavaPlugins.setInitComputationSchedulerHandler(
        Rx2Idler.create("RxJava 2.x Computation Scheduler"));
    // etc...
    
    super.onStart();
  }
}
```

If you have custom `Scheduler` implementations you can wrap them directly and then register them
with Espresso:

 *  RxJava 2.x:

    ```java
    IdlingResourceScheduler wrapped = Rx2Idler.wrap(myScheduler, "My Scheduler");
    Espresso.registerIdlingResources(wrapped);
    // Use 'wrapped' now instead of 'myScheduler'...
    ```

 *  RxJava 1.x:
    ```java
    IdlingResourceScheduler wrapped = RxIdler.wrap(myScheduler, "My Scheduler");
    Espresso.registerIdlingResources(wrapped);
    // Use 'wrapped' now instead of 'myScheduler'...
    ```


Download
--------

 *  RxJava 2.x:

    ```groovy
    dependencies {
      androidTestImplementation 'com.squareup.rx.idler:rx2-idler:0.9.0'
    }
    ```

 *  RxJava 1.x:

    ```groovy
    dependencies {
      androidTestImplementation 'com.squareup.rx.idler:rx1-idler:0.9.0'
    }
    ```

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap].


ProGuard
--------

If you use ProGuard on Espresso test builds, you may need to add the following rules into your ProGuard configuration.


RxJava 2.x:
```
-keep class io.reactivex.plugins.RxJavaPlugins { *; }
-keep class io.reactivex.disposables.CompositeDisposable { *; }
```

RxJava 1.x:
```
-keep class rx.plugins.RxJavaPlugins { *; }
-keep class rx.subscriptions.CompositeSubscription { *; }
```


License
-------

    Copyright 2017 Square, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.





 [snap]: https://oss.sonatype.org/content/repositories/snapshots/
