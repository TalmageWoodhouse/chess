package server;

import dataaccess.*;
import error.handleError;
import model.*;
import service.*;
import spark.*;
import com.google.gson.Gson;
import java.util.List;
import java.util.Map;

public class Server {
    private final UserDao userDao = new MemoryUserDataAccess();
    private final AuthTokenDao authTokenDao = new MemoryAuthDataAccess();
    private final GameDao gameDao = new MemoryGameDataAccess();
    private final UserService userService = new UserService(userDao, authTokenDao);
    private final ClearService clearService = new ClearService(userDao, authTokenDao, gameDao);
    private final GameService gameService = new GameService(gameDao, authTokenDao);

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::handleRegister);
        Spark.post("/session", this::handleLogin);
        Spark.delete("/session", this::handleLogout);
        Spark.delete("/db", this::handleClear);
        Spark.get("/game", this::handleGameList);
        Spark.post("/game", this::handleCreateGame);
        Spark.put("/game", this::handleJoinGame);

        Spark.awaitInitialization();
        return Spark.port();
    }

    public int port() {
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private Object handleRegister(Request req, Response res) {
        try {
            // parse the request body into userData object
            UserData user = new Gson().fromJson(req.body(), UserData.class);

            // if bad input return error
            if (user == null || user.username() == null || user.username().isBlank() ||
                    user.password() == null || user.password().isBlank() ||
                    user.email() == null || user.email().isBlank()) {
                res.status(400);
                return new Gson().toJson(Map.of("message","Error: bad request"));
            }

            // register the user
            AuthData auth = userService.registerResult(user);

            //converts the result object to a Json string
            String json = new Gson().toJson(auth); // turning authToken and username into json
            //sets success status code and returns the json string
            res.status(200);
            return json;

        } catch (DataAccessException e) {
            // create message object with message to return
            handleError message = new handleError(e.getMessage());
            res.status(e.getStatusCode());
            return new Gson().toJson(message);
        }
    }

    private Object handleLogin(Request req, Response res) {
        try {
            // turn json string body into userData object
            UserData user = new Gson().fromJson(req.body(), UserData.class);
            //check if valid input
            if (user == null ) {
                res.status(400);
                return new Gson().toJson(Map.of("message","Error: bad request"));
            }
            // login the user
            AuthData auth = userService.loginResult(user);
            //change back to json
            String json = new Gson().toJson(auth);
            // set status code and return Json object
            res.status(200);
            return json;

        } catch (DataAccessException e) {
            // create message object with message to return
            handleError message = new handleError(e.getMessage());
            res.status(e.getStatusCode());
            return new Gson().toJson(message);
        }
    }

    private Object handleLogout(Request req, Response res) {
        try {
            // naming authToken string into an authToken
            String authToken = req.headers("authorization");

            // request logout and pass in token to check
            userService.logoutResult(authToken);

            // return access code with empty string
            res.status(200);
            return "{}";

        } catch (DataAccessException e) {
            // create message object with message to return
            handleError message = new handleError(e.getMessage());
            res.status(e.getStatusCode());
            return new Gson().toJson(message);
        }
    }

    private Object handleGameList(Request req, Response res) {
        try {
            // naming authToken string into an authToken
            String authToken = req.headers("authorization");

            // request logout and pass in token to check
            Object games = gameService.listGames(authToken);

            Object json = new Gson().toJson(Map.of("games", games));
            // return access code with empty string
            res.status(200);
            return json;

        } catch (DataAccessException e) {
            // create message object with message to return
            handleError message = new handleError(e.getMessage());
            res.status(e.getStatusCode());
            return new Gson().toJson(message);
        }
    }

    private Object handleCreateGame(Request req, Response res) {
        try {
            // creating gameData object and putting in GameData
            GameData gameName = new Gson().fromJson(req.body(), GameData.class);
            String authToken = req.headers("authorization");
            // check if game input
            if (gameName.gameName() == null ) {
                res.status(400);
                return new Gson().toJson(Map.of("message","Error: bad request"));
            }
            // create a new game and return the gameID
            Object createGameResult = gameService.createGame(gameName, authToken);

            String json = new Gson().toJson(createGameResult);
            // return access code with empty string
            res.status(200);
            return json;

        } catch (DataAccessException e) {
            // create message object with message to return
            handleError message = new handleError(e.getMessage());
            res.status(e.getStatusCode());
            return new Gson().toJson(message);
        }
    }

    private Object handleJoinGame(Request req, Response res) {
        try {
            // creating gameData object and putting in GameData
            JoinGameData joinGameData = new Gson().fromJson(req.body(), JoinGameData.class);
            String authToken = req.headers("authorization");
            // check if game input
            if (joinGameData == null || joinGameData.playerColor() == null || joinGameData.playerColor().isBlank()
                    || joinGameData.gameID() <= 0) {
                res.status(400);
                return new Gson().toJson(Map.of("message","Error: bad request"));
            }
            // join game
            gameService.joinGame(joinGameData.gameID(), authToken, joinGameData.playerColor());

            // return access code with empty string
            res.status(200);
            return "{}";

        } catch (DataAccessException e) {
            // create message object with message to return
            handleError message = new handleError(e.getMessage());
            res.status(e.getStatusCode());
            return new Gson().toJson(message);
        }
    }

    private Object handleClear (Request req, Response res) {
        // clear out all of database
        clearService.clear();
        res.status(200);
        return "{}";
    }
}
