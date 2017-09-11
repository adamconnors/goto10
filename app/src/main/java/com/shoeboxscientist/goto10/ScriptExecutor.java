package com.shoeboxscientist.goto10;

import com.google.gson.JsonObject;
import com.shoeboxscientist.goto10.handlers.AbstractRainbowHatConnector;
import com.shoeboxscientist.goto10.handlers.RainbowHatConnector;
import com.shoeboxscientist.goto10.javascript.JavaContext;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.io.IOException;

/**
 * Executes javascript.
 */
public class ScriptExecutor {

    private final NanoHttpWebServer.LoggingOutput mLog;
    private final AbstractRainbowHatConnector mHat;
    private final NanoHttpWebServer.DataSource mSource;

    private Context mRhino = null;

    private static final String SERVER_SCRIPT_PATH = "/goto10_server.js";

    public ScriptExecutor(NanoHttpWebServer.DataSource source,
                          NanoHttpWebServer.LoggingOutput log,
                          AbstractRainbowHatConnector hat) {
        mLog = log;
        mHat = hat;
        mSource = source;
    }

    public void executeScript(String script) throws EcmaError {
        System.out.println("Executing " + script);
        mRhino = Context.enter();

        // Turn off optimization to make Rhino Android compatible
        mRhino.setOptimizationLevel(-1);

        // Init the js scope
        Scriptable scope = mRhino.initStandardObjects();

        // Adds a global instance of JavaContext to the js runtime.
        Object context = Context.javaToJS(new JavaContext(mHat, mLog, scope, mRhino), scope);
        ScriptableObject.putProperty(scope, "thing", context);

        // Execute server js.
        try {
            String serverDefns = mSource.getStringForPath(SERVER_SCRIPT_PATH);
            mRhino.evaluateString(scope, serverDefns, "<scr>", 1, null);
        } catch (IOException e) {
            mLog.log("Exception reading goto10_server: " + e.getMessage());
        }

        // Execute client js definitions.
        Object result = mRhino.evaluateString(scope, script, "<cmd>", 1, null);
        System.out.println("Execution finished.");
    }

    /**
     * Receives callbacks from peripherals and passes this back to the javaScript
     * @param cls - what kind of peripheral
     * @param json - details of the event.
     */
    public void onPeripheralEvent(String cls, JsonObject json) {
        mLog.log("onPeripheralEvent: " + cls + " : " + json);
    }
}
