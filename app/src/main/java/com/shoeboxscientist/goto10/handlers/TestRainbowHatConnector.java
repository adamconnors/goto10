package com.shoeboxscientist.goto10.handlers;

import com.google.android.things.contrib.driver.button.Button;
import com.shoeboxscientist.goto10.CommandException;

import org.json.JSONException;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by adamconnors on 5/17/17.
 */

public class TestRainbowHatConnector extends AbstractRainbowHatConnector {

    @Override
    public void cleanup() throws IOException {
        System.out.println("cleanup");
    }

    @Override
    public void playNoise(int v) throws IOException {
        System.out.println("play noise: " + v);
    }

    @Override
    public void updateDisplay(String value) throws IOException {
        System.out.println("update display: " + value);
    }

    @Override
    public void updateLedStrip(int n) throws IOException {
        System.out.println("update LED strip: " + n);
    }

    @Override
    public void updateLedStrip(int[] colours) throws IOException {
        System.out.println("update LED strip: " + Arrays.toString(colours));
    }

    /**
     * Simulates a button press event.
     * @param id
     */
    public void fakeButtonPress(String id) {
        try {
            mPeripheralListener.onPeripheralEvent(PERIPHERAL_CLASS_ID, newButtonMsg(id, true));
            mPeripheralListener.onPeripheralEvent(PERIPHERAL_CLASS_ID, newButtonMsg(id, false));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CommandException e) {
            e.printStackTrace();
        }
    }
}
