package com.shoeboxscientist.goto10.LocalInstanceClassesForTesting;

import com.shoeboxscientist.goto10.NanoHttpWebServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by adamconnors on 5/26/17.
 */

public class LocalDataSource implements NanoHttpWebServer.DataSource {

    private static final String PATH_TO_WWW = "/app/src/main/assets/www";
    private static final File WORKING_DIR = new File(System.getProperty("user.dir") + PATH_TO_WWW);

    @Override
    public InputStream getInputStreamForPath(String path) {
        String fileToServe = WORKING_DIR + path;
        System.out.println("Loading content from: " + fileToServe);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(fileToServe);
            return fis;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
