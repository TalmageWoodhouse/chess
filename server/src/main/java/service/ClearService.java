package service;


import dataaccess.AuthDao;
import dataaccess.DataAccessException;
import dataaccess.GameDao;
import dataaccess.UserDao;

public class ClearService {
    private final UserDao userDao;
    private final AuthDao authDao;
    private final GameDao gameDao;

    public ClearService(UserDao userDao, AuthDao authDao, GameDao gameDao) {
        this.gameDao = gameDao;
        this.userDao = userDao;
        this.authDao = authDao;
    }

    public void clear() throws DataAccessException {
        userDao.clear();
        authDao.clear();
        gameDao.clear();
    }
}
