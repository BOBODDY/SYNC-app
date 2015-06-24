package com.sync.syncapp.fragments;

import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.sync.syncapp.Constants;
import com.sync.syncapp.R;
import com.sync.syncapp.util.AccountHandler;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DashboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DashboardFragment extends Fragment {

    private String userId;

    AccountHandler accountHandler;

    TextView sleepDuration, co2Level, temperature, humidity;

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

        sleepDuration = (TextView) getActivity().findViewById(R.id.dash_sleep_value);
        co2Level = (TextView) getActivity().findViewById(R.id.dash_co2_value);
        temperature = (TextView) getActivity().findViewById(R.id.dash_temp_value);
        humidity = (TextView) getActivity().findViewById(R.id.dash_humidity_value);
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

        Ion.with(getActivity().getApplicationContext())
                .load(Constants.API + "/api/Dashboard")
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if(e != null) {
                            Log.e(Constants.TAG, "error loading dashboard:", e);
                            return;
                        }
                        if(result != null) {
                            Log.i(Constants.TAG, "Getting the dashboard works!");
                            //TODO: set data based on object received
                            //example:
                            //sleepDuration.setText(result.get("sleep_duration"));
                        }
                    }
                });
    }

}
