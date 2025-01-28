package main.java.com.checkers.ai;

import main.java.com.checkers.game.GameState;

/**
 * This class represents a Monte Carlo search tree.
 *
 * @param <E> The type representing the game state (e.g., board state).
 * @param <M> The type representing a move (e.g., a custom move class).
 */
public class MCTree<E extends GameState<M>, M> {

  // Root node of the tree
  private MCNode<E, M> root;

  // Size of the tree (number of nodes)
  private int size;

  // Constructor
  public MCTree(MCNode<E, M> root) {
    this.root = root;
    this.size = 1; // Tree starts with just the root
  }

  // Getters and setters
  public MCNode<E, M> getRoot() {
    return root;
  }

  public void setRoot(MCNode<E, M> root) {
    this.root = root;
  }

  public int getSize() {
    return size;
  }

  public void incrementSize() {
    this.size++;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("=======================================\n");
    sb.append("Monte Carlo Search Tree Debug Info\n");
    sb.append("=======================================\n");

    if (root == null) {
      sb.append("Root is null.\n");
    } else {
      sb.append("Root Node: \n");
      sb.append(root.toString(2, 0)); // Show up to depth 2 for debug
    }

    sb.append("Tree Size: ").append(size).append("\n");
    sb.append("=======================================\n");
    return sb.toString();
  }
  
}
