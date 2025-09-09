package chess;

import java.io.Console;
import java.util.ArrayList;
import java.util.Collection;

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
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        Collection<ChessMove> validPieceMoves = new ArrayList<ChessMove>();
        if (chessBoard.getPiece(startPosition) == null) {
            return validPieceMoves;
        }
        Collection<ChessMove> pieceMoves = chessBoard.getPiece(startPosition).pieceMoves(chessBoard, startPosition);
        for (ChessMove move : pieceMoves) {
            if (!simulatedMoveResultsInCheck(move, chessBoard.getPiece(startPosition))) {
                validPieceMoves.add(move);
            }
        }
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
        ChessPiece.PieceType pieceType = piece.getPieceType();
        if (move.getPromotionPiece() != null) {
            pieceType = move.getPromotionPiece();
        }
        chessBoard.addPiece(move.getStartPosition(), null);
        chessBoard.addPiece(move.getEndPosition(), new ChessPiece(teamTurn, pieceType));
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (validMoves(move.getStartPosition()).contains(move) && chessBoard.getPiece(move.getStartPosition()).getTeamColor() == teamTurn) {
            doMoveWithoutChecking(move);
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
}
