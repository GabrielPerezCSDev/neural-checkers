package main.java.com.checkers.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import main.java.com.checkers.ai.AdversarialSearch;
import main.java.com.checkers.ai.MonteCarloTreeSearch;
import main.java.com.checkers.game.CheckersData.Cell;

/**
 * Core logic for a checkers game.
 * Handles board state, game rules, and player turns.
 * Designed for integration with a separate frontend.
 */
public class Checkers {

  private CheckersData board; // The data for the checkers board.
  private int currentPlayer; // Current player (RED or BLACK).
  private int userColor;
  private int aiColor;
  private boolean gameInProgress; // Is the game active?
  private HashMap<Cell, ArrayList<CheckersMove>> legalMovesMap; // List of legal moves for the current player.
  private AdversarialSearch aiPlayer; // AI logic for BLACK.
  private int difficulty;

  /**
   * Initializes the game and sets up the board.
   */
  public String initializeGame(int difficulty, int userColor) {
    if (userColor != CheckersData.RED && userColor != CheckersData.BLACK) {
      return "Invalid player assignment";
    }
    if (difficulty != 1 && difficulty != 2 && difficulty != 3) {
      return "Invalid difficulty selection";
    }
    this.userColor = userColor;
    this.currentPlayer = CheckersData.RED; //RED always goes first
    this.difficulty = difficulty;
    setLegalMovesMap(currentPlayer);
    gameInProgress = true;
    this.aiColor =
      (currentPlayer == CheckersData.RED)
        ? CheckersData.BLACK
        : CheckersData.RED;

    aiPlayer = new MonteCarloTreeSearch(1, this.difficulty);
    aiPlayer.setCheckersData(board, aiColor);
    return "Success";
  }

  public void intializeBoard() {
    this.board = new CheckersData();
    board.setUpGame(); // Sets up the initial state of the board.
  }

  public void setLegalMovesMap(int currentPlayer) {
    this.legalMovesMap = board.getLegalMovesMap(currentPlayer);
  }

  /**
   * Processes a move for the current player.
   *
   * @param fromRow Starting row of the piece.
   * @param fromCol Starting column of the piece.
   * @param toRow   Target row for the move.
   * @param toCol   Target column for the move.
   * @return A message indicating the result of the move.
   */
  public String makeMove(int fromRow, int fromCol, int toRow, int toCol) {
    if (!gameInProgress) {
      return "The game is not active. Please start a new game.";
    }

    Cell fromCell = new Cell(fromRow, fromCol);
    ArrayList<CheckersMove> moves = legalMovesMap.get(fromCell);

    if (moves != null) {
      // Find a move that matches the destination
      for (CheckersMove move : moves) {
          if (move != null && 
              move.rows.get(move.rows.size() - 1) == toRow &&
              move.cols.get(move.cols.size() - 1) == toCol) {
              moveCheckersPiece(move);
              return "Move successful.";
          }
      }
  }

    return "Invalid move. Please try again.";
  }

  /**
   * Handles the AI's turn and returns the AI's move.
   *
   * @return The move made by the AI.
   */
  public String performAIMove() {
    // Flatten all moves into a single array
    CheckersMove[] movesArray = legalMovesMap.values().stream()
        .flatMap(Collection::stream)
        .filter(move -> move != null && move.getSize() > 1)
        .toArray(CheckersMove[]::new);

    if (movesArray.length == 0) {
        return "Failed AI move - No valid moves";
    }

    CheckersMove aiMove = aiPlayer.makeMove(movesArray);
    if(aiMove == null){
        System.out.println("Failed AI move");
        return "Failed AI move";
    }
    moveCheckersPiece(aiMove);
    return "Successfull AI Move";
  }

  public void moveCheckersPiece(CheckersMove checkersMove){
    board.makeMove(checkersMove);
    checkGameState();
    switchTurns();
  }

  /**
   * Resets the game to its initial state.
   */
  public void resetGame() {
    intializeBoard();
    initializeGame(this.difficulty, this.userColor);
  }

  /**
   * Stops the game and sets difficulty to -1
   */
  public void stopGame() {
    intializeBoard();
  }

  /**
   * Checks the state of the game to see if it has ended.
   */
  public void checkGameState() {
    HashMap<Cell, ArrayList<CheckersMove>> moves = board.getLegalMovesMap(currentPlayer);
   
    // Check if there are any valid moves
    boolean noMoves = moves.values().stream()
       .flatMap(Collection::stream)
       .filter(move -> move != null && move.rows.size() > 1)
       .count() == 0;
    
    if (board.numBlack <= 0) {
        gameInProgress = false;
    } else if (board.numRed <= 0) {
        gameInProgress = false;
    } else if (noMoves) {
        gameInProgress = false;
    }
  }

  /**
   * Switches to the next player's turn.
   */
  public void switchTurns() {
    currentPlayer = (currentPlayer == userColor) ? aiColor : userColor;
    legalMovesMap = board.getLegalMovesMap(currentPlayer);
 }

  /**
   * Returns the current state of the board.
   *
   * @return A 2D array representing the board.
   */
  public int[][] getBoardState() {
    return board.getBoard();
  }

  /**
   * Gets the current player.
   *
   * @return The current player (RED or BLACK).
   */
  public int getCurrentPlayer() {
    return currentPlayer;
  }

  /**
   * Checks if the game is currently in progress.
   *
   * @return True if the game is active, false otherwise.
   */
  public boolean isGameInProgress() {
    return gameInProgress;
  }

  /**
   * Gets the list of legal moves for the current player.
   *
   * @return A list of legal moves.
   */
  public CheckersMove[] getLegalMoves() {
   // Flatten all move lists into a single array
   return legalMovesMap.values().stream()
       .flatMap(Collection::stream)
       .filter(move -> move != null && move.rows.size() > 1)
       .toArray(CheckersMove[]::new);
}


  public ArrayList<int[]> getLegalMovesByPiece(int row, int col) {
    ArrayList<int[]> validDestinations = new ArrayList<>();
    Cell selectedCell = new Cell(row, col);
    
    ArrayList<CheckersMove> moves = legalMovesMap.get(selectedCell);
    if (moves != null) {
        for (CheckersMove move : moves) {
            if (move != null && move.rows.size() > 1) {
                // Skip first position (starting position) and add all subsequent positions
                for (int i = 1; i < move.rows.size(); i++) {
                    validDestinations.add(new int[]{move.rows.get(i), move.cols.get(i)});
                }
            }
        }
    }
    
    return validDestinations;
 }

  public CheckersData getCheckersData() {
    return board;
  }

  public void setCheckersData(CheckersData board) {
    this.board = board;
  
}
  public void printLegalMoves(ArrayList<int[]> moves) {
    System.out.println("Legal moves for selected piece:");
    for (int[] move : moves) {
        System.out.printf("Row: %d, Col: %d%n", move[0], move[1]);
    }
    if (moves.isEmpty()) {
        System.out.println("No legal moves available");
    }
 }
}

