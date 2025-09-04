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

    private ChessGame.TeamColor _pieceColor;
    private PieceType _type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        _pieceColor = pieceColor;
        _type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return _pieceColor == that._pieceColor && _type == that._type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_pieceColor, _type);
    }

    @Override
    public String toString() {
        return _pieceColor.toString() + " " + _type.toString();
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

        return _pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {

        return _type;
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
        switch(_type){
            case KING:
                appendDiagonalMoves(false, board, myPosition, possibleMoves);
                appendHorizontalMoves(false, board, myPosition, possibleMoves);
                break;
        }
        return possibleMoves;
    }

    public void appendDiagonalMoves(boolean unlimitedMove, ChessBoard board, ChessPosition myPosition, Collection<ChessMove> possibleMoves) {
        appendMovesIfValid(unlimitedMove, board, myPosition, possibleMoves, -1, -1);
        appendMovesIfValid(unlimitedMove, board, myPosition, possibleMoves, -1, 1);
        appendMovesIfValid(unlimitedMove, board, myPosition, possibleMoves, 1, 1);
        appendMovesIfValid(unlimitedMove, board, myPosition, possibleMoves, 1, -1);
    }

    public void appendHorizontalMoves(boolean unlimitedMove, ChessBoard board, ChessPosition myPosition, Collection<ChessMove> possibleMoves) {
        appendMovesIfValid(unlimitedMove, board, myPosition, possibleMoves, 1, 0);
        appendMovesIfValid(unlimitedMove, board, myPosition, possibleMoves, -1, 0);
        appendMovesIfValid(unlimitedMove, board, myPosition, possibleMoves, 0, -1);
        appendMovesIfValid(unlimitedMove, board, myPosition, possibleMoves, 0, 1);
    }

    public void appendMovesIfValid(boolean unlimitedMove, ChessBoard board, ChessPosition myPosition, Collection<ChessMove> possibleMoves, int xDirection, int yDirection) {
        int row = myPosition.getRow();
        int column = myPosition.getColumn();
        for (int i = 0; i < 8; i++) {
            row += xDirection;
            column += yDirection;
            if (row <= 0 || row >= 9 || column <= 0 || column >= 9) {
                break;
            }
            ChessPiece targetSpacePiece = board.getPiece(new ChessPosition(row, column));
            if (targetSpacePiece != null) {
                if (targetSpacePiece._pieceColor != this._pieceColor) {
                    possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row, column), null));
                }
                break;
            }
            else {
                possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row, column), null));
            }
            if (!unlimitedMove) {
                break;
            }
        }
    }
}
