package com.sync.syncapp.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.sync.syncapp.Constants;
import com.sync.syncapp.R;
import com.sync.syncapp.model.Account;
import com.sync.syncapp.model.Person;
import com.sync.syncapp.model.Room;
import com.sync.syncapp.util.AccountHandler;

import org.json.JSONArray;
import org.w3c.dom.Text;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DashboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DashboardFragment extends Fragment {

    private String userId;

    AccountHandler accountHandler;

    TextView sleepDuration, co2Level, light, temperature, humidity;

    Spinner roomDropdown;

    ArrayList<Room> rooms;
    String currentRoomId = "";

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DashboardFragment.
     */
    public static DashboardFragment newInstance() {
        return new DashboardFragment();
    }

    public DashboardFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        accountHandler = AccountHandler.newInstance(getActivity().getApplicationContext());
        userId = accountHandler.getUserId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    public void onStart() {
        super.onStart();

        Activity activity = getActivity();
        final Context context = activity.getApplicationContext();

        final Account account = new Account(accountHandler.getUserId());

        sleepDuration = (TextView) activity.findViewById(R.id.dash_sleep_value);
        co2Level = (TextView) activity.findViewById(R.id.dash_co2_value);
        temperature = (TextView) activity.findViewById(R.id.dash_temp_value);
        humidity = (TextView) activity.findViewById(R.id.dash_humidity_value);
        light = (TextView) activity.findViewById(R.id.dash_light_value);

        roomDropdown = (Spinner) activity.findViewById(R.id.dash_room_dropdown);
        roomDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(Constants.TAG, "Setting the room to " + rooms.get(position).getRoomType());
                currentRoomId = rooms.get(position).getId();
                updateDashboard();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
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
                                rooms = new ArrayList<>();
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
    }

    public void updateDashboard() {
        if(currentRoomId.equals("")) {
            String noData = "No data received";
            sleepDuration.setText(noData);
            temperature.setText(noData);
            humidity.setText(noData);
            light.setText(noData);
            co2Level.setText(noData);
        } else {
            Log.d(Constants.TAG, "the room id: " + currentRoomId);
            Ion.with(getActivity().getApplicationContext())
                    .load(Constants.API + "/api/RoomESData/" + currentRoomId)
                    .setHeader(Constants.AUTH_KEY, Constants.AUTH_VALUE)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject array) {
                            if (e != null) {
                                Log.e(Constants.TAG, "error fetching es data in Dashboard", e);
                                return;
                            }
                            if (array != null) {
                                JsonArray result = array.get("Result").getAsJsonArray();
                                int size = result.size();
                                if (size > 0) {
                                    JsonObject object = result.get(size - 1).getAsJsonObject();

                                    if (!object.get("Temperature").isJsonNull()) {
                                        temperature.setText(object.get("Temperature").getAsString() + " F");
                                    }

                                    if (!object.get("Humidity").isJsonNull()) {
                                        humidity.setText(object.get("Humidity").getAsString() + "%");
                                    }

                                    if (!object.get("Luminance").isJsonNull()) {
                                        light.setText(object.get("Luminance").getAsString() + " lux");
                                    }

                                    int j = size - 1;
                                    while (j >= 0 && !result.get(j).isJsonNull()) {
                                        JsonObject co2 = result.get(j).getAsJsonObject();
                                        
                                        if(!co2.get("CO2Level").isJsonNull()) {
                                            co2Level.setText(co2.get("CO2Level").getAsString());
                                            break;
                                        } else {
                                            j -= 1;
                                        }
                                    }

                                } else {
                                    String noData = "No data received";
                                    sleepDuration.setText(noData);
                                    temperature.setText(noData);
                                    humidity.setText(noData);
                                    light.setText(noData);
                                    co2Level.setText(noData);
                                }
                            }
                        }
                    });

            Ion.with(getActivity().getApplicationContext())
                    .load(Constants.API + "/api/PSDataByFitBitUser/3GTVKM")
                    .setHeader(Constants.AUTH_KEY, Constants.AUTH_VALUE)
                    .asJsonArray()
                    .setCallback(new FutureCallback<JsonArray>() {
                        @Override
                        public void onCompleted(Exception e, JsonArray result) {
                            if (e != null) {
                                Log.e(Constants.TAG, "error getting fitbit data for dashboard", e);
                                return;
                            }
                            if (result != null) {
                                Log.d(Constants.TAG, "got result " + result);
                                int size = result.size();
                                if (size > 0) {
                                    JsonObject lastSleep = result.get(size - 1).getAsJsonObject();
                                    JsonObject summary = lastSleep.get("summary").getAsJsonObject();

                                    String minutesAsleep = summary.get("totalMinutesAsleep").getAsString();

                                    int hours = 0;
                                    int minutes = 0;

                                    int remaining = Integer.valueOf(minutesAsleep);
                                    while (remaining - 60 >= 0) {
                                        remaining -= 60;
                                        hours++;
                                    }
                                    minutes += remaining;

                                    String duration = hours + " hrs " + minutes + " min";
                                    sleepDuration.setText(duration);
                                }
                            }
                        }
                    });
        }

    }

}