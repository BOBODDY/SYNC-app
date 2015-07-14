package com.sync.syncapp.activities;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.sync.syncapp.Constants;
import com.sync.syncapp.R;
import com.sync.syncapp.util.AccountHandler;

public class AddRoomActivity extends ActionBarActivity {

    Button doneButton;

    EditText roomType, roomDesc;

    Spinner person;

    AccountHandler accountHandler;

    ProgressBar loading;

    String[] userNames, userIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_room);

        loading = (ProgressBar) findViewById(R.id.add_room_progress);
        loading.setVisibility(View.VISIBLE);

        accountHandler = AccountHandler.newInstance(this);

        person = (Spinner) findViewById(R.id.add_room_person);

        Ion.with(this)
                .load(Constants.API + "/api/AccountPersons/" + accountHandler.getUserId())
                .setHeader(Constants.AUTH_KEY, Constants.AUTH_VALUE)
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        if (e != null) {
                            Log.e(Constants.TAG, "error loading users: ", e);
                            return;
                        }

                        if (result != null) {
                            loading.setVisibility(View.VISIBLE);
                            doneButton.setEnabled(false);

                            if(result.size() > 0) {
                                userNames = new String[result.size()];
                                userIds = new String[result.size()];

                                for(int i=0; i < result.size(); i++) {
                                    JsonObject user = result.get(i).getAsJsonObject();

                                    userNames[i] = user.get("nickname").getAsString();
                                    userIds[i] = user.get("id").getAsString();
                                }

                                ArrayAdapter<String> users = new ArrayAdapter<String>(getApplicationContext(),
                                        R.layout.spinner_item, userNames);
                                person.setAdapter(users);

                                loading.setVisibility(View.INVISIBLE);
                                doneButton.setEnabled(true);
                            } else {
                                String[] noUsers = {"No users added"};

                                ArrayAdapter<String> users = new ArrayAdapter<String>(getApplicationContext(),
                                        R.layout.spinner_item, noUsers);
                                person.setAdapter(users);

                                loading.setVisibility(View.INVISIBLE);
                                doneButton.setEnabled(false);
                            }
                        }
                    }
                });

        roomType = (EditText) findViewById(R.id.add_room_type);

        roomDesc = (EditText) findViewById(R.id.add_room_desc);

        doneButton = (Button) findViewById(R.id.add_room_done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loading.setVisibility(View.VISIBLE);
                doneButton.setEnabled(false);

                final String roomTypeStr = roomType.getText().toString();
                String roomDescStr = roomDesc.getText().toString();

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("account_id", accountHandler.getUserId());
                jsonObject.addProperty("person_id", userIds[person.getSelectedItemPosition()]);
                jsonObject.addProperty("RoomType", roomTypeStr);
                jsonObject.addProperty("RoomDescription", roomDescStr);

                Log.d(Constants.TAG, "about to POST this: " + jsonObject);
                
                Ion.with(getApplicationContext())
                        .load(Constants.API + "/api/Rooms")
                        .setHeader(Constants.AUTH_KEY, Constants.AUTH_VALUE)
                        .setJsonObjectBody(jsonObject)
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                if(e != null) {
                                    Toast.makeText(getApplicationContext(), "There was an error sending the room", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                if(result != null) {
                                    Log.d(Constants.TAG, "Got result: " + result);
                                    Log.d(Constants.TAG, "added room: " + roomTypeStr);
                                    Toast.makeText(getApplicationContext(), "Added room " + roomTypeStr, Toast.LENGTH_SHORT).show();

                                    finish();
                                }
                            }
                        });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_room, menu);
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
