package client;

public class ChessClient {
    private State state = State.PRELOGIN;
    private final ServerFacade server;
    private String authToken;

    public ChessClient(ServerFacade server) {
        this.server = server;
    }
}
