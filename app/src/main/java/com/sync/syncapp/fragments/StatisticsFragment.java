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
import com.github.mikephil.charting.components.YAxis;
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
import com.sync.syncapp.model.Data;
import com.sync.syncapp.model.Person;
import com.sync.syncapp.model.Room;
import com.sync.syncapp.util.AccountHandler;
import com.sync.syncapp.util.ApiWrapper;

import java.lang.reflect.Array;
import java.security.cert.LDAPCertStoreParameters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

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
     * 
     * @param params JsonElements for esData and sleep data, in that order
     */
    protected void parse(JsonElement... params) {
        HashMap<String, List<Data>> data = new HashMap<>();
        
        //Parse the environmental data
        Log.d(Constants.TAG, "starting to parse the environmental data");
        JsonObject esData = params[0].getAsJsonObject();
        
        JsonArray result = esData.get("Result").getAsJsonArray();
        int esSize = result.size();
        if(esSize > 0) {
            for(int i = 0; i < esSize; i++) {
                JsonObject datapoint = result.get(i).getAsJsonObject();
                
                String timestamp = datapoint.get("Timestamp").getAsString();
                String date = timestamp.split(" ")[0]; // assuming format YYYY-MM-DD hh:mm:ss

                List<Data> values = data.get(date);
                if(values == null) {
                    values = new ArrayList<>();
                    data.put(date, values);
                }
                
                boolean changed = false;

                if(co2Toggled) {
                    if(!datapoint.get("CO2Level").isJsonNull()) {
                        String co2Level = datapoint.get("CO2Level").getAsString();
                        values.add(new Data(timestamp, co2Level, Data.DATA_TYPE_CO2));
                        changed = true;
                    }
                }
                
                if(tempToggled) {
                    if(!datapoint.get("Temperature").isJsonNull()) {
                        String temperature = datapoint.get("Temperature").getAsString();
                        values.add(new Data(timestamp, temperature, Data.DATA_TYPE_TEMP));
                        changed = true;
                    }
                }
                
                if(lightToggled) {
                    if(!datapoint.get("Luminance").isJsonNull()) {
                        String light = datapoint.get("Luminance").getAsString();
                        values.add(new Data(timestamp, light, Data.DATA_TYPE_LIGHT));
                        changed = true;
                    }
                }
                
                if(changed)
                    data.put(date, values);
            }
        }
        
        // Parse the sleep data
        Log.d(Constants.TAG, "Starting to parse the sleep data");
        JsonArray sleepData = params[1].getAsJsonArray();
        int sleepDataSize = sleepData.size();
        if(sleepDataSize > 0) {
            for(int i = 0; i < sleepDataSize; i++) {
                JsonObject sleepObject = sleepData.get(i).getAsJsonObject();
                
                JsonArray sleepValues = sleepObject.get("sleep").getAsJsonArray();
                int sleepValuesSize = sleepValues.size();
                for(int j=0; j < sleepValuesSize; j++) {
                    JsonObject tmp = sleepValues.get(j).getAsJsonObject();
                    
                    String dateOfSleep = tmp.get("dateOfSleep").getAsString();
                    
                    List<Data> values = data.get(dateOfSleep);
                    if(values == null) {
                        values = new ArrayList<>();
                        data.put(dateOfSleep, values);
                    }
                    
                    JsonElement e = tmp.get("minuteData");
                    if(!e.isJsonNull()) {
                        JsonArray minuteData = e.getAsJsonArray();
                        int minuteDataSize = minuteData.size();
                        for (int k = 0; k < minuteDataSize; k++) {
                            JsonObject minuteValue = minuteData.get(k).getAsJsonObject();

                            String value = minuteValue.get("value").getAsString();
                            String dateTime = minuteValue.get("dateTime").getAsString();

                            String timestamp = dateOfSleep + " " + dateTime; // Should be in the form YYYY-MM-DD hh:mm:ss
                            
//                            Log.d(Constants.TAG, "sleep timestamp: " + timestamp);

                            values.add(new Data(timestamp, value, Data.DATA_TYPE_SLEEP));
                        }

                        data.put(dateOfSleep, values);
                    }
                }
            }
        }
        
        // Put all this together and graph it
        Log.d(Constants.TAG, "starting to graph the values");
        ArrayList<Entry> co2Entries = new ArrayList<>();
        ArrayList<Entry> tempEntries = new ArrayList<>();
        ArrayList<Entry> lightEntries = new ArrayList<>();
        ArrayList<Entry> sleepEntries = new ArrayList<>();
        
        ArrayList<String> xVals = new ArrayList<>();
        
        Set<String> keySet = data.keySet();
        String[] keys = new String[keySet.size()];
        keySet.toArray(keys);
        Arrays.sort(keys);
        
        int indexx = 0; // typo = sexy index
        int size = keys.length;
        for(int i = 0; i < size; i++) {
            List<Data> listOfData = data.get(keys[i]);
            Collections.sort(listOfData);
            
            int listSize = listOfData.size();
            for(int j = 0; j < listSize; j++, indexx++) {
                Data data1 = listOfData.get(j);
                
                Entry entry = new Entry(data1.getValueFloat(), indexx);
                Entry blank = new Entry(0, indexx);
                
                if(data1.getDataType() == Data.DATA_TYPE_CO2) {
                    co2Entries.add(entry);
                    tempEntries.add(blank);
                    lightEntries.add(blank);
                    sleepEntries.add(blank);
                } else if(data1.getDataType() == Data.DATA_TYPE_LIGHT) {
                    co2Entries.add(blank);
                    tempEntries.add(blank);
                    lightEntries.add(entry);
                    sleepEntries.add(blank);
                } else if(data1.getDataType() == Data.DATA_TYPE_TEMP) {
                    co2Entries.add(blank);
                    tempEntries.add(entry);
                    lightEntries.add(blank);
                    sleepEntries.add(blank);
                } else if(data1.getDataType() == Data.DATA_TYPE_SLEEP) {
                    co2Entries.add(blank);
                    tempEntries.add(blank);
                    lightEntries.add(blank);
                    sleepEntries.add(entry);
                }
                
                xVals.add(data1.getTimestamp().toString());
            }
        }

        ArrayList<LineDataSet> dataSets = new ArrayList<>();
        
        if(co2Entries.size() > 0) {
            LineDataSet co2DataSet = new LineDataSet(co2Entries, "CO2");
            co2DataSet.setColor(getResources().getColor(R.color.color_co2));
            co2DataSet.setCircleColor(getResources().getColor(R.color.color_co2));
            co2DataSet.setCircleColorHole(getResources().getColor(R.color.color_co2));
            dataSets.add(co2DataSet);
        }
        
        if(lightEntries.size() > 0) {
            LineDataSet lightDataSet = new LineDataSet(lightEntries, "Light");
            lightDataSet.setColor(getResources().getColor(R.color.color_light));
            lightDataSet.setCircleColor(getResources().getColor(R.color.color_light));
            lightDataSet.setCircleColorHole(getResources().getColor(R.color.color_light));
            dataSets.add(lightDataSet);
        }
        
        if(tempEntries.size() > 0) {
            LineDataSet tempDataSet = new LineDataSet(tempEntries, "Temperature");
            tempDataSet.setColor(getResources().getColor(R.color.color_temp));
            tempDataSet.setCircleColor(getResources().getColor(R.color.color_temp));
            tempDataSet.setCircleColorHole(getResources().getColor(R.color.color_temp));
            dataSets.add(tempDataSet);
        }
        
        if(sleepEntries.size() > 0) {
            LineDataSet sleepDataSet = new LineDataSet(sleepEntries, "Sleep");
            sleepDataSet.setColor(getResources().getColor(R.color.color_sleep));
            sleepDataSet.setCircleColor(getResources().getColor(R.color.color_sleep));
            sleepDataSet.setCircleColorHole(getResources().getColor(R.color.color_sleep));
            sleepDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
            dataSets.add(sleepDataSet);
        }

        LineData lineData = new LineData(xVals, dataSets);
        chart.setData(lineData);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(Constants.TAG, "invalidating chart and making visible");
                chart.invalidate();
                
                chart.setVisibility(View.VISIBLE);
                progress.setVisibility(View.INVISIBLE);
                status.setVisibility(View.INVISIBLE);
            }
        });
    }

    /**
     * Pass the ESData as the first parameter and the sleep data as the second
     */
    private class UpdateGraphTask extends AsyncTask<JsonElement, Void, Void> {
        protected Void doInBackground(JsonElement... params) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(Constants.TAG, "making the progress bar visible");
                    progress.setVisibility(View.VISIBLE);
                }
            });
            
            parse(params);
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
