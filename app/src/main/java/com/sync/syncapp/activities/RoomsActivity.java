package com.sync.syncapp.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.sync.syncapp.Constants;
import com.sync.syncapp.R;
import com.sync.syncapp.util.AccountHandler;

public class RoomsActivity extends ActionBarActivity {
    
    AccountHandler handler;
    
    ListView roomsList;
    Button roomAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);
        
        final Context context = getApplicationContext();
        
        handler = AccountHandler.newInstance(context);
        
        roomAdd = (Button) findViewById(R.id.rooms_add);
        roomAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), AddRoomActivity.class));
            }
        });
        
        roomsList = (ListView) findViewById(R.id.rooms_list);
    }
    
    protected void onStart() {
        super.onStart();
        Ion.with(getApplicationContext())
                .load(Constants.API + "/api/AccountRooms/" + handler.getUserId())
                .setHeader(Constants.AUTH_KEY, Constants.AUTH_VALUE)
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        if(e != null) {
                            Log.e(Constants.TAG, "error getting rooms", e);
                            return;
                        }
                        if(result != null) {
                            int size = result.size();
                            
                            String[] rooms = new String[size];
                            
                            if(size > 0) {
                                for(int i = 0; i < size; i++) {
                                    JsonObject room = result.get(i).getAsJsonObject();
                                    String roomName = room.get("RoomType").getAsString();
                                    
                                    rooms[i] = roomName;
                                }

                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                                        R.layout.spinner_item, rooms);
                                roomsList.setAdapter(adapter);
                            }
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_rooms, menu);
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
