package client;

import client.server.ServerFacade;
import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;
import server.Server;
import service.ClearService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;
    private static final UserDao userDao = new MySQLUserDataAccess();
    private static final AuthDao authDao = new MySQLAuthDataAccess();
    private static final GameDao gameDao = new MySQLGameDataAccess();
    private static ClearService clearService = new ClearService(userDao, authDao, gameDao);

    @BeforeAll
    public static void init() throws DataAccessException {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);


        clearService.clear();
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
    void registerNegativeDuplicate() throws Exception {
        // first registration
        UserData user = new UserData("player2", "pass2", "p2@gmail.com");
        facade.register(user);

        // second registration should fail
        assertThrows(DataAccessException.class, () -> {
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
        assertThrows(DataAccessException.class, () -> {
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
        assertThrows(DataAccessException.class, () -> {
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
        assertThrows(DataAccessException.class, () -> {
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
        assertThrows(DataAccessException.class, () -> {
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
        assertThrows(DataAccessException.class, () -> {
            facade.listGames(null);
        });
    }
}