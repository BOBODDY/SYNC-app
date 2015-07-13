package com.sync.syncapp.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.github.mikephil.charting.charts.LineChart;
import com.sync.syncapp.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StatisticsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatisticsFragment extends Fragment {
    
    CheckBox toggleCo2;
    CheckBox toggleTemp;
    CheckBox toggleLight;
    
    LineChart chart;

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
        
        Activity a = getActivity();
        toggleCo2 = (CheckBox) a.findViewById(R.id.stats_toggle_co2);
        toggleTemp = (CheckBox) a.findViewById(R.id.stats_toggle_temp);
        toggleLight = (CheckBox) a.findViewById(R.id.stats_toggle_light);
        
        chart = (LineChart) a.findViewById(R.id.stats_chart);
        
        //TODO: get statistics and set up the line chart
    }

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }
}
