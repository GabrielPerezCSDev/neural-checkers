package main.java.com.checkers.ai;

import java.util.ArrayList;
import java.util.Random;
import main.java.com.checkers.game.CheckersData;
import main.java.com.checkers.game.CheckersMove;

public class BlunderBot extends  AdversarialSearch {
    private int player;
    private Random random;

    public BlunderBot(int player) {
        this.player = player;
        this.random = new Random(); // Initialize a random number generator
    }

    @Override
    public void setCheckersData(CheckersData board, int currentPlayer) {
        // No advanced logic for BlunderBot; it just reacts to the given legal moves.
    }

    @Override
    public CheckersMove makeMove(CheckersMove[] legalMoves) {
        if (legalMoves != null && legalMoves.length > 0) {
            // Step 1: Filter for capture moves
            ArrayList<CheckersMove> captureMoves = new ArrayList<>();
            for (CheckersMove move : legalMoves) {
                if (move.isCapture()) {
                    captureMoves.add(move);
                }
            }

            // Step 2: If capture moves exist, pick one randomly
            if (!captureMoves.isEmpty()) {
                int randomIndex = random.nextInt(captureMoves.size());
                return captureMoves.get(randomIndex);
            }

            // Step 3: If no capture moves, pick any move randomly
            int randomIndex = random.nextInt(legalMoves.length);
            return legalMoves[randomIndex];
        }
        return null; // No move if none are available
    }

    
}
