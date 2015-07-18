package com.sync.syncapp.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.sync.syncapp.Constants;
import com.sync.syncapp.model.Account;
import com.sync.syncapp.model.Person;
import com.sync.syncapp.model.Room;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nick on 6/29/15.
 */
public class ApiWrapper {

    Context context;
    Account account;

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public static ApiWrapper newInstance(Context c, Account account) {
        ApiWrapper wrapper = new ApiWrapper();
        wrapper.setContext(c);
        wrapper.setAccount(account);
        return wrapper;
    }

    public ApiWrapper() {}
    
    public List<Room> getAccountRooms() {
        final List<Room> rooms = new ArrayList<>();
        
        Ion.with(context)
                .load(Constants.API + "/api/AccountRooms/" + account.getUserId())
                .setHeader(Constants.AUTH_KEY, Constants.AUTH_VALUE)
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        if (e != null) {
                            Log.e(Constants.TAG, "error getting Rooms for account", e);
                            return;
                        }
                        if (result != null) {
                            Log.d(Constants.TAG, "this is the result: " + result);
                            
                            int size = result.size();
                            if (size > 0) {

                                for (int i = 0; i < size; i++) {
                                    JsonObject room = result.get(i).getAsJsonObject();

                                    String roomName = room.get("RoomType").getAsString();
                                    String roomId = room.get("id").getAsString();
                                    
                                    Room newRoom = new Room(account, new Person(account, ""), "", roomName, "");
                                    newRoom.setId(roomId);
                                    
                                    rooms.add(newRoom);
                                }
                            }

                        }
                    }
                });
        
        return rooms;
    }

    /**
     * Returns all the accounts. Not sure this should be provided
     * @return List of all the accounts in the db
     */
    public List<Account> getAccounts() {
        final List<Account> accounts = new ArrayList<>();
        
        Ion.with(getContext())
                .load(Constants.API + "/api/Accounts")
                .setHeader(Constants.AUTH_KEY, Constants.AUTH_VALUE)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            Log.e(Constants.TAG, "error getting accounts: ", e);
                            return;
                        }

                        if (result != null) {
                            accounts.add(new Account("", "", "", "", "", ""));
                        }
                    }
                });

        return accounts;
    }

    public static void postESensor(final Context c, String dest, final JsonObject object) {
        Ion.with(c)
                .load(dest)
                .setHeader(Constants.AUTH_KEY, Constants.AUTH_VALUE)
                .setJsonObjectBody(object)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if(e != null) {
                            Log.e(Constants.TAG, "error pushing e-sensor", e);
                            return;
                        }

                        if(result != null) {
                            Log.d(Constants.TAG, "Successfully pushed e-sensor " + object.get("name").getAsString());
                            ((Activity) c).finish();
                        }
                    }
                });
    }
}
