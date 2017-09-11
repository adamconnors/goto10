package com.shoeboxscientist.goto10.handlers;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.things.contrib.driver.apa102.Apa102;
import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay;
import com.google.android.things.contrib.driver.pwmspeaker.Speaker;
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat;
import com.google.android.things.pio.PeripheralManagerService;
import com.shoeboxscientist.goto10.CommandException;

import org.json.JSONException;

import java.io.IOException;

public class RainbowHatConnector extends AbstractRainbowHatConnector {


    private RainbowHat mHat = new RainbowHat();
    private Button mButtonA;
    private Button mButtonB;
    private Button mButtonC;

    private int[] mRainbow = new int[7];

    private static final int BRIGHTNESS = 1;

    public RainbowHatConnector() throws IOException {

        // Set up the rainbow colours
        for (int i = 0; i < mRainbow.length; i++) {
            float[] hsv = {i * 360.f / mRainbow.length, 1.0f, 1.0f};
            mRainbow[i] = Color.HSVToColor(255, hsv);
        }

        // Connect buttons.
        initHat();
    }

    private void initHat() throws IOException {
        // Buttons
        mButtonA = connectButton(RainbowHat.BUTTON_A);
        mButtonB = connectButton(RainbowHat.BUTTON_B);
        mButtonC = connectButton(RainbowHat.BUTTON_C);
    }

    private Button connectButton(final String id) throws IOException {
        Button b = RainbowHat.openButton(id);
        b.setOnButtonEventListener(new Button.OnButtonEventListener() {
            @Override
            public void onButtonEvent(Button button, boolean pressed) {
                SendTask task = new SendTask(id, pressed);
                task.execute();
            }
        });
        return b;
    }

    @Override
    public void cleanup() throws IOException {
        // Don't think clean up is needed now I open/close for each call.
    }

    public synchronized void playNoise(int v) throws IOException {
        Speaker speaker = RainbowHat.openPiezo();
        speaker.play(440);
        speaker.close();
    }

    public synchronized void updateDisplay(String value) throws IOException {
        AlphanumericDisplay display = mHat.openDisplay();
        display.display(value);
        display.setEnabled(true);
        display.close();
    }

    public synchronized void updateLedStrip(int n) throws IOException {
        updateLedStrip(getColours(n));
    }

    public synchronized void updateLedStrip(int[] colours) throws IOException {
        try {
            Apa102 ledstrip = RainbowHat.openLedStrip();
            ledstrip.setBrightness(BRIGHTNESS);
            ledstrip.write(colours);
            // Do It Twice since something w/ Android Things does not work consistently.
            ledstrip.write(colours);
            ledstrip.close();
        } catch (IOException e) {
            throw new RuntimeException("Error initializing apa102 rainbow", e);
        }
    }

    // Converts the val into a position on the rainbow. The colors array is just to build up the
    // list of pretty colors.
    private int[] getColours(int n) {
        n = Math.max(0, Math.min(n, mRainbow.length));
        int[] colors = new int[mRainbow.length];
        for (int i = 0; i < n; i++) {
            int ri = mRainbow.length - 1 - i;
            colors[ri] = mRainbow[ri];
        }
        return colors;
    }

    private class SendTask extends AsyncTask<Void, Void, Void> {

        String mId;
        boolean mPressed;

        public SendTask(String id, boolean pressed) {
            mId = id;
            mPressed = pressed;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                mPeripheralListener.onPeripheralEvent(PERIPHERAL_CLASS_ID,
                        newButtonMsg(mId, mPressed));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (CommandException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void res) {
        }
    }
}
