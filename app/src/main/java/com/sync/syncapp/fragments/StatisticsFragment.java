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
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.sync.syncapp.Constants;
import com.sync.syncapp.R;
import com.sync.syncapp.model.Account;
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
        
        rooms = apiWrapper.getAccountRooms();
        if(rooms.size() == 0) {
            Room emptyRoom = new Room();
            emptyRoom.setRoomType("No Rooms Added");
            
            rooms = new ArrayList<>();
            rooms.add(new Room());
        }
        ArrayAdapter<Room> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, rooms);
        roomDropdown.setAdapter(adapter);
        
        //TODO: get statistics and set up the line chart
        updateGraph(currentRoomId);
    }
    
    private void updateGraph(final String roomId) {
        if(roomId.equals("")) { // No rooms have been added yet
            chart.setVisibility(View.INVISIBLE);
            status.setVisibility(View.VISIBLE);
        } else {
            //Set off an API call for the room esdata. if successful, launch an AsyncTask to update the graph
            Ion.with(context)
                    .load(Constants.API + "/api/RoomESData/" + roomId)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if(e != null) {
                                Log.e(Constants.TAG, "Error fetching room data for id " + roomId, e);
                                return;
                            }
                            
                            if(result != null) {
                                new UpdateGraphTask().execute(result);
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
    
    private class UpdateGraphTask extends AsyncTask<JsonObject, Void, Void> {
        protected Void doInBackground(JsonObject... params) {
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
            
            JsonArray result = params[0].getAsJsonArray("Result");
            int size = result.size();
            if(size > 0) {
                for(int i = 0; i < size; i++) {
                    JsonObject datapoint = result.get(i).getAsJsonObject();
                    
                    String timestamp = datapoint.get("Timestamp").getAsString();
                    boolean changed = false;
                    
                    //Get the CO2 data if the user wants it
                    if(co2Toggled) {
                        if(datapoint.has("CO2Level")) {
                            co2Data.add(new Entry(
                                    Float.valueOf(datapoint.get("CO2Level").getAsString()), 
                                    i
                            ));
                            changed = true;
                        }
                    }
                    
                    //Get the light data if the user wants it
                    if(lightToggled) {
                        if(datapoint.has("Luminance")) {
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
            
            ArrayList<LineDataSet> dataSets = new ArrayList<>();
            
            LineDataSet co2DataSet;
            LineDataSet tempDataSet;
            LineDataSet lightDataSet;
            
            if(co2Data.size() > 0) {
                co2DataSet = new LineDataSet(co2Data, "CO2");
                dataSets.add(co2DataSet);
            }
            
            if(tempData.size() > 0) {
                tempDataSet = new LineDataSet(tempData, "Temp");
                dataSets.add(tempDataSet);
            }
            
            if(lightData.size() > 0) {
                lightDataSet = new LineDataSet(lightData, "Light");
                dataSets.add(lightDataSet);
            }

            LineData data = new LineData(dates, dataSets);
            
            chart.setData(data);
            chart.invalidate();
            
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
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
        }
    }
}
