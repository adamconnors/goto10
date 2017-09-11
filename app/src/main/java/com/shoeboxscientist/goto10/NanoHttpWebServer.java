package com.shoeboxscientist.goto10;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class NanoHttpWebServer extends NanoHTTPD {
    private static final String TAG = "HttpSvr";

    private static final String MIME_HTML = "text/html";
    private static final String MIME_CSS = "text/css";

    private DataSource mSource;
    private LoggingOutput mLog;
    private MessageHandler mExec;

    public NanoHttpWebServer(int port, DataSource source, LoggingOutput log, MessageHandler exec) {
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

        String mime = MIME_HTML;
        if (path.endsWith("css")) {
            mime = MIME_CSS;
        }

        try {
            return newChunkedResponse(Response.Status.OK, mime,
                    mSource.getInputStreamForPath(path));
        } catch (IOException e) {
            mLog.log("Couldn't read file: " + e.getMessage());
            return newFixedLengthResponse("ERR:" + e.getMessage());
        }
    }

    public interface DataSource {
        InputStream getInputStreamForPath(String path) throws IOException;
        String getStringForPath(String path) throws IOException;
    }

    public static interface LoggingOutput {
        void log(String text);
    }
}
