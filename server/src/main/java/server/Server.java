package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.*;
import io.javalin.*;
import model.*;
import io.javalin.http.Context;
import service.*;

import java.util.Map;

public class Server {

    private final Javalin javalin;
    private final UserDao userDao = new MemoryUserDataAccess();
    private final AuthDao authDao = new MemoryAuthDataAccess();
    private final GameDao gameDao = new MemoryGameDataAccess();
    private final UserService userService = new UserService(userDao, authDao);
    private final ClearService clearService = new ClearService(userDao, authDao, gameDao);
//    private final GameService gameService = new GameService(gameDao, authDao);

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"))
                .post("/user", this::handleRegister)
                .post("/session", this::handleLogin)
                .delete("/session", this::handleLogout)
                .post("/game", this::handleCreateGame)
                .put("/game", this::handleJoinGame)
                .get("/game", this::handleListGames)
                .post("/db", this::handleClear)
                .exception(ResponseException.class, this::exceptionHandler);

        // Register your endpoints and exception handlers here.

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    public void HandleRegister(Context ctx) {
        try {
            //parse the request body into userData object
            UserData user = new Gson().fromJson(ctx.body(), UserData.class);

            //if  bad input return error
            if (user == null || user.username() == null || user.password().isBlank()
                    || user.password() == null || user.password().isBlank() ||
                    user.email() == null || user.email().isBlank()) {
                ctx.status(400);
                ctx.result(new Gson().toJson(Map.of("message", "Error: bad request")));
            }
            //register the user
            AuthData auth = userService.register(user);
            //converts the result object to a json string

            //sets success status code and sends result
            ctx.status(200);
            ctx.result(json);
        } catch (DataAccessException e) {
            //create message object with message to return


        }
    }


}
