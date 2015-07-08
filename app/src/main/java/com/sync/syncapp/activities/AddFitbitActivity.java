package com.sync.syncapp.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.auth0.core.UserIdentity;
import com.auth0.core.UserProfile;
import com.auth0.lock.Lock;
import com.auth0.lock.LockActivity;
import com.google.gson.JsonObject;
import com.sync.syncapp.Constants;
import com.sync.syncapp.R;
import com.sync.syncapp.util.AccountHandler;

import java.util.List;

import static com.auth0.lock.Lock.AUTHENTICATION_ACTION;
import static com.auth0.lock.Lock.CANCEL_ACTION;

public class AddFitbitActivity extends ActionBarActivity {

    TextView title;
    Button authFitbit;

    LocalBroadcastManager broadcastManager;
    private BroadcastReceiver authenticationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(Constants.TAG, "AddFitbitActivity intent's component: " + intent.getComponent());

            UserProfile profile = intent.getParcelableExtra(Lock.AUTHENTICATION_ACTION_PROFILE_PARAMETER);

            Log.d(Constants.TAG, "Received login (hopefully fitbit) " + profile);
            //TODO: get token from intent extras
            String a = "";
            String clientId = (String) profile.getExtraInfo().get("clientID");

            String fitbitId = profile.getId().split("\\|")[1];
            Log.d(Constants.TAG, "user_id: " + fitbitId);

            Log.d(Constants.TAG, "extra info: " + profile.getExtraInfo());

            a += "Client ID: " + clientId + "\n";

            Log.d(Constants.TAG, "profile.getIdentities(): " + profile.getIdentities());

            //TODO: can't get identities without a ClassCastException

            //TODO: do we need these? get them
            String apiKey = "";
            String apiSecret = "";

            AccountHandler handler = AccountHandler.newInstance(getApplicationContext());
            String userId = handler.getUserId();

            JsonObject jason = new JsonObject();
            jason.addProperty("pstype", "Fitbit");
            jason.addProperty("fitbit_user_id", fitbitId);
            jason.addProperty("account_id", userId);
            jason.addProperty("description", "Fitbit for user: " + userId);
            jason.addProperty("api_key", apiKey);
            jason.addProperty("api_secret", apiSecret);

            title.setText(a);
        }
    };

    private BroadcastReceiver cancelReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(Constants.TAG, "user canceled logging in");
//            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sensor);

        title = (TextView) findViewById(R.id.add_sensor_title);

        authFitbit = (Button) findViewById(R.id.add_sensor_fitbit);
        authFitbit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(AddFitbitActivity.this)
                        .setTitle("Notice")
                        .setIcon(R.drawable.fitbit_logo)
                        .setMessage("Make sure to log in to Fitbit")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(getApplicationContext(), LockActivity.class));
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .show();
            }
        });

        Log.d(Constants.TAG, "Registering AddFitbitActivity BroadcastReceivers");
        broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.registerReceiver(authenticationReceiver, new IntentFilter(AUTHENTICATION_ACTION));
        broadcastManager.registerReceiver(cancelReceiver, new IntentFilter(CANCEL_ACTION));
    }

    protected void onDestroy() {
        Log.d(Constants.TAG, "Unregistering AddFitbitActivity BroadcastReceivers");
        broadcastManager.unregisterReceiver(authenticationReceiver);
        broadcastManager.unregisterReceiver(cancelReceiver);

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_sensor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
