package com.shoeboxscientist.goto10.handlers;

import android.app.Notification;
import android.util.Log;

import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.shoeboxscientist.goto10.CommandException;
import com.shoeboxscientist.goto10.MessageHandler;

import org.json.JSONException;

import java.io.IOException;

/**
 * Handles parsing of JSON messages to/from client and calls appropriate subclass methods.
 */
public abstract class AbstractRainbowHatConnector {

    protected static final String TAG = "RainbowHat";

    // Class used for mapping client/server messages.
    public static final String PERIPHERAL_CLASS_ID = "rh";

    public static final String ID_DISPLAY = "display";
    public static final String ID_LEDSTRIP = "ledstrip";

    protected MessageHandler mPeripheralListener;

    public void setPeripheralListener(MessageHandler listener) {
        mPeripheralListener = listener;
    }

    public abstract void cleanup() throws IOException;

    public abstract void playNoise(int v) throws IOException;

    public abstract void updateDisplay(String value) throws IOException;

    public abstract void updateLedStrip(int n) throws IOException;

    public abstract void updateLedStrip(int[] colours) throws IOException;

    public void execute(JsonObject payload) throws CommandException {
        String display = asString(payload.get(ID_DISPLAY));
        String ledstrip = asString(payload.get(ID_LEDSTRIP));

        try {
            if (display != null) {
                updateDisplay(display);
            }

            if (ledstrip != null) {
                updateLedStrip(Integer.parseInt(ledstrip));
            }
        } catch (IOException e) {
            Log.e(TAG, "Couldn't update hat state: " + e.getMessage());
        }
    }

    private String asString(JsonElement e) {
        return (e == null) ? null : e.getAsString();
    }

    /**
     * Helper method to create messages to send back to the client.
     * @param id - ID of the button pressed (a, b, c)
     * @param pressed - pressed state as sent by the event.
     */
    public static JsonObject newButtonMsg(String id, boolean pressed) throws JSONException {
        JsonObject msg = new JsonObject();
        msg.addProperty("id", id);
        msg.addProperty("pressed", pressed);
        return msg;
    }
}
