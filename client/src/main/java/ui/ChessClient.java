package ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import chess.ChessBoard;
import chess.ChessGame;
import ui.server.ServerFacade;
import ui.ResponseException;
import model.*;
import ui.websocket.WebSocketFacade;

import static ui.EscapeSequences.*;

public class ChessClient {

    private final ServerFacade serverFacade;
    private State state = State.SIGNEDOUT;
    private String authToken = null;
    private String username = null;
    private List<GameData> games = new ArrayList<>();
    private final String serverUrl;
    private GamePlayUI gamePlayUI;
    private WebSocketFacade webSocketFacade;

    public ChessClient(String serverUrl) {
        this.serverUrl = serverUrl;
        this.serverFacade = new ServerFacade(serverUrl);
    }

    public void run() {
        System.out.println("Welcome to 240 Chess. Type help to get started.");

        Scanner scanner = new Scanner(System.in);
        var result = "";

        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = eval(line);
                System.out.print(SET_TEXT_COLOR_BLUE + result);
            } catch (Throwable e) {
                System.out.print(e.getMessage());
            }
        }
        System.out.println();
    }

    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + RESET_BG_COLOR + ">>> " + SET_TEXT_COLOR_GREEN);
    }

    public String eval(String input) {
        try {
            if (state == State.INGAME) {
                gamePlayUI.handleCommand(input);
                return "";
            }

            String[] tokens = input.split(" ");
            String cmd = (tokens.length > 0) ? tokens[0].toLowerCase() : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);

            return switch (cmd) {
                case "register" -> register(params);
                case "login" -> login(params);
                case "logout" -> logout();
                case "list" -> listGames();
                case "create" -> createGame(params);
                case "join" -> joinGame(params);
                case "observe" -> observeGame(params);
                case "quit" -> "quit";
                default -> help();
            };

        } catch (Exception ex) {
            return ex.getMessage();
        }
    }


    // -------- PRELOGIN COMMANDS ------------

    public String register(String... params) throws ResponseException {
        if (params.length == 3) {
            String username = params[0];
            String password = params[1];
            String email = params[2];
            UserData user = new UserData(username, password, email);

            AuthData auth = serverFacade.register(user);

            this.authToken = auth.authToken();
            this.username = auth.username();
            state = State.SIGNEDIN;

            return String.format("Registered and logged in as %s", username);
        }
        throw new ResponseException(400, "Expected: <username> <password> <email>");
    }

    public String login(String... params) throws ResponseException {
        if (params.length == 2) {
            String username = params[0];
            String password = params[1];
            UserData user = new UserData(username, password, null);

            AuthData auth = serverFacade.login(user);

            this.authToken = auth.authToken();
            this.username = auth.username();
            state = State.SIGNEDIN;

            return String.format("Logged in as %s", username);
        }
        throw new ResponseException(400, "Expected: <username> <password>");
    }


    // --------- POSTLOGIN COMMANDS ------------

    public String logout() throws ResponseException {
        assertSignedIn();

        serverFacade.logout(authToken);
        state = State.SIGNEDOUT;
        authToken = null;
        username = null;

        return "Logged out";
    }

    public String createGame(String... params) throws ResponseException {
        assertSignedIn();

        if (params.length == 1) {
            String gameName = params[0];
            GameData game = new GameData(0, null, null, gameName, new ChessGame());

            serverFacade.createGame(game, authToken);

            return String.format("Created game '%s'", gameName);
        }
        throw new ResponseException(400, "Expected: <game name>");
    }

    public String listGames() throws ResponseException {
        assertSignedIn();

        games = serverFacade.listGames(authToken);

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < games.size(); i++) {
            GameData game = games.get(i);

            result.append(String.format(
                    "%d: Name: %s | White: %s | Black: %s%n",
                    i + 1,
                    game.gameName(),
                    game.whiteUsername(),
                    game.blackUsername()
            ));
        }
        return result.toString();
    }

    public String joinGame(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length != 2) {
            throw new ResponseException(400, "Expected: <gameNumber> <WHITE|BLACK>");
        }
        // Parse inputs
        int gameNumber;
        try {
            gameNumber = Integer.parseInt(params[0]);
        } catch (NumberFormatException ex) {
            return "Error: gameNumber must be a number";
        }

        String playerColor = params[1].toUpperCase();
        int gameID;
        try {
            gameID = getGameIDFromSelection(gameNumber);
            serverFacade.joinGame(playerColor, authToken, gameID);
        } catch (ResponseException ex) {
            return "Error: Invalid Game";
        }
        // create gameplay UI
        gamePlayUI = new GamePlayUI(
                new ChessGame(), // temporary placeholder
                ChessGame.TeamColor.valueOf(playerColor),
                authToken,
                gameID
        );
        // Create Websocket
        try {
            webSocketFacade = new WebSocketFacade(serverUrl, gamePlayUI);
            gamePlayUI.setWebSocketFacade(webSocketFacade);
            webSocketFacade.connect(authToken, gameID);
        } catch (Exception e) {
            return "Error connecting to the game server: " + e.getMessage();
        }

        state = State.INGAME;
        return String.format("Joined game %d as %s", gameNumber, playerColor);
    }

    public int getGameIDFromSelection(int selection) throws ResponseException {
        if (selection < 1 || selection > games.size()) {
            throw new ResponseException(400, "Invalid selection. Use 'list' to see available games.");
        }
        return games.get(selection - 1).gameID();
    }

    public String observeGame(String... params) throws ResponseException {
        assertSignedIn();

        if (params.length != 1) {
            throw new ResponseException(400, "Expected: <gameNumber>");
        }

        // parse input
        int gameNumber;
        try {
            gameNumber = Integer.parseInt(params[0]);
        } catch (NumberFormatException ex) {
            return "Error: gameNumber must be a number";
        }
        // get gameID
        int gameID;
        try {
            gameID = getGameIDFromSelection(gameNumber);
        } catch (Exception e) {
            return "Error: Invalid Game";
        }

        gamePlayUI = new GamePlayUI(new ChessGame(), null, authToken, gameID);

        try {
            webSocketFacade = new WebSocketFacade(serverUrl, gamePlayUI);
            gamePlayUI.setWebSocketFacade(webSocketFacade);
            webSocketFacade.connect(authToken, gameID);
        } catch (Exception e) {
            return "Error connecting to game server: " + e.getMessage();
        }

        ChessBoardUI.draw(gamePlayUI.getCurrentGame(),null, null);

        state = State.INGAME;
        return String.format("Observing game %d", gameNumber);
    }

    // --------- HELP --------------

    public String help() {
        if (state == State.SIGNEDOUT) {
            return """
                    - register <username> <password> <email>
                    - login <username> <password>
                    - quit
                    - help
                    """;
        }

        return """
                - list - games
                - create <game name>
                - join <gameID> <WHITE|BLACK>
                - observe <gameID>
                - logout
                - quit
                - help
                """;
    }

    private void assertSignedIn() throws ResponseException {
        if (state == State.SIGNEDOUT) {
            throw new ResponseException(401, "You must be logged in");
        }
    }
}
