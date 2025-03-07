package server;

import dataaccess.*;
import error.handleError;
import model.AuthData;
import model.UserData;
import service.UserService;
import spark.*;
import com.google.gson.Gson;

import javax.xml.crypto.Data;

public class Server {
    private final UserDao userDao = new MemoryUserDataAccess();
    private final AuthTokenDao authTokenDao = new MemoryAuthDataAccess();
    private final UserService userService = new UserService(userDao, authTokenDao);

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::handleRegister);
        Spark.post("/session", this::handleLogin);


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
            handleError message = new handleError(e.getMessage());
            res.status(401);
            return new Gson().toJson(message);
        }
    }
}
