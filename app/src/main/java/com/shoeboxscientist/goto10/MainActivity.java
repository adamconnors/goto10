package com.shoeboxscientist.goto10;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PORT = 8080;
    private RainbowHatConnector mRainbowHat;
    private NanoHttpWebServer mServer;

    private NanoHttpWebServer.DataSource mSource;
    private NanoHttpWebServer.LoggingOutput mLog;
    private NanoHttpWebServer.CommandExecutor mExec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Started HelloRainbow");
        setContentView(R.layout.activity_main);

        // Set up the rainbow hat
        try {
            mRainbowHat = new RainbowHatConnector();
            mRainbowHat.init();
            mRainbowHat.updateDisplay("REDY");
            mRainbowHat.updateLedStrip(new int[] { Color.BLACK, Color.BLACK, Color.BLACK,
                    Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK});
        } catch (IOException e) {
            Log.e(TAG, "Couldn't connect rainbow hat.", e);
            finish();
            return;
        }

        mSource = new DataSource();
        mLog = new LoggingOutput();
        mExec = new CommandExecutor();

        mServer = new NanoHttpWebServer(PORT, mSource, mLog, mExec);

        try {
            mServer.start();
        } catch(IOException e) {
            Log.e(TAG, "Couldn't start Http Server", e);
            finish();
            return;
        }

        Log.d(TAG, "Http Server started and listening on port: " + mServer.getListeningPort());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRainbowHat.cleanup();

        if (mServer != null) {
            mServer.stop();
        }
    }

    private class DataSource implements NanoHttpWebServer.DataSource {
        @Override
        public InputStream getInputStreamForPath(String path) throws IOException {
            return getAssets().open("www" + path);
        }
    }

    private class LoggingOutput implements NanoHttpWebServer.LoggingOutput {
        @Override
        public void log(String text) {
            Log.d(TAG, text);
        }
    }

    private class CommandExecutor implements NanoHttpWebServer.CommandExecutor {

        @Override
        public void execute(String json) throws NanoHttpWebServer.CommandException {
            Log.d(TAG, "Executing json: " + json);

            JsonObject obj = new JsonParser().parse(json).getAsJsonObject();
            JsonElement cls = obj.get("class");
            JsonObject payload = obj.get("payload").getAsJsonObject();

            // TODO: Handle different payloads properly & import this into a class properly.
            String display = asString(payload.get("display"));
            String ledstrip = asString(payload.get("ledstrip"));

            try {
                if (display != null) {
                    mRainbowHat.updateDisplay(display);
                }

                if (ledstrip != null) {
                    mRainbowHat.updateLedStrip(Integer.parseInt(ledstrip));
                }
            } catch (IOException e) {
                Log.e(TAG, "Couldn't update hat state.", e);
            }
        }

        private String asString(JsonElement e) {
            return (e == null) ? null : e.getAsString();
        }
    }
}
