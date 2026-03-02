package service;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.Data;

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
        CreateGameResult gameIDObject = gameService.createGame(newGame, auth.authToken());

        // join game
        gameDao.joinGame(gameIDObject.gameID(), "BLACK", auth.username());
        GameData gameData = gameDao.getGameData(gameIDObject.gameID());

        //  Check if blackUser name was input meaning game was joined
        Assertions.assertNotNull(gameData.blackUsername());
    }
}
