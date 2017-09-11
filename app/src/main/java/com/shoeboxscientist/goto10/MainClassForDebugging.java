package com.shoeboxscientist.goto10;

import com.shoeboxscientist.goto10.LocalInstanceClassesForTesting.LocalContentStore;
import com.shoeboxscientist.goto10.LocalInstanceClassesForTesting.LocalDataSource;
import com.shoeboxscientist.goto10.handlers.RainbowHatConnector;
import com.shoeboxscientist.goto10.handlers.TestRainbowHatConnector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import fi.iki.elonen.NanoHTTPD;

/**
 * A regular java entrypoint. Run this as a regular app to test locally.
 */

public class MainClassForDebugging {

    public static void main(String[] args) {
        System.out.print("Starting server...");

        LocalDataSource source = new LocalDataSource();
        LocalContentStore store = new LocalContentStore();
        LoggingOutput log = new LoggingOutput();
        TestRainbowHatConnector rh = new TestRainbowHatConnector();

        MessageHandler exec = new MessageHandler(source, store, rh, log);

        // Starts web socket server.
        NanoHttpdWebSocketServer socketServer = new NanoHttpdWebSocketServer(8081, log, exec);

        // Passes socket server back to executor so it can send messages back.
        // TODO: Use some, um, dependency injection, I guess.
        exec.setSocketServer(socketServer);

        try {
            socketServer.start(0);
        } catch (IOException e) {
            System.out.println("Socketserver didn't start");
            e.printStackTrace();
            System.exit(-1);
        }

        // Starts web server and waits for user input.
        NanoHttpWebServer server = new NanoHttpWebServer(8080, source, log, exec);
        try {
            server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        } catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
            System.exit(-1);
        }

        System.out.println("Server started, Hit Enter to stop.\n");

        try {
            String line;
            do {
                InputStreamReader r = new InputStreamReader(System.in);
                BufferedReader br = new BufferedReader(r);
                line = br.readLine();
                System.out.println("CH: " + line);
                if ("a".equals(line) || "b".equals(line) || "c".equals(line)) {
                    rh.fakeButtonPress(line);
                }
            } while (line.length() > 0);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        server.stop();
        System.out.println("Server stopped.\n");
    }


    private static class LoggingOutput implements NanoHttpWebServer.LoggingOutput {
        @Override
        public void log(String text) {
            System.out.println(text);
        }
    }

}


