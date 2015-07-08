package com.sync.syncapp.activities;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.sync.syncapp.Constants;
import com.sync.syncapp.R;
import com.sync.syncapp.util.AccountHandler;

public class AddESensorActivity extends ActionBarActivity {

    Spinner esensorType;
    EditText esensorName;
    EditText esensorDeviceId;
    Button doneButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_esensor);

        //TODO: populate the rooms available for a sensor by querying db

        esensorType = (Spinner) findViewById(R.id.add_esensor_type);

        esensorName = (EditText) findViewById(R.id.add_esensor_name);

        esensorDeviceId = (EditText) findViewById(R.id.add_esensor_device_id);

        doneButton = (Button) findViewById(R.id.add_esensor_done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String type = esensorType.getSelectedItem().toString();
                String name  = esensorName.getText().toString();
                String deviceId = esensorDeviceId.getText().toString();

                AccountHandler handler = AccountHandler.newInstance(getApplicationContext());
                String userId = handler.getUserId();

                JsonObject json = new JsonObject();
                json.addProperty("estype", type);
                json.addProperty("account_id", userId);
                json.addProperty("name", name);
                json.addProperty("description", type + " sensor added to " + userId);
                json.addProperty("Device_ID", deviceId);

                //TODO: send data to backend
                Ion.with(getApplicationContext())
                        .load(Constants.API + "/api/ESensors")
                        .setJsonObjectBody(json)
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                //TODO: handle the result
                            }
                        });
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
