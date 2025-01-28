package main.java.com.checkers.ai;

import java.util.ArrayList;
import java.util.List;
import main.java.com.checkers.game.GameState;

/**
 * Node type for the Monte Carlo search tree.
 *
 * @param <E> The type representing the game state (e.g., board state).
 * @param <M> The type representing a move (e.g., a custom move class).
 */
public class MCNode<E extends GameState<M>, M> {

  // State of the game at this node
  private E state;

  // The move that led to this node
  private M move;

  // Parent node
  private MCNode<E, M> parent;

  // List of child nodes
  private List<MCNode<E, M>> children;

  // Statistics
  private int visits;
  private double totalReward;

  // Unexplored moves from this node
  private List<M> unexploredMoves;

  // Constructor
  public MCNode(E state, M move, MCNode<E, M> parent) {
    this.state = state;
    this.move = move;
    this.parent = parent;
    this.children = new ArrayList<>();
    this.visits = 0;
    this.totalReward = 0.0;
    this.unexploredMoves = new ArrayList<>();
  }

  // Getters and Setters
  public E getState() {
    return state;
  }

  public void setState(E state) {
    this.state = state;
  }

  public M getMove() {
    return move;
  }

  public void setMove(M move) {
    this.move = move;
  }

  public MCNode<E, M> getParent() {
    return parent;
  }

  public void setParent(MCNode<E, M> parent) {
    this.parent = parent;
  }

  public List<MCNode<E, M>> getChildren() {
    return children;
  }

  public void addChild(MCNode<E, M> child) {
    this.children.add(child);
  }

  public int getVisits() {
    return visits;
  }

  public void incrementVisits() {
    this.visits++;
  }

  public double getTotalReward() {
    return totalReward;
  }

  public void updateReward(double reward) {
    this.totalReward += reward;
  }

  public List<M> getUnexploredMoves() {
    return unexploredMoves;
  }

  public void setUnexploredMoves(List<M> unexploredMoves) {
    this.unexploredMoves = new ArrayList<>(unexploredMoves);
  }

  public void addUnexploredMove(M move) {
    this.unexploredMoves.add(move);
  }

  public void generateChildren() {
    if (unexploredMoves == null || unexploredMoves.isEmpty()) {
        throw new IllegalStateException("No unexplored moves to generate children.");
    }

    for (M currentMove : unexploredMoves) { 
        E newState = getNewState(currentMove);
        MCNode<E, M> child = new MCNode<>(newState, currentMove, this);
        this.children.add(child);
    }

    // Clear unexplored moves after generation
    unexploredMoves.clear();
}

  public E getNewState(M move) {
    if (state == null) {
      throw new IllegalStateException(
        "State cannot be null when generating a new state."
      );
    }
    if (!(state instanceof GameState)) {
      throw new IllegalArgumentException(
        "State must implement GameState to support cloning and applying moves."
      );
    }

    // Clone the current state
    E cloneState = (E) ((GameState<M>) state).clone(); // Safe cast assuming E implements GameState<M>

    // Apply the move to the cloned state
    ((GameState<M>) cloneState).applyMove(move); // Safe cast for applying the move

    // Return the updated state
    return cloneState;
  }

  /**
   * Selects the child node with the highest UCB1 score.
   *
   * @param explorationConstant The exploration constant (e.g., √2 for MCTS).
   * @return The selected child node.
   */
  public MCNode<E, M> selectChildUsingUCB1(double explorationConstant) {
    if (children.isEmpty()) {
      throw new IllegalStateException(
        "Cannot select a child from a node with no children."
      );
    }

    MCNode<E, M> bestChild = null;
    double bestScore = Double.NEGATIVE_INFINITY;

    for (MCNode<E, M> child : children) {
      double exploitation = child.totalReward / (child.visits + 1e-6); // Avoid division by zero
      double exploration =
        explorationConstant *
        Math.sqrt(Math.log(this.visits + 1) / (child.visits + 1e-6));
      double ucb1Score = exploitation + exploration;

      if (ucb1Score > bestScore) {
        bestScore = ucb1Score;
        bestChild = child;
      }
    }

    return bestChild;
  }

  @Override
  public String toString() {
    return toString(1, 0); // Default depth of 2
  }

  public String toString(int maxDepth, int currentDepth) {
    StringBuilder sb = new StringBuilder();

    // Define colors for depth levels
    final String RESET = "\u001B[0m";
    final String LEVEL_0_COLOR = "\u001B[34m"; // Blue for root level
    final String LEVEL_1_COLOR = "\u001B[32m"; // Green for depth 1
    final String OTHER_LEVEL_COLOR = "\u001B[31m"; // Red for depth > 1

    // Determine the color based on currentDepth
    String color =
      switch (currentDepth) {
        case 0 -> LEVEL_0_COLOR;
        case 1 -> LEVEL_1_COLOR;
        default -> OTHER_LEVEL_COLOR;
      };

    // Apply color to the node details
    sb
      .append(color)
      .append("Node Level ")
      .append(currentDepth)
      .append(":")
      .append(RESET)
      .append("\n");
    sb
      .append(color)
      .append("---------------------------------------")
      .append(RESET)
      .append("\n");

    // State section
    sb.append(color).append("State:").append(RESET).append("\n");
    sb.append(state != null ? state.toString() : "null").append("\n\n");

    // Move leading to the node
    sb.append(color).append("Move Leading to Node:").append(RESET).append("\n");
    sb
      .append("  ")
      .append(move != null ? move.toString() : "None")
      .append("\n\n");

    // Statistics
    sb
      .append(color)
      .append("Visits: ")
      .append(RESET)
      .append(visits)
      .append("\n");
    sb
      .append(color)
      .append("Total Reward: ")
      .append(RESET)
      .append(String.format("%.2f", totalReward))
      .append("\n\n");

    // Unexplored moves
    sb.append(color).append("Unexplored Moves:").append(RESET).append("\n");
    if (unexploredMoves != null && !unexploredMoves.isEmpty()) {
      for (M move : unexploredMoves) {
        sb.append("  - ").append(move.toString()).append("\n");
      }
    } else {
      sb.append("  None\n");
    }

    // Children section
    if (currentDepth < maxDepth && !children.isEmpty()) {
      sb.append(color).append("Children:").append(RESET).append("\n");
      for (MCNode<E, M> child : children) {
        sb.append(child.toString(maxDepth, currentDepth + 1)); // Recursive call for children
      }
    } else if (!children.isEmpty()) {
      sb.append("Children not displayed (max depth reached).\n");
    }

    sb
      .append(color)
      .append("---------------------------------------")
      .append(RESET)
      .append("\n");
    return sb.toString();
  }
}
