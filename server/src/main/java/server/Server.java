package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.*;
import io.javalin.*;
import model.*;
import io.javalin.http.Context;
import service.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class Server {

    private final Javalin javalin;
    private final UserDao userDao = new MySQLUserDataAccess();
    private final AuthDao authDao = new MySQLAuthDataAccess();
    private final GameDao gameDao = new MySQLGameDataAccess();
    private final UserService userService = new UserService(userDao, authDao);
    private final Gson gson = new Gson();
    private final ClearService clearService = new ClearService(userDao, authDao, gameDao);
    private final GameService gameService = new GameService(gameDao, authDao);

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"))
                .post("/user", this::handleRegister)
                .post("/session", this::handleLogin)
                .delete("/session", this::handleLogout)
                .post("/game", this::handleCreateGame)
                .put("/game", this::handleJoinGame)
                .get("/game", this::handleListGames)
                .delete("/db", this::handleClear);



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
        } catch (Exception e) {
            ctx.status(500);
            ctx.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }

    public void handleLogin(Context ctx) {
       try {
           // turn json string body into userData object
           UserData user = gson.fromJson(ctx.body(), UserData.class);
           //check if valid input
           if (user.password() == null || user.username() == null) {
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
       } catch (Exception e) {
           ctx.status(500);
           ctx.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
       }
    }

    public void handleLogout(Context ctx) {
        try {
            // name authToken string into an authToken
            String authToken = ctx.header("authorization");
            //request logout and pass in token to check
            userService.logout(authToken);

            //return status code and empty string
            ctx.status(200);
            ctx.result();

        } catch (DataAccessException e) {
            ctx.status(e.getStatusCode());
            ctx.result(gson.toJson(Map.of("message", e.getMessage())));
        } catch (Exception e) {
                ctx.status(500);
                ctx.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
            }
    }

    public void handleCreateGame(Context ctx) {
        try {
            //convert json to gameData
            GameData game = gson.fromJson(ctx.body(), GameData.class);
            String authToken = ctx.header("authorization");
            //check for bad request
            if (game.gameName() == null || game.gameName().isEmpty()) {
                ctx.status(400);
                ctx.result(gson.toJson(Map.of("message", "Error:bad request")));
                return;
            }
            //create game
            int gameID = gameService.createGame(game, authToken);

            ctx.status(200);
            ctx.result(gson.toJson(Map.of("gameID", gameID)));

        } catch (DataAccessException e){
            ctx.status(e.getStatusCode());
            ctx.result(gson.toJson(Map.of("message", e.getMessage())));
        } catch (Exception e) {
            ctx.status(500);
            ctx.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }

    public void handleListGames(Context ctx) {
        try {
            // validate auth
            String authToken = ctx.header("authorization");
            // get game list
            List<GameData> gameList = gameService.listGames(authToken);

            // output result
            ctx.status(200);
            ctx.result(gson.toJson(Map.of("games", gameList)));

        } catch (DataAccessException e){
            ctx.status(e.getStatusCode());
            ctx.result(gson.toJson(Map.of("message", e.getMessage())));
        } catch (Exception e) {
            ctx.status(500);
            ctx.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }

    public void handleJoinGame(Context ctx) {
        try{
            //get gameID and Player Color from Json
            JoinGameRequest requestData = gson.fromJson(ctx.body(), JoinGameRequest.class);
            String playerColor = requestData.playerColor();
            String authToken = ctx.header("authorization");
            //validate input
            if (requestData.playerColor() == null || requestData.playerColor().isEmpty() || requestData.gameID() < 1) {
                ctx.status(400);
                ctx.result(gson.toJson(Map.of("message", "Error: bad request")));
                return;
            }
            //Join game
            gameService.joinGame(playerColor, authToken, requestData.gameID());
            //output status and empty response body
            ctx.status(200);
            ctx.result();

        } catch (DataAccessException e){
            ctx.status(e.getStatusCode());
            ctx.result(gson.toJson(Map.of("message", e.getMessage())));
        } catch (Exception e) {
            ctx.status(500);
            ctx.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }

    public void handleClear(Context ctx) {
        try {
            clearService.clear();
            ctx.status(200);
            ctx.result();
        } catch (DataAccessException e) {
            ctx.status(e.getStatusCode());
            ctx.result(gson.toJson(Map.of("message", e.getMessage())));
        } catch (Exception e) {
            ctx.status(500);
            ctx.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }

}
