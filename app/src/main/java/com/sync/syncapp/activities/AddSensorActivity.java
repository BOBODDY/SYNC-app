package com.sync.syncapp.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.auth0.core.UserIdentity;
import com.auth0.core.UserProfile;
import com.auth0.lock.Configuration;
import com.auth0.lock.Lock;
import com.auth0.lock.LockActivity;
import com.auth0.lock.LockProvider;
import com.sync.syncapp.Constants;
import com.sync.syncapp.R;
import com.sync.syncapp.SyncApplication;
import com.sync.syncapp.SyncApplication.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static com.auth0.lock.Lock.AUTHENTICATION_ACTION;
import static com.auth0.lock.Lock.CANCEL_ACTION;

public class AddSensorActivity extends Activity {

    TextView title;
    Button authFitbit;

    LocalBroadcastManager broadcastManager;
    private BroadcastReceiver authenticationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(Constants.TAG, "AddSensorActivity intent's component: " + intent.getComponent());

            UserProfile profile = intent.getParcelableExtra(Lock.AUTHENTICATION_ACTION_PROFILE_PARAMETER);

            Log.d(Constants.TAG, "Received login (hopefully fitbit) " + profile);
            //TODO: get token from intent extras
            String a = "";
            String clientId = (String) profile.getExtraInfo().get("clientID");

            Log.d(Constants.TAG, "extra info: " + profile.getExtraInfo());

            a += "Client ID: " + clientId + "\n";

            Log.d(Constants.TAG, "profile.getIdentities(): " + profile.getIdentities());

            //TODO: can't get identities without a ClassCastException
            List<UserIdentity> idents = profile.getIdentities();
            Log.d(Constants.TAG, "idents: " + idents);

//            for(int i=0; i < idents.size(); i++) {
//                Log.d(Constants.TAG, "identity: " + idents.get(0));
//
//                UserIdentity identity = (UserIdentity) idents.get(i);
//
//                String token = identity.getAccessToken();
////                String token = (String) identity.get("access_token");
//                String tokenSecret = identity.getAccessTokenSecret();
////                String tokenSecret = (String) identity.get("access_token_secret");
//                String userId = identity.getId();
////                String userId = (String) identity.get("user_id");
//
//                a += "Access Token: " + token + "\n";
//                a += "Access Token Secret: " + tokenSecret + "\n";
//                a += "User ID: " + userId + "\n";
//                a += "\n";
//            }

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
                new AlertDialog.Builder(AddSensorActivity.this)
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

        Log.d(Constants.TAG, "Registering AddSensorActivity BroadcastReceivers");
        broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.registerReceiver(authenticationReceiver, new IntentFilter(AUTHENTICATION_ACTION));
        broadcastManager.registerReceiver(cancelReceiver, new IntentFilter(CANCEL_ACTION));
    }

    protected void onDestroy() {
        Log.d(Constants.TAG, "Unregistering AddSensorActivity BroadcastReceivers");
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
