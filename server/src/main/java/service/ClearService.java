package service;

import dataaccess.AuthTokenDao;
import dataaccess.GameDao;
import dataaccess.UserDao;

public class ClearService {
    private final UserDao userData;
    private final AuthTokenDao authData;
    private final GameDao gameData;

    public ClearService(UserDao userData, AuthTokenDao authData, GameDao gameData) {
        this.userData = userData;
        this.authData = authData;
        this.gameData = gameData;
    }

    public void clear() {
        userData.clear();
        authData.clear();
        gameData.clear();
    }
}