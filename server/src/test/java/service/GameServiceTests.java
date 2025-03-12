package service;

import dataaccess.*;
import org.junit.jupiter.api.*;
import model.*;
import results.*;

import java.util.List;


public class GameServiceTests {

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
    void createGame_Success() throws DataAccessException {
        // Create and store a valid auth token
        UserData newUser = new UserData("testUser", "password123", "test@email.com");
        userDao.addUser(newUser);
        AuthData auth = authTokenDao.createAuthData(newUser.username());

        // Simulate game creation returning a gameID
        GameData newGame = new GameData(0, null, null, "gameName", null);

        // Call the method
        CreateGameResult resultObject = gameService.createGame(newGame, auth.authToken());

        Assertions.assertNotNull(resultObject);
        Assertions.assertEquals(1, resultObject.gameID());
    }

    @Test
    void createGame_failure() throws DataAccessException {
        // Create and store a valid auth token
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
    void joinGame_Success() throws DataAccessException {
        // Create and store a valid auth token
        UserData newUser = new UserData("testUser", "password123", "test@email.com");
        userDao.addUser(newUser);
        AuthData auth = authTokenDao.createAuthData(newUser.username());

        // create the game and store the gameID
        GameData newGame = new GameData(0, null, null, "gameName", null);
        CreateGameResult gameIDObject = gameService.createGame(newGame, auth.authToken());

        // join game
        gameDao.joinGame(gameIDObject.gameID(), "BLACK", auth.username());
        GameData gameData = gameDao.getGameData(gameIDObject.gameID());

        //  Check if blackUser name was input meaning game was joined
        Assertions.assertNotNull(gameData.blackUsername());
    }

    @Test
    void joinGame_failure() throws DataAccessException {
        // Create and store a valid auth token
        UserData newUser = new UserData("testUser", "password123", "test@email.com");
        userDao.addUser(newUser);
        AuthData auth = authTokenDao.createAuthData(newUser.username());

        // create the game and store the gameID
        GameData newGame = new GameData(0, null, null, "gameName", null);
        CreateGameResult gameIDObject = gameService.createGame(newGame, auth.authToken());

        // try to join game and throw error
        DataAccessException thrown = Assertions.assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(gameIDObject.gameID(), auth.authToken(), "BLack");
        });

        // Check if error was thrown for bad casing
        Assertions.assertEquals(400, thrown.getStatusCode());
        Assertions.assertEquals("Error: bad request", thrown.getMessage());
    }

    @Test
    void listGames_Success() throws DataAccessException {
        // Create and store a valid auth token
        UserData newUser = new UserData("testUser", "password123", "test@email.com");
        userDao.addUser(newUser);
        AuthData auth = authTokenDao.createAuthData(newUser.username());

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
    void listGames_InvalidAuthToken() {
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