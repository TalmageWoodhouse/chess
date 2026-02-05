package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor turn;
    private ChessBoard board;
    public ChessGame() {
        this.turn = TeamColor.WHITE;
        this.board = new ChessBoard();
        board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return turn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.turn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPos the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPos) {
        ChessPiece piece = board.getPiece(startPos);
        if (piece == null) {
            return null;
        }
        Collection<ChessMove> moves = piece.pieceMoves(board, startPos);
        Collection<ChessMove> validMoves = new ArrayList<>();
        //iterate through possible moves from start pos
        for (ChessMove move : moves) {
            // make a copy of the board
            ChessBoard boardCopy = board.boardCopy();
            //make move in the copy
            boardCopy.addPiece(move.getStartPosition(), null);
            boardCopy.addPiece(move.getEndPosition(), piece);

            ChessBoard originalBoard = this.board;
            this.board = boardCopy;

            //check if copy not inCheck
            if (!isInCheck(piece.getTeamColor())) {
                validMoves.add(move);
            }
            this.board = originalBoard;
        }
        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPos = move.getStartPosition();
        ChessPosition endPos = move.getEndPosition();
        ChessPiece.PieceType promotionP = move.getPromotionPiece();
        ChessPiece piece = board.getPiece(startPos);

        if (piece == null) {
            throw new InvalidMoveException("No piece at start position.");
        }
        if (piece.getTeamColor() != turn) {
            throw new InvalidMoveException("It is not your turn to move.");
        }
        Collection<ChessMove> validMoves = validMoves(startPos);
        //check if move is invalid
        if (!validMoves.contains(move)) {
            throw new InvalidMoveException("Invalid move.");
        }
        //check if promotion move and do move
        if (promotionP != null) {
            board.addPiece(endPos, new ChessPiece(getTeamTurn(), promotionP));
        } else {
            board.addPiece(endPos, piece);
        }
        board.addPiece(startPos, null);
        //change turn
        if (turn == TeamColor.BLACK) {
            turn = TeamColor.WHITE;
        } else {
            turn = TeamColor.BLACK;
        }
    }

    private ChessPosition findKing(TeamColor teamColor) {
        for (int row = 1; row < 9; row++) {
            for (int col = 1; col < 9; col++) {
                ChessPosition kingPos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(kingPos);
                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor) {
                    return kingPos;
                }
            }
        }
        throw new IllegalStateException("king not found for team: " + teamColor);
    }

    private List<ChessPosition> getEnemyPieces(TeamColor teamColor) {
        List<ChessPosition> enemyPositions = new ArrayList<>();

        for (int row = 1; row < 9; row++) {
            for (int col = 1; col < 9; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);
                if (piece == null || piece.getTeamColor() == teamColor) {
                    continue; // skip empty squares and friendly pieces
                }
                enemyPositions.add(pos);
            }
        }
        return enemyPositions;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        //locate the king for the given team
        ChessPosition kingPos = findKing(teamColor);

        //look at every opposing piece
        for (ChessPosition enemyPos : getEnemyPieces(teamColor)) {
            ChessPiece enemy = board.getPiece(enemyPos);
            //could this piece move to the kings square
            for (ChessMove move : enemy.pieceMoves(board, enemyPos)) {
                if (move.getEndPosition().equals(kingPos)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
        return checkMoves(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        return checkMoves(teamColor);
    }

    private boolean checkMoves(TeamColor teamColor) {
        for(int row = 1; row < 9; row++) {
            for (int col = 1; col < 9; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);

                if (piece != null && piece.getTeamColor() == teamColor && !validMoves(pos).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
