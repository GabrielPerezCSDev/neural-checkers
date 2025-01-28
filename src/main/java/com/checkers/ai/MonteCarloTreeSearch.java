package main.java.com.checkers.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.stream.Collectors;
import main.java.com.checkers.game.CheckersData;
import main.java.com.checkers.game.CheckersData.Cell;
import main.java.com.checkers.game.CheckersMove;

/**
 * This class implements the Monte Carlo Tree Search (MCTS) method
 * to find the best move at the current state of the Checkers game.
 */
public class MonteCarloTreeSearch extends AdversarialSearch {

  // Constant for UCB1 formula (exploration vs exploitation)
  //private final double C = Math.sqrt(2.0);
  private final int EASY = 1;
  private final int MEDIUM = 2;
  private final int HARD = 3;

  private final int AI;

  public MonteCarloTreeSearch(int ai, int difficulty) {
    this.AI = ai;
    switch (difficulty) {
      case EASY -> super.setMaxIterations(50);
      case MEDIUM -> super.setMaxIterations(250);
      case HARD -> super.setMaxIterations(1000);
      default -> throw new IllegalArgumentException("Invalid difficulty level");
    }

    super.setConstant(Math.sqrt(2));
  }

  /**
   * Find the best move using the Monte Carlo Tree Search algorithm.
   *
   * @param legalMoves All the legal moves for the agent at the current step.
   * @return The best move determined by MCTS.
   */
  @Override
  public CheckersMove makeMove(CheckersMove[] legalMoves) {
  
    // Step 1: Create the root node and initialize the MCTree
    MCNode<CheckersData, CheckersMove> root = new MCNode<>(board, null, null); // Current board as the root state
    
    // Step 2: Add all legal moves as unexplored moves in the root node
    root.setUnexploredMoves(new ArrayList<>(Arrays.asList(legalMoves)));
    // Step 3: Run MCTS iterations
    for (int i = 0; i < maxIterations; i++) {
      MCNode<CheckersData, CheckersMove> selectedNode = selection(root);
      MCNode<CheckersData, CheckersMove> expandedNode = expansion(selectedNode);
      // Skip simulation and backpropagation if expansion returns null
      if (expandedNode == null) {
        continue; // Skip to the next iteration
      }

      double simulationResult = simulation(expandedNode);
      backPropagation(expandedNode, simulationResult);
    }
    // Step 4: Choose the best move (child of root with highest visit count)
    CheckersMove bestMove = getBestMove(root);

    return bestMove;
  }

  /**
   * Selection step: Traverse the tree to select the most promising node
   * using the UCB1 formula.
   *
   * @param node The root node of the current subtree.
   * @return The selected node for expansion.
   */
  private MCNode<CheckersData, CheckersMove> selection(
    MCNode<CheckersData, CheckersMove> node
  ) {
    while (
      !node.getChildren().isEmpty() || !node.getUnexploredMoves().isEmpty()
    ) {
      if (node.getUnexploredMoves().isEmpty()) {
        node = node.selectChildUsingUCB1(C);
      } else {
        break;
      }
    }
    return node;
  }

  /**
   * Expansion step: Add a child node corresponding to an unexplored move.
   *
   * @param node The node to expand.
   * @return The newly added child node.
   */
  private MCNode<CheckersData, CheckersMove> expansion(
    MCNode<CheckersData, CheckersMove> node
  ) {
    // Ensure there are unexplored moves
    
    // Ensure there are unexplored moves
    if (node.getUnexploredMoves().isEmpty()) {
      return null; // Skip this expansion
    }

    // Select the first unexplored move (arbitrary choice for now)
    CheckersMove moveToExpand = node.getUnexploredMoves().remove(0);

    // Generate a new state by applying the move
    CheckersData newState = node.getNewState(moveToExpand);

    // Create a new child node
    MCNode<CheckersData, CheckersMove> childNode = new MCNode<>(
      newState,
      moveToExpand,
      node
    );

   // Get legal moves for the new state and flatten them into a single list
    HashMap<Cell, ArrayList<CheckersMove>> moves = childNode.getState().getLegalMovesMap(AI);
    ArrayList<CheckersMove> unexploredMoves = moves.values().stream()
       .flatMap(Collection::stream)
       .filter(move -> move != null && move.getSize() > 1)
       .collect(Collectors.toCollection(ArrayList::new));
   
   childNode.setUnexploredMoves(unexploredMoves);
    // Add the new child to the current node
    node.addChild(childNode);

    // Return the newly created child node
    return childNode;
  }

  /**
   * Simulation step: Perform a random playout starting from the node.
   *
   * @param node The node to simulate from.
   * @return The result of the simulation (e.g., 1 for win, 0 for loss, 0.5 for draw).
   */
  private double simulation(MCNode<CheckersData, CheckersMove> node) {
    
    CheckersData state = node.getState().clone();  // Make sure to clone initial state
    int currentPlayer = (AI == CheckersData.RED) ? CheckersData.BLACK : CheckersData.RED;  // Start with opposite player
    int endCondition = -1;
    int moves = 0;
    final int MAX_MOVES = 5;  // Prevent infinite loops
    
    while (endCondition == -1) {
        state = state.newRandomState(currentPlayer);
        if (state == null) break;
        
        endCondition = state.terminationState(currentPlayer);
        if (endCondition != -1) break;
        
        currentPlayer = (currentPlayer == CheckersData.RED) ? CheckersData.BLACK : CheckersData.RED;
        moves++;
        //state.printGameState();
    }

    // Update results based on termination state
    if (endCondition == 0) return 1.0; // AI wins
    if (endCondition == 1) return 0.0; // Player wins
    if (endCondition == 2) return 0.5; // Draw
    return 0;
  }

  /**
   * Backpropagation step: Update the statistics of all nodes along the path
   * from the current node to the root.
   *
   * @param node   The node where the simulation ended.
   * @param reward The result of the simulation (e.g., 1 for win, 0 for loss, 0.5 for draw).
   */
  private void backPropagation(
    MCNode<CheckersData, CheckersMove> node,
    double reward
  ) {
    
    // Start with the current node and backpropagate to the root
    while (node != null) {
      // Update the visit count for the current node
      node.incrementVisits();

      // Update the total reward for the current node
      node.updateReward(reward);

      // Move to the parent node
      node = node.getParent();
    }
  }

  /**
   * Retrieve the best move from the root node based on visit count.
   *
   * @param root The root node of the search tree.
   * @return The best move based on the highest visit count.
   */
  private CheckersMove getBestMove(MCNode<CheckersData, CheckersMove> root) {
    if (root.getChildren().isEmpty()) {
      throw new IllegalStateException(
        "Root node has no children to select a move from."
      );
    }

    MCNode<CheckersData, CheckersMove> bestChild = null;
    int maxVisits = -1;

    // First, check for the best capture move (if any)
    for (MCNode<CheckersData, CheckersMove> child : root.getChildren()) {
      CheckersMove move = child.getMove();
      if (move.isCapture() && child.getVisits() > maxVisits) {
        bestChild = child;
        maxVisits = child.getVisits();
      }
    }

    // If no capture move is found, fall back to the best non-capture move
    if (bestChild == null) {
      for (MCNode<CheckersData, CheckersMove> child : root.getChildren()) {
        if (child.getVisits() > maxVisits) {
          bestChild = child;
          maxVisits = child.getVisits();
        }
      }
    }

    return bestChild != null ? bestChild.getMove() : null;
  }

  @Override
  public void setMaxIterations(int maxIterations) {
    this.maxIterations = maxIterations;
  }

  @Override
  public int getMaxIterations() {
    return this.maxIterations;
  }

  @Override
  public void setConstant(double constant) {
    this.C = constant;
  }

  @Override
  public double getConstant() {
    return this.C;
  }
}
