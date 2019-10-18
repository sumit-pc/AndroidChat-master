package com.example.aymen.androidchat;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.nkzawa.emitter.Emitter;

import org.json.JSONObject;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class ChatBoxActivity extends AppCompatActivity {

    public static final String NICKNAME_KEY = "application_user_nickname";
    private static final String ALGORITHM = "Blowfish";
    private static final String MODE = "Blowfish/CBC/PKCS5Padding";
    private static final String IV = "abcdefgh";
    public static int onlineUserCount = 0;
    public static String Nickname;
    private static String KEY;
    public static RecyclerView myRecylerView;
    public static List<Message> MessageList;
    //declare socket object
    //private Socket socket;
    public static ChatBoxAdapter chatBoxAdapter;
    public EditText messagetxt;
    public static TextView onlineUser;
    public Button send;
    public static DatabaseHelper myDB;
    private SharedPreferences mPreferences;
    private String sharedPrefFile = "com.example.android.hellosharedprefs";
    //private static final String KEY= "Sumit-Sumit";

    public static String encrypt(String value) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(MODE);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(IV.getBytes()));
        byte[] values = cipher.doFinal(value.getBytes());
        return Base64.encodeToString(values, Base64.DEFAULT);
    }

    public static String decrypt(String value) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        byte[] values = Base64.decode(value, Base64.DEFAULT);
        SecretKeySpec secretKeySpec = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(MODE);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(IV.getBytes()));
        return new String(cipher.doFinal(values));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_box);

        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);

        messagetxt = (EditText) findViewById(R.id.message);
        onlineUser = (TextView) findViewById(R.id.onlineUserCount);
        send = (Button) findViewById(R.id.send);
        // get the nickame of the user
        try {
            Nickname = (String) getIntent().getExtras().getString(MainActivity.NICKNAME);
            KEY = (String) getIntent().getExtras().getString(MainActivity.SECRET_KEY);
            Log.i("Secret_key_debug", KEY);
            SharedPreferences.Editor preferencesEditor = mPreferences.edit();
            preferencesEditor.putString(NICKNAME_KEY, Nickname);
            preferencesEditor.putString(MainActivity.SECRET_KEY, KEY);
            Log.i("Secret_key_debug", KEY);
            preferencesEditor.apply();
        } catch (Exception e) {
            Nickname = mPreferences.getString(NICKNAME_KEY, "");
            KEY = mPreferences.getString(MainActivity.SECRET_KEY, "");
            Log.i("Secret_key_debug", KEY);
        }


        myDB = new DatabaseHelper(this);
        Cursor data = myDB.getListMessage();

        //connect you socket client to the server
        /* socket = IO.socket("http://203.193.171.133:56455");
         socket.connect();*/
        //ClientSocket.socketIO.emit("requireCount");
        //setting up recyler
        MessageList = new ArrayList<>();
        myRecylerView = (RecyclerView) findViewById(R.id.messagelist);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        myRecylerView.setLayoutManager(mLayoutManager);
        ((LinearLayoutManager) mLayoutManager).setSmoothScrollbarEnabled(true);
        myRecylerView.setItemAnimator(new DefaultItemAnimator());

        if (data.getCount() == 0) {
            Log.i("Database_issue", "Database is Empty");
        } else {
            while (data.moveToNext()) {
                MessageList.add(new Message(data.getString(1), data.getString(2)));
                Log.i("nickname_person", data.getString(1));
                Log.i("message_person", data.getString(2));
            }
        }

        chatBoxAdapter = new ChatBoxAdapter(MessageList);
        myRecylerView.setAdapter(chatBoxAdapter);
        myRecylerView.scrollToPosition(chatBoxAdapter.getItemCount() - 1);
        //implementing socket listeners


        // message send action
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //retrieve the nickname and the message content and fire the event messagedetection
                if (!messagetxt.getText().toString().isEmpty()) {
                    String message_send = "";
                    try {
                        message_send = encrypt(messagetxt.getText().toString());
                    } catch (Exception e) {
                        Log.e("Error", "Encryption Failed");
                    }
                    ClientSocket.socketIO.emit("messagedetection", Nickname, message_send);
                    ClientSocket.socketIO.emit("requireCount");
                    messagetxt.setText(" ");
                }


            }
        });


//        if (MainActivity.mPreferences.getString(MainActivity.FIRST_RUN, "").equals("TRUE")) {
//          //if(true){
//            SharedPreferences.Editor preferencesEditor = MainActivity.mPreferences.edit();
//            preferencesEditor.putString(MainActivity.FIRST_RUN, "FALSE");
//            Log.i("FIRST_RUN", "FALSE");
//            preferencesEditor.apply();
//            ClientSocket.socketIO.on("countChange", new Emitter.Listener() {
//                @Override
//                public void call(final Object... args) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            int data = (int) args[0];
//                            onlineUser.setText(String.valueOf(data));
//                            //Toast.makeText(ChatBoxActivity.this,String.valueOf(data),Toast.LENGTH_LONG).show();
//                        }
//                    });
//                }
//            });
//
//            ClientSocket.socketIO.on("userjoinedthechat", new Emitter.Listener() {
//                @Override
//                public void call(final Object... args) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            String data = (String) args[0];
//                            ClientSocket.socketIO.emit("requireCount");
//                            Toast.makeText(ChatBoxActivity.this, data, Toast.LENGTH_LONG).show();
//
//                        }
//                    });
//                }
//            });
//            ClientSocket.socketIO.on("userdisconnect", new Emitter.Listener() {
//                @Override
//                public void call(final Object... args) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            String data = (String) args[0];
//                            ClientSocket.socketIO.emit("requireCount");
//                            //Toast.makeText(ChatBoxActivity.this,data,Toast.LENGTH_SHORT).show();
//
//                        }
//                    });
//                }
//            });
//
//            ClientSocket.socketIO.on("killAppuser", new Emitter.Listener() {
//                @Override
//                public void call(final Object... args) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            String data = (String) args[0];
//                            ClientSocket.socketIO.emit("requireCount");
//                            Toast.makeText(ChatBoxActivity.this, data, Toast.LENGTH_LONG).show();
//
//                        }
//                    });
//                }
//            });
//
//            ClientSocket.socketIO.on("message", new Emitter.Listener() {
//                @Override
//                public void call(final Object... args) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            ClientSocket.socketIO.emit("requireCount");
//                            JSONObject data = (JSONObject) args[0];
//                            try {
//                                String nickname = data.getString("senderNickname");
//                                String message = data.getString("message");
//                                message = decrypt(message);
//                                addData(nickname, message);
//                                Message m = new Message(nickname, message);
//                                MessageList.add(m);
//
//                                chatBoxAdapter.notifyDataSetChanged();
//                               // chatBoxAdapter = new ChatBoxAdapter(MessageList);
//                                //DataSetChaneged();
//                              //  myRecylerView.setAdapter(chatBoxAdapter);
//                             //   myRecylerView.scrollToPosition(chatBoxAdapter.getItemCount() - 1);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                                Log.e("MessageError", e.getMessage());
//                            }
//
//
//                        }
//                    });
//                }
//            });
//
//            ClientSocket.socketIO.emit("requireCount");
//        }


    }


    public static void addData(String name, String message) {
        boolean insertData = myDB.addData(name, message);

        if (insertData == true) {
            Log.i("Database_issue", "Added to database");
        } else {
            Log.i("Database_issue", "Something went wrong");
        }
    }

    public void DataSetChaneged(){
       //public chatBoxAdapter.notifyItemInserted(MessageList.size());

        chatBoxAdapter = new ChatBoxAdapter(MessageList);
        myRecylerView.removeAllViewsInLayout();
        myRecylerView =  findViewById(R.id.messagelist);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        myRecylerView.setLayoutManager(mLayoutManager);
        ((LinearLayoutManager) mLayoutManager).setSmoothScrollbarEnabled(true);
        myRecylerView.setItemAnimator(new DefaultItemAnimator());


        myRecylerView.setAdapter(chatBoxAdapter);
        myRecylerView.scrollToPosition(chatBoxAdapter.getItemCount() - 1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*ClientSocket.socketIO.disconnect();
        Intent background = new Intent(ChatBoxActivity.this, AppBackgroundService.class);
        background.putExtra(MainActivity.NICKNAME,Nickname);
        startService(background);*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor preferencesEditor = MainActivity.mPreferences.edit();
        preferencesEditor.putString(MainActivity.FIRST_RUN, "CLOSE");
        Log.i("FIRST_RUN", "CLOSE");
        preferencesEditor.apply();
        ClientSocket.socketIO.disconnect();
        stopService(MainActivity.background);
        ClientSocket.socketIO.emit("killApp", Nickname);
        //ClientSocket.socketIO.disconnect();
        //AppBackgroundService.isRunning = false;
        //Intent background = new Intent(ChatBoxActivity.this, AppBackgroundService.class);
        //background.putExtra(MainActivity.NICKNAME,Nickname);
        //startService(background);
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }

    public void scrollMethod(View view) {
        myRecylerView.scrollToPosition(chatBoxAdapter.getItemCount() - 1);
    }

    public void logOut(View view) {
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.clear();
        preferencesEditor.apply();
        Log.i("Secret_key_debug", mPreferences.getString(MainActivity.SECRET_KEY, "nullable"));

        stopService(MainActivity.background);

        ClientSocket.socketIO.disconnect();
        ChatBoxActivity.this.finish();
        Intent in = new Intent(this, MainActivity.class);
        startActivity(in);

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

//    public static void setText(String count)
//    {
//        onlineUser.setText(String.valueOf(count));
//    }
}
