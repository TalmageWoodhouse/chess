package ui;

import chess.*;

import static chess.ChessGame.TeamColor.WHITE;
import static ui.EscapeSequences.*;

public class BoardRenderer {

    public static void draw(ChessGame game) {
        ChessBoard board = game.getBoard();

        System.out.println();

        for (int row = 8; row >= 1; row--) {
            System.out.print(SET_TEXT_COLOR_WHITE + row + " ");

            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);

                boolean isLight = (row + col) % 2 == 0;
                String bg = isLight ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_DARK_GREEN;

                System.out.print(bg);

                if (piece == null) {
                    System.out.print(EMPTY);
                } else {
                    System.out.print(getPieceSymbol(piece));
                }

                System.out.print(RESET_BG_COLOR);
            }

            System.out.println(RESET_TEXT_COLOR);
        }

        // column labels
        System.out.print("  ");
        for (char c = 'a'; c <= 'h'; c++) {
            System.out.print(" " + c + " ");
        }
        System.out.println("\n");
    }

    private static String getPieceSymbol(ChessPiece piece) {
        return switch (piece.getPieceType()) {
            case KING -> piece.getTeamColor() == WHITE ? WHITE_KING : BLACK_KING;
            case QUEEN -> piece.getTeamColor() == WHITE ? WHITE_QUEEN : BLACK_QUEEN;
            case ROOK -> piece.getTeamColor() == WHITE ? WHITE_ROOK : BLACK_ROOK;
            case BISHOP -> piece.getTeamColor() == WHITE ? WHITE_BISHOP : BLACK_BISHOP;
            case KNIGHT -> piece.getTeamColor() == WHITE ? WHITE_KNIGHT : BLACK_KNIGHT;
            case PAWN -> piece.getTeamColor() == WHITE ? WHITE_PAWN : BLACK_PAWN;
        };
    }
}
