package com.shoeboxscientist.goto10.AndroidInstanceClasses;

import android.content.Context;

import com.shoeboxscientist.goto10.ContentStore;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by adamconnors on 5/26/17.
 */

public class AndroidContentStore implements ContentStore {

    private static final String DEFAULT_FILE_NAME = "current_script.txt";
    private Context mCtx;

    public AndroidContentStore(Context ctx) {
        mCtx = ctx;
    }

    @Override
    public boolean savescript(String script) throws IOException {
        FileOutputStream outputStream;
        try {
            outputStream = mCtx.openFileOutput(DEFAULT_FILE_NAME, Context.MODE_PRIVATE);
            outputStream.write(script.getBytes());
            outputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String readscript() throws IOException {
        FileInputStream inputStream;
        StringBuilder s = new StringBuilder();
        try {
            inputStream = mCtx.openFileInput(DEFAULT_FILE_NAME);
            int i=0;
            while( (i=inputStream.read())!=-1) {
                s.append((char)i);
            }
            inputStream.close();
        } catch(Exception e){
            System.out.println(e);
            return null;
        }
        return s.toString();
    }
}
