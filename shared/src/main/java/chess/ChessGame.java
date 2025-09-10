package chess;

import java.io.Console;
import java.text.CollationElementIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor teamTurn;
    private ChessBoard chessBoard;

    public ChessGame() {
        teamTurn = TeamColor.WHITE;
        chessBoard = new ChessBoard();
        chessBoard.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {

        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    public TeamColor otherTeam(TeamColor team) {
        if (team == TeamColor.WHITE) {
            return TeamColor.BLACK;
        }
        else {
            return TeamColor.WHITE;
        }
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    private void addSpecificCastleMoveIfValid(ChessPosition rookPosition, ChessPosition kingPosition, Collection<ChessMove> castleMove) {
        //check if rookPosition has rook and if rook has moved
        ChessPiece rook = chessBoard.getPiece(rookPosition);
        ChessPiece king = chessBoard.getPiece(kingPosition);
        if (rook == null || rook.getHasMoved() == true) {
            return;
        }
        int modifier = 1;
        if (kingPosition.getColumn() - rookPosition.getColumn() > 0) {
            modifier = -1;
        }
        //check if spaces empty between them
        //check if king moving once or twice will put him in check
        for (int i = 1; i < (Math.abs(rookPosition.getColumn() - kingPosition.getColumn())); i++) {
            int newColumn = kingPosition.getColumn() + (i * modifier);
            ChessPosition targetSquare = new ChessPosition(kingPosition.getRow(), newColumn);
            if (chessBoard.getPiece(targetSquare) != null) {
                return;
            }
            if (simulatedMoveResultsInCheck(new ChessMove(kingPosition, targetSquare, null), king)) {
                return;
            }
        }
        //return move where king moves two toward rook and rook goes on other side of king
        castleMove.add(new ChessMove(kingPosition, new ChessPosition(kingPosition.getRow(), kingPosition.getColumn() + (2 * modifier)), null));
    }

    private Collection<ChessMove> validCastlingMove(ChessPosition startPosition) {
        Collection<ChessMove> castleMove = new ArrayList<ChessMove>();
        //check if king has moved
        if (chessBoard.getPiece(startPosition).getHasMoved() || isInCheck(chessBoard.getPiece(startPosition).getTeamColor())) {
            return castleMove;
        }
        int kingRow = startPosition.getRow();
        //call addSpecific castle move for left
        addSpecificCastleMoveIfValid(new ChessPosition(kingRow, 1), startPosition, castleMove);
        //call add specific castle move for right
        addSpecificCastleMoveIfValid(new ChessPosition(kingRow, 8), startPosition, castleMove);

        return castleMove;
    }


    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        Collection<ChessMove> validPieceMoves = new ArrayList<ChessMove>();
        ChessPiece piece = chessBoard.getPiece(startPosition);
        if (piece == null) {
            return validPieceMoves;
        }
        Collection<ChessMove> pieceMoves = piece.pieceMoves(chessBoard, startPosition);
        for (ChessMove move : pieceMoves) {
            if (!simulatedMoveResultsInCheck(move, piece)) {
                validPieceMoves.add(move);
            }
        }
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            Collection<ChessMove> castleMove = validCastlingMove(startPosition);
            if (!castleMove.isEmpty()) {
                validPieceMoves.addAll(castleMove);
            }
        }
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN && piece.getEnPassant()) {
            validPieceMoves.add(piece.getEnPassantMove());
            piece.setEnPassant(false);
        }

        //if piece is pawn and en passant is true
            //add en passant move to validMoves (current row + enpassant column)
        return validPieceMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void doMoveWithoutChecking(ChessMove move) {
        ChessPiece piece = chessBoard.getPiece(move.getStartPosition());
        Boolean hasMoved = piece.getHasMoved();
        ChessPiece.PieceType pieceType = piece.getPieceType();
        TeamColor color  = piece.getTeamColor();
        if (move.getPromotionPiece() != null) {
            piece.changePieceType(move.getPromotionPiece());
        }
        chessBoard.addPiece(move.getStartPosition(), null);
        chessBoard.addPiece(move.getEndPosition(), piece);
    }

    public void enPassant(ChessPosition pawnSpot, int colDirection) {
        //take position of pawn that moved two
        //is there a pawn to the left or right of it?
        int row = pawnSpot.getRow();
        int col = pawnSpot.getColumn();
        if (col + colDirection > 0 && col + colDirection < 9) {
            ChessPosition neighborPosition = new ChessPosition(row, pawnSpot.getColumn() - colDirection);
            ChessPiece neighbor = chessBoard.getPiece(neighborPosition);
            if (neighbor != null && neighbor.getPieceType() == ChessPiece.PieceType.PAWN && neighbor.getTeamColor() != chessBoard.getPiece(pawnSpot).getTeamColor()) {
                neighbor.setEnPassant(true);
                int rowDirection = 1;
                if (neighbor.getTeamColor() == TeamColor.BLACK) {
                    rowDirection = -1;
                }
                neighbor.setEnPassantMove(new ChessMove(neighborPosition, new ChessPosition(neighborPosition.getRow() + rowDirection, neighborPosition.getColumn() + colDirection), null));
            }
        }
            //if so, mark that pawn as En Passant -> true and column of pawn that can be killed
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = chessBoard.getPiece(move.getStartPosition());
        if (validMoves(move.getStartPosition()).contains(move) && piece.getTeamColor() == teamTurn) {
            doMoveWithoutChecking(move);
            piece.moved();
            //if piece is pawn
            if (piece.getPieceType() == ChessPiece.PieceType.PAWN && piece.getEnPassantMove() == move) {
                ChessPosition enemyPosition = new ChessPosition(move.getStartPosition().getRow(), move.getEndPosition().getColumn());
                chessBoard.addPiece(enemyPosition, null);
            }
            int colDifference = Math.abs(move.getStartPosition().getColumn() - move.getEndPosition().getColumn());
            if (piece.getPieceType() == ChessPiece.PieceType.PAWN && colDifference == 2) {
                enPassant(move.getEndPosition(), -1);
                enPassant(move.getEndPosition(), 1);
            }
                //and move was 2, do En Passant method
            if (piece.getPieceType() == ChessPiece.PieceType.KING && Math.abs(move.getStartPosition().getColumn() - move.getEndPosition().getColumn()) == 2) {
                int rookStartColumn = 8;
                int rookEndColumn = 6;
                int rookRow = move.getStartPosition().getRow();
                if (move.getStartPosition().getColumn() > move.getEndPosition().getColumn()) {
                    rookStartColumn = 1;
                    rookEndColumn = 4;
                }
                doMoveWithoutChecking(new ChessMove(new ChessPosition(rookRow, rookStartColumn), new ChessPosition(rookRow, rookEndColumn), null));
            }
            teamTurn = otherTeam(teamTurn);
        }
        else {
            throw new InvalidMoveException();
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        TeamColor opponent = otherTeam(teamColor);
        //for each square of board
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPiece square = chessBoard.getPiece(new ChessPosition(i, j));
                if (square != null && square.getTeamColor() == opponent) {
                    Collection<ChessMove> opponentMoves = square.pieceMoves(chessBoard, new ChessPosition(i, j));
                    for (ChessMove move : opponentMoves) {
                        ChessPiece targetSquare = chessBoard.getPiece(move.getEndPosition());
                        if (targetSquare != null && targetSquare.getPieceType() == ChessPiece.PieceType.KING) {
                            return true;
                        }
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
        //if king is not in check, return false
        if (!isInCheck(teamColor)) {
            return false;
        }
        //for all pieces of team color
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPiece square = chessBoard.getPiece(new ChessPosition(i, j));
                if (square != null && square.getTeamColor() == teamColor) {
                    //if the result of the move is king not being in check, return false
                    Collection<ChessMove> potentialMoves = square.pieceMoves(chessBoard, new ChessPosition(i, j));
                    for (ChessMove move : potentialMoves) {
                        if (!simulatedMoveResultsInCheck(move, square)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private ChessBoard copyBoard() {
        ChessBoard newBoard = new ChessBoard();
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPiece piece = chessBoard.getPiece(new ChessPosition(i, j));
                ChessPiece newPiece;
                if (piece == null) {
                     newPiece = null;
                }
                else {
                    newPiece = new ChessPiece(piece.getTeamColor(), piece.getPieceType());
                }
                newBoard.addPiece(new ChessPosition(i, j), newPiece);
            }
        }
        return newBoard;
    }

    private boolean simulatedMoveResultsInCheck(ChessMove move, ChessPiece piece) {
        ChessGame simulation;
        simulation = new ChessGame();
        simulation.chessBoard = this.copyBoard();
        try {
            simulation.doMoveWithoutChecking(move);
        }
        catch (Exception e) {
            return true;
        }
        return simulation.isInCheck(piece.getTeamColor());
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
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPiece square = chessBoard.getPiece(new ChessPosition(i, j));
                if (square != null && square.getTeamColor() == teamColor) {
                    Collection<ChessMove> validMoves = this.validMoves(new ChessPosition(i, j));
                    if (!validMoves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {

        chessBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {

        return chessBoard;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return teamTurn == chessGame.teamTurn && Objects.equals(chessBoard, chessGame.chessBoard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, chessBoard);
    }
}
