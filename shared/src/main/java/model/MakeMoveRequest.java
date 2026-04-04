package model;

import chess.ChessMove;

public class MakeMoveRequest {
    private int gameID;
    private ChessMove move;

    public MakeMoveRequest(int gameID, ChessMove move) {
        this.gameID = gameID;
        this.move = move;
    }
}
