package chess;

import java.util.ArrayList;
import java.util.Collection;

public interface PieceMovesCalculator {

    Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPos);

    default Collection<ChessMove> slideMoves(ChessBoard board, ChessPosition myPos, int[][] directions) {
        Collection<ChessMove> moves = new ArrayList<>();

        int row = myPos.getRow();
        int col = myPos.getColumn();
        for (int[] d : directions) {
            row += d[0];
            col += d[1];
            ChessPosition checkPos = new ChessPosition(row, col);
            while (board.isInBounds(checkPos) && !board.isFriendly(myPos, checkPos)) {
                if (board.isEnemy(myPos, checkPos)) {
                    moves.add(new ChessMove(myPos, checkPos, null));
                    break;
                } else {
                    moves.add(new ChessMove(myPos, checkPos, null));
                }
                row += d[0];
                col += d[1];
                checkPos = new ChessPosition(row, col);
            }
            row = myPos.getRow();
            col = myPos.getColumn();
        }
        return moves;
    }

    default Collection<ChessMove> jumpMoves(ChessBoard board, ChessPosition myPos, int[][] directions) {
        Collection<ChessMove> moves = new ArrayList<>();

        int row = myPos.getRow();
        int col = myPos.getColumn();
        for (int[] d : directions) {
            row += d[0];
            col += d[1];
            ChessPosition checkPos = new ChessPosition(row, col);
            if (board.isInBounds(checkPos) && !board.isFriendly(myPos, checkPos)) {
                moves.add(new ChessMove(myPos, checkPos, null));
            }
            row = myPos.getRow();
            col = myPos.getColumn();
        }
        return moves;
    }
}
