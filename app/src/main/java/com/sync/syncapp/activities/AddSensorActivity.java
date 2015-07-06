package com.sync.syncapp.activities;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.sync.syncapp.Constants;
import com.sync.syncapp.R;

public class AddSensorActivity extends ActionBarActivity {

    final static String fitbitApi = "https://api.fitbit.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sensor);
    }

    protected void onStart() {
        super.onStart();

//        SharedPreferencesCredentialStore credentialStore =
//                new SharedPreferencesCredentialStore(getApplicationContext(),
//                        "creditial_store", new JacksonFactory());
//
//        AuthorizationFlow.Builder builder = new AuthorizationFlow.Builder(
//                BearerToken.authorizationHeaderAccessMethod(),
//                AndroidHttp.newCompatibleTransport(),
//                new JacksonFactory(),
//                new GenericUrl("https://www.fitbit.com/oauth2/authorize"),
//                new ClientParametersAuthentication("229MD4", "ab74ad7dee201f8b44a7f2e4a168652d"),
//                "229MD4",
//                "https://www.fitbit.com/oauth2/authorize");
//        builder.setCredentialStore(credentialStore);
//        AuthorizationFlow flow = builder.build();
//
//        AuthorizationUIController controller =
//                new DialogFragmentController(getFragmentManager()) {
//
//                    @Override
//                    public String getRedirectUri() throws IOException {
//                        return "http://localhost/Callback";
//                    }
//
//                    @Override
//                    public boolean isJavascriptEnabledForWebView() {
//                        return true;
//                    }
//
//                };
//
//        OAuthManager oauth = new OAuthManager(flow, controller);
//
//        Credential credential;
//
//        try {
//            credential = oauth.authorizeImplicitly("userId", null, null).getResult();
//            String accessToken = credential.getAccessToken();
//            Log.d(Constants.TAG, "Access token: " + accessToken);
//        } catch(IOException ioe) {
//            Log.e(Constants.TAG, "error authorizing implicitly", ioe);
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_sensor, menu);
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
