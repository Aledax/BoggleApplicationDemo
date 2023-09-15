package com.example.buttondemo;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ServerActivity extends AppCompatActivity {

    final static String TAG = "ServerActivity";

    final static int SIGN_IN_REQUEST_CODE = 1;

    private GoogleSignInClient mGoogleSignInClient;

    private LinearLayout listLayout;
    private TextView ipServerText;
    private TextView ipClientText;
    private TextView timeServerText;
    private TextView timeClientText;
    private TextView yourNameText;
    private TextView userNameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        listLayout = findViewById(R.id.server_list_layout);
        listLayout.setVisibility(INVISIBLE);

        ipServerText = findViewById(R.id.server_ip_server);
        ipClientText = findViewById(R.id.server_ip_client);
        timeServerText = findViewById(R.id.server_time_server);
        timeClientText = findViewById(R.id.server_time_client);
        yourNameText = findViewById(R.id.server_your_name);
        userNameText = findViewById(R.id.server_user_name);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null) {
            signIn();
        } else {
            onSignIn(account);
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, SIGN_IN_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SIGN_IN_REQUEST_CODE:
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            onSignIn(account);
        } catch (ApiException e) {
            Log.w(TAG, "singInResult:failed code=" + e.getStatusCode());
            onSignIn(null);
        }
    }

    private void onSignIn(GoogleSignInAccount account) {
        if (account == null) {
            finish();
            return;
        }

        // Get client values
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        ipClientText.setText("You: " + ip);

        Date currentTime = Calendar.getInstance().getTime();
        timeClientText.setText("You: " + new SimpleDateFormat("HH:mm:ss").format(currentTime));

        userNameText.setText("You: " + account.getGivenName() + " " + account.getFamilyName());

        // Get server values
        RequestQueue volleyQueue = Volley.newRequestQueue(ServerActivity.this);
        String url = getResources().getString(R.string.server_address);

        JsonObjectRequest ipServerRequest = new JsonObjectRequest( Request.Method.GET, url + "/info/ip", null,
                response -> {
                    try {
                        String message = response.getString("message");
                        ipServerText.setText("Server: " + message);
                    } catch (JSONException e) { Log.w(TAG, "JSON Error"); }
                }, error -> {
                    Log.e(TAG, "Error: " + error.getLocalizedMessage());
                }
        );

        JsonObjectRequest timeServerRequest = new JsonObjectRequest(Request.Method.GET, url + "/info/time", null,
                response -> {
                    try {
                        String message = response.getString("message");
                        timeServerText.setText("Server: " + message);
                    } catch (JSONException e) { Log.w(TAG, "JSON Error"); }
                }, error -> { Log.e(TAG, "Error: " + error.getLocalizedMessage()); }
        );

        JsonObjectRequest nameServerRequest = new JsonObjectRequest(Request.Method.GET, url + "/info/name", null,
                response -> {
                    try {
                        String message = response.getString("message");
                        yourNameText.setText("Server: " + message);
                    } catch (JSONException e) { Log.w(TAG, "JSON Error"); }
                }, error -> { Log.e(TAG, "Error: " + error.getLocalizedMessage()); }
        );

        volleyQueue.add(ipServerRequest);
        volleyQueue.add(timeServerRequest);
        volleyQueue.add(nameServerRequest);

        listLayout.setVisibility(VISIBLE);
        listLayout.setAnimation(AnimationUtils.loadAnimation(ServerActivity.this, R.anim.fade_in));
    }
}