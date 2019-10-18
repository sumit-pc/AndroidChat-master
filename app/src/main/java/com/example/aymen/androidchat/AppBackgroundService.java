package com.example.aymen.androidchat;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import static com.example.aymen.androidchat.ChatBoxActivity.decrypt;

public class AppBackgroundService extends Service {
    // Notification channel ID.
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";
    private final IBinder mBinder = new MyLocalBinder();
    NotificationManager mNotifyManager;
    private Thread notificationThread;
    private  Thread countChnage;
    private Thread userJoinedChat;
    private Thread userDisconnect;
    private Thread message;
    //ChatBoxActivity activity;
    // private Socket mSocket;
    private String Nickname;
    private String SECRET_KEY;
    private int count = 0;

    private Runnable notificationTask = new Runnable() {
        @Override
        public void run() {
            // do something in here

            Log.i("INFO", "SOCKET BACKGROUND SERVICE IS RUNNING");
            ClientSocket.socketIO.on("message", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        //extract data from fired event
                        String nickname = data.getString("senderNickname");
                        String message = data.getString("message");

                        try {
                            message = decrypt(message);
                            Message m = new Message(nickname, message);

                            Log.i("INFO", message);

                            if (!nickname.equals(ChatBoxActivity.Nickname)) {
                                count++;
                                sendNotification(nickname, message);
                            }
                        } catch (Exception e) {
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });


            //we're not stopping self as we want this service to be continuous
            //stopSelf();
        }
    };

    private Runnable countChangeTask = new Runnable() {
        @Override
        public void run() {
            // do something in here

            //Log.i("INFO", "SOCKET BACKGROUND SERVICE IS RUNNING");
            ClientSocket.socketIO.on("countChange", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    int data = (int) args[0];
                    //ChatBoxActivity.setText(String.valueOf(data));
                }
            });


            //we're not stopping self as we want this service to be continuous
            //stopSelf();
        }
    };


    private Runnable userJoinedChatTask = new Runnable() {
        @Override
        public void run() {
            // do something in here

            //Log.i("INFO", "SOCKET BACKGROUND SERVICE IS RUNNING");
            ClientSocket.socketIO.on("userjoinedthechat", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    String data = (String) args[0];
                    ClientSocket.socketIO.emit("requireCount");
                    //Toast.makeText(ChatBoxActivity.this, data, Toast.LENGTH_LONG).show();

                }
            });


            //we're not stopping self as we want this service to be continuous
            //stopSelf();
        }
    };

    private Runnable userDisconnectTask = new Runnable() {
        @Override
        public void run() {
            // do something in here

            //Log.i("INFO", "SOCKET BACKGROUND SERVICE IS RUNNING");
            ClientSocket.socketIO.on("userdisconnect", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    String data = (String) args[0];
                    ClientSocket.socketIO.emit("requireCount");
                    //Toast.makeText(ChatBoxActivity.this,data,Toast.LENGTH_SHORT).show();

                }
            });
        }
    };

    private Runnable messageTask = new Runnable() {
        @Override
        public void run() {
            // do something in here

            //Log.i("INFO", "SOCKET BACKGROUND SERVICE IS RUNNING");
            ClientSocket.socketIO.on("message", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    ClientSocket.socketIO.emit("requireCount");
                    JSONObject data = (JSONObject) args[0];
                    try {
                        String nickname = data.getString("senderNickname");
                        String message = data.getString("message");
                        message = decrypt(message);
                        ChatBoxActivity.addData(nickname, message);
                        Message m = new Message(nickname, message);
                        ChatBoxActivity.MessageList.add(m);

                        ChatBoxActivity.chatBoxAdapter.notifyDataSetChanged();
                        ChatBoxActivity.myRecylerView.scrollToPosition(ChatBoxActivity.chatBoxAdapter.getItemCount() - 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("MessageError", e.getMessage());
                    }

                }
            });


            //we're not stopping self as we want this service to be continuous
            //stopSelf();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        try {
            // instantiate socket connection
            //mSocket = IO.socket("http://203.193.171.133:56455");
            ClientSocket.socketIO = IO.socket("http://203.193.171.133:56455");
            // establish the socket connection
            //mSocket.connect();
            ClientSocket.socketIO.connect();


        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startMyOwnForeground();
        } else {
            startForeground(1, new Notification());
        }

    }

    private void startMyOwnForeground() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel(PRIMARY_CHANNEL_ID, "Job Service notification", NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    //.setSmallIcon()
                    .setContentTitle("App is running in background")
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(2, notification);
        }
    }

    public void createNotificationChannel() {

        // Define notification manager object.
        mNotifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Notification channels are only available in OREO and higher.
        // So, add a check on SDK version.
        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.O) {

            // Create the NotificationChannel with all the parameters.
            NotificationChannel notificationChannel = new NotificationChannel
                    (PRIMARY_CHANNEL_ID,
                            "Job Service notification",
                            NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription
                    ("Notifications from Job Service");

            mNotifyManager.createNotificationChannel(notificationChannel);
        }
    }

    public void sendNotification(String nickname, String message) {
        PendingIntent contentPendingIntent = PendingIntent.getActivity
                (this, 0, new Intent(this, ChatBoxActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder
                (this, PRIMARY_CHANNEL_ID)
                .setContentTitle(nickname)
                .setContentText(message)
                .setContentIntent(contentPendingIntent)
                .setSmallIcon(R.drawable.background_transparent_logo)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
        ;

        mNotifyManager.notify(count, builder.build());
    }

    @Override
    public void onDestroy() {
        SharedPreferences.Editor preferencesEditor = MainActivity.mPreferences.edit();
        preferencesEditor.putString(MainActivity.FIRST_RUN, "CLOSE");
        Log.i("FIRST_RUN", "CLOSE");
        preferencesEditor.apply();
        //Intent broadcastIntent = new Intent(this, SensorRestarterBroadcastReceiver.class);
        //sendBroadcast(broadcastIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        try{
            Nickname = intent.getExtras().getString(MainActivity.NICKNAME);
            SECRET_KEY = intent.getExtras().getString(MainActivity.SECRET_KEY);
        }
        catch (Exception e)
        {
            Nickname = MainActivity.mPreferences.getString(ChatBoxActivity.NICKNAME_KEY, "nullable");
            SECRET_KEY = MainActivity.mPreferences.getString(SECRET_KEY, "public");
        }

        Log.i("Secret_key_debug", SECRET_KEY);
        //if (MainActivity.mPreferences.getString(MainActivity.FIRST_RUN, "").equals("TRUE")) {
        if(true){
            if( this.notificationThread == null ) {
                this.notificationThread = new Thread(notificationTask);
                this.notificationThread.start();
            }
            if( this.countChnage == null ) {
                this.countChnage = new Thread(countChangeTask);
                this.countChnage.start();
            }
            if( this.userJoinedChat == null ) {
                this.userJoinedChat = new Thread(userJoinedChatTask);
                this.userJoinedChat.start();
            }
            if( this.userDisconnect == null ) {
                this.userDisconnect = new Thread(userDisconnectTask);
                this.userDisconnect.start();
            }
            if( this.message == null ) {
                this.message = new Thread(messageTask);
                this.message.start();
            }
            //Toast.makeText(getApplicationContext(),Nickname,Toast.LENGTH_LONG).show();
            ClientSocket.socketIO.emit("join", Nickname);
            //if(ClientSocket.socketIO.connected()){

        }
        if (!ClientSocket.socketIO.connected()) {
            try {
                ClientSocket.socketIO = IO.socket("http://203.193.171.133:56455");
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            ClientSocket.socketIO.connect();
        }
        //}
        return START_NOT_STICKY;


    }


    public Socket getSocket() {
        return ClientSocket.socketIO;
    }

    public class MyLocalBinder extends Binder {
        AppBackgroundService getService() {
            return AppBackgroundService.this;
        }
    }


}
