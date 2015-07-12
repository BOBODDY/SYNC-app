package com.sync.syncapp.activities;

import android.support.v7.app.AppCompatActivity;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.sync.syncapp.Constants;
import com.sync.syncapp.R;
import com.sync.syncapp.util.AccountHandler;

import org.json.JSONObject;

public class AddPersonActivity extends AppCompatActivity {

    TextView nameTitle;
    EditText personName;
    ProgressBar loading;
    Button done;

    AccountHandler theHandler; //https://www.youtube.com/watch?v=BF1DQr5dKW8

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_person);

        loading = (ProgressBar) findViewById(R.id.add_person_loading);
        loading.setVisibility(View.INVISIBLE);

        nameTitle = (TextView) findViewById(R.id.add_person_name_title);

        personName = (EditText) findViewById(R.id.add_person_name);

        done = (Button) findViewById(R.id.add_person_done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loading.setVisibility(View.VISIBLE);

                JsonObject personJson = new JsonObject();

                String userId = theHandler.getUserId();
//                personJson.addProperty("user_id", userId);
                personJson.addProperty("account_id", userId);
                final String name = personName.getText().toString();
                personJson.addProperty("nickname", name);

                // TODO: set authorization headers
                Ion.with(getApplicationContext())
                        .load(Constants.API + "/api/Persons")
                        .setJsonObjectBody(personJson)
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                if(e != null) {
                                    Log.e(Constants.TAG, "Error POSTing the new person", e);
                                    return;
                                }

                                if(result != null) {
                                    Log.d(Constants.TAG, "Successfully posted person");
                                    Toast.makeText(getApplicationContext(), "Added " + name, Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }
                        });
            }
        });

        theHandler = AccountHandler.newInstance(this);
//        String userid = theHandler.getUserId();
//
//        hideAllShowLoading();
//
//        Ion.with(this)
//                .load(Constants.API + "/api/PersonRooms/" + userid)
//                .asJsonArray()
//                .setCallback(new FutureCallback<JsonArray>() {
//                    @Override
//                    public void onCompleted(Exception e, JsonArray result) {
//                        if(e != null) {
//                            Log.e(Constants.TAG, "error loading rooms: ", e);
//                            return;
//                        }
//
//                        if(result != null) {
//                            String[] rooms = new String[result.size()];
//                            for(int i=0; i < result.size(); i++) {
//                                JsonObject room = result.get(i).getAsJsonObject();
//                                rooms[i] = room.get("RoomType").getAsString();
//                            }
//
//                            ArrayAdapter<String> spinnerAdapter =
//                                    new ArrayAdapter<String>(getApplicationContext(),
//                                            android.R.layout.simple_spinner_dropdown_item, rooms);
//
//                            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                            personRoom.setAdapter(spinnerAdapter);
//
//                            hideLoadingShowAll();
//                        }
//                    }
//                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_person, menu);
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
