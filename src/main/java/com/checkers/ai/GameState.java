package main.java.com.checkers.ai;

public interface GameState<M> {
    GameState<M> clone(); // Clone the state
    void applyMove(M move); // Apply a move to the state
}



