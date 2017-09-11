package com.shoeboxscientist.goto10;

import static com.shoeboxscientist.goto10.NanoHttpWebServer.LoggingOutput;
import static com.shoeboxscientist.goto10.NanoHttpWebServer.DataSource;

import android.renderscript.Script;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shoeboxscientist.goto10.handlers.AbstractRainbowHatConnector;

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.EcmaError;

import java.io.IOException;

/**
 * Parses and executes commands and passes them onto the appropriate handlers.
 * Messages all take the form: { typ: 'type', cls: 'class', payload: 'payload' }
 *
 * Only messages of type = 'cmd' have the attribute 'cls' to identify the handler that will be
 * used to parse the payload.
 */

public class MessageHandler implements AbstractRainbowHatConnector.PeripheralListener {

    private AbstractRainbowHatConnector mRainbowHat;
    private ContentStore mStore;

    public static final String ATTR_TYPE = "typ";
    public static final String ATTR_PAYLOAD = "payload";
    public static final String ATTR_CLASS = "cls";

    private static final String TYPE_SAVE = "save";
    private static final String TYPE_EXECUTE = "execute";
    private static final String TYPE_LOAD = "load";
    private static final String TYPE_CMD = "cmd";
    private static final String TYPE_LOG = "log";

    private NanoHttpdWebSocketServer mSocketServer;
    private ScriptExecutor mExecutor;

    public MessageHandler(DataSource source,
                          ContentStore store,
                          AbstractRainbowHatConnector rainbowHat,
                          LoggingOutput log) {
        mRainbowHat = rainbowHat;
        mRainbowHat.setPeripheralListener(this);
        mExecutor = new ScriptExecutor(source, log, mRainbowHat);
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
        mExecutor.onPeripheralEvent(cls, json);
        mSocketServer.send(buildJSMsg("Event: " + cls + ": " + json));
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
        } else if (TYPE_EXECUTE.equals(type)) {
            String payload = obj.get(ATTR_PAYLOAD).getAsString();
            System.out.println("Saving and executing script: " + payload);
            mStore.savescript(payload);

            try {
                mExecutor.executeScript(payload);
                mSocketServer.send(buildJSMsg("OK"));
            } catch (EcmaError e) {
                e.printStackTrace();
                mSocketServer.send(buildJSMsg("Error: " + e.getMessage()));
            }
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

    private JsonObject buildJSMsg(String s) {
        JsonObject msg = new JsonObject();
        msg.addProperty(ATTR_TYPE, TYPE_LOG);
        msg.addProperty(ATTR_PAYLOAD, s);
        return msg;
    }

    // Deprecated -- this was used when execution was in the browser, now execution is all local
    // there are no callbacks sent to the browser.
    @Deprecated
    private JsonObject XbuildCommandPayload(String cls, JsonObject payload) {
        JsonObject msg = new JsonObject();
        msg.addProperty(ATTR_TYPE, TYPE_CMD);
        msg.addProperty(ATTR_CLASS, cls);
        msg.add(ATTR_PAYLOAD, payload);
        return msg;
    }
}
