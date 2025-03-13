package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import chess.ChessPieceMoveCalculator;

import static chess.ChessGame.TeamColor.BLACK;
import static chess.ChessGame.TeamColor.WHITE;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece implements ChessPieceMoveCalculator {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }
    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }
    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }
    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }
    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPieceMoveCalculator moves = switch(getPieceType()) {
            case QUEEN -> new QueenMoveCalculator();
            case BISHOP -> new BishopMoveCalculator();
            case KING -> new KingMoveCalculator();
            case ROOK -> new RookMoveCalculator();
            case PAWN -> new PawnMoveCalculator();
            case KNIGHT -> new KnightMoveCalculator();
            default -> throw new RuntimeException("invalid type in pieceMoves");
        };
        return moves.pieceMoves(board, myPosition);
    }

    public interface ChessPieceMoveCalculator {
        /**
         * calculates all possible moves for a chess piece
         *
         * @param board The current chessboard
         * @param myPosition The position of the piece
         * @return A collection of valid moves for the piece
         */
        Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition);
    }


    public class RookMoveCalculator implements ChessPieceMoveCalculator {
        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
            int[][] rookMoveDirections = {
                    {1, 0}, {-1, 0}, {0, 1}, {0, -1}
            };
            return slideMoves(board, myPosition, rookMoveDirections);
        }
    }

    public class BishopMoveCalculator implements ChessPieceMoveCalculator {
        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
            int[][] bishopMoveDirections = {
                    {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
            };
            return slideMoves(board, myPosition, bishopMoveDirections);
        }
    }


    public class QueenMoveCalculator implements ChessPieceMoveCalculator {
        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
            int[][] queenMoveDirections = {
                    {1, 1}, {1, -1}, {1, 0}, {-1, -1}, //bishop like moves
                    {-1, 1}, {-1, 0}, {0, 1}, {0, -1}  //rook like moves
            };
            return slideMoves(board, myPosition, queenMoveDirections);
        }
    }


    public class KnightMoveCalculator implements ChessPieceMoveCalculator {
        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
            int[][] bishopMoveDirections = {
                    {2, 1}, {2, -1}, {-2, 1}, {-2, -1}, //forward moves
                    {1, 2}, {1, -2}, {-1, 2}, {-1, -2}  //back moves
            };
            return stepMoves(board, myPosition, bishopMoveDirections);
        }
    }


    public class KingMoveCalculator implements ChessPieceMoveCalculator {
        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
            Collection<ChessMove> moves = new ArrayList<>();
            int[][] kingMoveDirections = {
                    {1, 1}, {1, -1}, {-1, 1}, {-1, -1}, //bishop like moves
                    {1, 0}, {-1, 0}, {0, 1}, {0, -1}  //rook like moves
            };
            return stepMoves(board, myPosition, kingMoveDirections);
        }
    }



    public class PawnMoveCalculator implements ChessPieceMoveCalculator {
        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
            Collection<ChessMove> moves = new ArrayList<>();
            //determine direction and initial row based on piece color
            int initialRow = (board.getPiece(myPosition).pieceColor == WHITE) ? 2:7;
            int promotionRow = (board.getPiece(myPosition).pieceColor == WHITE) ? 8:1;

            int row = myPosition.getRow();
            int col = myPosition.getColumn();

            if (board.getPiece(myPosition).pieceColor == WHITE) {
                // move forward
                ChessPosition frontPosition = new ChessPosition(row + 1, col);
                if (board.isPositionValid(frontPosition)
                        && !board.isPositionOccupiedByFriendly(frontPosition, myPosition)
                        && !board.isPositionOccupiedByEnemy(frontPosition, myPosition)) {
                    moves.addAll(addMoves(promotionRow, row + 1, myPosition, frontPosition));
                }

                //right attack
                ChessPosition rightPosition = new ChessPosition(row + 1, col+1);
                moves.addAll(pawnAttack(board, myPosition, rightPosition, row + 1, promotionRow));
                // left attack
                ChessPosition leftPosition = new ChessPosition(row +1, col-1);
                moves.addAll(pawnAttack(board, myPosition, leftPosition, row + 1, promotionRow));

                // two steps forward move
                ChessPosition twoStepPosition = new ChessPosition(row + 2, col);
                if (board.isPositionValid(twoStepPosition)
                        && !board.isPositionOccupiedByEnemy(twoStepPosition, myPosition)
                        && !board.isPositionOccupiedByFriendly(twoStepPosition, myPosition)
                        && !board.isPositionOccupiedByFriendly(frontPosition, myPosition)
                        && row == initialRow) {
                    moves.add(new ChessMove(myPosition, twoStepPosition, null));
                }
            }
            if (board.getPiece(myPosition).pieceColor == BLACK) {
                // move forward
                ChessPosition frontPosition = new ChessPosition(row - 1, col);
                if (board.isPositionValid(frontPosition)
                        && !board.isPositionOccupiedByFriendly(frontPosition, myPosition)
                        && !board.isPositionOccupiedByEnemy(frontPosition, myPosition)) {
                    moves.addAll(addMoves(promotionRow, row - 1, myPosition, frontPosition));
                }
                //right attack
                ChessPosition rightPosition = new ChessPosition(row - 1, col +1);
                moves.addAll(pawnAttack(board, myPosition, rightPosition, row - 1, promotionRow));

                // left attack
                ChessPosition leftPosition = new ChessPosition(row-1, col-1);
                moves.addAll(pawnAttack(board, myPosition, leftPosition, row - 1, promotionRow));

                // two steps forward move
                ChessPosition twoStepPosition = new ChessPosition(row - 2, col);
                if (board.isPositionValid(twoStepPosition)
                        && !board.isPositionOccupiedByEnemy(twoStepPosition, myPosition)
                        && !board.isPositionOccupiedByFriendly(twoStepPosition, myPosition)
                        && !board.isPositionOccupiedByFriendly(frontPosition, myPosition)
                        && row == initialRow) {
                    moves.add(new ChessMove(myPosition, twoStepPosition, null));
                }
            }
            return moves;
        }
    }

    @Override
    public String toString() {
        String toStringType = switch(getPieceType()) {
            case QUEEN -> "Q";
            case BISHOP -> "B";
            case KING -> "K";
            case ROOK -> "R";
            case PAWN -> "P";
            case KNIGHT -> "KN";
            default -> throw new RuntimeException("invalid type in pieceMoves");
        };
        return toStringType;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
}
