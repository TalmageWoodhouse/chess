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
    private boolean inGame = true;

    public GamePlayUI(ChessGame game, ChessGame.TeamColor color, String authToken, int gameID) {
        this.currentGame = game;
        this.myColor = color;
        this.authToken = authToken;
        this.gameID = gameID;
    }

    public void setWebSocketFacade(WebSocketFacade ws) {
        this.webSocketFacade = ws;
    }

    public ChessGame getCurrentGame() {
        return currentGame;
    }

    public ChessGame.TeamColor getMyColor() {
        return myColor;
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
        ChessBoardUI.draw(currentGame, myColor, null);
    }

    private void leave() {
        try {
            webSocketFacade.leave(authToken, gameID);
            inGame = false;
            System.out.println("You left the game.");
        } catch (Exception e) {
            System.out.println("Error leaving game: " + e.getMessage());
        }
    }

    public boolean isInGame() {
        return inGame;
    }

    private void makeMove(String[] params) {
        if (params.length != 1) {
            System.out.println("Usage: move <e2e4>");
            return;
        }

        try {
            String moveStr = params[0].toLowerCase();
            int startCol = moveStr.charAt(0) - 'a' + 1;
            int startRow = moveStr.charAt(1) - '0';

            int endCol = moveStr.charAt(2) - 'a' + 1;
            int endRow = moveStr.charAt(3) - '0';

            ChessPosition start = new ChessPosition(startRow, startCol);
            ChessPosition end = new ChessPosition(endRow, endCol);

            ChessMove move = new ChessMove(start, end, null);

            if (currentGame.getTeamTurn() != myColor) {
                System.out.println("Not your turn!");
                return;
            }

            if (!currentGame.validMoves(move.getStartPosition()).contains(move)) {
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
            String input = params[0].toLowerCase();
            int col = input.charAt(0) - 'a' + 1;
            int row = input.charAt(1) - '0';

            ChessPosition position = new ChessPosition(row, col);
            Collection<ChessMove> moves = currentGame.validMoves(position);

            if (moves == null || moves.isEmpty()) {
                System.out.println("No legal moves for that piece.");
                return;
            }

            Set<ChessPosition> highlightPositions = new HashSet<>();
            for (ChessMove move : moves) {
                highlightPositions.add(move.getEndPosition());
            }

            ChessBoardUI.draw(currentGame, myColor, highlightPositions);

        } catch (Exception e) {
            System.out.println("Invalid square.");
        }
    }

    // ================= SERVER UPDATE =================

    public void updateGame(ChessGame updatedGame) {
        System.out.println("updateGame called");
        this.currentGame = updatedGame;
        ChessBoardUI.draw(currentGame, myColor, null);
    }

    @Override
    public void notify(String message) {
        Gson gson = new Gson();
        ServerMessage base = gson.fromJson(message, ServerMessage.class);

        if (base.getServerMessageType() == null) {
            System.out.println("Invalid message type");
            return;
        }

        switch (base.getServerMessageType()) {
            case LOAD_GAME -> {
                LoadGameMessage msg = gson.fromJson(message, LoadGameMessage.class);
                updateGame(msg.getGame());
            }
            case ERROR -> {
                ErrorMessage msg = gson.fromJson(message, ErrorMessage.class);
                System.out.println("Error: " + msg.getErrorMessage());
            }
            case NOTIFICATION -> {
                NotificationMessage msg = gson.fromJson(message, NotificationMessage.class);
                System.out.println(msg.getMessage());
            }
        }
    }
}
