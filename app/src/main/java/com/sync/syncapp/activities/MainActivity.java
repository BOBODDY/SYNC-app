package com.sync.syncapp.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.auth0.core.UserProfile;
import com.auth0.lock.Lock;
import com.auth0.lock.LockActivity;
import com.sync.syncapp.Constants;
import com.sync.syncapp.fragments.DashboardFragment;
import com.sync.syncapp.R;
import com.sync.syncapp.fragments.SettingsFragment;
import com.sync.syncapp.util.AccountHandler;

import java.lang.reflect.Field;

import static com.auth0.lock.Lock.AUTHENTICATION_ACTION;
import static com.auth0.lock.Lock.CANCEL_ACTION;

public class MainActivity extends AppCompatActivity {

    private String drawer[] = {"Dashboard", "Statistics", "Settings"};

    /*
        This is for remembering the last screen.
        If the user backs out from a subscreen of the settings (Add user, add sensor, etc)
        They should be dropped back into the settings fragment, not the dashboard
     */
    private final int SCREEN_DASHBOARD = 0;
    private final int SCREEN_STATISTICS = 1;
    private final int SCREEN_SETTINGS = 2;
    private static int lastScreen = -1;

    private ActionBarDrawerToggle drawerToggle;

    private DrawerLayout drawerLayout;
    private ListView drawerList;

    private CharSequence title;
    private CharSequence drawerTitle;

    AccountHandler accountHandler;

    LocalBroadcastManager broadcastManager;
    private BroadcastReceiver authenticationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(Constants.TAG, "MainActivity intent's component: " + intent.getComponent());

            UserProfile profile = intent.getParcelableExtra(Lock.AUTHENTICATION_ACTION_PROFILE_PARAMETER);
            String userId = profile.getId();

            Log.d(Constants.TAG, "current userID: '" + userId + "'");

            if(accountHandler.getUserId().equals("")) {
                accountHandler.addAccount(profile);

                Log.i(Constants.TAG, "User is successfully logged in, ~id: " + userId);

                selectItem(SCREEN_DASHBOARD); // Assuming Dashboard is the first item

                Log.d(Constants.TAG, "Unregistering MainActivity BroadcastReceivers due to login");
                broadcastManager.unregisterReceiver(authenticationReceiver);
                broadcastManager.unregisterReceiver(cancelReceiver);
            } else {
                Log.d(Constants.TAG, "User is already logged in, must be a fitbit login");
            }


        }
    };

    private BroadcastReceiver cancelReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(Constants.TAG, "user canceled logging in");
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accountHandler = AccountHandler.newInstance(getApplicationContext());

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);

        // Set up the adapter for the listview
        drawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, drawer));
        drawerList.setOnItemClickListener(new DrawerItemClickListener());

        title = drawerTitle = getTitle();

        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.drawer_open,
                R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                if(getActionBar() != null) {
                    getActionBar().setTitle(title);
                }
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if(getActionBar() != null) {
                    getActionBar().setTitle(drawerTitle);
                }
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        drawerLayout.setDrawerListener(drawerToggle);

        try {
            Field mDragger = drawerLayout.getClass().getDeclaredField("mLeftDragger");//mRightDragger for right obviously
            mDragger.setAccessible(true);
            ViewDragHelper draggerObj = (ViewDragHelper) mDragger.get(drawerLayout);

            Field mEdgeSize = draggerObj.getClass().getDeclaredField("mEdgeSize");

            mEdgeSize.setAccessible(true);
            int edge = mEdgeSize.getInt(draggerObj);

            mEdgeSize.setInt(draggerObj, edge * 5); //optimal value as for me, you may set any constant in dp
        } catch(NoSuchFieldException nsfe) {
            Log.e(Constants.TAG, "NoSuchFieldException: ", nsfe);
        } catch(IllegalAccessException iae) {
            Log.e(Constants.TAG, "IllegalAccessException: ", iae);
        }

        Log.d(Constants.TAG, "Registering MainActivity BroadcastReceivers");
        broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.registerReceiver(authenticationReceiver, new IntentFilter(AUTHENTICATION_ACTION));
        broadcastManager.registerReceiver(cancelReceiver, new IntentFilter(CANCEL_ACTION));
    }

    private boolean isFirstRun() {
        return accountHandler.getUserId().equals("");
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
//        boolean drawerOpen = drawerLayout.isDrawerOpen(drawerList);
//        menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    protected void onPostResume() {
        super.onPostResume();

        if(isFirstRun()) {
            Context applicationContext = getApplicationContext();
            Toast.makeText(applicationContext, "Not logged in yet", Toast.LENGTH_SHORT).show();
            Intent lockIntent = new Intent(applicationContext, LockActivity.class);
            startActivity(lockIntent);
        } else {
            String userId = getSharedPreferences(Constants.PREFS, 0).getString(Constants.USER_ID, "");

            if(lastScreen != -1) {
                selectItem(lastScreen);
            } else {
                selectItem(SCREEN_DASHBOARD);
            }
        }
    }

    protected void onDestroy() {
        Log.d(Constants.TAG, "Unregistering MainActivity BroadcastReceivers");
        broadcastManager.unregisterReceiver(authenticationReceiver);
        broadcastManager.unregisterReceiver(cancelReceiver);

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            accountHandler.logout();
                            Toast.makeText(getApplicationContext(), "Logout Successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    /** Swaps fragments in the main content view */
    //TODO: This is causing issues when logging out and logging back in again. Fix it.
    private void selectItem(int position) {
        Fragment fragment = null;

        //This is probably very bad
        if(position == SCREEN_DASHBOARD) {
            lastScreen = SCREEN_DASHBOARD;
            fragment = new DashboardFragment();
        } else if(position == SCREEN_STATISTICS) {
            //TODO: make a statistics fragment and set it here
            lastScreen = SCREEN_STATISTICS;
            //fragment = new StatisticsFragment();
        } else if(position == SCREEN_SETTINGS) {
            lastScreen = SCREEN_SETTINGS;
            fragment = new SettingsFragment();
        }

        if(fragment == null) {
            Log.e(Constants.TAG, "No fragment to set to");
            return;
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commitAllowingStateLoss();

        // Highlight the selected item, update the title, and close the drawer
        drawerList.setItemChecked(position, true);
        setTitle(drawer[position]);
        drawerLayout.closeDrawer(drawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        title = title.toString();
        if(getActionBar() != null) {
            getActionBar().setTitle(title);
        }
    }
}
