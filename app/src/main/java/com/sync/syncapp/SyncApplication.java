package com.sync.syncapp;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import com.auth0.core.Strategies;
import com.auth0.googleplus.GooglePlusIdentityProvider;
import com.auth0.identity.IdentityProvider;
import com.auth0.identity.IdentityProviderCallback;
import com.auth0.identity.IdentityProviderRequest;
import com.auth0.identity.WebIdentityProvider;
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
                .withIdentityProvider(Strategies.GooglePlus, new GooglePlusIdentityProvider(this))
                .useConnections("fitbit")
                .closable(true)
                .build();
    }

    public Lock getLock() {
        return lock;
    }
}
