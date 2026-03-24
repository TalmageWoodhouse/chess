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


}
