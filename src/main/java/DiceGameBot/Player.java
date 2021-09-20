package DiceGameBot;

import DiceGameBot.configuration.Constants;
import org.telegram.telegrambots.meta.api.objects.User;


class Player {

    private User user;
    private String playerName;
    private int playerScore;
    private int turnScore;
    private int numDiceInUse;
    private boolean inGame;

    Player(User user) {
        this.user = user;
        playerName = user.getFirstName();
        playerScore = 0;
        turnScore = 0;
        numDiceInUse = 0;
        inGame = true;
    }

    String getName() {
        return playerName;
    }

    void setPlayerScore(int score) {
        playerScore = playerScore + score;
    }

    int getPlayerScore() {
        return playerScore;
    }

    void setTurnScore(int score) {
        turnScore = turnScore + score;
    }

    Integer getTurnScore() {
        return turnScore;
    }

    void resetTurnScore() {
        turnScore = 0;
    }

    void setNumDiceInUse(Integer numDice) {
        numDiceInUse = numDiceInUse + numDice;
    }

    Integer getNumDiceInUse() {
        return numDiceInUse;
    }

    void resetNumDiceInUse() {
        numDiceInUse = 0;
    }

    void resetPlayerTurn() {
        resetTurnScore();
        resetNumDiceInUse();
    }

    User getUser() {
        return user;
    }

    boolean isInGame() {
        return inGame;
    }

    void setInGame(boolean inGame) {
        this.inGame = inGame;
    }

    void bankScore(){
        playerScore = playerScore + turnScore;
    }
}
