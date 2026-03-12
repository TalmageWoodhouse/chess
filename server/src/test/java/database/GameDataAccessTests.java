package database;
import dataaccess.*;
import model.GameData;
import chess.ChessGame;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameDataAccessTests {

    private static GameDao gameDao;

    @BeforeAll
    static void setup() throws DataAccessException {
        gameDao = new MySQLGameDataAccess();
        gameDao.clear();
    }

    @AfterEach
    void afterEach() throws DataAccessException {
        gameDao.clear(); // isolate tests
    }

    @Test
    void createGameSuccess() throws DataAccessException {
        GameData game = new GameData(0, null, null, "Test Game", null);

        int gameID = gameDao.createGame(game);

        GameData storedGame = gameDao.getGameData(gameID);

        Assertions.assertNotNull(storedGame);
        Assertions.assertEquals("Test Game", storedGame.gameName());
    }

    @Test
    void createGameFailure() {
        GameData game = new GameData(0, null, null, null, null);

        DataAccessException thrown = Assertions.assertThrows(DataAccessException.class, () -> {
            gameDao.createGame(game);
        });

        Assertions.assertTrue(thrown.getMessage().contains("Error"));
    }

    @Test
    void joinGameSuccess() throws DataAccessException {
        GameData game = new GameData(0, null, null, "Join Game", null);
        int gameID = gameDao.createGame(game);

        gameDao.joinGame("BLACK", gameID, "testUser");

        GameData storedGame = gameDao.getGameData(gameID);

        Assertions.assertEquals("testUser", storedGame.blackUsername());
    }

    @Test
    void joinGameFailure() throws DataAccessException {

        GameData game = new GameData(0, null, null, "Join Game", null);
        int gameID = gameDao.createGame(game);

        // Try joining non-existent game
        gameDao.joinGame("BLACK", 999, "testUser");

        // Original game should still have no players
        GameData storedGame = gameDao.getGameData(gameID);

        Assertions.assertNull(storedGame.blackUsername());
    }

    @Test
    void listGamesSuccess() throws DataAccessException {
        GameData game1 = new GameData(0, null, null, "Game One", null);
        GameData game2 = new GameData(0, null, null, "Game Two", null);

        gameDao.createGame(game1);
        gameDao.createGame(game2);

        List<GameData> games = gameDao.listGames();

        Assertions.assertNotNull(games);
        Assertions.assertEquals(2, games.size());
    }

    @Test
    void clearSuccess() throws DataAccessException {
        GameData game = new GameData(0, null, null, "Test Game", null);
        gameDao.createGame(game);

        gameDao.clear();

        List<GameData> games = gameDao.listGames();

        Assertions.assertEquals(0, games.size());
    }
}
