package chess;

import java.util.ArrayList;
import java.util.Collection;

public interface ChessPieceMoveCalculator {
    /**
     * calculates all possible moves for a chess piece
     *
     * @param board The current chessboard
     * @param myPosition The position of the piece
     * @return A collection of valid moves for the piece
     */
    Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition);


    default Collection<ChessMove> slideMoves(ChessBoard board, ChessPosition myPosition, int[][] MoveDirections) {
        Collection<ChessMove> moves = new ArrayList<>();

        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        for (int[] direction : MoveDirections) {
            row += direction[0];
            col += direction[1];
            ChessPosition newPosition = new ChessPosition(row,col);
            while (board.isPositionValid(newPosition) && !board.isPositionOccupiedByFriendly(newPosition, myPosition)) {
                if (board.isPositionOccupiedByEnemy(newPosition, myPosition)) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                    break;
                }
                moves.add(new ChessMove(myPosition, newPosition, null));
                row += direction[0];
                col += direction[1];
                newPosition = new ChessPosition(row, col);
            }
            row = myPosition.getRow();
            col = myPosition.getColumn();
        }
        return moves;
    }

    default Collection<ChessMove> stepMoves(ChessBoard board, ChessPosition myPosition, int[][] MoveDirections) {
        Collection<ChessMove> moves = new ArrayList<>();

        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        for (int[] direction : MoveDirections) {
            row += direction[0];
            col += direction[1];
            ChessPosition newPosition = new ChessPosition(row,col);
            if (board.isPositionValid(newPosition) && !board.isPositionOccupiedByFriendly(newPosition, myPosition)) {
                moves.add(new ChessMove(myPosition, newPosition, null));
            }
            row = myPosition.getRow();
            col = myPosition.getColumn();
        }
        return moves;
    }

    default Collection<ChessMove> pawnAttack(ChessBoard board, ChessPosition myPosition,
                                             ChessPosition attackPosition, int row, int promotionRow) {
        Collection<ChessMove> moves = new ArrayList<>();

        if (board.isPositionValid(attackPosition)
                && board.isPositionOccupiedByEnemy(attackPosition, myPosition)) {
            if (row + 1 == promotionRow) {
                moves.add(new ChessMove(myPosition, attackPosition, ChessPiece.PieceType.ROOK));
                moves.add(new ChessMove(myPosition, attackPosition, ChessPiece.PieceType.BISHOP));
                moves.add(new ChessMove(myPosition, attackPosition, ChessPiece.PieceType.QUEEN));
                moves.add(new ChessMove(myPosition, attackPosition, ChessPiece.PieceType.KNIGHT));
            } else {
                moves.add(new ChessMove(myPosition, attackPosition, null));
            }
        }
        return moves;
    }

}
