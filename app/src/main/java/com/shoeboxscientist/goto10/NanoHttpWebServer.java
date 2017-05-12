package com.shoeboxscientist.goto10;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import fi.iki.elonen.NanoHTTPD;

public class NanoHttpWebServer extends NanoHTTPD {
    private static final String TAG = "HttpSvr";

    private static final String MIME_HTML = "text/html";
    private static final String MIME_CSS = "text/css";

    private DataSource mSource;
    private LoggingOutput mLog;
    private CommandExecutor mExec;

    public NanoHttpWebServer(int port, DataSource source, LoggingOutput log, CommandExecutor exec) {
        super(port);
        mSource = source;
        mLog = log;
        mExec = exec;
    }

    private int readContentLength(Map<String, String> headers) {
        String cl = headers.get("content-length");
        if (cl == null) {
            return 0;
        }
        return Integer.parseInt(cl);
    }

    private byte[] readData(InputStream in, int length) throws IOException {
        int nRead;
        byte[] data = new byte[length];

        try {
            while ((nRead = in.read(data, 0, data.length)) != -1 && nRead < length);
            return data;
        } finally {
            in.close();
        }
    }

    public String parsePost(InputStream in, int length) throws IOException, ResponseException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }

        byte[] outputBytes = outputStream.toByteArray();
        return new String(outputBytes);
    }

    public String parsePost(IHTTPSession session) throws IOException, ResponseException {
        Map<String, String> files = new HashMap<String, String>();
        Method method = session.getMethod();
        if (Method.PUT.equals(method) || Method.POST.equals(method)) {
            session.parseBody(files);
        }
        return files.get("postData");
    }

    @Override
    public Response serve(IHTTPSession session) {

        String path = session.getUri();

        // Default to index.html
        if (path == null || path.equals("/")) {
            path = "/index.html";
        }

        // Intercept commands and execute them.
        if (path.equals("/command")) {
            try {
                String cmd = parsePost(session);
                mExec.execute(cmd);
                return new Response(Response.Status.OK, MIME_HTML, "OK");
            } catch (IOException e) {
                return new Response(Response.Status.OK, MIME_HTML, "ERR:" + e.getMessage());
            } catch (ResponseException re) {
                return new Response(Response.Status.OK, MIME_HTML, "ERR:" + re.getMessage());
            } catch (CommandException ce) {
                return new Response(Response.Status.OK, MIME_HTML, "ERR:" + ce.getMessage());
            }
        }

        String mime = MIME_HTML;
        if (path.endsWith("css")) {
            mime = MIME_CSS;
        }

        try {
            return new NanoHTTPD.Response(Response.Status.OK, mime,
                    mSource.getInputStreamForPath(path));
        } catch (IOException e) {
            mLog.log("Couldn't read file: " + e.getMessage());
            return new Response(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "ERR:" + e.getMessage());
        }
    }

    public static interface DataSource {
        InputStream getInputStreamForPath(String path) throws IOException;
    }

    public static interface LoggingOutput {
        void log(String text);
    }

    public static interface CommandExecutor {
        void execute(String json) throws CommandException;
    }

    public static class CommandException extends Exception {
    }

}
