package client;

import java.util.Arrays;
import java.util.Scanner;

import client.server.ServerFacade;
import dataaccess.DataAccessException;
import model.*;

import static ui.EscapeSequences.*;

public class ChessClient {

    private final ServerFacade serverFacade;
    private State state = State.SIGNEDOUT;
    private String authToken = null;
    private String username = null;

    public ChessClient(String serverUrl) {
        serverFacade = new ServerFacade(serverUrl);
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
            String[] tokens = input.split(" ");
            String cmd = (tokens.length > 0) ? tokens[0].toLowerCase() : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);

            return switch (cmd) {
                case "register" -> register(params);
                case "login" -> login(params);
                case "logout" -> logout();
                case "quit" -> "quit";
                default -> help();
            };

        } catch (Exception ex) {
            return ex.getMessage();
        }
    }


    // -------- PRELOGIN COMMANDS ------------

    public String register(String... params) throws DataAccessException {
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
        throw new DataAccessException(400, "Expected: <username> <password> <email>");
    }

    public String login(String... params) throws DataAccessException {
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
        throw new DataAccessException(400, "Expected: <username> <password>");
    }


    // --------- POSTLOGIN COMMANDS ------------

    public String logout() throws DataAccessException {
        assertSignedIn();

        serverFacade.logout(authToken);
        state = State.SIGNEDOUT;
        authToken = null;
        username = null;

        return "Logged out";
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
                - logout
                - quit
                """;
    }

    private void assertSignedIn() throws DataAccessException {
        if (state == State.SIGNEDOUT) {
            throw new DataAccessException(401, "You must be logged in");
        }
    }
}
