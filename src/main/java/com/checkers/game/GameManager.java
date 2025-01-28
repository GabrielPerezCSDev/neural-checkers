package main.java.com.checkers.game;

import java.util.ArrayList;
import main.java.com.checkers.util.GameResponse;
import main.java.com.checkers.util.GameResponseUtil;

public class GameManager {

  private Checkers checkersGame;
  private int playerColor;
  private int aiColor;
  private boolean setHasLegalMoves;

  public GameManager() {
    this.checkersGame = new Checkers();
    this.setHasLegalMoves = false;
    checkersGame.intializeBoard();
  }

  /**
   * Starts a new game with the specified difficulty and player color.
   */
  public GameResponse<Void> startGame(int difficulty, int playerColor) {
    this.playerColor = playerColor;
    this.aiColor =
      (playerColor == CheckersData.BLACK)
        ? CheckersData.RED
        : CheckersData.BLACK;

    String response = checkersGame.initializeGame(difficulty, playerColor);
    if (!response.equals("Success")) {
      return new GameResponse<>(false, response);
    }
    checkersGame.getCheckersData().printGameState();
    return new GameResponse<>(true, "Game started successfully.");
  }

  /**
   * Handle the player's move.
   */
  public GameResponse<Void> initiatePlayerMove(
    int fromRow,
    int fromCol,
    int toRow,
    int toCol
  ) {
    if (checkersGame.getCurrentPlayer() != playerColor) {
      return new GameResponse<>(false, "Not the player's turn.");
    }
    if (!getHasSetLegalMoves()) {
      return new GameResponse<>(
        false,
        "Legal moves have not been set for the player"
      );
    }

    String playerResp = checkersGame.makeMove(fromRow, fromCol, toRow, toCol);

    if (!playerResp.equals("Move successful.")) { // Fix string comparison
      return new GameResponse<>(false, playerResp);
    }

    if (!checkersGame.isGameInProgress()) {
      return new GameResponse<>(true, playerColor + " wins");
    }

    checkersGame.setLegalMovesMap(aiColor);
    checkersGame.getCheckersData().printGameState();
    return new GameResponse<>(true, "Player move successful. AI's turn.");
  }

  public void setHasSetLegalMoves(boolean setHasLegalMoves) {
    this.setHasLegalMoves = setHasLegalMoves;
  }

  public boolean getHasSetLegalMoves() {
    return this.setHasLegalMoves;
  }

  /**
   * Handle the AI's move.
   */
  public GameResponse<Void> initiateAIMove() {
    if (checkersGame.getCurrentPlayer() != aiColor) {
      return new GameResponse<>(false, "Not AI's turn.");
    }

    checkersGame.performAIMove();

    if (!checkersGame.isGameInProgress()) {
      return new GameResponse<>(true, aiColor + " wins");
    }

    //set the legal moves for the Player
    checkersGame.setLegalMovesMap(playerColor);

    return new GameResponse<>(true, "AI move successful. Player's turn.");
  }

  public void resetGame() {
    checkersGame.resetGame();
  }

  public void newGame() {
    checkersGame.intializeBoard();
  }

  public GameResponse<int[][]> getBoard() {
    try {
      int[][] board = checkersGame.getCheckersData().getBoard();
      if (board == null) {
        return new GameResponse<>(false, "No active board found", null);
      }
      return new GameResponse<>(true, "Board retrieved successfully", board);
    } catch (Exception e) {
      return new GameResponse<>(
        false,
        "Failed to get board: " + e.getMessage(),
        null
      );
    }
  }

  public GameResponse<ArrayList<int[]>> getLegalMoves(int row, int col) {
    ArrayList<int[]> moves = checkersGame.getLegalMovesByPiece(row, col);
    return new GameResponse<>(true, "Legal moves retrieved", moves);
}
  
  public boolean isValidPiece(int r, int c){
    int playerKing = (playerColor == CheckersData.RED) ? CheckersData.RED_KING : CheckersData.BLACK_KING;
    int[][] board = checkersGame.getBoardState();  
    return board[r][c] == playerColor || board[r][c] == playerKing;
  }

  public GameResponse<Void> makePlayerMove(int fRow, int fCol, int tRow, int tCol){
    ArrayList<int[]> moves = checkersGame.getLegalMovesByPiece(fRow, fCol);
    boolean validMove = false;
    for(int[] coord : moves){
      if(coord[0] == tRow && coord[1] == tCol){
        validMove = true;
      }
    }

    if(!validMove){
      return new GameResponse<>(false, "Illegal move");
    }

    //valid move so make the move 
    String message = checkersGame.makeMove(fRow, fCol, tRow, tCol);

    return new GameResponse<>(validMove, message);
  }

  public GameResponse<Void> makeAIMove(){
    String response = checkersGame.performAIMove();
    if(response.equals("Failed AI move")){
      return new GameResponse<>(false, response);
    }  

    return new GameResponse<>(true, response);
  }

  public GameResponse<Integer> getGameState(){
    int status = checkersGame.getCheckersData().terminationState(playerColor);
    return GameResponseUtil.generateResponse(true, "hello", status);
  }
}
