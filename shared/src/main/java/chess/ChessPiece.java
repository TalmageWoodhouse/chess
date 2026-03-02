package chess;

import java.util.*;

import static chess.ChessGame.TeamColor.WHITE;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece implements PieceMovesCalculator {
    private final ChessGame.TeamColor pieceColor;
    private final ChessPiece.PieceType type;
    private final PieceMovesCalculator movesCalculator;
    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
        switch (type) {
            case ROOK -> movesCalculator = new RookMoveCalc();
            case BISHOP -> movesCalculator = new BishopMoveCalc();
            case QUEEN -> movesCalculator = new QueenMoveCalc();
            case KING -> movesCalculator = new KingMoveCalc();
            case KNIGHT -> movesCalculator = new KnightMoveCalc();
            case PAWN -> movesCalculator = new PawnMoveCalc();
            default -> movesCalculator = new DefaultCalc();
        }
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
        return movesCalculator.pieceMoves(board, myPosition);
    }

    public interface PieceMovesCalculator {
        Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition);
    }

    public class RookMoveCalc implements PieceMovesCalculator {
        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPos) {
            int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            return slideMoves(board, myPos, directions);
        }
    }

    public class BishopMoveCalc implements PieceMovesCalculator {
        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPos) {
            int[][] directions = {{1, 1}, {-1, -1}, {-1, 1}, {1, -1}};
            return slideMoves(board, myPos, directions);
        }
    }

    public class QueenMoveCalc implements PieceMovesCalculator {
        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPos) {
            int[][] directions = {{1, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            return slideMoves(board, myPos, directions);
        }
    }

    public class KingMoveCalc implements PieceMovesCalculator {
        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPos) {
            int[][] directions = {{1, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            return jumpMoves(board, myPos, directions);
        }
    }

    public class KnightMoveCalc implements PieceMovesCalculator {
        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPos) {
            int[][] directions = {{2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {1, -2}, {-1, 2}, {-1, -2}};
            return jumpMoves(board, myPos, directions);
        }
    }

    public class PawnMoveCalc implements PieceMovesCalculator {
        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPos) {
            Collection<ChessMove> moves = new ArrayList<>();

            int promotionRow = (board.getPiece(myPos).pieceColor == WHITE) ? 8:1;
            int row = myPos.getRow();
            int col = myPos.getColumn();

            if (pieceColor == ChessGame.TeamColor.BLACK) {
                row -= 1;
                // Check front
                ChessPosition frontPos = new ChessPosition(row, col);
                if (board.isInBounds(frontPos) && !board.isFriendly(myPos, frontPos) && !board.isEnemy(myPos, frontPos)) {
                    addMoves(myPos, moves, row, frontPos, promotionRow);
                }
                // Attack Right
                ChessPosition rightPos = new ChessPosition(row, col - 1);
                if (board.isInBounds(rightPos) && board.isEnemy(myPos, rightPos)) {
                    addMoves(myPos, moves, row, rightPos, promotionRow);
                }

                // Attack Left
                ChessPosition leftPos = new ChessPosition(row, col + 1);
                if (board.isInBounds(leftPos) && board.isEnemy(myPos, leftPos)) {
                    addMoves(myPos, moves, row, leftPos, promotionRow);
                }

                // two-step
                if (myPos.getRow() == 7) {
                    row -= 1;
                    ChessPosition checkPos = new ChessPosition(row, col);
                    if (board.isInBounds(checkPos) && !board.isFriendly(myPos, checkPos) && !board.isEnemy(myPos, checkPos)
                        && !board.isFriendly(myPos, frontPos) && !board.isEnemy(myPos, frontPos)) {
                        moves.add(new ChessMove(myPos, checkPos, null));
                    }
                }
            } else {
                row += 1;
                //Check Front
                ChessPosition frontPos = new ChessPosition(row, col);
                if (board.isInBounds(frontPos) && !board.isFriendly(myPos, frontPos) && !board.isEnemy(myPos, frontPos)) {
                    addMoves(myPos, moves, row, frontPos, promotionRow);
                }
                //Attack Right
                ChessPosition rightPos = new ChessPosition(row, col + 1);
                if (board.isInBounds(rightPos) && board.isEnemy(myPos, rightPos)) {
                    addMoves(myPos, moves, row, rightPos, promotionRow);
                }
                //Attack Left
                ChessPosition leftPos = new ChessPosition(row, col - 1);
                if (board.isInBounds(leftPos) && board.isEnemy(myPos, leftPos)) {
                    addMoves(myPos, moves, row, leftPos, promotionRow);
                }
                // two-step
                if (myPos.getRow() == 2) {
                    row += 1;
                    ChessPosition checkPos = new ChessPosition(row, col);
                    if (board.isInBounds(checkPos) && !board.isFriendly(myPos, checkPos) && !board.isEnemy(myPos, checkPos)
                        && !board.isFriendly(myPos, frontPos) && !board.isEnemy(myPos, frontPos)) {
                        moves.add(new ChessMove(myPos, checkPos, null));
                    }
                }
            }
            return moves;
        }

        private void addMoves(ChessPosition myPos, Collection<ChessMove> moves, int row, ChessPosition frontPos, int promotionRow) {
            if (row == promotionRow) {
                moves.add(new ChessMove(myPos, frontPos, PieceType.ROOK));
                moves.add(new ChessMove(myPos, frontPos, PieceType.KNIGHT));
                moves.add(new ChessMove(myPos, frontPos, PieceType.BISHOP));
                moves.add(new ChessMove(myPos, frontPos, PieceType.QUEEN));
            } else {
                moves.add(new ChessMove(myPos, frontPos, null));
            }
        }
    }

    public class DefaultCalc implements PieceMovesCalculator {

        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
            return List.of();
        }
    }


    @Override
    public String toString() {
        return "ChessPiece{" +
                "pieceColor=" + pieceColor +
                ", type=" + type +
                '}';
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
