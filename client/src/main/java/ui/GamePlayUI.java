package ui;

import chess.ChessGame;
import chess.ChessMove;
import ui.server.ServerFacade;
import ui.websocket.WebSocketFacade;

import java.util.*;

public class GameplayUI {

    private final ChessBoardUI boardUI;
    private ChessGame currentGame;
    private final ChessGame.TeamColor myColor;
    private WebSocketFacade webSocketFacade;
    private final String authToken;

    public GameplayUI(ChessGame game, ChessGame.TeamColor color,
                      ServerFacade serverFacade, String authToken) {
        this.currentGame = game;
        this.myColor = color;
        this.webSocketFacade = webSocketFacade;
        this.authToken = authToken;
        this.boardUI = new ChessBoardUI();

        boardUI.draw(currentGame, myColor);
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
        boardUI.draw(currentGame, myColor);
    }

    private void leave() {
        try {
            webSocketFacade.leave(authToken, currentGame.getGameID());
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

            if (!currentGame.isMyTurn(myColor)) {
                System.out.println("Not your turn!");
                return;
            }

            if (!currentGame.isLegalMove(move)) {
                System.out.println("Illegal move!");
                return;
            }

            currentGame.makeMove(move);
            boardUI.draw(currentGame, myColor);

            webSocketFacade.makeMove(authToken, currentGame.getGameID(), move);

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
                webSocketFacade.resign(authToken, currentGame.getGameID());
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
            Square square = Square.fromString(params[0]);
            Set<Square> moves = currentGame.getLegalMoves(square);

            boardUI.draw(currentGame, myColor);
            boardUI.highlightSquares(moves);

        } catch (Exception e) {
            System.out.println("Invalid square.");
        }
    }

    // ================= SERVER UPDATE =================

    public void updateGame(ChessGame updatedGame) {
        this.currentGame = updatedGame;
        boardUI.draw(currentGame, myColor);
    }
}
