package com.example.aymen.androidchat;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {


    public static final String NICKNAME = "usernickname";
    public static final String SECRET_KEY = "secretkey";
    public static final String FIRST_RUN = "boolean";
    private static final int REQ_CODE = 9001;
    public static GoogleApiClient googleApiClient;
    public static Intent background;
    public static SharedPreferences mPreferences;
    private String sharedPrefFile = "com.example.android.hellosharedprefs";
    private SignInButton signIn;
    private EditText secretKey;
    //new
    public static AppBackgroundService mAppBackgroundService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        secretKey = (EditText) findViewById(R.id.secretKey);
        signIn = findViewById(R.id.signIn);
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions).build();

        //call UI component  by id
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        Log.i("Nickname_problem", mPreferences.getString(ChatBoxActivity.NICKNAME_KEY, "nullable"));
        String nick = mPreferences.getString(ChatBoxActivity.NICKNAME_KEY, "nullable");
        String key = mPreferences.getString(SECRET_KEY, "public");
        Log.i("Secret_key_debug", key);
        String status = (mPreferences.getString(FIRST_RUN, "CLOSE"));

        if(status.equals("CLOSE"))
        {
            SharedPreferences.Editor preferencesEditor = MainActivity.mPreferences.edit();
            preferencesEditor.putString(MainActivity.FIRST_RUN, "TRUE");
            Log.i("FIRST_RUN", "TRUE");
            preferencesEditor.apply();
        }


        mAppBackgroundService = new AppBackgroundService();

        if (!nick.equals("nullable")) {
            //Toast.makeText(getApplicationContext(),nick,Toast.LENGTH_LONG).show();
            if (isMyServiceRunning(mAppBackgroundService.getClass())) {
                Intent in = new Intent(this, ChatBoxActivity.class);
                startActivity(in);
                //MainActivity.this.finish();
            } else {
                background = new Intent(getApplicationContext(), mAppBackgroundService.getClass());
                background.putExtra(NICKNAME, nick);
                background.putExtra(SECRET_KEY, key);
                startService(background);

                Intent intent2 = new Intent(this, ChatBoxActivity.class);
                intent2.putExtra(MainActivity.NICKNAME, nick);
                intent2.putExtra(MainActivity.SECRET_KEY, key);
                Log.i("Secret_key_debug", SECRET_KEY);
                startActivity(intent2);
            }
        }


        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(secretKey.getText())) {
                    Toast.makeText(getApplicationContext(), "Please Enter Secret Key", Toast.LENGTH_SHORT).show();
                } else {
                    signIn();
                }

            }
        });

    }

    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("isMyServiceRunning?", true + "");
                return true;
            }
        }
        Log.i("isMyServiceRunning?", false + "");
        return false;
    }

    private void signIn() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(intent, REQ_CODE);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleResult(result);
        }
    }

    private void handleResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            String name = account.getDisplayName();
            String email = account.getEmail();


            background = new Intent(MainActivity.this, AppBackgroundService.class);
            background.putExtra(NICKNAME, name);
            background.putExtra(SECRET_KEY, secretKey.getText().toString().trim());
            Log.i("Secret_key_debug", secretKey.getText().toString().trim());
            startService(background);
            //MainActivity.this.finish();

            Intent intent2 = new Intent(this, ChatBoxActivity.class);
            intent2.putExtra(MainActivity.NICKNAME, name);
            intent2.putExtra(MainActivity.SECRET_KEY, secretKey.getText().toString().trim());
            Log.i("Secret_key_debug", SECRET_KEY);
            startActivity(intent2);

            //Toast.makeText(getApplicationContext(), name, Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(getApplicationContext(), result.getStatus().getStatusMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(background);
        Log.i("onDestoyOfMainActivity","ON Destroy called");
    }
}
