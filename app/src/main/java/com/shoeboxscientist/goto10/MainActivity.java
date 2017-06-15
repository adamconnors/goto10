package com.shoeboxscientist.goto10;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat;
import com.shoeboxscientist.goto10.AndroidInstanceClasses.AndroidContentStore;
import com.shoeboxscientist.goto10.handlers.RainbowHatConnector;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PORT = 8080;
    private RainbowHatConnector mRainbowHat;
    private NanoHttpWebServer mServer;
    private NanoHttpdWebSocketServer mSocketServer;

    private NanoHttpWebServer.DataSource mSource;
    private NanoHttpWebServer.LoggingOutput mLog;
    private MessageHandler mExec;
    private ContentStore mContentStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Started HelloRainbow");
        setContentView(R.layout.activity_main);

        // Set up the rainbow hat
        try {
            mRainbowHat = new RainbowHatConnector();
            mContentStore = new AndroidContentStore();
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
        mExec = new MessageHandler(mContentStore, mRainbowHat);

        mServer = new NanoHttpWebServer(PORT, mSource, mLog, mExec);
        mSocketServer = new NanoHttpdWebSocketServer(8081, mLog, mExec);
        mExec.setSocketServer(mSocketServer);

        try {
            mSocketServer.start(0);
            mServer.start();
        } catch(IOException e) {
            Log.e(TAG, "Couldn't start servers: ", e);
            finish();
            return;
        }

        Log.d(TAG, "Http Server started and listening on port: " + mServer.getListeningPort());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            mRainbowHat.cleanup();
        } catch (IOException e) {
            Log.e(TAG, "Couldn't clean up RainbowHat", e);
        }

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
}
