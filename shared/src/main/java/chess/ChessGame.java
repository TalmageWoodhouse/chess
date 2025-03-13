package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor turn;

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
    //
    /**
     * Gets valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {return null;}
        Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();
        // iterate through all possible moves
        for (ChessMove move : moves) {
            //make copy of the board
            ChessBoard boardCopy = board.boardCopy();
                //make move in the copy
                boardCopy.addPiece(move.getStartPosition(), null);
                boardCopy.addPiece(move.getEndPosition(), piece);

                ChessBoard originalBoard = this.board;
                this.board = boardCopy;

                //check copy if != isincheck
                if (!isInCheck(piece.getTeamColor())) {
                    validMoves.add(move);
                }

                this.board = originalBoard;
        }
        return validMoves;
    }
    //
    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPos = move.getStartPosition();
        ChessPosition endPos = move.getEndPosition();
        ChessPiece.PieceType promotionPiece = move.getPromotionPiece();
        ChessPiece piece = board.getPiece(startPos);

        if (piece == null) {
            throw new InvalidMoveException("No piece at start position");
        }
        // ensure that its the turn of piece being moved
        if ( piece.getTeamColor() != turn) {
            throw new InvalidMoveException("it's not your turn");
        }

        Collection<ChessMove> validMoves = validMoves(startPos);
        // if not valid move
        if (!validMoves.contains(move)) {
            throw new InvalidMoveException("Invalid move.");
        }
        //check if promotion move and do move
        if(promotionPiece != null){
            board.addPiece(endPos, new ChessPiece(getTeamTurn(), move.getPromotionPiece()));
        }
        else{
            board.addPiece(endPos, board.getPiece(startPos));
        }
        board.addPiece(startPos, null);

        // change turns
        turn = (turn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }
    //
    private ChessPosition findKing(TeamColor teamColor) {
        for (int row = 1; row < 9; row++) {
            for (int col= 1; col < 9; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);

                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor) {
                    return pos;
                }
            }
        }
        throw new IllegalStateException("king not found for team: " + teamColor);
    }
    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        //locate the king of the given team color
        ChessPosition kingPosition = findKing(teamColor);

        //check if opponents pieces can attack king
        for (int row= 1; row < 9; row++) {
            for (int col = 1; col <9; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);

                //skip empty and friendly squares
                if (piece == null || piece.getTeamColor() == teamColor) {
                    continue;
                }
                // if this opponent piece can move to the kings position return true
                Collection<ChessMove> enemyMoves = piece.pieceMoves(board, pos);
                for (ChessMove move : enemyMoves){
                    if (move.getEndPosition().equals(kingPosition)) {
                        return true;
                    }
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

        // Iterate through all pieces of the given team
        return iterate(teamColor);
    }

    private boolean iterate(TeamColor teamColor) {
        for (int row = 1; row < 9; row++) {
            for (int col = 1; col < 9; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);

                if (piece != null && piece.getTeamColor() == teamColor && !validMoves(pos).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }

        // Iterate through all pieces of the given team
        return iterate(teamColor);
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
