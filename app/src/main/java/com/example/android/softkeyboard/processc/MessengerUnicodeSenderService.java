package com.example.android.softkeyboard.processc;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.widget.Toast;

public class MessengerUnicodeSenderService extends Service {
    static final int MSG_START_INPUT = 9;
    static final int MSG_STOP_INPUT = 10;
    static final int MSG_STOP_SENDING_CHINESE_SYMBOLS = 3;
    static final int MSG_GET_CHINESE_SYMBOL = 6;
    static final int MSG_GET_REPLY_TO_MESSENGER = 7;

    private static final boolean DEBUG = false;
    //    boolean mBound;
    boolean MSG_START_SENDING_CHINESE_SYMBOLS_PENDING;
//    String chineseText = "现在键盘已完全移除";
String chineseText = "ABCDEFGHIJKLMNOPQR";
    int currentChineseSymbol = 0;
    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            if (MSG_START_SENDING_CHINESE_SYMBOLS_PENDING == true) {
                if (mUnicodeReceiverServiceMessenger != null) {
                    int n;
                    if (currentChineseSymbol < chineseText.length()) {
                        n = chineseText.charAt(currentChineseSymbol);
                        currentChineseSymbol++;
                    } else {
                        n = ' ';
                        currentChineseSymbol = 0;
                        MSG_START_SENDING_CHINESE_SYMBOLS_PENDING = false;
                    }
                    Message msg = Message.obtain(null, MSG_GET_CHINESE_SYMBOL, n, 0);
                    try {
                        mUnicodeReceiverServiceMessenger.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    timerHandler.postDelayed(this, 1000);
                } else {
                    doBind();
                }
            }
        }
    };

    private Messenger mUnicodeReceiverServiceMessenger;
    private Messenger mReplyToMessenger = new Messenger(new IncomingHandler());

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service. We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mUnicodeReceiverServiceMessenger = new Messenger(binder);
            sendMessage(MSG_GET_REPLY_TO_MESSENGER);

//            if(MSG_START_SENDING_CHINESE_SYMBOLS_PENDING){
//                startSending();
//            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mUnicodeReceiverServiceMessenger = null;
        }
    };

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START_INPUT:
                    if (DEBUG) {
                        Toast.makeText(getApplicationContext(), "C: MSG_START_INPUT", Toast.LENGTH_SHORT).show();
                    }
                    startSending();
                    break;
                case MSG_STOP_INPUT:
                    if (DEBUG) {
                        Toast.makeText(getApplicationContext(), "C: MSG_STOP_INPUT", Toast.LENGTH_SHORT).show();
                    }
                    stopSending();
                    break;
                default:
                    break;
            }
        }
    }

    private void sendMessage(int what) {
        Message msg = Message.obtain(null, what, 0, 0);
        msg.replyTo = mReplyToMessenger;
        try {
            mUnicodeReceiverServiceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void doBind() {
        if (mUnicodeReceiverServiceMessenger == null) {
            // Bind to the service
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.example.android.softkeyboard", "com.example.android.softkeyboard.UnicodeReceiverService"));
            boolean b = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    void doUnbindService() {
        if (mUnicodeReceiverServiceMessenger != null) {
            // Detach our existing connection.
            unbindService(mConnection);
            mUnicodeReceiverServiceMessenger = null;
        }
    }

    private void startSending() {
        currentChineseSymbol = 0;
        MSG_START_SENDING_CHINESE_SYMBOLS_PENDING = true;
        if (mUnicodeReceiverServiceMessenger != null) {
            timerHandler.postDelayed(timerRunnable, 1000);
        } else {
            doBind();
        }
    }

    private void stopSending() {
        MSG_START_SENDING_CHINESE_SYMBOLS_PENDING = false;
        timerHandler.removeCallbacks(timerRunnable);
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) {
            Toast.makeText(getApplicationContext(), "C: onStartCommand", Toast.LENGTH_SHORT).show();
        }
        doBind();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (DEBUG) {
            Toast.makeText(getApplicationContext(), "C: onDestroy", Toast.LENGTH_SHORT).show();
        }
        stopSending();
        doUnbindService();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
