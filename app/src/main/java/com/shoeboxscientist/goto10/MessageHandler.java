package com.shoeboxscientist.goto10;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shoeboxscientist.goto10.handlers.AbstractRainbowHatConnector;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Parses and executes commands and passes them onto the appropriate handlers.
 * Messages all take the form: { typ: 'type', cls: 'class', payload: 'payload' }
 *
 * Only messages of type = 'cmd' have the attribute 'cls' to identify the handler that will be
 * used to parse the payload.
 */

public class MessageHandler {

    private AbstractRainbowHatConnector mRainbowHat;
    private ContentStore mStore;

    public static final String ATTR_TYPE = "typ";
    public static final String ATTR_PAYLOAD = "payload";
    public static final String ATTR_CLASS = "cls";

    private static final String TYPE_SAVE = "save";
    private static final String TYPE_LOAD = "load";
    private static final String TYPE_CMD = "cmd";

    private NanoHttpdWebSocketServer mSocketServer;

    public MessageHandler(ContentStore store,
                          AbstractRainbowHatConnector rainbowHat) {
        mRainbowHat = rainbowHat;
        mRainbowHat.setPeripheralListener(this);
        mStore = store;
    }

    public void setSocketServer(NanoHttpdWebSocketServer socketServer) {
        this.mSocketServer = socketServer;
    }

    /**
     * Pass events from the peripherals and send them down to the client.
     */
    public void onPeripheralEvent(String cls, JsonObject json)
            throws CommandException, IOException, JSONException {
        mSocketServer.send(buildCommandPayload(cls, json));
    }

    /**
     * Parse incoming commands from the client.
     */
    public void onIncomingMessage(String json) throws CommandException, IOException, JSONException {

        if (mSocketServer == null) {
            throw new CommandException("You forgot to set up the socket server.");
        }

        System.out.println("EXECUTING JSON MESSAGE: " + json);
        JsonObject obj = new JsonParser().parse(json).getAsJsonObject();
        JsonElement typ = obj.get(ATTR_TYPE);
        String type = typ.getAsString();
        if (TYPE_SAVE.equals(type)) {
            String payload = obj.get(ATTR_PAYLOAD).getAsString();
            System.out.println("Saving script: " + payload);
            mStore.savescript(payload);
        } else if (TYPE_LOAD.equals(type)) {
            String script = mStore.readscript();
            mSocketServer.send(buildLoadScriptPayload(script));
        } else if (TYPE_CMD.equals(type)) {
            // TODO: Route messages to correct handler based on class.
            JsonObject payload = obj.get(ATTR_PAYLOAD).getAsJsonObject();
            mRainbowHat.execute(payload);
        }
    }

    private JsonObject buildLoadScriptPayload(String script) {
        JsonObject msg = new JsonObject();
        msg.addProperty(ATTR_TYPE, TYPE_LOAD);
        msg.addProperty(ATTR_PAYLOAD, script);
        return msg;
    }

    private JsonObject buildCommandPayload(String cls, JsonObject payload) {
        JsonObject msg = new JsonObject();
        msg.addProperty(ATTR_TYPE, TYPE_CMD);
        msg.addProperty(ATTR_CLASS, cls);
        msg.add(ATTR_PAYLOAD, payload);
        return msg;
    }
}
