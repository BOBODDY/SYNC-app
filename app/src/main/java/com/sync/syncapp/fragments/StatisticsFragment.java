package com.sync.syncapp.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.sync.syncapp.Constants;
import com.sync.syncapp.R;
import com.sync.syncapp.model.Account;
import com.sync.syncapp.model.Person;
import com.sync.syncapp.model.Room;
import com.sync.syncapp.util.AccountHandler;
import com.sync.syncapp.util.ApiWrapper;

import java.security.cert.LDAPCertStoreParameters;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StatisticsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatisticsFragment extends Fragment {
    
    Context context;
    
    AccountHandler handler;
    ApiWrapper apiWrapper;
    Account account;
    
    CheckBox toggleCo2;
    CheckBox toggleTemp;
    CheckBox toggleLight;
    LineChart chart;
    Spinner roomDropdown;
    TextView status;
    ProgressBar progress;
    
//    String[] roomNames, roomIds;
    List<Room> rooms;
    String currentRoomId = "";
    
    boolean co2Toggled;
    boolean tempToggled;
    boolean lightToggled;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment StatisticsFragment.
     */
    public static StatisticsFragment newInstance() { return new StatisticsFragment(); }
    
    public StatisticsFragment() {}
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    public void onStart() {
        super.onStart();
        
        final Activity a = getActivity();
        context = a.getApplicationContext();
        handler = AccountHandler.newInstance(context);
        apiWrapper = ApiWrapper.newInstance(context, new Account(handler.getUserId()));
        account = new Account(handler.getUserId());
        
        toggleCo2 = (CheckBox) a.findViewById(R.id.stats_toggle_co2);
        toggleCo2.setOnCheckedChangeListener(new ToggleCheckChangeListener());
        co2Toggled = toggleCo2.isChecked();
        
        toggleTemp = (CheckBox) a.findViewById(R.id.stats_toggle_temp);
        toggleTemp.setOnCheckedChangeListener(new ToggleCheckChangeListener());
        tempToggled = toggleTemp.isChecked();
        
        toggleLight = (CheckBox) a.findViewById(R.id.stats_toggle_light);
        toggleLight.setOnCheckedChangeListener(new ToggleCheckChangeListener());
        lightToggled = toggleLight.isChecked();
        
        chart = (LineChart) a.findViewById(R.id.stats_chart);
        chart.setVisibility(View.INVISIBLE);
        
        status = (TextView) a.findViewById(R.id.stats_status);
        status.setVisibility(View.VISIBLE);
        
        progress = (ProgressBar) a.findViewById(R.id.stats_progress);
        progress.setVisibility(View.INVISIBLE);
        
        roomDropdown = (Spinner) a.findViewById(R.id.stats_room_dropdown);
        roomDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(Constants.TAG, "Setting the room to " + rooms.get(position).getRoomType());
                currentRoomId = rooms.get(position).getId();
                updateGraph(currentRoomId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        
//        rooms = apiWrapper.getAccountRooms(); // I wish I could synchronize this
        rooms = new ArrayList<>();
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

                                Log.d(Constants.TAG, "populating the dropdown");
                                ArrayAdapter<Room> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, rooms);
                                roomDropdown.setAdapter(adapter);
                            } else {
                                rooms = new ArrayList<>();
                                Room noRoom = new Room();
                                noRoom.setRoomType("No rooms added");
                                
                                ArrayAdapter<Room> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, rooms);
                                roomDropdown.setAdapter(adapter);
                            }

                        }
                    }
                });
        
        //TODO: get statistics and set up the line chart
        updateGraph("");
    }
    
    private void updateGraph(final String roomId) {
        if(roomId.equals("")) { // No rooms have been added yet
            chart.setVisibility(View.INVISIBLE);
            status.setVisibility(View.VISIBLE);
        } else {
            // Set off an API call for the user's personal data
            // If successful, launch another call for the room's esdata
            // If that is successful, start an UpdateGraphTask
            Ion.with(context)
                    .load(Constants.API + "/api/PSDataByFitBitUser/3GTVKM") //TODO: figure out how to store and retrieve this
                    .setHeader(Constants.AUTH_KEY, Constants.AUTH_VALUE)
                    .asJsonArray()
                    .setCallback(new FutureCallback<JsonArray>() {
                        @Override
                        public void onCompleted(Exception e, JsonArray result) {
                            if(e != null) {
                                Log.e(Constants.TAG, "Error getting fitbit data for user", e);
                                return;
                            }
                            if(result != null) {
                                final JsonArray sleepData = result;

                                Ion.with(context)
                                        .load(Constants.API + "/api/RoomESData/" + roomId)
                                        .setHeader(Constants.AUTH_KEY, Constants.AUTH_VALUE)
                                        .asJsonObject()
                                        .setCallback(new FutureCallback<JsonObject>() {
                                            @Override
                                            public void onCompleted(Exception e, JsonObject result) {
                                                if(e != null) {
                                                    Log.e(Constants.TAG, "Error fetching room data for id " + roomId, e);
                                                    return;
                                                }

                                                if(result != null) {
                                                    new UpdateGraphTask().execute(result, sleepData);
                                                }
                                            }
                                        });
                            }
                        }
                    });
        }
    }

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    /**
     * Pass the ESData as the first parameter and the sleep data as the second
     */
    private class UpdateGraphTask extends AsyncTask<JsonElement, Void, Void> {
        protected Void doInBackground(JsonElement... params) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progress.setVisibility(View.VISIBLE);
                }
            });

            ArrayList<Entry> co2Data = new ArrayList<>();
            ArrayList<Entry> tempData = new ArrayList<>();
            ArrayList<Entry> lightData = new ArrayList<>();
            
            ArrayList<String> dates = new ArrayList<>();
            
            JsonObject esData = params[0].getAsJsonObject();
            
            JsonArray result = esData.getAsJsonArray("Result");
            int size = result.size();
            if(size > 0) {
                for(int i = 0; i < size; i++) {
                    JsonObject datapoint = result.get(i).getAsJsonObject();
                    
                    String timestamp = datapoint.get("Timestamp").getAsString();
                    boolean changed = false;
                    
                    //Get the CO2 data if the user wants it
                    if(co2Toggled) {
                        if(datapoint.has("CO2Level") && !datapoint.get("CO2Level").isJsonNull()) {
                            co2Data.add(new Entry(
                                    Float.valueOf(datapoint.get("CO2Level").getAsString()), 
                                    i
                            ));
                            changed = true;
                        }
                    }
                    
                    //Get the light data if the user wants it
                    if(lightToggled) {
                        if(datapoint.has("Luminance") && !datapoint.get("Luminance").isJsonNull()) {
                            lightData.add(new Entry(
                                    Float.valueOf(datapoint.get("Luminance").getAsString()),
                                    i
                            ));
                            changed = true;
                        }
                    }
                    
                    //Get the temperature data if the user wants it
                    if(tempToggled) {
                        if(datapoint.has("Temperature")) {
                            tempData.add(new Entry(
                                    Float.valueOf(datapoint.get("Temperature").getAsString()),
                                    i
                            ));
                            changed = true;
                        }
                    }
                    
                    if(changed) {
                        dates.add(timestamp);
                    }
                }
            }
            
            JsonArray sleepData = params[1].getAsJsonArray();
            
            //TODO: create a data set for sleep data and plot on graph
            int sleepSize = sleepData.size();
            if(sleepSize > 0) {
                for(int i=0; i < sleepSize; i++) {
                    JsonObject dp = sleepData.get(i).getAsJsonObject();
                    JsonArray sleep = dp.get("sleep").getAsJsonArray();
                }
            }
            
            ArrayList<LineDataSet> dataSets = new ArrayList<>();
            
            LineDataSet co2DataSet;
            LineDataSet tempDataSet;
            LineDataSet lightDataSet;
            
            if(co2Data.size() > 0) {
                co2DataSet = new LineDataSet(co2Data, "CO2");
                co2DataSet.setColor(getResources().getColor(R.color.color_co2));
                co2DataSet.setCircleColor(getResources().getColor(R.color.color_co2));
                co2DataSet.setCircleColorHole(getResources().getColor(R.color.color_co2));
                dataSets.add(co2DataSet);
            }
            
            if(tempData.size() > 0) {
                tempDataSet = new LineDataSet(tempData, "Temp");
                tempDataSet.setColor(getResources().getColor(R.color.color_temp));
                tempDataSet.setCircleColor(getResources().getColor(R.color.color_temp));
                tempDataSet.setCircleColorHole(getResources().getColor(R.color.color_temp));
                dataSets.add(tempDataSet);
            }
            
            if(lightData.size() > 0) {
                lightDataSet = new LineDataSet(lightData, "Light");
                lightDataSet.setColor(getResources().getColor(R.color.color_light));
                lightDataSet.setCircleColor(getResources().getColor(R.color.color_light));
                lightDataSet.setCircleColorHole(getResources().getColor(R.color.color_light));
                dataSets.add(lightDataSet);
            }

            LineData data = new LineData(dates, dataSets);
            
            chart.setData(data);
            
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    chart.invalidate();
                    
                    status.setVisibility(View.INVISIBLE);
                    chart.setVisibility(View.VISIBLE);
                    
                    progress.setVisibility(View.INVISIBLE);
                }
            });
            return null;
        }
    }
    
    private class ToggleCheckChangeListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            int toggleId = compoundButton.getId();
            
            Log.d(Constants.TAG, "Toggling " + compoundButton.getText() + " to " + isChecked);
            
            switch (toggleId) {
                case R.id.stats_toggle_co2:
                    co2Toggled = isChecked;
                    break;
                case R.id.stats_toggle_temp:
                    tempToggled = isChecked;
                    break;
                case R.id.stats_toggle_light:
                    lightToggled = isChecked;
                    break;
                default:
                    Log.e(Constants.TAG, "Unknown button id: " + toggleId);
                    
                    break;
            }
            
            updateGraph(currentRoomId);
        }
    }
}
