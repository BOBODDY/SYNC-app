package com.sync.syncapp.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.sync.syncapp.Constants;

/**
 * Created by nick on 6/23/15.
 *
 * Taking care of user operations
 */
public class AccountHandler {

    private Context context;

    private String userId;

    public static AccountHandler newInstance(Context c) {
        AccountHandler handler = new AccountHandler();
        handler.setContext(c);
        return handler;
    }

    public AccountHandler() {}

    public String getUserId() {
        String userId = context.getSharedPreferences(Constants.PREFS, 0).getString(Constants.USER_ID, "");
        this.userId = userId;
        return userId;
    }

    public void setUserId(String id) {
        this.userId = id;

        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS, 0);
        prefs.edit().putString(Constants.USER_ID, id).apply();
    }

    protected void setContext(Context c) {
        context = c;
    }
}
