package service;

import dataaccess.*;
import org.junit.jupiter.api.*;
import model.*;

public class UserServiceTests {

    private String expected;
    private final UserDao userDao = new MemoryUserDataAccess();
    private final AuthTokenDao authTokenDao = new MemoryAuthDataAccess();
    private final GameDao gameDao = new MemoryGameDataAccess();
    private final UserService userService = new UserService(userDao, authTokenDao);
    private final ClearService clearService = new ClearService(userDao, authTokenDao, gameDao);
    private final GameService gameService = new GameService(gameDao, authTokenDao);


    @BeforeEach
    public void setup() {
        clearService.clear();
    }

    @Test
    public void goodRegisterTest() throws DataAccessException {
        // user data
        UserData mockUser = new UserData("testUser", "password123", "test@email.com");

        // Call the method
        AuthData result = userService.registerResult(mockUser);

        // Verify successful login
        Assertions.assertNotNull(result);
        Assertions.assertEquals("testUser", result.username());
        Assertions.assertNotNull(result.authToken());
        Assertions.assertNotNull(authTokenDao.getAuthData(result.authToken()));

    }

    @Test
    void badRegisterTest() throws DataAccessException {
        // Store a user with a known password
        UserData newUser = new UserData("testUser", "password", "test@email.com");
        userDao.addUser(newUser);

        // Attempt login with wrong password
        UserData badRequest = new UserData("testUser", "password", "test@email.com");

        DataAccessException thrown = Assertions.assertThrows(DataAccessException.class, () -> {
            userService.registerResult(badRequest);
        });

        Assertions.assertEquals(403, thrown.getStatusCode());
        Assertions.assertEquals("Error: already taken", thrown.getMessage());
    }

    @Test
    void testIncorrectPassword() throws DataAccessException {
        // Store a user with a known password
        UserData newUser = new UserData("testUser", "password123", "test@email.com");
        userDao.addUser(newUser);

        //  Attempt login with wrong password
        UserData invalidLogin = new UserData("testUser", "wrongPassword", "test@email.com");

        DataAccessException thrown = Assertions.assertThrows(DataAccessException.class, () -> {
            userService.loginResult(invalidLogin);
        });

        Assertions.assertEquals(401, thrown.getStatusCode());
        Assertions.assertEquals("Error: unauthorized", thrown.getMessage());
    }

    @Test
    public void goodLogin() throws DataAccessException {
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
    void badLogout() throws DataAccessException {
        // creat and add user
        UserData newUser = new UserData("testUser", "password123", "test@email.com");
        userDao.addUser(newUser);

        String invalidAuthToken = "invalidToken123";

        DataAccessException thrown = Assertions.assertThrows(DataAccessException.class, () -> {
            userService.logoutResult(invalidAuthToken);
        });

        Assertions.assertEquals(401, thrown.getStatusCode());
        Assertions.assertEquals("Error: unauthorized", thrown.getMessage());
    }

    @Test
    public void goodLogout() throws DataAccessException {
        // Mock user data
        UserData newUser = new UserData("testUser", "password123", "test@email.com");
        userDao.addUser(newUser);
        AuthData auth = authTokenDao.createAuthData(newUser.username());

        // Call the method
        userService.logoutResult(auth.authToken());

        // Verify successful logout
        Assertions.assertNull(authTokenDao.getAuthData(auth.authToken()));

    }

}
