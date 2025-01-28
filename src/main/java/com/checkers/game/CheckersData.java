package main.java.com.checkers.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An object of this class holds data about a game of checkers.
 * It knows what kind of piece is on each square of the checkerboard.
 * Note that RED moves "up" the board (i.e. row number decreases)
 * while BLACK moves "down" the board (i.e. row number increases).
 * Methods are provided to return lists of available legal moves.
 */
public class CheckersData implements GameState<CheckersMove>, Cloneable {

  /*  The following constants represent the possible contents of a square
      on the board.  The constants RED and BLACK also represent players
      in the game. */

  public static final int EMPTY = 0, RED = 1, RED_KING = 2, BLACK = 3, BLACK_KING = 4;

  int[][] board; // board[r][c] is the contents of row r, column c.

  //r,c key with checkersmove list
  HashMap<Cell, ArrayList<CheckersMove>> validRedMoves;
  HashMap<Cell, ArrayList<CheckersMove>> validBlackMoves;

  int numRed = 12;
  int numBlack = 12;
  int currentPlayer;
  int currentKing;

  boolean hasCaptureMove = false; //used to track if there is a capture move for the current legal moves
  public record Cell(int row, int col) {}

  /**
   * Constructor.  Create the board and set it up for a new game.
   */
  CheckersData() {
    board = new int[8][8];
    validRedMoves = new HashMap<>();
    validBlackMoves = new HashMap<>();
    setUpGame();
  }

  public static final String ANSI_RESET = "\u001B[0m";
  public static final String ANSI_RED = "\u001B[31m";
  public static final String ANSI_YELLOW = "\u001B[33m";
  public static final String ANSI_CYAN = "\u001B[36m";

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < board.length; i++) {
      int[] row = board[i];
      sb.append(0 + i).append(" ");
      for (int n : row) {
        if (n == 0) {
          sb.append(" ");
        } else if (n == 1) {
          sb.append(ANSI_RED + "R" + ANSI_RESET);
        } else if (n == 2) {
          sb.append(ANSI_RED + "K" + ANSI_RESET);
        } else if (n == 3) {
          sb.append(ANSI_YELLOW + "B" + ANSI_RESET);
        } else if (n == 4) {
          sb.append(ANSI_YELLOW + "K" + ANSI_RESET);
        }
        sb.append(" ");
      }
      sb.append(System.lineSeparator());
    }
    sb.append("  0 1 2 3 4 5 6 7");

    return sb.toString();
  }

  int[][] getBoard() {
    return this.board;
  }

  /**
   * Clears and repopulates the valid move maps (validRedMoves, validBlackMoves)
   * based on the current board state.
   */
  void setMoveMap() {
    validRedMoves.clear();
    validBlackMoves.clear();

    for (int i = 0; i < board.length; i++) {
        for (int j = 0; j < board[i].length; j++) {
            int piece = board[i][j];
            if (piece == RED || piece == RED_KING) {
                Cell cell = new Cell(i, j);
                ArrayList<CheckersMove> moves = new ArrayList<>();
                moves.add(new CheckersMove(i, j));
                validRedMoves.put(cell, moves);
            }
            else if (piece == BLACK || piece == BLACK_KING) {
                Cell cell = new Cell(i, j);
                ArrayList<CheckersMove> moves = new ArrayList<>();
                moves.add(new CheckersMove(i, j));
                validBlackMoves.put(cell, moves);
            }
        }
    }
  }

  @FunctionalInterface
  interface BoardPlacer {
    void place(int[][] board, int row, int col, int color);
  }

  /**
   * Set up the board with checkers in position for the beginning
   * of a game.  Note that checkers can only be found in squares
   * that satisfy  row % 2 == col % 2.  At the start of the game,
   * all such squares in the first three rows contain black squares
   * and all such squares in the last three rows contain red squares.
   */
  void setUpGame() {
    // Set up the board with pieces BLACK, RED, and EMPTY

    // Define the BoardPlacer lambdas using the custom functional interface
    BoardPlacer evenPlace = (b, r, c, color) -> {
      if (c % 2 == 0) {
        board[r][c] = color;
      } else {
        board[r][c] = EMPTY;
      }
    };

    BoardPlacer oddPlace = (b, r, c, color) -> {
      if (c % 2 == 1) {
        board[r][c] = color;
      } else {
        board[r][c] = EMPTY;
      }
    };

    // Fill the board based on row and col values
    for (int row = 0; row < board.length; row++) {
      for (int col = 0; col < board[row].length; col++) {
        if (row <= 2) {
          if (row == 0 || row == 2) {
            evenPlace.place(board, row, col, BLACK);
          } else {
            oddPlace.place(board, row, col, BLACK);
          }
        } else if (row >= board.length - 3) {
          if (row == board.length - 2) {
            evenPlace.place(board, row, col, RED);
          } else {
            oddPlace.place(board, row, col, RED);
          }
        } else {
          board[row][col] = EMPTY;
        }
      }
    }

    setMoveMap();
  }

  /**
   * Builds and returns a map of all legal moves for the given player.
   * Each key is a Cell(row, col) for a piece belonging to the player,
   * and each value is a CheckersMove holding the possible moves from that cell.
   *
   * @param player RED or BLACK
   * @return A map where each key is a Cell(row, col) and each value is a CheckersMove.
   */
  public HashMap<Cell, ArrayList<CheckersMove>> getLegalMovesMap(int player) {
    this.hasCaptureMove = false;
    if (player != RED && player != BLACK) {
      System.out.println("ERROR: Invalid player color: " + player);
      return new HashMap<>();
    }

    currentPlayer = player;
    currentKing = (player == RED) ? RED_KING : BLACK_KING;

    setMoveMap(); // Clears and populates either validRedMoves or validBlackMoves
    HashMap<Cell, ArrayList<CheckersMove>> moveMap = (player == RED)
    ? validRedMoves
    : validBlackMoves;

    for (int row = 0; row < board.length; row++) {
        for (int col = 0; col < board[row].length; col++) {
            int piece = board[row][col];
            if (piece == player || piece == currentKing) {
              if(!hasCaptureMove){ //only search for regular moves if there is no capture availiable
                addRegularMoves(player, row, col, moveMap);
              }
                addJumps(player, row, col, null, moveMap);
            }
        }
    }
    
    HashMap<Cell, ArrayList<CheckersMove>> filteredMap = new HashMap<>();
    /**
     * Filtering to ensure the moves are > 1
     */
    for (Map.Entry<Cell, ArrayList<CheckersMove>> entry : moveMap.entrySet()) {
      Cell cell = entry.getKey();
      ArrayList<CheckersMove> moves = entry.getValue();
      
      ArrayList<CheckersMove> validMoves = moves.stream()
          .filter(move -> move != null && move.rows.size() > 1)
          .collect(Collectors.toCollection(ArrayList::new));
      
      if (!validMoves.isEmpty()) {
          filteredMap.put(cell, validMoves);
      }
  }
  
  /** 
   * Filter such that if there is a capture move only incude those in the legal moves
  */
  
  if (hasCaptureMove) {
      HashMap<Cell, ArrayList<CheckersMove>> captureOnlyMap = new HashMap<>();
      for (Map.Entry<Cell, ArrayList<CheckersMove>> entry : filteredMap.entrySet()) {
          Cell cell = entry.getKey();
          ArrayList<CheckersMove> moves = entry.getValue();
          
          ArrayList<CheckersMove> captureMoves = moves.stream()
              .filter(move -> move.captures.size() >= 1)
              .collect(Collectors.toCollection(ArrayList::new));
          
          if (!captureMoves.isEmpty()) {
              captureOnlyMap.put(cell, captureMoves);
          }
      }
      filteredMap = captureOnlyMap;
  }

    return filteredMap;
  }

  void makeMove(CheckersMove move) {
    // Remove captured pieces first
    for (Integer[] captureCoord : move.captures) {
        board[captureCoord[0]][captureCoord[1]] = EMPTY;
        if (currentPlayer == RED) numBlack--; 
        else numRed--;
    }
    
    // Get start and end positions
    int startRow = move.rows.get(0);
    int startCol = move.cols.get(0);
    int endRow = move.rows.get(move.rows.size() - 1);
    int endCol = move.cols.get(move.cols.size() - 1);
    
    // Move piece
    int piece = board[startRow][startCol];
    board[startRow][startCol] = EMPTY;
    
    // Check for king promotion
    if (piece == RED && endRow == 0) piece = RED_KING;
    else if (piece == BLACK && endRow == board.length - 1) piece = BLACK_KING;
    
    board[endRow][endCol] = piece;
}

  @Override
  public void applyMove(CheckersMove move) {
    makeMove(move);
  }

  /* Helper Methods */

  /**
   * Checks if a position is within board bounds.
   * @param row The row index.
   * @param col The column index.
   * @return true if the position is valid, false otherwise.
   */
  private boolean isValidPosition(int row, int col) {
    return row >= 0 && row < board.length && col >= 0 && col < board[0].length;
  }

  /**
   * Adds all regular moves for the given piece at (row, col) to the moves list.
   * @param player The player color.
   * @param row    The row of the piece.
   * @param col    The column of the piece.
   * @param moves  The list to add moves to.
   */
  private void addRegularMoves(
    int player,
    int row,
    int col,
    HashMap<Cell, ArrayList<CheckersMove>> moveMap
  ) {
    int direction = (player == RED) ? 1 : -1;

    // Forward diagonal checks (could be "up" or "down" depending on board orientation)
    diagLeft(row, col, direction, moveMap);
    diagRight(row, col, direction, moveMap);

    // If a king piece, check the opposite direction as well
    if (board[row][col] == RED_KING || board[row][col] == BLACK_KING) {
      diagLeft(row, col, -direction, moveMap);
      diagRight(row, col, -direction, moveMap);
    }
  }

  private void diagLeft(
    int row,
    int col,
    int direction,
    HashMap<Cell, ArrayList<CheckersMove>> moveMap
  ) {
    // Adjust row based on direction
    int newRow = row - direction;
    int newCol = col - 1;

    if (isValidPosition(newRow, newCol) && board[newRow][newCol] == EMPTY) {
      Cell key = new Cell(row, col);
      CheckersMove moveEntry = new CheckersMove(row, col, newRow, newCol);
      ArrayList<CheckersMove> moves = moveMap.computeIfAbsent(key, k -> new ArrayList<>());
      moves.add(moveEntry);
    }
  }

  private void diagRight(
    int row,
    int col,
    int direction,
    HashMap<Cell, ArrayList<CheckersMove>> moveMap
  ) {
    int newRow = row - direction;
    int newCol = col + 1;

    if (isValidPosition(newRow, newCol) && board[newRow][newCol] == EMPTY) {
      Cell key = new Cell(row, col);
      CheckersMove moveEntry = new CheckersMove(row, col, newRow, newCol);
      ArrayList<CheckersMove> moves = moveMap.computeIfAbsent(key, k -> new ArrayList<>());
      moves.add(moveEntry);
    }
  }

  /**
   * Adds all legal jumps for the given piece at (row, col) to the moves list.
   * This method should handle multiple jumps in future updates.
   * @param player The player color.
   * @param row    The row of the piece.
   * @param col    The column of the piece.
   * @param jumps  The list to add jumps to.
   */
  private void addJumps(
    int player,
    int row,
    int col,
    CheckersMove jumps,
    HashMap<Cell, ArrayList<CheckersMove>> moveMap
  ) {

    if(!isOnBoard(row, col)){
      System.out.println("Invalid position " + row +" " + col);
      return;
    }

    int direction = (player == RED) ? -1 : 1;
    Cell cell = new Cell(row, col);

    // If we're starting fresh (no jumps yet)...
    CheckersMove currentChain;
    if (jumps == null) {
      currentChain = new CheckersMove(row, col);
    } else {
      // Just use the first chain in the list as our base (one chain per cell)
      currentChain = jumps.clone();
    }

    // Determine our current position (end of the chain so far)
    int cRow = currentChain.rows.get(currentChain.rows.size() - 1);
    int cCol = currentChain.cols.get(currentChain.cols.size() - 1);

    // ---------------------------------------------------------
    // 1) Forward jumps
    // ---------------------------------------------------------
    CheckersJumpData leftJump = new CheckersJumpData(true, null);
    CheckersJumpData rightJump = new CheckersJumpData(true, null);

      leftJump = canJump(player, cRow, cCol, direction, "LEFT");
      if(leftJump.canJump){
        handleJump(leftJump, currentChain, cell, moveMap, player);
      }

      rightJump = canJump(player, cRow, cCol, direction, "RIGHT");
      if(rightJump.canJump){
        handleJump(rightJump, currentChain, cell, moveMap, player);
      }

    // ---------------------------------------------------------
    // 2) King jumps (reverse direction)
    // ---------------------------------------------------------
    int piece = board[cRow][cCol];
    boolean isKing =
      (player == RED && piece == RED_KING) ||
      (player == BLACK && piece == BLACK_KING);

    if (isKing) {
      CheckersJumpData leftJumpK = new CheckersJumpData(true, null);
      CheckersJumpData rightJumpK = new CheckersJumpData(true, null);

        leftJumpK = canJump(player, cRow, cCol, -direction, "LEFT");
        if(leftJumpK.canJump){
          handleJump(leftJumpK, currentChain, cell, moveMap, player);
        }

        rightJumpK = canJump(player, cRow, cCol, -direction, "RIGHT");
        if(rightJumpK.canJump){
          handleJump(rightJumpK, currentChain, cell, moveMap, player);
        }
      }
  
  }

  /**
   * Processes a valid jump move, checking if it's valid and not previously visited.
   * If valid, creates an extended move chain, adds it to the move map, and recursively
   * checks for additional jumps from the new position.
   *
   * @param jump Current jump data to process
   * @param currentChain The current sequence of jumps
   * @param cell Starting cell position
   * @param moveMap Map of valid moves for each cell
   * @param player Current player (RED/BLACK)
   * @param row Initial row position
   * @param col Initial column position
   */
  private void handleJump(
    CheckersJumpData jump,
    CheckersMove currentChain,
    Cell cell,
    HashMap<Cell, ArrayList<CheckersMove>> moveMap,
    int player
  ) {

    if (!currentChain.hasVisited(jump.r, jump.c)) {
    
      CheckersMove extended = currentChain.clone();
      extended.addCaptureMove(jump.r, jump.c, jump.capture);
       // Get or create list of moves for this cell
       ArrayList<CheckersMove> moves = moveMap.computeIfAbsent(cell, k -> new ArrayList<>());
       extended.toString();
       moves.add(extended);
       this.hasCaptureMove = true;
      
      addJumps(player, jump.r, jump.c, extended, moveMap);
    }
  }

  /**
   * Checks if the piece at (row, col) belonging to 'player' can jump in
   * the specified direction and returns the jump data.
   */
  private CheckersJumpData canJump(
    int player,
    int row,
    int col,
    int verticalDir,
    String horizontalDir
  ) {
    // Identify opposing pieces
    int opposing = (player == RED) ? BLACK : RED;
    int opposingKing = (opposing == RED) ? RED_KING : BLACK_KING;

    // Determine horizontal/vertical offsets (2 steps for a jump)
    int dx;
    int dy = verticalDir * 2;

    switch (horizontalDir) {
      case "LEFT" -> dx = -2;
      case "RIGHT" -> dx = 2;
      default -> {
        System.out.println("bad directive...");
        return new CheckersJumpData(false, null);
      }
    }

    int newRow = row + dy;
    int newCol = col + dx;

    // Check board boundaries for the landing square
    if (!isOnBoard(newRow, newCol)) {
      return new CheckersJumpData(false, null);
    }


    // *** Check that the landing square is EMPTY ***
    if (board[newRow][newCol] != EMPTY) {
      return new CheckersJumpData(false, null);
    }

    // The piece we might capture is halfway between (row, col) and (newRow, newCol)
    int capRow = row + dy / 2;
    int capCol = col + dx / 2;

    // Check boundaries for the captured piece
    if (!isOnBoard(capRow, capCol)) {
      return new CheckersJumpData(false, null);
    }
    // Check if the piece in between is an opposing piece or its king
    int pieceToCapture = board[capRow][capCol];
    if (pieceToCapture != opposing && pieceToCapture != opposingKing) {
      return new CheckersJumpData(false, null);
    }
    // Valid jump: return jump data
    Integer[] capturePiece = { capRow, capCol };
    return new CheckersJumpData(true, capturePiece, newRow, newCol);
  }

  /**
   * Utility method to check if the given row/col is within the board.
   */
  private boolean isOnBoard(int r, int c) {
    return r >= 0 && r < board.length && c >= 0 && c < board[0].length;
  }

  class CheckersJumpData {

    public final boolean canJump;
    public final Integer[] capture;
    public final int r;
    public final int c;

    public CheckersJumpData(boolean canJump, Integer[] capture) {
      this.canJump = canJump;
      this.capture = capture;
      this.r = -1;
      this.c = -1;
    }

    public CheckersJumpData(boolean canJump, Integer[] capture, int r, int c) {
      this.canJump = canJump;
      this.capture = capture;
      this.r = r;
      this.c = c;
    }
  }

  //clone emthod
  @Override
public CheckersData clone() {
   CheckersData cloned = new CheckersData();
   
   // Deep copy board
   cloned.board = new int[board.length][];
   for (int i = 0; i < board.length; i++) {
       cloned.board[i] = board[i].clone();
   }

    // Deep copy move maps with ArrayList
   cloned.validRedMoves = new HashMap<>();
   cloned.validBlackMoves = new HashMap<>();
   for (Map.Entry<Cell, ArrayList<CheckersMove>> entry : validRedMoves.entrySet()) {
       ArrayList<CheckersMove> clonedMoves = new ArrayList<>();
       for (CheckersMove move : entry.getValue()) {
           clonedMoves.add(move.clone());
       }
       cloned.validRedMoves.put(entry.getKey(), clonedMoves);
   }
   for (Map.Entry<Cell, ArrayList<CheckersMove>> entry : validBlackMoves.entrySet()) {
       ArrayList<CheckersMove> clonedMoves = new ArrayList<>();
       for (CheckersMove move : entry.getValue()) {
           clonedMoves.add(move.clone());
       }
       cloned.validBlackMoves.put(entry.getKey(), clonedMoves);
   }

   // Copy primitives
   cloned.numRed = this.numRed;
   cloned.numBlack = this.numBlack;
   cloned.currentPlayer = this.currentPlayer;
   cloned.currentKing = this.currentKing;

   return cloned;
}

public CheckersData newRandomState(int player) {
  //clear the move map
  setMoveMap();
  HashMap<Cell, ArrayList<CheckersMove>> moveMap = getLegalMovesMap(player);
   
   if (moveMap.isEmpty()) {
       System.out.println("No moves available");
       return null;
   }

   // Flatten all moves into a single list
   ArrayList<CheckersMove> allMoves = moveMap.values().stream()
       .flatMap(Collection::stream)
       .filter(move -> move != null && move.rows.size() > 1)
       .distinct()
       .collect(Collectors.toCollection(ArrayList::new));

   if (allMoves.isEmpty()) {
       System.out.println("No valid moves after filtering");
       return null;
   }

   CheckersMove randMove = allMoves.get((int) (Math.random() * allMoves.size()));

   CheckersData copy = this.clone();
   copy.applyMove(randMove);
   copy.currentPlayer = (player == RED) ? BLACK : RED;
   copy.getLegalMovesMap(copy.currentPlayer);
   
   return copy;
}

  public int terminationState(int player) {
    if (numRed <= 0) return 0;
    if (numBlack <= 0) return 1;
    //if (getLegalMovesMap(player).isEmpty()) return 2;
     // Dynamically select the move map based on the current player
     HashMap<Cell, ArrayList<CheckersMove>> currentMoves = getCurrentMoveMap(player);

     // If the current move map is null or empty, no valid moves are available
     if (currentMoves == null || currentMoves.isEmpty()) {
         return 2; // No valid moves available
     }
    return -1;
  }
/**
 * Retrieves the appropriate move map based on the player.
 *
 * @param player The current player (e.g., RED or BLACK).
 * @return The corresponding move map for the player.
 */
private HashMap<Cell, ArrayList<CheckersMove>> getCurrentMoveMap(int player) {
    if (player == RED) {
        return validRedMoves;
    } else if (player == BLACK) {
        return validBlackMoves;
    }
    return null;
}

  public int getCurrentPlayer() {
    return currentPlayer;
  }

  public void setCurrentPlayer(int player){
    this.currentPlayer = player;
  }

  /**
   * Print all relevant information about the current game state.
   */
  public void printGameState() {
    String turnHeaderColor = (currentPlayer == RED) ? ANSI_RED : ANSI_YELLOW;

    System.out.println(
      "\n" +
      turnHeaderColor +
      "======================================================"
    );
    System.out.println(
      "               PROCESSING " +
      (currentPlayer == RED ? "RED" : "BLACK") +
      " TURN               "
    );
    System.out.println(
      "======================================================" + ANSI_RESET
    );
    System.out.println(
      "Player " + (currentPlayer == RED ? "RED" : "BLACK") + "'s turn!"
    );
    System.out.println("Remaining Pieces: ");
    System.out.println("  RED: " + numRed);
    System.out.println("  BLACK: " + numBlack);
    System.out.println("Current Board State:");
    System.out.println(toString());

     // Add legal moves section
     System.out.println("\nLegal Moves for " + (currentPlayer == RED ? "RED" : "BLACK") +":");
     HashMap<Cell, ArrayList<CheckersMove>> moves = getLegalMovesMap(currentPlayer);
     if (moves.isEmpty()) {
        System.out.println("No legal moves available");
     } else {
        moves.values().forEach(movesList -> 
            movesList.forEach(move -> {
                if (move != null && move.rows.size() > 1) {
                    System.out.println(move.toString());
                }
            })
        );
     }
   System.out.println();
    
  }
}
