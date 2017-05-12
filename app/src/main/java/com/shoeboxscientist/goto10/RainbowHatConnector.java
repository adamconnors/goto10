package com.shoeboxscientist.goto10;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import com.google.android.things.contrib.driver.apa102.Apa102;
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay;
import com.google.android.things.contrib.driver.pwmspeaker.Speaker;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

public class RainbowHatConnector {

    private static final String TAG = "RainbowHat";

    private AlphanumericDisplay mDisplay;
    private Apa102 mLedstrip;
    private Gpio mLed;
    private int[] mRainbow = new int[7];

    private Speaker mSpeaker;
    private ValueAnimator mSpeakerAnimator;

    private static final int BRIGHTNESS = 1;

    public void init() throws IOException {

        // Set up the rainbow colours
        for (int i = 0; i < mRainbow.length; i++) {
            float[] hsv = {i * 360.f / mRainbow.length, 1.0f, 1.0f};
            mRainbow[i] = Color.HSVToColor(255, hsv);
        }


        // Connect to the peripherals.
        // TODO: Use proper RainbowHat peripheral.
        try {
            mDisplay = new AlphanumericDisplay(BoardDefaults.getI2cBus());
            mDisplay.setEnabled(true);
            mDisplay.setBrightness(BRIGHTNESS);
            mDisplay.clear();
            Log.d(TAG, "Initialized I2C Display");
        } catch (IOException e) {
            Log.e(TAG, "Error initializing display", e);
            Log.d(TAG, "Display disabled");
            mDisplay = null;
        }

        // SPI ledstrip
        mLedstrip = new Apa102(BoardDefaults.getSpiBus(), Apa102.Mode.BGR);
        mLedstrip.setBrightness(BRIGHTNESS);

        // GPIO led
        PeripheralManagerService pioService = new PeripheralManagerService();
        mLed = pioService.openGpio(BoardDefaults.getLedGpioPin());
        mLed.setEdgeTriggerType(Gpio.EDGE_NONE);
        mLed.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        mLed.setActiveType(Gpio.ACTIVE_HIGH);

        // PWM speaker
        mSpeaker = new Speaker(BoardDefaults.getSpeakerPwmPin());
    }

    public void cleanup() {
        if (mDisplay != null) {
            try {
                Log.d(TAG, "Closing display peripheral.");
                mDisplay.clear();
                mDisplay.setEnabled(false);
                mDisplay.close();
            } catch (IOException e) {
                Log.e(TAG, "Error disabling display", e);
            } finally {
                mDisplay = null;
            }
        }

        if (mLedstrip != null) {
            try {
                Log.d(TAG, "Closing led strip peripheral.");
                mLedstrip.write(new int[7]);
                mLedstrip.setBrightness(0);
                mLedstrip.close();
            } catch (IOException e) {
                Log.e(TAG, "Error disabling ledstrip", e);
            } finally {
                mLedstrip = null;
            }
        }

        if (mLed != null) {
            try {
                Log.d(TAG, "Closing GPIO peripheral.");
                mLed.setValue(false);
                mLed.close();
            } catch (IOException e) {
                Log.e(TAG, "Error disabling led", e);
            } finally {
                mLed = null;
            }
        }
    }

    public synchronized void playNoise(int v) throws IOException {
        mSpeaker.play(v);
    }

    public synchronized void updateDisplay(String value) throws IOException {
        mDisplay.display(value);
    }

    public void updateLedStrip(int n) throws IOException {
        updateLedStrip(getColours(n));
    }

    public synchronized void updateLedStrip(int[] colours) throws IOException {
        mLedstrip.write(colours);
    }

    public void setLED(boolean on) throws IOException {
        mLed.setValue(on);
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
}
