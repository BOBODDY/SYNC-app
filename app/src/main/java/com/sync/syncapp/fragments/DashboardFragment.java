package com.sync.syncapp.fragments;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

    TextView welcomeText;

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

        //TODO: load the user's dashboard from the back end
        welcomeText = (TextView) getActivity().findViewById(R.id.dashboard_fragment_welcome);
        welcomeText.setText("Welcome, " + userId);


    }

}
