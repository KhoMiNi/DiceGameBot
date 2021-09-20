package DiceGameBot;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.ArrayList;
import java.util.HashMap;

class Game {
    ArrayList<Player> getPlayers() {
        return this.players;
    }

    String getGameOverText() {
        return this.gameOverText;
    }

    long getChatId() {
        return this.chatId;
    }

    boolean isGameOver() {
        return this.gameOver;
    }

    Player getCurrentPlayer() {
        return this.currentPlayer;
    }

    private long chatId;
    Message actualMessage;
    Menu menu;
    private boolean gameOver;
    private String gameOverText;
    private Player currentPlayer;
    private HashMap<String, User> userIdMap;
    private ArrayList<Player> players;
    ArrayList<User> users;
    private int index, surrendered;
    private Dices dices;

    Dices getDices() {
        return this.dices;
    }

    Game(long chatId) {
        this.actualMessage = new Message();
        this.chatId = chatId;
        this.gameOver = true;
        this.index = 0;
        this.userIdMap = new HashMap<>();
        dices = new Dices();
        users = new ArrayList<>();
        players = new ArrayList<>();
        surrendered = 0;
        this.gameOverText = "default";
        this.menu = new Menu(this);
    }

    void bankAction() {
        int turnScore = currentPlayer.getTurnScore();
        currentPlayer.setPlayerScore(turnScore);
        if (currentPlayer.getPlayerScore() >= 10000) { //Winscore should be replaced by variable
            this.gameOverText = currentPlayer.getName() + " won the game with " + currentPlayer.getPlayerScore() + " points!";
            gameOver();
        } else if (this.surrendered == (this.players.size() - 1)) {
            this.gameOverText = currentPlayer.getName() + " won the game as the last player!";
            gameOver();
        } else {
            nextPlayer();
        }
    }

    //////RENAME IT!
    void gameOver() {
        this.users.clear();
        this.players.clear();
        this.index = 0;
        this.surrendered = 0;
        this.gameOver = true;
    }

    ////////////////////////////////////////////////////////
    void nextPlayer() {
        this.index++;
        if (this.index >= players.size()) this.index = 0;
        currentPlayer.resetPlayerTurn();
        currentPlayer = players.get(this.index);
        if (!(currentPlayer.isInGame())) nextPlayer();  //It shouldn't be a problem but add check
    }

    void rollAction() {
        this.menu.rollMenuCheck = false;
        dices.rollAndAnalyzeDice(this.currentPlayer, this.menu.numDiceInHand);

        //Move it to menu class?
        this.menu.rollMenuText = dices.rollResult + dices.rollText;
        this.menu.rollMenuCallBack = "Take All";
        this.menu.rollMenuCallBackText = "Take All";
        if (dices.zonk) {
            this.menu.rollMenuCallBack = "End Turn";
            this.menu.rollMenuCallBackText = "End Turn";
        }
        //

    }

    void setPlayersList() {
        this.players.clear();
        this.index = 0;
        this.users.forEach((n) -> this.players.add(new Player(n)));
        currentPlayer = players.get(this.index);
        this.gameOver = false;
    }

    void addUser(User user) {
        users.add(user);
        userIdMap.put(user.getUserName(), user);
    }

    ////ADD index check. Should be 0-4
    void take(int index) {
        this.menu.rollMenuCheck = true;
        currentPlayer.setTurnScore(dices.diceRollCounts.get(index).points);
        currentPlayer.setNumDiceInUse(dices.diceRollCounts.get(index).indices.size());
        dices.diceRollCounts.remove(dices.diceRollCounts.get(index));
        this.menu.rollMenuText = currentPlayer.getName() + "'s current score for this turn: " + currentPlayer.getTurnScore();
        if (currentPlayer.getNumDiceInUse() >= 6) currentPlayer.resetNumDiceInUse();
    }

    void takeAll() {
        for (DiceCombinations diceCombinations : dices.diceRollCounts) {
            currentPlayer.setTurnScore(diceCombinations.points);
            currentPlayer.setNumDiceInUse(diceCombinations.indices.size());
        }
        dices.diceRollCounts.clear();
        if (currentPlayer.getNumDiceInUse() >= 6) currentPlayer.resetNumDiceInUse();
    }

    void surrender(User from) {
        int index = this.users.indexOf(from);
        if (this.players.get(index).isInGame()) {
            this.players.get(index).setInGame(false);
            this.surrendered++;
            if (this.surrendered >= this.players.size()) {
                gameOver();
                this.gameOverText = "There is no players";
            }
        }
    }

    User getUser(String username) {
        return userIdMap.get(username);
    }


}