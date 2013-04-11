package com.ja.saillog.serv;

import com.ja.saillog.utilities.Propulsion;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

public class TrackSavingService extends Service {
 
    final class TrackSavingMsgHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            System.err.println("handleMessage: " + msg.what);
            switch (msg.what) {
            case TrackSavingServiceConstants.MSG_STOP_SAVING:
                saver.stopSaving();
                break;
            case TrackSavingServiceConstants.MSG_START_SAVING:
                saver.startSaving();
                break;
            case TrackSavingServiceConstants.MSG_CHANGE_PROPULSION:
                saver.changePropulsion(new Propulsion(msg.getData()));
                break;
            default:
                // Hmm. Ignore.
            }
        }
    }
    
    @Override
    public void onCreate() {
        if (null == handler) {
            handler = new TrackSavingMsgHandler();
            messenger = new Messenger(handler);
        }
        
        if (null == saver) {
            saver = new TrackSaver(this);
        }
    }
                    
    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        System.err.println("Starting TrackSavingService");
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        System.err.println("Binding");
        return messenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (false == saver.isSaving()) {
            stopSelf();
        }
        
        System.err.println("Unbound.");
        return false; // Do not ask for onRebind to be called later.
    }
    
    @Override
    public void onDestroy() {
        System.err.println("Destroying TrackSavingService");
        
        handler = null;
        messenger = null;

        saver.stopSaving();
        saver = null;
        
        super.onDestroy();
    }

    // For testing.
    public static void setSaver(TrackSaver saver) {
        TrackSavingService.saver = saver;
    }
    
    public static TrackSaver getSaver() {
        return TrackSavingService.saver;
    }
    
    private TrackSavingMsgHandler handler;
    private Messenger messenger;
    private static TrackSaver saver;
}
