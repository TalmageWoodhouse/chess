package service;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClearServiceTest {

    private final UserDao userDao = new MySQLUserDataAccess();
    private final AuthDao authDao = new MySQLAuthDataAccess();
    private final GameDao gameDao = new MySQLGameDataAccess();
    private final ClearService clearService = new ClearService(userDao, authDao, gameDao);
    private final GameService gameService = new GameService(gameDao, authDao);


    @BeforeEach
    public void setup() throws DataAccessException { clearService.clear(); }

    @Test
    void clearGameSuccess() throws DataAccessException {
        //create and store a user, auth, and game
        UserData newUser = new UserData("testUser", "password123", "test@email.com");
        userDao.addUser(newUser);
        AuthData auth = authDao.createAuthData(newUser.username());
        //create and store new game
        GameData newGame = new GameData(0, null, null, "Test Game", null);
        gameService.createGame(newGame, auth.authToken());

        // Ensure data exists before clearing
        Assertions.assertNotNull(userDao.getUser(auth.username()));
        Assertions.assertNotNull(authDao.getAuthData(auth.authToken()));
        Assertions.assertFalse(gameDao.listGames().isEmpty());

        clearService.clear();

        //ensure all data is deleted
        Assertions.assertNull(authDao.getAuthData(auth.authToken()));
        Assertions.assertTrue(gameDao.listGames().isEmpty());

    }
}
