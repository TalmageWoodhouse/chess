package service;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.Data;
import java.util.List;

public class GameServiceTests {

    private final UserDao userDao = new MemoryUserDataAccess();
    private final AuthDao authDao = new MemoryAuthDataAccess();
    private final GameDao gameDao = new MemoryGameDataAccess();
    private final ClearService clearService = new ClearService(userDao, authDao, gameDao);
    private final GameService gameService = new GameService(gameDao, authDao);

    @BeforeEach
    public void setup() { clearService.clear(); }

    @Test
    void createGameSuccess() throws DataAccessException {
        UserData newUser = new UserData("testUser", "password", "test@email.com");
        userDao.addUser(newUser);
        AuthData auth = authDao.createAuthData(newUser.username());

        GameData newGame = new GameData(0, null, null, "gameName", null);

        int gameID = gameService.createGame(newGame, auth.authToken());

        Assertions.assertEquals(1, gameID);
    }

    @Test
    void createGameFailure() throws DataAccessException {
        // Create and store an invalid auth token
        UserData newUser = new UserData("testUser", "password123", "test@email.com");
        userDao.addUser(newUser);
        String invalidAuthToken = "invalidToken123";

        // Simulate game creation returning a gameID
        GameData newGame = new GameData(0, null, null, null, null);

        DataAccessException thrown = Assertions.assertThrows(DataAccessException.class, () -> {
            gameService.createGame(newGame, invalidAuthToken);
        });

        Assertions.assertEquals(401, thrown.getStatusCode());
        Assertions.assertEquals("Error: unauthorized", thrown.getMessage());
    }

    @Test
    void JoinGameSuccess() throws DataAccessException {
        UserData newUser = new UserData("testUser", "password", "test@email.com");
        userDao.addUser(newUser);
        AuthData auth = authDao.createAuthData(newUser.username());

        // create the game and store the gameID
        GameData newGame = new GameData(0, null, null, "gameName", null);
        int gameID = gameService.createGame(newGame, auth.authToken());

        // join game
        gameDao.joinGame("BLACK", gameID, auth.username());
        GameData gameData = gameDao.getGameData(gameID);

        //  Check if blackUser name was input meaning game was joined
        Assertions.assertNotNull(gameData.blackUsername());
    }

    @Test
    void joinGameFailure() throws DataAccessException {
        // Create and store a valid auth token
        UserData newUser = new UserData("testUser", "password123", "test@email.com");
        userDao.addUser(newUser);
        AuthData auth = authDao.createAuthData(newUser.username());

        // create the game and store the gameID
        GameData newGame = new GameData(0, null, null, "gameName", null);
        int gameID = gameService.createGame(newGame, auth.authToken());

        // try to join game and throw error
        DataAccessException thrown = Assertions.assertThrows(DataAccessException.class, () -> {
            gameService.joinGame("BLack", auth.authToken(), gameID);
        });

        // Check if error was thrown for bad casing
        Assertions.assertEquals(400, thrown.getStatusCode());
        Assertions.assertEquals("Error: bad request", thrown.getMessage());
    }

    @Test
    void listGamesSuccess() throws DataAccessException {
        // Create and store a valid auth token
        UserData newUser = new UserData("testUser", "password123", "test@email.com");
        userDao.addUser(newUser);
        AuthData auth = authDao.createAuthData(newUser.username());

        // Create two games and store them
        GameData game1 = new GameData(0, null, null, "Game One", null);
        GameData game2 = new GameData(0, null, null, "Game Two", null);
        gameService.createGame(game1, auth.authToken());
        gameService.createGame(game2, auth.authToken());

        // Retrieve the list of games
        List<GameData> games = gameService.listGames(auth.authToken());

        //Ensure both games are returned
        Assertions.assertNotNull(games);
        Assertions.assertEquals(2, games.size());
        Assertions.assertEquals("Game One", games.get(0).gameName());
        Assertions.assertEquals("Game Two", games.get(1).gameName());
    }

    @Test
    void listGamesInvalidAuthToken() {
        // Create a fake auth token that does not exist
        String invalidAuthToken = "invalidToken123";

        // Attempt to list games with an invalid token
        DataAccessException thrown = Assertions.assertThrows(DataAccessException.class, () -> {
            gameService.listGames(invalidAuthToken);
        });

        // Ensure correct error is thrown
        Assertions.assertEquals(401, thrown.getStatusCode());
        Assertions.assertEquals("Error: unauthorized", thrown.getMessage());
    }
}
