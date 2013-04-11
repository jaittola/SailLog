package com.ja.saillog.test.android;

import junit.framework.Assert;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.test.ServiceTestCase;

import com.ja.saillog.serv.TrackSaver;
import com.ja.saillog.serv.TrackSavingService;
import com.ja.saillog.serv.TrackSavingServiceConstants;
import com.ja.saillog.utilities.Propulsion;

public class TestTrackSavingService extends ServiceTestCase<TrackSavingService> {
    
    private final class FakeTrackSaver extends TrackSaver {

        public FakeTrackSaver(Context context) {
            super(null);
        }
        
        public synchronized boolean startSaving() {
            ++startCalled;
            saving = true;
            return true;
        }
        
        public synchronized void stopSaving() {
            ++stopCalled;
            saving = false;
        }
        
        public synchronized void changePropulsion(Propulsion prop) {
            propulsion = prop;
        }
        
        public synchronized int getStartCalled() {
            return startCalled;
        }
        
        public synchronized int getStopCalled() {
            return stopCalled;
        }
        
        public synchronized Propulsion getPropulsion() {
            return propulsion;
        }
        
        public synchronized boolean isSaving() {
            return saving;
        }
        
        private int startCalled = 0;
        private int stopCalled = 0;
        private Propulsion propulsion;
        private boolean saving = false;
    }
 
    protected interface IntValueFetcher {
        public int getValue();
        
        public static final int maxLoops = 5;
        public static final int sleepBetweenPolls = 100; // ms
    }
    
    protected int pollForInt(IntValueFetcher fetcher) throws InterruptedException {

        for (int i = 0; i < IntValueFetcher.maxLoops; ++i) {
            int value = fetcher.getValue();
            if (0 != value) {
                return value;
            }       
        
            Thread.sleep(IntValueFetcher.sleepBetweenPolls);
        }   
 
        return fetcher.getValue();
    }

    public TestTrackSavingService() {
        super(TrackSavingService.class);
                
    }
    
    @Override
    protected void setUp() throws Exception {
        System.err.println("setUp()");
        
        super.setUp();

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                outgoingMessenger = new Messenger(binder);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) { 
                System.err.println("Disconnected");
                outgoingMessenger = null;
            }
        };
        
        theIntent = new Intent(TrackSavingServiceConstants.intentName);
        fakeSaver = new FakeTrackSaver(null);
        TrackSavingService.setSaver(fakeSaver);
        
        getContext().startService(theIntent);
        getContext().bindService(theIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        while (null == outgoingMessenger) {
            System.err.println("Waiting for messenger");
            Thread.sleep(100);
        }
    }
    
    @Override
    protected void tearDown() throws Exception {
        if (null != serviceConnection) {
            getContext().unbindService(serviceConnection);
        }
        getContext().stopService(theIntent);
        
        fakeSaver = null;
        theIntent = null;
        outgoingMessenger = null;
        serviceConnection = null;
        
        super.tearDown();
    }
        
    public void testSendStartMsg() {
        int startCalled = sendMsgAndVerify(TrackSavingServiceConstants.MSG_START_SAVING,
                new IntValueFetcher() {
                    public int getValue() {
                        return fakeSaver.getStartCalled();
                    }
                });
        Assert.assertEquals(1, startCalled);
    }
    
    public void testSendStopMsg() {
        int stopCalled = sendMsgAndVerify(TrackSavingServiceConstants.MSG_STOP_SAVING,
                new IntValueFetcher() {
                    public int getValue() {
                        return fakeSaver.getStopCalled();
            }
        });

        Assert.assertEquals(1, stopCalled);
    }
    
    public void testSendChangePropusion() {
        try {
            Propulsion prop = new Propulsion(0x1, Propulsion.engineOn);
            
            Message msg = Message.obtain(null, 
                                         TrackSavingServiceConstants.MSG_CHANGE_PROPULSION);
            msg.setData(prop.toBundle());
            outgoingMessenger.send(msg);
            pollForInt(new IntValueFetcher() {
                public int getValue() {
                    return (null != fakeSaver.getPropulsion() ? 1 : 0);
                }
            });
        
            Propulsion resultPropulsion = fakeSaver.getPropulsion();
            Assert.assertNotNull(resultPropulsion);
            Assert.assertEquals(prop.getSailPlan(), resultPropulsion.getSailPlan());
            Assert.assertEquals(prop.getEngine(), resultPropulsion.getEngine());
        } catch (Exception ex) {
            Assert.fail("Caught exception: " + ex);
        }       
    }
    
    
    public void testStaysSavingOnUnbind() {
        sendMsgAndVerify(TrackSavingServiceConstants.MSG_START_SAVING,
                new IntValueFetcher() {
                    public int getValue() {
                        return fakeSaver.getStartCalled();
                    }
                });
        Assert.assertTrue(fakeSaver.isSaving());

        getContext().unbindService(serviceConnection);
        serviceConnection = null;
        Assert.assertTrue(fakeSaver.isSaving());
    }
    
    public void testStopWhenUnbinding() {
        // Send this msg to be sure that the service is running.
        sendMsgAndVerify(TrackSavingServiceConstants.MSG_STOP_SAVING,
                new IntValueFetcher() {
                    public int getValue() {
                        return fakeSaver.getStartCalled();
                    }
                });

        getContext().unbindService(serviceConnection);
        serviceConnection = null;
        
        Assert.assertFalse(fakeSaver.isSaving());
        Assert.assertNull(TrackSavingService.getSaver());
    }

    private int sendMsgAndVerify(int msgType, IntValueFetcher verifier) {
        try {
            Message msg = Message.obtain(null, msgType);
            outgoingMessenger.send(msg);
            return pollForInt(verifier);
        } catch (Exception ex) {
            Assert.fail("Caught exception: " + ex);
        }
     
        return -1;
    }

    private Intent theIntent;
    private Messenger outgoingMessenger;
    private ServiceConnection serviceConnection;
    private FakeTrackSaver fakeSaver;
}
