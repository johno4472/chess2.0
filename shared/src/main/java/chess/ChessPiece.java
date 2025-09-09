package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private ChessGame.TeamColor pieceColor;
    private PieceType type;
    private Boolean hasMoved;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
        this.hasMoved = false;
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

    @Override
    public String toString() {
        return pieceColor.toString() + " " + type.toString();
    }

    public Boolean getHasMoved() {
        return hasMoved;
    }

    public void moved() {
        hasMoved = true;
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
        Collection<ChessMove> possibleMoves = new ArrayList<ChessMove>();
        switch(type){
            case KING:
                appendDiagonalMoves(false, board, myPosition, possibleMoves);
                appendHorizontalMoves(false, board, myPosition, possibleMoves);
                break;
            case QUEEN:
                appendDiagonalMoves(true, board, myPosition, possibleMoves);
                appendHorizontalMoves(true, board, myPosition, possibleMoves);
                break;
            case BISHOP:
                appendDiagonalMoves(true, board, myPosition, possibleMoves);
                break;
            case KNIGHT:
                appendMovesIfValid(false, false, board, myPosition, possibleMoves, 2, 1);
                appendMovesIfValid(false, false, board, myPosition, possibleMoves, 2, -1);
                appendMovesIfValid(false, false, board, myPosition, possibleMoves, -2, 1);
                appendMovesIfValid(false, false, board, myPosition, possibleMoves, -2, -1);
                appendMovesIfValid(false, false, board, myPosition, possibleMoves, 1, 2);
                appendMovesIfValid(false, false, board, myPosition, possibleMoves, 1, -2);
                appendMovesIfValid(false, false, board, myPosition, possibleMoves, -1, -2);
                appendMovesIfValid(false, false, board, myPosition, possibleMoves, -1, 2);
                break;
            case ROOK:
                appendHorizontalMoves(true, board, myPosition, possibleMoves);
                break;
            case PAWN:
                int modifier = 1;
                int startRow = 2;
                int endRow = 8;
                if (pieceColor == ChessGame.TeamColor.BLACK) {
                    modifier = -1;
                    startRow = 7;
                    endRow = 1;
                }
                if (myPosition.getRow() == startRow && board.getPiece(new ChessPosition(startRow + modifier, myPosition.getColumn())) == null) {
                    appendMovesIfValid(false, false, board, myPosition, possibleMoves, 2 * modifier, 0);
                }
                boolean promotion = false;
                if (myPosition.getRow() + modifier == endRow) {
                    promotion = true;
                }
                appendMovesIfValid(false, promotion, board, myPosition, possibleMoves, modifier, 0);
                appendMovesIfValid(false, promotion, board, myPosition, possibleMoves, modifier, -1);
                appendMovesIfValid(false, promotion, board, myPosition, possibleMoves, modifier, 1);
        }
        return possibleMoves;
    }

    public void appendDiagonalMoves(boolean unlimitedMove, ChessBoard board, ChessPosition myPosition, Collection<ChessMove> possibleMoves) {
        appendMovesIfValid(unlimitedMove, false, board, myPosition, possibleMoves, -1, -1);
        appendMovesIfValid(unlimitedMove, false, board, myPosition, possibleMoves, -1, 1);
        appendMovesIfValid(unlimitedMove, false, board, myPosition, possibleMoves, 1, 1);
        appendMovesIfValid(unlimitedMove, false, board, myPosition, possibleMoves, 1, -1);
    }

    public void appendHorizontalMoves(boolean unlimitedMove, ChessBoard board, ChessPosition myPosition, Collection<ChessMove> possibleMoves) {
        appendMovesIfValid(unlimitedMove, false, board, myPosition, possibleMoves, 1, 0);
        appendMovesIfValid(unlimitedMove, false, board, myPosition, possibleMoves, -1, 0);
        appendMovesIfValid(unlimitedMove, false, board, myPosition, possibleMoves, 0, -1);
        appendMovesIfValid(unlimitedMove, false, board, myPosition, possibleMoves, 0, 1);
    }

    public void appendMovesIfValid(boolean unlimitedMove, boolean promotedPawn, ChessBoard board, ChessPosition myPosition,
                                   Collection<ChessMove> possibleMoves, int rowDirection, int columnDirection) {
        int row = myPosition.getRow();
        int column = myPosition.getColumn();
        for (int i = 0; i < 8; i++) {
            row += rowDirection;
            column += columnDirection;
            if (row <= 0 || row >= 9 || column <= 0 || column >= 9) {
                break;
            }
            ChessPiece targetSpacePiece = board.getPiece(new ChessPosition(row, column));
            if (targetSpacePiece != null) {
                if (targetSpacePiece.pieceColor != this.pieceColor) {
                    if (this.type == PieceType.PAWN && columnDirection == 0) {
                        break;
                    }
                    else if (promotedPawn) {
                        addPossibleMovesWithPromotion(myPosition, new ChessPosition(row, column), possibleMoves);
                    }
                    else {
                        possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row, column), null));
                    }
                }
                break;
            }
            else {
                if (this.type == PieceType.PAWN && Math.abs(columnDirection) == 1) {
                    break;
                }
                else if (promotedPawn) {
                    addPossibleMovesWithPromotion(myPosition, new ChessPosition(row, column), possibleMoves);
                }
                else {
                    possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row, column), null));
                }
            }
            if (!unlimitedMove) {
                break;
            }
        }
    }

    private void addPossibleMovesWithPromotion(ChessPosition myPosition, ChessPosition newPosition, Collection<ChessMove> possibleMoves) {
        possibleMoves.add(new ChessMove(myPosition, newPosition, PieceType.QUEEN));
        possibleMoves.add(new ChessMove(myPosition, newPosition, PieceType.ROOK));
        possibleMoves.add(new ChessMove(myPosition, newPosition, PieceType.BISHOP));
        possibleMoves.add(new ChessMove(myPosition, newPosition, PieceType.KNIGHT));
    }
}
