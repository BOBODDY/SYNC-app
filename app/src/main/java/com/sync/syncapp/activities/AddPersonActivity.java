package com.sync.syncapp.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

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
    TextView roomTitle;
    EditText personName;
    Spinner personRoom;

    ProgressBar loading;

    AccountHandler theHandler; //https://www.youtube.com/watch?v=BF1DQr5dKW8

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_person);

        loading = (ProgressBar) findViewById(R.id.add_person_loading);

        nameTitle = (TextView) findViewById(R.id.add_person_name_title);

        roomTitle = (TextView) findViewById(R.id.add_person_room_title);

        personName = (EditText) findViewById(R.id.add_person_name);

        personRoom = (Spinner) findViewById(R.id.add_person_room);

        theHandler = AccountHandler.newInstance(this);
        String userid = theHandler.getUserId();

        hideAllShowLoading();

        Ion.with(this)
                .load(Constants.API + "/api/PersonRooms/" + userid)
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        if(e != null) {
                            Log.e(Constants.TAG, "error loading rooms: ", e);
                            return;
                        }

                        if(result != null) {
                            String[] rooms = new String[result.size()];
                            for(int i=0; i < result.size(); i++) {
                                JsonObject room = result.get(i).getAsJsonObject();
                                rooms[i] = room.get("RoomType").getAsString();
                            }

                            ArrayAdapter<String> spinnerAdapter =
                                    new ArrayAdapter<String>(getApplicationContext(),
                                            android.R.layout.simple_spinner_dropdown_item, rooms);

                            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            personRoom.setAdapter(spinnerAdapter);

                            hideLoadingShowAll();
                        }
                    }
                });
    }

    private void hideAllShowLoading() {
        nameTitle.setVisibility(View.INVISIBLE);
        roomTitle.setVisibility(View.INVISIBLE);
        personName.setVisibility(View.INVISIBLE);
        personRoom.setVisibility(View.INVISIBLE);

        loading.setVisibility(View.VISIBLE);
    }

    private void hideLoadingShowAll() {
        nameTitle.setVisibility(View.VISIBLE);
        roomTitle.setVisibility(View.VISIBLE);
        personName.setVisibility(View.VISIBLE);
        personRoom.setVisibility(View.VISIBLE);

        loading.setVisibility(View.INVISIBLE);
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
