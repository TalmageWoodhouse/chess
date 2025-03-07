package server;

import dataaccess.*;
import error.handleError;
import model.*;
import service.*;
import spark.*;
import com.google.gson.Gson;

import java.util.List;

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


        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

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
            if (user.username() == null || user.password() == null || user.email() == null) {
                res.status(400);
                return new Gson().toJson("Error: bad request");
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
            res.status(403);
            return new Gson().toJson(message);
        }
    }

    private Object handleLogin(Request req, Response res) {
        try {
            // turn json string body into userData object
            UserData user = new Gson().fromJson(req.body(), UserData.class);

            // login the user
            AuthData auth = userService.loginResult(user);

            String json = new Gson().toJson(auth);
            // set status code and return Json object
            res.status(200);
            return json;

        } catch (DataAccessException e) {
            // create message object with message to return
            handleError message = new handleError(e.getMessage());
            res.status(401);
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
            res.status(401);
            return new Gson().toJson(message);
        }
    }

    private Object handleGameList(Request req, Response res) {
        try {
            // naming authToken string into an authToken
            String authToken = req.headers("authorization");

            // request logout and pass in token to check
            List<GameData> games = gameService.listGames(authToken);

            // return access code with empty string
            res.status(200);
            return games;

        } catch (DataAccessException e) {
            // create message object with message to return
            handleError message = new handleError(e.getMessage());
            res.status(401);
            return new Gson().toJson(message);
        }
    }

    private Object handleClear(Request req, Response res) {
        // clear out all of database
        clearService.clear();
        res.status(200);
        return "{}";
    }
}
