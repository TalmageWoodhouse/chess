package chess;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private ChessPiece[][] squares;

    public ChessBoard() {
         squares = new ChessPiece[8][8];
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow()-1][position.getColumn()-1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getRow()-1][position.getColumn()-1];
    }

    public ChessBoard boardCopy() {
        ChessBoard copy = new ChessBoard();
        copy.squares = Arrays.copyOf(squares, squares.length);
        for (int i = 0; i < 8; i++) {
            copy.squares[i] = Arrays.copyOf(squares[i], squares[i].length);
        }
        return copy;
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        squares = new ChessPiece[8][8];
        // add pawns
        for (int col = 1; col < 9; col++) {
            addPiece(new ChessPosition(2,col), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            addPiece(new ChessPosition(7,col), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
        }
        // add white pieces
        addPiece(new ChessPosition(1,1), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(1,2), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(1,3), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(1,4), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN));
        addPiece(new ChessPosition(1,5), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING));
        addPiece(new ChessPosition(1,6), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(1,7), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(1,8), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));

        // add black pieces
        addPiece(new ChessPosition(8,1), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(8,2), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(8,3), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(8,4), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN));
        addPiece(new ChessPosition(8,5), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING));
        addPiece(new ChessPosition(8,6), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(8,7), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(8,8), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK));
    }
    /**
     * Checks if the given position is within the boundaries of the chessboard.
     *
     * @param position The position to check
     * @return true if the position is valid; false otherwise.
     */
    public boolean isPositionValid(ChessPosition position) {
        if (position == null) {
            return false; // null position would be off the board
        }
        int row = position.getRow();
        int column = position.getColumn();

        // Check if the row and column are within the valid range
        return row >= 1 && row <= 8 && column >= 1 && column <= 8;
    }

    /**
     * Checks if the given position is occupied by a friendly piece.
     *
     * @param position The position to check
     * @param myPosition The position of the piece calling this method
     * @return true if the position is occupied by the friendly piece; else false.
     */
    public boolean isPositionOccupiedByFriendly(ChessPosition position, ChessPosition myPosition) {
        if (position == null || myPosition == null) {
            throw new IllegalArgumentException("Positions cannot be null"); //invalid input
        }
        ChessPiece myPiece = getPiece(myPosition);
        ChessPiece targetPiece = getPiece(position);
        // Checks edge case for myPiece and if there is no piece at target position.
        if (myPiece == null || targetPiece == null) {
            return false;
        }
        return myPiece.getTeamColor() == targetPiece.getTeamColor();
    }

    /**
     * Checks if the given position is occupied by a friendly piece.
     *
     * @param position The position to check
     * @return true if the position is occupied by the friendly piece; else false.
     */
    public boolean isPositionOccupiedByEnemy(ChessPosition position, ChessPosition myPosition) {
        if (position == null || myPosition == null) {
            throw new IllegalArgumentException("Positions cannot be null"); //invalid input
        }
        ChessPiece myPiece = getPiece(myPosition);
        ChessPiece targetPiece = getPiece(position);
        // Checks edge case for myPiece and if there is no piece at target position.
        if (myPiece == null || targetPiece == null) {
            return false;
        }
        return myPiece.getTeamColor() != targetPiece.getTeamColor();
    }


    @Override
    public String toString() {
        return "ChessBoard{" +
                "squares=" + Arrays.deepToString(squares) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(squares, that.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(squares);
    }
    //alldone
}
