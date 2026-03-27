package client;

import ui.ResponseException;
import ui.server.ServerFacade;
import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;
import server.Server;
import service.ClearService;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;
    private static final UserDao USER_DAO = new MySQLUserDataAccess();
    private static final AuthDao AUTH_DAO = new MySQLAuthDataAccess();
    private static final GameDao GAME_DAO = new MySQLGameDataAccess();
    private static final ClearService CLEAR_SERVICE = new ClearService(USER_DAO, AUTH_DAO, GAME_DAO);

    @BeforeAll
    public static void init() throws DataAccessException {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);


        CLEAR_SERVICE.clear();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    void registerPositive() throws Exception {
        UserData user = new UserData("player1", "pass1", "play@gmail.com");
        var authData = facade.register(user);
        assertNotNull(authData);
        assertTrue(authData.authToken().length() > 0);
    }

    @Test
    void registerNegativeDuplicate() throws ResponseException {
        // first registration
        UserData user = new UserData("player2", "pass2", "p2@gmail.com");
        facade.register(user);

        // second registration should fail
        assertThrows(ResponseException.class, () -> {
            facade.register(user);
        });
    }

    @Test
    void loginPositive() throws Exception {
        UserData user = new UserData("player3", "pass3", "p3@gmail.com");
        facade.register(user);
        var authData = facade.login(user);
        assertNotNull(authData);
        assertTrue(authData.authToken().length() > 0);
    }

    @Test
    void loginNegativeWrongPassword() throws Exception {
        UserData user = new UserData("player40", "pass40", "p40@gmail.com");
        AuthData auth = facade.register(user);
        UserData badUser = new UserData("player40", "wrongPass", "p40@email.com");
        assertThrows(ResponseException.class, () -> {
            facade.login(badUser);
        });
    }

    @Test
    void logoutPositive() throws Exception {
        UserData user = new UserData("player5", "pass5", "p5@gmail.com");
        var authData = facade.register(user);
        assertDoesNotThrow(() -> facade.logout(authData.authToken()));
    }

    @Test
    void logoutNegativeInvalidToken() {
        assertThrows(ResponseException.class, () -> {
            facade.logout("invalid-token");
        });
    }

    @Test
    void createGamePositive() throws Exception {
        UserData user = new UserData("player6", "pass6", "p6@gmail.com");
        var auth = facade.register(user);
        int gameID = facade.createGame(new GameData(0, null, null, "gameName", null), auth.authToken());
        assertTrue(gameID > 0);
    }

    @Test
    void createGameNegativeNoAuth() {
        assertThrows(ResponseException.class, () -> {
            facade.createGame(new GameData(0, null, null, "gameName", null), null);
        });
    }

    @Test
    void joinGamePositive() throws Exception {
        UserData user = new UserData("player7", "pass7", "p7@gmail.com");
        var auth = facade.register(user);
        int gameID = facade.createGame(new GameData(0, null, null, "gameName", null), auth.authToken());
        assertDoesNotThrow(() -> facade.joinGame("WHITE", auth.authToken(), gameID));
    }

    @Test
    void joinGameNegativeInvalidID() throws Exception {
        UserData user = new UserData("player8", "pass8", "p8@gmail.com");
        var auth = facade.register(user);
        int gameID = facade.createGame(new GameData(0, null, null, "gameName", null), auth.authToken());
        assertThrows(ResponseException.class, () -> {
            facade.joinGame("WHITE", auth.authToken(), 999999);
        });
    }

    @Test
    void listGamesPositive() throws Exception {
        UserData user = new UserData("player9", "pass9", "p9@gmail.com");
        var auth = facade.register(user);
        facade.createGame(new GameData(0, null, null, "gameName", null), auth.authToken());
        var games = facade.listGames(auth.authToken());
        assertNotNull(games);
        assertTrue(games.size() > 0);
    }

    @Test
    void listGamesNegativeNoAuth() {
        assertThrows(ResponseException.class, () -> {
            facade.listGames(null);
        });
    }
}