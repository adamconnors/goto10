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

/**
 * Used as access point to call local methods from within Rhino JS interpreter.
 */
public class JavaContext {

    private final AbstractRainbowHatConnector mHat;
    private final NanoHttpWebServer.LoggingOutput mLog;
    private final Scriptable mJSScope;
    private final Context mJSContext;

    public JavaContext(AbstractRainbowHatConnector hat, NanoHttpWebServer.LoggingOutput log,
                       Scriptable scope, Context ctx) {
        mHat = hat;
        mLog = log;
        mJSContext = ctx;
        mJSScope = scope;
    }

    public void log(String s) {
        mLog.log(s);
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

    // TODO: Use continuations to make this cleaner.
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

