package ui.websocket;

import chess.ChessMove;
import com.google.gson.Gson;
import ui.ResponseException;
import jakarta.websocket.*;
import websocket.commands.UserGameCommand;
import websocket.commands.MakeMoveCommand;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {

    private Session session;
    private final Gson gson = new Gson();

    public WebSocketFacade(String url, NotificationHandler notificationHandler) throws ResponseException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            // message handler
            this.session.addMessageHandler((MessageHandler.Whole<String>) notificationHandler::notify);

        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    // required override
    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;
    }

    // ================= SEND =================

    public void connect(String authToken, int gameID) throws ResponseException {
        try {
            var cmd = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
            send(cmd);
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void makeMove(String authToken, int gameID, ChessMove move) throws ResponseException {
        try {
            var cmd = new MakeMoveCommand(authToken, gameID, move);
            send(cmd);
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void resign(String authToken, int gameID) throws ResponseException {
        try {
            var cmd = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
            send(cmd);
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void leave(String authToken, int gameID) throws ResponseException {
        try {
            var cmd = new UserGameCommand(
                    UserGameCommand.CommandType.LEAVE,
                    authToken,
                    gameID
            );
            send(cmd);
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    private void send(Object command) throws IOException {
        session.getBasicRemote().sendText(gson.toJson(command));
    }
}