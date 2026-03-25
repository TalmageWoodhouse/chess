package client.server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;

public class ServerFacade {

    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public AuthData register(UserData user) throws DataAccessException {
        var req = buildRequest("POST", "/user", user, null);
        var res = sendRequest(req);
        return handleResponse(res, AuthData.class);
    }

    public AuthData login(UserData user) throws DataAccessException {
        var req = buildRequest("POST", "/session", user, null);
        var res = sendRequest(req);
        return handleResponse(res, AuthData.class);
    }

    public void logout(String authToken) throws DataAccessException {
        var req = buildRequest("DELETE", "/session", null, authToken);
        sendRequest(req);
    }

    private HttpRequest buildRequest(String method, String path, Object body, String authToken) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));
        if (body != null) {
            request.header("Content-Type", "application/json");
        }
        if (authToken != null) {
            request.header("Authorization", authToken);
        }
        return request.build();
    }

    public int createGame(GameData game, String authToken) throws DataAccessException {
        var req = buildRequest("POST", "/session", game, authToken);
        var res = sendRequest(req);
        return handleResponse(res,);
    }

    public void joinGame(String playerColor, String authToken, int gameID) throws DataAccessException {
        var req = buildRequest("POST", "/session", null, authToken);
        var res = sendRequest(req);
    }

    public List<GameData> listGames(String authToken) throws DataAccessException {
        var req = buildRequest("GET", "/session", null, authToken);
        var res = sendRequest(req);
        return handleResponse(res, GameData.class);
    }

    private BodyPublisher makeRequestBody(Object request) {
        if (request != null) {
            return BodyPublishers.ofString(gson.toJson(request));
        } else {
            return BodyPublishers.noBody();
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws DataAccessException {
        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new DataAccessException(500, ex.getMessage());
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws DataAccessException {
        int status = response.statusCode();
        String body = response.body();

        if (!isSuccessful(status)) {
            String message = "other failure: " + status;

            if (body != null) {
                try {
                    var jsonObj = new com.google.gson.JsonParser().parse(body).getAsJsonObject();
                    if (jsonObj.has("message")) {
                        message = jsonObj.get("message").getAsString();
                    }
                    if (jsonObj.has("statusCode")) {
                        status = jsonObj.get("statusCode").getAsInt();
                    }
                } catch (Exception ex) {
                    // Parsing failed, keep default message and status
                }
            }

            throw new DataAccessException(status, "other failure: " + status);
        }

        if (responseClass != null) {
            return gson.fromJson(response.body(), responseClass);
        }

        return null;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
