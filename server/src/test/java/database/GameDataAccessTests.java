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
    @Order(1)
    @DisplayName("createGame: successfully creates a game")
    void createGamePositive() throws DataAccessException {
        ChessGame chess = new ChessGame();
        GameData game = new GameData(0, null, null, "Test Game", chess);

        int gameID = gameDao.createGame(game);
        assertTrue(gameID > 0);

        GameData fetched = gameDao.getGameData(gameID);
        assertNotNull(fetched);
        assertEquals("Test Game", fetched.gameName());
        assertNull(fetched.whiteUsername());
        assertNull(fetched.blackUsername());
    }

    @Test
    @Order(2)
    @DisplayName("listGames: returns all created games")
    void listGamesPositive() throws DataAccessException {
        GameData game1 = new GameData(0, null, null, "Game1", new ChessGame());
        GameData game2 = new GameData(0, null, null, "Game2", new ChessGame());

        int id1 = gameDao.createGame(game1);
        int id2 = gameDao.createGame(game2);

        List<GameData> games = gameDao.listGames();

        assertEquals(2, games.size());
        assertTrue(games.stream().anyMatch(g -> g.gameName().equals("Game1")));
        assertTrue(games.stream().anyMatch(g -> g.gameName().equals("Game2")));
    }

    @Test
    @Order(3)
    @DisplayName("joinGame: assigns player to correct color")
    void joinGamePositive() throws DataAccessException {
        GameData game = new GameData(0, null, null, "JoinTest", new ChessGame());
        int gameID = gameDao.createGame(game);

        // join as white
        gameDao.joinGame("WHITE", gameID, "alice");
        GameData fetched1 = gameDao.getGameData(gameID);
        assertEquals("alice", fetched1.whiteUsername());

        // join as black
        gameDao.joinGame("BLACK", gameID, "bob");
        GameData fetched2 = gameDao.getGameData(gameID);
        assertEquals("bob", fetched2.blackUsername());
    }
}
