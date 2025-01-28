package main.java.com.checkers.ai;

import main.java.com.checkers.game.CheckersData;
import main.java.com.checkers.game.CheckersMove;

/**
 * 
 * @author
 *
 */

/**
 * This class is to be extended by the class MonteCarloTreeSearch.
 */
public abstract class AdversarialSearch {
    protected CheckersData board;
    protected int player;
    protected int maxIterations;
    protected double C;
    // An instance of this class will be created in the Checkers.Board
    // It would be better to keep the default constructor.

    public void setCheckersData(CheckersData board, int player) {
        this.board = board;
        this.player = player;
    }
    
    /** 
     * 
     * @return an array of valid moves
     */
    protected CheckersMove[] legalMoves() {
    	return board.getLegalMovesMap(player).values().toArray(new CheckersMove[0]); 
    }
	
    /**
     * Return a move returned from the Monte Carlo tree search.
     * 
     * @param legalMoves
     * @return CheckersMove 
     */
    public abstract CheckersMove makeMove(CheckersMove[] legalMoves);

    protected void setMaxIterations(int iterations){
        this.maxIterations = iterations;
    }
    protected int getMaxIterations(){
        return maxIterations;
    }

    protected void setConstant(double C){
        this.C = C;
    }

    protected double getConstant(){
        return this.C;
    }
}
