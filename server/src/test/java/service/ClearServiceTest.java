package service;

import dataaccess.*;
import org.junit.jupiter.api.*;
import model.*;
import results.*;


public class ClearServiceTest {

    private final UserDao userDao = new MemoryUserDataAccess();
    private final AuthTokenDao authTokenDao = new MemoryAuthDataAccess();
    private final GameDao gameDao = new MemoryGameDataAccess();
    private final UserService userService = new UserService(userDao, authTokenDao);
    private final ClearService clearService = new ClearService(userDao, authTokenDao, gameDao);
    private final GameService gameService = new GameService(gameDao, authTokenDao);


    @Test
    void clearGames_Success() throws DataAccessException {
        // Create and store a user, auth token, and game
        UserData newUser = new UserData("testUser", "password123", "test@email.com");
        userDao.addUser(newUser);
        AuthData auth = authTokenDao.createAuthData(newUser.username());

        GameData newGame = new GameData(0, null, null, "Test Game", null);
        gameService.createGame(newGame, auth.authToken());

        // Ensure data exists before clearing
        Assertions.assertNotNull(userDao.getUser(auth.username()));
        Assertions.assertNotNull(authTokenDao.getAuthData(auth.authToken()));
        Assertions.assertFalse(gameDao.listGames().isEmpty());

        // Call clear()
        clearService.clear();

        // Ensure all data is removed
        Assertions.assertNull(authTokenDao.getAuthData(auth.authToken()));
        Assertions.assertTrue(gameDao.listGames().isEmpty());

    }
}


