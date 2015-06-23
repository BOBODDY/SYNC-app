package com.sync.syncapp.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.sync.syncapp.Constants;

/**
 * Created by nick on 6/23/15.
 *
 * Taking care of user operations
 */
public class UserHandler {

    private Context context;

    private String userId;

    public static UserHandler newInstance(Context c) {
        UserHandler handler = new UserHandler();
        handler.setContext(c);
        return handler;
    }

    public UserHandler() {}

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
