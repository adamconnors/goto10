package com.shoeboxscientist.goto10;

import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import static com.shoeboxscientist.goto10.NanoHttpWebServer.LoggingOutput;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;

public class NanoHttpdWebSocketServer extends NanoWSD {

    private LoggingOutput mLog;
    private MessageHandler mExec;

    private static final String SUCCESS_PAYLOAD = "{ type:'msg' message: '$1' }";
    private static final String FAIL_PAYLOAD = "{ type:'msg' message: '$1' }";
    private CommandWebSocket mSocket;

    public NanoHttpdWebSocketServer(int port, NanoHttpWebServer.LoggingOutput log,
                                    MessageHandler exec) {
        super(port);
        mLog = log;
        mExec = exec;
    }

    /**
     * Send a payload to the client.
     */
    public void send(JsonObject payload) throws IOException, JSONException {
        mLog.log("Sending message: " + payload.toString());
        if (mSocket != null) {
            mSocket.send(payload.toString());
        }
    }

    @Override
    protected WebSocket openWebSocket(IHTTPSession handshake) {
        mSocket = new CommandWebSocket(handshake);
        return mSocket;
    }

    private class CommandWebSocket extends NanoWSD.WebSocket {

        public CommandWebSocket(NanoHTTPD.IHTTPSession handshakeRequest) {
            super(handshakeRequest);
        }

        @Override
        protected void onOpen() {
            mLog.log("Websocket opened,");
        }

        @Override
        protected void onClose(NanoWSD.WebSocketFrame.CloseCode code, String reason,
                               boolean initiatedByRemote) {
            mLog.log("Websocket closed: " + reason);
        }

        @Override
        protected void onMessage(NanoWSD.WebSocketFrame message) {
            try {
                mExec.onIncomingMessage(message.getTextPayload());
            } catch (CommandException e) {
                mLog.log("Failed to handle incoming message: " + e.getMessage());
            } catch (IOException e) {
                mLog.log("Failed to handle incoming message: " + e.getMessage());
            } catch (JSONException e) {
                mLog.log("Failed to handle incoming message: " + e.getMessage());
            }
        }

        @Override
        protected void onPong(NanoWSD.WebSocketFrame pong) {
        }

        @Override
        protected void onException(IOException exception) {
            exception.printStackTrace();
        }
    }
}