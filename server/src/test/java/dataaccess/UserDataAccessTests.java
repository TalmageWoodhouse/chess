package dataaccess;

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
    void addUserSuccess() throws DataAccessException {
        UserData newUser = new UserData("alex", "password123", "alex@example.com");

        assertDoesNotThrow(() -> userDao.addUser(newUser), "Adding a valid user should not throw");

        UserData fetched = userDao.getUser("alex");
        assertNotNull(fetched, "Fetched user should not be null");
        assertEquals("alex", fetched.username());
        assertEquals("password123", fetched.password());
        assertEquals("alex@example.com", fetched.email());
    }

    @Test
    @Order(2)
    void addUserDuplicateFails() throws DataAccessException {
        UserData user = new UserData("joe", "pass", "joe@example.com");
        userDao.addUser(user);

        UserData duplicate = new UserData("joe", "newpass", "joe2@example.com");

        DataAccessException thrown = assertThrows(DataAccessException.class, () -> userDao.addUser(duplicate));
        assertTrue(thrown.getMessage().contains("Duplicate entry") || thrown.getMessage().contains("PRIMARY"));
    }

    @Test
    @Order(3)
    void getUserSuccess() throws DataAccessException {
        UserData newUser = new UserData("bob", "pwd", "bob@example.com");
        userDao.addUser(newUser);

        UserData fetched = userDao.getUser("bob");
        assertNotNull(fetched);
        assertEquals("bob", fetched.username());
        assertEquals("pwd", fetched.password());
        assertEquals("bob@example.com", fetched.email());
    }

    @Test
    @Order(4)
    void getUserNotFoundReturnsNull() throws DataAccessException {
        UserData fetched = userDao.getUser("nonexistent");
        assertNull(fetched, "Fetching a non-existent user should return null");
    }

    @Test
    @Order(5)
    void clearTableRemovesAllUsers() throws DataAccessException {
        UserData newUser = new UserData("user", "pwd", "dave@example.com");
        userDao.addUser(newUser);
        assertNotNull(userDao.getUser("user"), "User should exist before clearing");

        userDao.clear();

        assertNull(userDao.getUser("user"), "User should be null after clearing the table");
    }
}