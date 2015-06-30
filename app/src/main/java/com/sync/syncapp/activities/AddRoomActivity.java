package com.sync.syncapp.activities;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.sync.syncapp.Constants;
import com.sync.syncapp.R;
import com.sync.syncapp.util.AccountHandler;

public class AddRoomActivity extends ActionBarActivity {

    Button doneButton;

    EditText roomType, roomDesc;

    AccountHandler accountHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_room);

        accountHandler = AccountHandler.newInstance(getApplicationContext());

        roomType = (EditText) findViewById(R.id.add_room_type);

        roomDesc = (EditText) findViewById(R.id.add_room_desc);

        doneButton = (Button) findViewById(R.id.add_room_done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String roomTypeStr = roomType.getText().toString();
                String roomDescStr = roomDesc.getText().toString();

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("user_id", accountHandler.getUserId());
                jsonObject.addProperty("RoomType", roomTypeStr);
                jsonObject.addProperty("RoomDescription", roomDescStr);

                Ion.with(getApplicationContext())
                        .load(Constants.API + "/api/Rooms")
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
