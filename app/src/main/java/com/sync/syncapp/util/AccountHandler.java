package com.sync.syncapp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.auth0.core.UserIdentity;
import com.auth0.core.UserProfile;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.sync.syncapp.Constants;

import java.util.LinkedHashMap;
import java.util.List;

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

    public void addAccount(UserProfile profile) {
        String user_id = profile.getId().split("\\|")[1]; //remove the provider
        setUserId(user_id);
        String connection = "";
        String email = profile.getEmail();
        
        List<UserIdentity> identities = profile.getIdentities();
        Log.i(Constants.TAG, "identities: " + identities.get(0));
        UserIdentity identity = identities.get(0);
        String provider = identity.getProvider();
//        String provider = "auth0"; // from ye olden times when the sdk was broken
        String nickname = profile.getNickname();
        String picture = profile.getPictureURL();

//        {
//             "id": "string",
//             "user_id": "string",
//             "connection": "string",
//             "email": "string",
//             "provider": "string",
//             "nickname": "string",
//             "picture": "string"
//         }

        JsonObject json = new JsonObject();
        json.addProperty("user_id", user_id);
        json.addProperty("connection", connection);
        json.addProperty("email", email);
        json.addProperty("provider", provider);
        json.addProperty("nickname", nickname);
        json.addProperty("picture", picture);

        Ion.with(context)
                .load(Constants.API + "/api/Accounts")
                .setHeader(Constants.AUTH_KEY, Constants.AUTH_VALUE)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error
                        if(e != null) {
                            Log.e(Constants.TAG, "error POSTing new account", e);
                            return;
                        }
                        if(result != null) {
                            Log.i(Constants.TAG, "Received result: " + result);
                            Toast.makeText(context, "We didn't die!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void logout() {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS, 0);
        prefs.edit().putString(Constants.USER_ID, "").apply();
    }
}
