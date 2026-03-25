package client;

import java.util.Arrays;
import java.util.Scanner;

import exception.ResponseException;
import model.*;
import server.ServerFacade;

import static client.EscapeSequences.*;

public class ChessClient {

    private final ServerFacade server;
    private State state = State.SIGNEDOUT;
    private String authToken = null;
    private String username = null;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
    }

    public void run() {
        System.out.println("Welcome to Chess. Sign in to start.");
        System.out.print(help());

        Scanner scanner = new Scanner(System.in);
        var result = "";

        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = eval(line);
                System.out.print(BLUE + result);
            } catch (Throwable e) {
                System.out.print(e.getMessage());
            }
        }
        System.out.println();
    }

    private void printPrompt() {
        System.out.print("\n" + RESET + ">>> " + GREEN);
    }

    public String eval(String input) {
        try {
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
                case "quit" -> "quit";
                default -> help();
            };

        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    // =========================
    // PRELOGIN COMMANDS
    // =========================

    public String register(String... params) throws ResponseException {
        if (params.length == 3) {
            String username = params[0];
            String password = params[1];
            String email = params[2];

            AuthData auth = server.register(username, password, email);

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

            AuthData auth = server.login(username, password);

            this.authToken = auth.authToken();
            this.username = auth.username();
            state = State.SIGNEDIN;

            return String.format("Logged in as %s", username);
        }
        throw new ResponseException(400, "Expected: <username> <password>");
    }

    // =========================
    // POSTLOGIN COMMANDS
    // =========================

    public String logout() throws ResponseException {
        assertSignedIn();

        server.logout(authToken);
        state = State.SIGNEDOUT;
        authToken = null;
        username = null;

        return "Logged out";
    }

    public String listGames() throws ResponseException {
        assertSignedIn();

        var games = server.listGames(authToken);

        StringBuilder result = new StringBuilder();
        for (GameData game : games) {
            result.append(String.format("ID: %d | Name: %s%n",
                    game.gameID(), game.gameName()));
        }

        return result.toString();
    }

    public String createGame(String... params) throws ResponseException {
        assertSignedIn();

        if (params.length == 1) {
            String gameName = params[0];

            int gameID = server.createGame(authToken, gameName);

            return String.format("Created game '%s' (ID: %d)", gameName, gameID);
        }
        throw new ResponseException(400, "Expected: <game name>");
    }

    public String joinGame(String... params) throws ResponseException {
        assertSignedIn();

        if (params.length == 2) {
            int gameID = Integer.parseInt(params[0]);
            String color = params[1].toUpperCase();

            server.joinGame(authToken, gameID, color);

            return String.format("Joined game %d as %s", gameID, color);
        }
        throw new ResponseException(400, "Expected: <gameID> <WHITE|BLACK>");
    }

    // =========================
    // HELP
    // =========================

    public String help() {
        if (state == State.SIGNEDOUT) {
            return """
                    - register <username> <password> <email>
                    - login <username> <password>
                    - quit
                    """;
        }

        return """
                - list
                - create <game name>
                - join <gameID> <WHITE|BLACK>
                - logout
                - quit
                """;
    }

    private void assertSignedIn() throws ResponseException {
        if (state == State.SIGNEDOUT) {
            throw new ResponseException(401, "You must be logged in");
        }
    }
}
