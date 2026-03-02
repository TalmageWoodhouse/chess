package service;

import chess.ChessPiece;
import dataaccess.*;
import model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserServiceTests {

    private final UserDao userDao = new MemoryUserDataAccess();
    private final AuthDao authDao = new MemoryAuthDataAccess();
    private final GameDao gameDao = new MemoryGameDataAccess();
    private final ClearService clearService = new ClearService(userDao, authDao, gameDao);
    private final UserService userService = new UserService(userDao, authDao);


    @BeforeEach
    public void setup() { clearService.clear(); }

    @Test
    public void goodRegisterTest() throws DataAccessException {
        // creat and store user
        UserData newUser = new UserData("testUser", "password", "test@email.com");

        //call the method
        AuthData auth = userService.register(newUser);

        Assertions.assertNotNull(auth);
        Assertions.assertEquals("testUser", auth.username());
        Assertions.assertNotNull(auth.authToken());
        Assertions.assertNotNull(authDao.getAuthData(auth.authToken()));
    }

    @Test
    void badRegisterTest() throws DataAccessException {
        UserData newUser = new UserData("testUser", "password", "test@email.com");
        userDao.addUser(newUser);

        //create duplicate user
        UserData badReq = new UserData("testUser", "password", "test@email.com");

        DataAccessException thrown = Assertions.assertThrows(DataAccessException.class, () -> {
            userService.register(badReq);
        });

        Assertions.assertEquals(403, thrown.getStatusCode());
        Assertions.assertEquals("Error: already taken", thrown.getMessage());
    }

    @Test
    void testIncorrectPassword() throws DataAccessException {
        UserData newUser = new UserData("testUser", "password", "test@email.com");
        userDao.addUser(newUser);

        //try login with wrong password
        UserData invalidPassword = new UserData("testUser", "pass", "test@email.com");

        DataAccessException thrown = Assertions.assertThrows(DataAccessException.class, () -> {
            userService.login(invalidPassword);
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
        AuthData result = userService.login(mockUser);

        // Verify successful login
        Assertions.assertNotNull(result);
        Assertions.assertEquals("testUser", result.username());
        Assertions.assertNotNull(result.authToken());
        Assertions.assertNotNull(authDao.getAuthData(result.authToken()));
    }

    @Test
    void badLogout() throws DataAccessException {
        // creat and add user
        UserData newUser = new UserData("testUser", "password123", "test@email.com");
        userDao.addUser(newUser);

        String invalidAuthToken = "invalidToken123";

        DataAccessException thrown = Assertions.assertThrows(DataAccessException.class, () -> {
            userService.logout(invalidAuthToken);
        });

        Assertions.assertEquals(401, thrown.getStatusCode());
        Assertions.assertEquals("Error: unauthorized", thrown.getMessage());
    }

    @Test
    public void goodLogout() throws DataAccessException {
        // Mock user data
        UserData newUser = new UserData("testUser", "password123", "test@email.com");
        userDao.addUser(newUser);
        AuthData auth = authDao.createAuthData(newUser.username());

        // Call the method
        userService.logout(auth.authToken());

        // Verify successful logout
        Assertions.assertNull(authDao.getAuthData(auth.authToken()));
    }
}
