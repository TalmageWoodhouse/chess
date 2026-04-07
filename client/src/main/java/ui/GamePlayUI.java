package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import ui.websocket.NotificationHandler;
import ui.websocket.WebSocketFacade;
import websocket.messages.*;

import java.util.*;

public class GamePlayUI implements NotificationHandler {

    private ChessGame currentGame;
    private final ChessGame.TeamColor myColor;
    private WebSocketFacade webSocketFacade;
    private final String authToken;
    private final int gameID;

    public GamePlayUI(ChessGame game, ChessGame.TeamColor color, String authToken, int gameID) {
        this.currentGame = game;
        this.myColor = color;
        this.authToken = authToken;
        this.gameID = gameID;

        ChessBoardUI.draw(currentGame, myColor);
    }


    public void setWebSocketFacade(WebSocketFacade ws) {
        this.webSocketFacade = ws;
    }

    // ================= COMMAND HANDLER =================
    public void handleCommand(String input) {
        String[] parts = input.split(" ");
        String command = parts[0].toLowerCase();
        String[] params = Arrays.copyOfRange(parts, 1, parts.length);

        try {
            switch (command) {
                case "help" -> displayHelp();
                case "redraw" -> redrawBoard();
                case "leave" -> leave();
                case "move" -> makeMove(params);
                case "resign" -> resign();
                case "highlight" -> highlightLegalMoves(params);
                default -> System.out.println("Unknown command. Type 'help'.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ================= COMMANDS =================

    private void displayHelp() {
        System.out.println("Commands:");
        System.out.println("help - Show commands");
        System.out.println("redraw - Redraw the board");
        System.out.println("leave - Leave the game");
        System.out.println("move <from><to> - Make a move (e.g., e2e4)");
        System.out.println("resign - Resign the game");
        System.out.println("highlight <square> - Show legal moves (e.g., e2)");
    }

    private void redrawBoard() {
        ChessBoardUI.draw(currentGame, myColor);
    }

    private void leave() {
        try {
            webSocketFacade.leave(authToken, gameID);
            System.out.println("You left the game.");
        } catch (Exception e) {
            System.out.println("Error leaving game: " + e.getMessage());
        }
    }

    private void makeMove(String[] params) {
        if (params.length != 1) {
            System.out.println("Usage: move <e2e4>");
            return;
        }

        try {
            ChessMove move = ChessMove.fromString(params[0]);

            if (currentGame.getTeamTurn() != myColor) {
                System.out.println("Not your turn!");
                return;
            }

            if (!currentGame.validMoves(move.getStartPosition()).isEmpty()) {
                System.out.println("Illegal move!");
                return;
            }

            webSocketFacade.makeMove(authToken, gameID, move);

        } catch (Exception e) {
            System.out.println("Invalid move format.");
        }
    }

    private void resign() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Are you sure you want to resign? (yes/no): ");
        String response = scanner.nextLine();

        if (response.equalsIgnoreCase("yes")) {
            try {
                webSocketFacade.resign(authToken, gameID);
                System.out.println("You resigned the game.");
            } catch (Exception e) {
                System.out.println("Error resigning: " + e.getMessage());
            }
        } else {
            System.out.println("Resign cancelled.");
        }
    }

    private void highlightLegalMoves(String[] params) {
        if (params.length != 1) {
            System.out.println("Usage: highlight <square>");
            return;
        }

        try {
            ChessPosition position = ChessPosition.fromString(params[0]);
            Collection<ChessMove> moves = currentGame.validMoves(position);

            ChessBoardUI.draw(currentGame, myColor);
            ChessBoardUI.highlightSquares(moves);

        } catch (Exception e) {
            System.out.println("Invalid square.");
        }
    }

    // ================= SERVER UPDATE =================

    public void updateGame(ChessGame updatedGame) {
        this.currentGame = updatedGame;
        ChessBoardUI.draw(currentGame, myColor);
    }

    @Override
    public void notify(String message) {

        ServerMessage base = new Gson().fromJson(message, ServerMessage.class);

        switch (base.getServerMessageType()) {

            case LOAD_GAME -> {
                LoadGameMessage msg = new Gson().fromJson(message, LoadGameMessage.class);
                updateGame(msg.getGame());
            }

            case ERROR -> {
                ErrorMessage msg = new Gson().fromJson(message, ErrorMessage.class);
                System.out.println("Error: " + msg.getMessage());
            }

            case NOTIFICATION -> {
                NotificationMessage msg = new Gson().fromJson(message, NotificationMessage.class);
                System.out.println(msg.getMessage());
            }
        }
    }
}
