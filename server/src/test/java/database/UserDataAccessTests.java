package database;

import dataaccess.*;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDataAccessTests {

    private static UserDao userDao;

    @BeforeAll
    static void setup() throws DataAccessException {
        userDao = new MySQLUserDataAccess();
        userDao.clear();
    }

    @AfterEach
    void afterEach() throws DataAccessException {
        userDao.clear();
    }

    @Test
    @Order(1)
    @DisplayName("addUser: successfully adds a new user")
    void addUserPositive() throws DataAccessException {
        UserData user = new UserData("alex", "password123", "alex@example.com");
        assertDoesNotThrow(() -> userDao.addUser(user));

        UserData fetched = userDao.getUser("alex");
        assertNotNull(fetched);
        assertEquals("alex", fetched.username());
        assertEquals("password123", fetched.password());
        assertEquals("alex@example.com", fetched.email());
    }

    @Test
    @Order(2)
    @DisplayName("addUser: fails when username already exists")
    void addUserNegativeDuplicate() throws DataAccessException {
        UserData user = new UserData("joe", "pass", "joe@example.com");
        userDao.addUser(user);

        UserData duplicate = new UserData("joe", "newpass", "joe2@example.com");

        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> userDao.addUser(duplicate));

        assertTrue(exception.getMessage().contains("Duplicate entry")
                || exception.getMessage().contains("PRIMARY")); // DB-specific
    }

    @Test
    @Order(3)
    @DisplayName("getUser: returns correct user")
    void getUserPositive() throws DataAccessException {
        UserData user = new UserData("bob", "pwd", "bob@example.com");
        userDao.addUser(user);

        UserData fetched = userDao.getUser("bob");
        assertNotNull(fetched);
        assertEquals("bob", fetched.username());
        assertEquals("pwd", fetched.password());
        assertEquals("bob@example.com", fetched.email());
    }

    @Test
    @Order(4)
    @DisplayName("getUser: returns null if user does not exist")
    void getUserNegativeNotFound() throws DataAccessException {
        UserData fetched = userDao.getUser("nonexistent");
        assertNull(fetched);
    }


    @Test
    @Order(5)
    @DisplayName("clear: empties users table")
    void clearPositive() throws DataAccessException {
        userDao.addUser(new UserData("dave", "pwd", "dave@example.com"));

        // Make sure user exists before clearing
        assertNotNull(userDao.getUser("dave"));

        userDao.clear();

        // Table should be empty
        assertNull(userDao.getUser("dave"));
    }

}
