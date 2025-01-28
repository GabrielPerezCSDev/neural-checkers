package main.java.com.checkers.api;

import java.util.ArrayList;
import main.java.com.checkers.game.GameManager;
import main.java.com.checkers.util.GameResponse;

public class GameThread extends Thread {

  private GameManager gameManager;
  private String connectionId;
  private boolean hasActiveGame;

  public GameThread(String connectionId) {
    this.connectionId = connectionId;
    this.gameManager = new GameManager();
    this.hasActiveGame = false;
  }

  @Override
  public void run() {}

  public void newGame() {
    gameManager.newGame();
    this.hasActiveGame = false;
  }

  public GameResponse<Void> startGame(int difficulty, int playerColor) {
    this.hasActiveGame = true;
    return gameManager.startGame(difficulty, playerColor);
  }

  public void resetGame() {
    gameManager.resetGame();
    this.hasActiveGame = false;
  }

  public GameResponse<int[][]> getBoard() {
    return gameManager.getBoard();
  }

  public GameResponse<ArrayList<int[]>> getLegalMoves(int row, int col) {
    return gameManager.getLegalMoves(row, col);
  }

  public boolean hasActiveGame() {
    return this.hasActiveGame;
  }

  public boolean isValidPiece(int r, int c){
    return this.gameManager.isValidPiece(r, c);
  }

  public GameResponse<Void> makePlayerMove(int fRow, int fCol, int tRow, int tCol){
    //TO-DO
    return this.gameManager.makePlayerMove(fRow, fCol, tRow, tCol);
  }

  public GameResponse<Void> makeAIMove(){
    return this.gameManager.makeAIMove();
  }

  public GameResponse<Integer> gameStatus(){
    return this.gameManager.getGameState();
  }
}
