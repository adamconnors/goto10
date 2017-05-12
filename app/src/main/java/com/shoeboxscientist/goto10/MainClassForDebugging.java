package com.shoeboxscientist.goto10;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import fi.iki.elonen.ServerRunner;

/**
 * A regular java entrypoint. Run this as a regular app to test locally.
 */

public class MainClassForDebugging {

    private static final String PATH_TO_WWW = "/app/src/main/assets/www";
    private static final File WORKING_DIR = new File(System.getProperty("user.dir") + PATH_TO_WWW);

    public static void main(String[] args) {
        System.out.print("Starting server...");

        LocalDataSource source = new LocalDataSource();
        LoggingOutput log = new LoggingOutput();
        DummyCommandExecutor exec = new DummyCommandExecutor();

        NanoHttpWebServer server = new NanoHttpWebServer(8080, source, log, exec);
        ServerRunner.executeInstance(server);
        System.out.println("Started!");
    }

    private static class LocalDataSource implements NanoHttpWebServer.DataSource {
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

    private static class LoggingOutput implements NanoHttpWebServer.LoggingOutput {
        @Override
        public void log(String text) {
            System.out.println(text);
        }
    }

    private static class DummyCommandExecutor implements NanoHttpWebServer.CommandExecutor {
        @Override
        public void execute(String json) {
            System.out.println("EXECUTING JSON MESSAGE: " + json);
            JsonObject obj = new JsonParser().parse(json).getAsJsonObject();
            JsonElement cls = obj.get("class");
            JsonElement payload = obj.get("payload");
            System.out.println("cls: " + cls.getAsString());
        }
    }
}

