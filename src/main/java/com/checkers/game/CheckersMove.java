package main.java.com.checkers.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * A CheckersMove object represents a move in the game of Checkers.
 * It holds the row and column of the piece that is to be moved
 * and the row and column of the square to which it is to be moved.
 * (This class makes no guarantee that the move is legal.)
 *
 * It represents an action in the game of Checkers.
 * There may be a single move or multiple jumps in an action.
 * It holds a sequence of the rows and columns of the piece
 * that is to be moved, for example:
 * a single move: (2, 0) -> (3, 1)
 * a sequnce of jumps: (2, 0) -> (4, 2) -> (6, 0)
 *
 */
public class CheckersMove {

  ArrayList<Integer> rows = new ArrayList<>();
  ArrayList<Integer> cols = new ArrayList<>();
  ArrayList<Integer[]> captures = new ArrayList<>();
  boolean isCapture;
  Set<String> visitedPositions = new HashSet<>();

  public CheckersMove(int r1, int c1){
    rows.add(r1);
    cols.add(c1);
    visitedPositions.add(r1 + "," + c1);
  }

  public CheckersMove(int r1, int c1, int r2, int c2){
    rows.add(r1);
    cols.add(c1);
    visitedPositions.add(r1 + "," + c1);
    rows.add(r2);
    cols.add(c2);
    visitedPositions.add(r2 + "," + c2);
  }

  CheckersMove() {
    // Constructor, create an empty move
  }

  public boolean isCapture() {
    return this.isCapture;
  }

  void addMove(int r, int c) {
    rows.add(r);
    cols.add(c);
  }

  void addCaptureMove(int r, int c, Integer[] capture){
    rows.add(r);
    cols.add(c);
    captures.add(capture);
    visitedPositions.add(r + "," + c);
  }

  boolean hasVisited(int r, int c) {
    return visitedPositions.contains(r + "," + c);
  }

  public int getSize(){
    return this.rows.size();
  }

  //get a copy of this move
  @Override
  public CheckersMove clone() {
    CheckersMove move = new CheckersMove();
    move.rows.addAll(this.rows);
    move.cols.addAll(this.cols);
    move.captures.addAll(this.captures);
    move.isCapture = this.isCapture; // Ensure the capture status is preserved
    return move;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("CheckersMove: ");

    if (rows.isEmpty() || cols.isEmpty()) {
      sb.append("No moves recorded.");
      return sb.toString();
    }

    // Build the move sequence representation
    for (int i = 0; i < rows.size(); i++) {
      sb
        .append("(")
        .append(rows.get(i))
        .append(", ")
        .append(cols.get(i))
        .append(")");
      if (i < rows.size() - 1) {
        sb.append(" -> ");
      }
    }

    return sb.toString();
  }
} // end class CheckersMove.
