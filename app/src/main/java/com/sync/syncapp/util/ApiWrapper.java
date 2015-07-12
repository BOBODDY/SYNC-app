package com.sync.syncapp.util;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.sync.syncapp.Constants;
import com.sync.syncapp.model.Account;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nick on 6/29/15.
 */
public class ApiWrapper {

    Context context;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public static ApiWrapper newInstance(Context c) {
        ApiWrapper wrapper = new ApiWrapper();
        wrapper.setContext(c);
        return wrapper;
    }

    public ApiWrapper() {}

    /**
     * Returns all the accounts. Not sure this should be provided
     * @return List of all the accounts in the db
     */
    public List<Account> getAccounts() {
        final List<Account> accounts = new ArrayList<>();

        // TODO: set authorization headers
        Ion.with(getContext())
                .load(Constants.API + "/api/Accounts")
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if(e != null) {
                            Log.e(Constants.TAG, "error getting accounts: ", e);
                            return;
                        }

                        if(result != null) {
                            accounts.add(new Account("","","","","",""));
                        }
                    }
                });

        return accounts;
    }

}
