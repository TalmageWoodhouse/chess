package service;

import dataaccess.*;
import org.junit.jupiter.api.*;
import model.*;
import java.net.HttpURLConnection;
import service.ClearService;
import service.GameService;
import service.UserService;


public class UserServiceTests {

    private String expected;
    private final UserDao userDao = new MemoryUserDataAccess();
    private final AuthTokenDao authTokenDao = new MemoryAuthDataAccess();
    private final GameDao gameDao = new MemoryGameDataAccess();
    private final UserService userService = new UserService(userDao, authTokenDao);
    private final ClearService clearService = new ClearService(userDao, authTokenDao, gameDao);
    private final GameService gameService = new GameService(gameDao, authTokenDao);



    @Test
    @DisplayName("Normal login test")
    public void goodLoginTest() throws DataAccessException {
        // Mock user data
        UserData mockUser = new UserData("testUser", "password123", "test@email.com");
        userDao.addUser(mockUser);

        // Call the method
        AuthData result = userService.loginResult(mockUser);


        // Verify successful login
        Assertions.assertNotNull(result);
        Assertions.assertEquals("testUser", result.username());
        Assertions.assertNotNull(result.authToken());
        Assertions.assertNotNull(authTokenDao.getAuthData(result.authToken()));

    }

    @Test
    @DisplayName("login with incorrect password")
    public void nullLoginTest() throws DataAccessException {
        // Mock user data
        UserData mockUser = new UserData("testUser", null, "test@email.com");
        userDao.addUser(mockUser);

        // Call the method
        AuthData result = userService.loginResult(mockUser);

        Assertions.assertHttpUnauthorized(result);
        Assertions.assertAuthFieldsMissing(result);
    }
}
