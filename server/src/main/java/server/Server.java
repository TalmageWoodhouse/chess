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
    private final Gson gson = new Gson();
    private final ClearService clearService = new ClearService(userDao, authDao, gameDao);
//    private final GameService gameService = new GameService(gameDao, authDao);

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"))
                .post("/user", this::handleRegister)
                .post("/session", this::handleLogin)
                .delete("/session", this::handleLogout)
//                .post("/game", this::handleCreateGame)
//                .put("/game", this::handleJoinGame)
//                .get("/game", this::handleListGames)
                .post("/db", this::handleClear);

        // Register your endpoints and exception handlers here.

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    public void handleRegister(Context ctx) {
        try {
            //parse the request body into userData object
            UserData user = gson.fromJson(ctx.body(), UserData.class);

            //if  bad input return error
            if (user == null || user.username() == null || user.username().isEmpty()
                    || user.password() == null || user.password().isEmpty() ||
                    user.email() == null || user.email().isEmpty()) {
                ctx.status(400);
                ctx.result(gson.toJson(Map.of("message", "Error: bad request")));
                return;
            }
            //register the user
            AuthData auth = userService.register(user);
            //converts the result object to a json string
            String json = gson.toJson(auth);
            //sets success status code and sends result
            ctx.status(200);
            ctx.result(json);
        } catch (DataAccessException e) {
            ctx.status(e.getStatusCode());
            ctx.result(gson.toJson(Map.of("message", e.getMessage())));
        }
    }

    public void handleLogin(Context ctx) {
       try {
           // turn json string body into userData object
           UserData user = gson.fromJson(ctx.body(), UserData.class);
           //check if valid input
           if (user == null) {
               ctx.status(400);
               ctx.result(gson.toJson(Map.of("message", "Error: bad request")));
               return;
           }
           //login the user
           AuthData auth = userService.login(user);
           //change back to json
           String json = gson.toJson(auth);
           //set status code and return json object
           ctx.status(200);
           ctx.result(json);
       } catch (DataAccessException e){
           ctx.status(e.getStatusCode());
           ctx.result(gson.toJson(Map.of("message", e.getMessage())));
        }
    }

    public void handleLogout(Context ctx) {
        try {
            // name authtoken string into an authtoken
            String authToken = ctx.header("authorization");
            //request logout and pass in token to check
            userService.logout(authToken);

            //return status code and empty string
            ctx.status(200);
            ctx.result();

        } catch (DataAccessException e){
            ctx.status(e.getStatusCode());
            ctx.result(gson.toJson(Map.of("message", e.getMessage())));
        }
    }

    public void handleClear(Context ctx) {
        clearService.clear();
        ctx.status(200);
        ctx.result();
    }




}
