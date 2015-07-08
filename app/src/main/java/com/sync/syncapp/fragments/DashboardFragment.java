package com.sync.syncapp.fragments;

import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.sync.syncapp.Constants;
import com.sync.syncapp.R;
import com.sync.syncapp.util.AccountHandler;

import org.json.JSONArray;
import org.w3c.dom.Text;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DashboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DashboardFragment extends Fragment {

    private String userId;

    AccountHandler accountHandler;

    TextView sleepDuration, co2Level, light, temperature, humidity;

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

        sleepDuration = (TextView) getActivity().findViewById(R.id.dash_sleep_value);
        co2Level = (TextView) getActivity().findViewById(R.id.dash_co2_value);
        temperature = (TextView) getActivity().findViewById(R.id.dash_temp_value);
        humidity = (TextView) getActivity().findViewById(R.id.dash_humidity_value);
        light = (TextView) getActivity().findViewById(R.id.dash_light_value);

        Ion.with(getActivity().getApplicationContext())
                .load(Constants.API + "/api/Dashboard")
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        if (e != null) {
                            Log.e(Constants.TAG, "error loading dashboard:", e);
                            return;
                        }
                        if (result != null) {
                            Log.i(Constants.TAG, "Getting the dashboard works! result: " + result);
                            Log.i(Constants.TAG, "Number of results: " + result.size());
                            //TODO: set data based on object received
                            //example:
                            if(result.size() > 0) {
                                JsonObject object = result.get(0).getAsJsonObject();
                                JsonObject summary = object.get("summary").getAsJsonObject();
                                JsonObject esData = object.get("esdata").getAsJsonObject();

                                sleepDuration.setText(summary.get("totalTimeInBed").getAsString());

                                temperature.setText(esData.get("Temperature").getAsString() + " F");
                                humidity.setText(esData.get("Humidity").getAsString() + "%");
                                light.setText(esData.get("Luminance").getAsString() + " lux");
                                co2Level.setText(esData.get("CO2Level").getAsString() + " ppm");
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
    }

}
