package server;

import spark.*;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        Spark.post("/game", this::addGame);
        Spark.get("/game", this::listPets);
        Spark.delete("/game/:id", this::deleteGame);
        Spark.delete("/game", this::deleteAllGames);


        // Register your endpoints and handle exceptions here.
        Spark.exception(ResponseException.class, this::exceptionHandler);


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

    private void exceptionHandler(ResponseException ex, Request req, Response res) {
        res.status(ex.StatusCod());
        res.body(ex.toJson());
    }
}
