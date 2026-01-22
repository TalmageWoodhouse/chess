package chess;

import java.util.*;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private ChessGame.TeamColor pieceColor;
    private ChessPiece.PieceType type;
    private PieceMovesCalculator movesCalculator;
    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
        switch (type) {
            case ROOK -> movesCalculator = new RookMoveCalc();
            case BISHOP -> movesCalculator = new BishopMoveCalc();
            case QUEEN -> movesCalculator = new QueenMoveCalc();
//            case ROOK -> movesCalculator = new RookMoveCalc();
//            case ROOK -> movesCalculator = new RookMoveCalc();
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
            Collection<ChessMove> moves = new ArrayList<>();
            int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

            int row = myPos.getRow();
            int col = myPos.getColumn();
            for (
                    int[] d : directions) {
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
    }

    public class BishopMoveCalc implements PieceMovesCalculator {
        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPos) {
            Collection<ChessMove> moves = new ArrayList<>();
            int[][] directions = {{1, 1}, {-1, -1}, {-1, 1}, {1, -1}};

            int row = myPos.getRow();
            int col = myPos.getColumn();
            for (
                    int[] d : directions) {
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
    }

    public class QueenMoveCalc implements PieceMovesCalculator {
        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPos) {
            Collection<ChessMove> moves = new ArrayList<>();
            int[][] directions = {{1, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}};

            int row = myPos.getRow();
            int col = myPos.getColumn();
            for (
                    int[] d : directions) {
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
    }

    public class KingMoveCalc implements PieceMovesCalculator {
        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPos) {
            Collection<ChessMove> moves = new ArrayList<>();
            int[][] directions = {{1, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}};

            int row = myPos.getRow();
            int col = myPos.getColumn();
            for (
                    int[] d : directions) {
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
