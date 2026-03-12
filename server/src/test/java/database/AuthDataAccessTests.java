package database;
import dataaccess.*;
import model.AuthData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthDataAccessTests {
    private static AuthDao authDao;

    @BeforeAll
    static void setup() throws DataAccessException {
        authDao = new MySQLAuthDataAccess();
        authDao.clear(); // ensure table is empty
    }

    @AfterEach
    void afterEach() throws DataAccessException {
        authDao.clear(); // isolate tests
    }

    @Test
    @Order(1)
    @DisplayName("createAuthData: successfully creates a new auth token")
    void createAuthDataPositive() throws DataAccessException {
        AuthData auth = authDao.createAuthData("user");

        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertEquals("user", auth.username());

        // verify you can fetch it from the DB
        AuthData fetched = authDao.getAuthData(auth.authToken());
        assertNotNull(fetched);
        assertEquals("user", fetched.username());
    }

    @Test
    @Order(2)
    @DisplayName("getAuthData: returns the correct AuthData")
    void getAuthDataPositive() throws DataAccessException {
        AuthData auth = authDao.createAuthData("joe");

        AuthData fetched = authDao.getAuthData(auth.authToken());

        assertNotNull(fetched);
        assertEquals(auth.authToken(), fetched.authToken());
        assertEquals("joe", fetched.username());
    }

    @Test
    @Order(3)
    @DisplayName("getAuthData: returns null if token does not exist")
    void getAuthDataNegativeNotFound() throws DataAccessException {
        AuthData fetched = authDao.getAuthData("nonexistent-token");
        assertNull(fetched);
    }

    @Test
    @Order(4)
    @DisplayName("deleteAuthToken: removes token from DB")
    void deleteAuthTokenPositive() throws DataAccessException {
        AuthData auth = authDao.createAuthData("user");

        // token exists
        assertNotNull(authDao.getAuthData(auth.authToken()));

        // delete it
        authDao.deleteAuthToken(auth.authToken());

        // token should no longer exist
        assertNull(authDao.getAuthData(auth.authToken()));
    }


    @Test
    @Order(5)
    @DisplayName("clear: empties auths table")
    void clearPositive() throws DataAccessException {
        AuthData auth1 = authDao.createAuthData("user1");
        AuthData auth2 = authDao.createAuthData("user2");

        // make sure tokens exist
        assertNotNull(authDao.getAuthData(auth1.authToken()));
        assertNotNull(authDao.getAuthData(auth2.authToken()));

        authDao.clear();

        // tokens should no longer exist
        assertNull(authDao.getAuthData(auth1.authToken()));
        assertNull(authDao.getAuthData(auth2.authToken()));
    }

}
