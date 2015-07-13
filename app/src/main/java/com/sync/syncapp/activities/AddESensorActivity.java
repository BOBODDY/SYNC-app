package com.sync.syncapp.activities;

import android.app.Activity;
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
import com.sync.syncapp.util.ApiWrapper;

public class AddESensorActivity extends ActionBarActivity {

    Spinner esensorType;
    Spinner esensorRoom;
    EditText esensorName;
    EditText esensorDeviceId;
    Button doneButton;
    ProgressBar progress;

    AccountHandler theHandler;

    String[] roomNames = new String[0];
    String[] roomIds = new String[0];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_esensor);

        theHandler = AccountHandler.newInstance(getApplicationContext());

        progress = (ProgressBar) findViewById(R.id.add_esensor_progress);
        progress.setVisibility(View.VISIBLE);
//        progress.setZ(20);

        esensorType = (Spinner) findViewById(R.id.add_esensor_type);

        esensorName = (EditText) findViewById(R.id.add_esensor_name);

        esensorDeviceId = (EditText) findViewById(R.id.add_esensor_device_id);

        doneButton = (Button) findViewById(R.id.add_esensor_done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progress.setVisibility(View.VISIBLE);

                String type = esensorType.getSelectedItem().toString();
                final String name  = esensorName.getText().toString();
                String deviceId = esensorDeviceId.getText().toString();
                String roomId = roomIds[(int) esensorRoom.getSelectedItemId()];

                String userId = theHandler.getUserId();

                JsonObject json = new JsonObject();
                json.addProperty("estype", type);
                json.addProperty("account_id", userId);
                json.addProperty("name", name);
                json.addProperty("description", type + " sensor added to " + userId);
                json.addProperty("Device_ID", deviceId);
                json.addProperty("room_id", roomId);

//              TODO:  ApiWrapper.postESensor(getApplicationContext(), Constants.API + "/api/ESensors", json);
                Ion.with(getApplicationContext())
                        .load(Constants.API + "/api/ESensors")
                        .setHeader(Constants.AUTH_KEY, Constants.AUTH_VALUE)
                        .setJsonObjectBody(json)
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                if(e != null) {
                                    Log.e(Constants.TAG, "error pushing e-sensor", e);
                                    return;
                                }

                                if(result != null) {
                                    Log.d(Constants.TAG, "Successfully pushed e-sensor " + name);
                                    Toast.makeText(getApplicationContext(), "Successfully added sensor " + name, Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }
                        });
            }
        });

        esensorRoom = (Spinner) findViewById(R.id.add_esensor_room);
        
        // TODO: ApiWrapper.getAccountRooms()
        Ion.with(getApplicationContext())
                .load(Constants.API + "/api/AccountRooms/" + theHandler.getUserId())
                .setHeader(Constants.AUTH_KEY, Constants.AUTH_VALUE)
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        if (e != null) {
                            Log.e(Constants.TAG, "error fetching rooms in AddESensorActivity", e);
                            return;
                        }

                        if (result != null) {
                            int size = result.size();
                            if (size > 0) {
                                roomNames = new String[size];
                                roomIds = new String[size];

                                for (int i = 0; i < size; i++) {
                                    JsonObject room = result.get(i).getAsJsonObject();

                                    String roomName = room.get("RoomType").getAsString();
                                    String roomId = room.get("id").getAsString();

                                    roomNames[i] = roomName;
                                    roomIds[i] = roomId;
                                }

                                ArrayAdapter<String> roomAdapter =
                                        new ArrayAdapter<>(getApplicationContext(),
                                                android.R.layout.simple_spinner_dropdown_item,
                                                roomNames);
                                esensorRoom.setAdapter(roomAdapter);
                                doneButton.setEnabled(true);
                            }
                        } else {
                            String[] noRooms = {"No rooms added"};
                            ArrayAdapter<String> roomAdapter =
                                    new ArrayAdapter<>(getApplicationContext(),
                                            android.R.layout.simple_spinner_dropdown_item,
                                            noRooms);
                            esensorRoom.setAdapter(roomAdapter);

                            doneButton.setEnabled(false);
                        }
                        progress.setVisibility(View.INVISIBLE);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_esensor, menu);
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
