package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.websocket.*;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.jetbrains.annotations.NotNull;
import websocket.commands.*;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final Gson gson = new Gson();
    private final GameDao gameDAO;
    private final AuthDao authDao;
    private final Set<Integer> resignedGames = ConcurrentHashMap.newKeySet();


    public WebSocketHandler(GameDao gameDAO, AuthDao authDao) {
        this.gameDAO = gameDAO;
        this.authDao = authDao;
    }

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("WebSocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext ctx) {
        try {
            UserGameCommand command =
                    gson.fromJson(ctx.message(), UserGameCommand.class);

            switch (command.getCommandType()) {
                case CONNECT -> connect(command, ctx.session);
                case MAKE_MOVE -> {MakeMoveCommand moveCmd = gson.fromJson(ctx.message(),
                    MakeMoveCommand.class);
                    makeMove(moveCmd, ctx.session);
                }
                case LEAVE -> leave(command, ctx.session);
                case RESIGN -> resign(command, ctx.session);
            }

        } catch (Exception ex) {
            sendError(ctx.session, ex.getMessage());
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("WebSocket closed");
        connections.remove(ctx.session);
    }

    // ================= COMMAND HANDLERS =================

    private void connect(UserGameCommand cmd, Session session) throws IOException, DataAccessException {
        System.out.println("Server connect() called");
        // validate auth and game
        AuthData auth = validateAuth(cmd.getAuthToken());
        System.out.println("server auth ok");
        int gameID = cmd.getGameID();
        GameData gameData = validateGame(gameID);
        System.out.println("server game ok");
        // add connection and send game
        connections.add(gameID, session);
        System.out.println("server connection added");
        ChessGame game = gameData.game();
        System.out.println("server game object =" + game);
        // send game to this user
        var loadMsg = new LoadGameMessage(game);
        System.out.println("Server sending load_game");
        connections.send(session, loadMsg);
        // notify others
        String username = auth.username();
        String message = username + " connected to the game";
        var note = new NotificationMessage(message);
        connections.broadcast(gameID, session, note);
    }

    private void makeMove(MakeMoveCommand cmd, Session session) throws IOException, DataAccessException, InvalidMoveException {
        AuthData auth = validateAuth(cmd.getAuthToken());
        int gameID = cmd.getGameID();
        GameData gameData = validateGame(gameID);
        if (resignedGames.contains(gameID)) {
            throw new DataAccessException(400, "Error: game is over");
        }

        ChessMove move = cmd.getMove();
        ChessGame game = gameData.game();

        String username = auth.username();
        boolean isWhitePlayer = username.equals(gameData.whiteUsername());
        boolean isBlackPlayer = username.equals(gameData.blackUsername());

        if (!isWhitePlayer && !isBlackPlayer) {
            throw new DataAccessException(400, "Error: observers cannot make moves");
        }

        ChessGame.TeamColor playerColor = isWhitePlayer
                ? ChessGame.TeamColor.WHITE
                : ChessGame.TeamColor.BLACK;

        if (game.getTeamTurn() != playerColor) {
            throw new DataAccessException(400, "Error: not your turn");
        }

        ChessPiece piece = game.getBoard().getPiece(move.getStartPosition());
        if (piece == null || piece.getTeamColor() != playerColor) {
            throw new DataAccessException(400, "Error: cannot move opponent piece");
        }

        game.makeMove(move);
        gameDAO.updateGame(gameID, game);

        var loadMsg = new LoadGameMessage(game);
        connections.broadcast(gameID, null, loadMsg);

        var moveNote = new NotificationMessage(username + " made a move");
        connections.broadcast(gameID, session, moveNote);

        ChessGame.TeamColor currentTurn = game.getTeamTurn();

        if (game.isInCheckmate(currentTurn)) {
            var mateNote = new NotificationMessage(username + " delivered checkmate");
            connections.broadcast(gameID, null, mateNote);
        } else if (game.isInStalemate(currentTurn)) {
            var stalemateNote = new NotificationMessage("Game ended in stalemate");
            connections.broadcast(gameID, null, stalemateNote);
        } else if (game.isInCheck(currentTurn)) {
            var checkNote = new NotificationMessage(username + " put the opponent in check");
            connections.broadcast(gameID, null, checkNote);
        }
    }

    private void leave(UserGameCommand cmd, Session session) throws IOException, DataAccessException {
        AuthData auth = validateAuth(cmd.getAuthToken());

        int gameID = cmd.getGameID();
        GameData gameData = validateGame(gameID);

        String username = auth.username();

        boolean isWhitePlayer = username.equals(gameData.whiteUsername());
        boolean isBlackPlayer = username.equals(gameData.blackUsername());

        // remove websocket connection
        connections.remove(gameID, session);

        // if player, remove them from the game in DAO
        if (isWhitePlayer || isBlackPlayer) {
            String white = gameData.whiteUsername();
            String black = gameData.blackUsername();

            if (isWhitePlayer) {
                white = null;
            }
            if (isBlackPlayer) {
                black = null;
            }

            GameData updatedGame = new GameData(
                    gameData.gameID(),
                    white,
                    black,
                    gameData.gameName(),
                    gameData.game()
            );

            gameDAO.updateGameData(updatedGame);
        }

        var note = new NotificationMessage(username + " left the game");
        connections.broadcast(gameID, session, note);
    }

    private void resign(UserGameCommand cmd, Session session) throws IOException, DataAccessException {
        // validate auth and game
        AuthData auth = validateAuth(cmd.getAuthToken());
        int gameID = cmd.getGameID();
        GameData gameData = validateGame(gameID);

        String username = auth.username();
        boolean isWhitePlayer = username.equals(gameData.whiteUsername());
        boolean isBlackPlayer = username.equals(gameData.blackUsername());

        if (!isWhitePlayer && !isBlackPlayer) {
            throw new DataAccessException(400, "Error: observers cannot resign");
        }

        if (resignedGames.contains(gameID)) {
            throw new DataAccessException(400, "Error: game is over");
        }
        resignedGames.add(gameID);

        var note = new NotificationMessage(username + " resigned");
        connections.broadcast(gameID, null, note);
    }

    private void sendError(Session session, String message) {
        try {
            var errorMsg = new ErrorMessage(message);
            session.getRemote().sendString(gson.toJson(errorMsg));
        } catch (Exception ignored) {}
    }

    private AuthData validateAuth(String authToken) throws DataAccessException {
        AuthData auth = authDao.getAuthData(authToken);
        if (auth == null) {
            throw new DataAccessException(401, "Error: unauthorized");
        }
        return auth;
    }

    private GameData validateGame(int gameID) throws DataAccessException {
        GameData gameData = gameDAO.getGameData(gameID);
        if (gameData == null) {
            throw new DataAccessException(400, "Error: invalid game");
        }
        return gameData;
    }

    public void clearState() {
        resignedGames.clear();
    }
}