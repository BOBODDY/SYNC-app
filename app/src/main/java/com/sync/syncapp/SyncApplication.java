package com.sync.syncapp;

import android.app.Application;

import com.auth0.lock.Lock;
import com.auth0.lock.LockProvider;

/**
 * Created by nick on 6/23/15.
 *
 * Auth0 requires setting up the Lock in this class
 */
public class SyncApplication extends Application implements LockProvider {

    private Lock lock;

    public void onCreate() {
        super.onCreate();
        lock = new Lock.Builder()
                .loadFromApplication(this)
                //TODO: add native G+ login here
                .closable(true)
                .build();
    }

    public Lock getLock() {
        return lock;
    }
}
