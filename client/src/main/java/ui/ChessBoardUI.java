package ui;

import chess.*;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static chess.ChessGame.TeamColor.WHITE;
import static ui.EscapeSequences.*;

public class ChessBoardUI {

    private static final int BOARD_SIZE = 8;
    private static final int SQUARE_SIZE = 1;

    public static void draw(ChessGame game) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

        out.print(ERASE_SCREEN);

        drawHeaders(out);
        drawBoard(out, game);

        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_WHITE);
    }

    private static void drawHeaders(PrintStream out) {
        out.print(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK);
        out.print("   ");
        for (char c = 'a'; c <= 'h'; c++) {
            out.print(" " + c + " ");
        }
        out.println();
    }

    private static void drawBoard(PrintStream out, ChessGame game) {
        ChessBoard board = game.getBoard();

        for (int row = 8; row >= 1; row--) {
            drawRow(out, board, row);
        }
    }

    private static void drawRow(PrintStream out, ChessBoard board, int row) {

        for (int squareRow = 0; squareRow < SQUARE_SIZE; squareRow++) {
            // row label on left
            if (squareRow == SQUARE_SIZE / 2) {
                out.print(" " + row + " ");
            } else {
                out.print("   ");
            }

            for (int col = 1; col <= BOARD_SIZE; col++) {
                boolean isLight = (row + col) % 2 == 0;
                if (isLight) setLight(out);
                else setDark(out);

                if (squareRow == SQUARE_SIZE / 2) {
                    ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                    printPiece(out, piece);
                } else {
                    out.print(EMPTY.repeat(SQUARE_SIZE));
                }

                out.print(RESET_BG_COLOR);
            }

            // row label on right
            if (squareRow == SQUARE_SIZE / 2) {
                out.print(" " + row);
            }

            out.println();
        }
    }

    private static void printPiece(PrintStream out, ChessPiece piece) {
        if (piece == null) {
            out.print(EMPTY);
            return;
        }

        String symbol = getSymbol(piece);

        if (piece.getTeamColor() == WHITE) {
            out.print(SET_TEXT_COLOR_WHITE);
        } else {
            out.print(SET_TEXT_COLOR_BLACK);
        }

        out.print(symbol);
    }

    private static String getSymbol(ChessPiece piece) {
        return switch (piece.getPieceType()) {
            case KING -> piece.getTeamColor() == WHITE ? WHITE_KING : BLACK_KING;
            case QUEEN -> piece.getTeamColor() == WHITE ? WHITE_QUEEN : BLACK_QUEEN;
            case ROOK -> piece.getTeamColor() == WHITE ? WHITE_ROOK : BLACK_ROOK;
            case BISHOP -> piece.getTeamColor() == WHITE ? WHITE_BISHOP : BLACK_BISHOP;
            case KNIGHT -> piece.getTeamColor() == WHITE ? WHITE_KNIGHT : BLACK_KNIGHT;
            case PAWN -> piece.getTeamColor() == WHITE ? WHITE_PAWN : BLACK_PAWN;
        };
    }

    private static void setLight(PrintStream out) {
        out.print(SET_BG_COLOR_LIGHT_GREY);
    }

    private static void setDark(PrintStream out) {
        out.print(SET_BG_COLOR_DARK_GREEN);
    }
}
