package com.shoeboxscientist.goto10.javascript;

import com.google.android.things.pio.PeripheralManagerService;
import com.google.gson.JsonObject;
import com.shoeboxscientist.goto10.CommandException;
import com.shoeboxscientist.goto10.MessageHandler;
import com.shoeboxscientist.goto10.NanoHttpWebServer;
import com.shoeboxscientist.goto10.handlers.AbstractRainbowHatConnector;

import org.json.JSONException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Used as access point to call local methods from within Rhino JS interpreter.
 */
public class JavaContext {

    private final AbstractRainbowHatConnector mHat;
    private final NanoHttpWebServer.LoggingOutput mLog;
    private Scriptable mJSScope;
    private Context mJSContext;

    private boolean threadsRunning = false;
    private ArrayList<Thread> mActiveThreads = new ArrayList<Thread>();


    public JavaContext(AbstractRainbowHatConnector hat, NanoHttpWebServer.LoggingOutput log) {
        mHat = hat;
        mLog = log;
    }

    public void log(String s) {
        mLog.log(s);
    }

    public void init(Scriptable scope, Context ctx) {
        mJSContext = ctx;
        mJSScope = scope;
        threadsRunning = false; // ensure we stop any previously existing intervals.

        // Make sure to clean up all interrupted threads so we don't leave old threads running
        // from a previous execution... This is really ghetto.
        // TODO: clean this shit up.
        for (Thread th : mActiveThreads) {
            th.interrupt();

            try {
                th.join();
            } catch (InterruptedException e) {
                // ignore
            }
        }
        mActiveThreads.clear();
    }

    public void setDisplay(String s) {
        try {
            mHat.updateDisplay(s);
        } catch (IOException e) {
            mLog.log("IOException setting hat display: " + e.getMessage());
        }
    }

    public void setButtonListener(final Function callback) {
        mLog.log("Set button listener: " + callback);

        mHat.setPeripheralListener(new AbstractRainbowHatConnector.PeripheralListener() {
            @Override
            public void onPeripheralEvent(String cls, JsonObject json) throws CommandException,
                    IOException, JSONException {

                // TODO: Lots of unnecessary to/from json here, clean up.
                // This is a hangover from when it was executed on the client.
                String id = json.get("id").toString();
                String pressed = json.get("pressed").toString();

                // TODO: Check this somewhere sensible.
                String realID;
                if (id.equals("\"BCM21\"")) {
                    realID = "A";
                } else if (id.equals("\"BCM20\"")) {
                    realID = "B";
                } else {
                    realID = "C";
                }

                callback.call(mJSContext, mJSScope, mJSScope, new Object[] { realID, pressed });
            }
        });
    }

    public void setTimeout(final Function func, final int ms) {
        threadsRunning = true;
        final Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sleep(ms);
                    func.call(mJSContext, mJSScope, mJSScope, new Object[] {});
                } catch (Error e) {
                    mLog.log("Error from running thread: " + e.getMessage());
                }
            }
        });

        mLog.log("Running setTimeout on " + func + " on thread " + mActiveThreads.size());
        mActiveThreads.add(th);
        th.start();
    }

    public void setInterval(final Function func, final int ms) {
        threadsRunning = true;
        final Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (threadsRunning) {
                        sleep(ms);
                        func.call(mJSContext, mJSScope, mJSScope, new Object[] {});
                        mLog.log("Running setTimeout on " + func + " on thread " + mActiveThreads.size());
                    }
                } catch (Error e) {
                    mLog.log("Error from running thread: " + e.getMessage());
                }
            }
        });

        mLog.log("Running setInterval on " + func + " on thread " + th.getId());
        mActiveThreads.add(th);
        th.start();
    }

    // TODO: This blocks button events... Can only call this if running on a separate thread?
    public void sleep(long ms) {
        long now = System.currentTimeMillis();
        while (System.currentTimeMillis() - now < ms) {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                // ignore.
            }
        }
    }

    public void setLedStrip(int pos) {
        try {
            mHat.updateLedStrip(pos);
        } catch (IOException e) {
            mLog.log("IOException setting led strip: " + e.getMessage());
        }
    }
}

