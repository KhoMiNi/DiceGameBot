package DiceGameBot;

import DiceGameBot.configuration.EnglishText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.ArrayList;
import java.util.HashMap;

class Game {
    private int winScore;
    private final long chatId;
    private Message actualMessage;
    private Menu menu;
    private boolean gameOver;
    private String gameOverText;
    private Player currentPlayer;
    private final HashMap<String, User> userIdMap;
    private final ArrayList<Player> players;
    private ArrayList<User> users;
    private int index;
    private int surrendered;
    private final Dices dices;

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
        currentPlayer.bankScore();
        checkWin();
    }

    private void checkWin() {
        if (isWin()) {
            gameOver(String.format(EnglishText.WINNER_MESSAGE, currentPlayer.getName(),currentPlayer.getPlayerScore()));
        } else if (isLastPlayer()) {
            gameOver(String.format(EnglishText.WINNER_AS_LAST, currentPlayer.getName()));
        } else {
            nextPlayer();
        }
    }

    private boolean isLastPlayer() {
        return this.surrendered == (this.players.size() - 1);
    }

    private boolean isWin() {
        return currentPlayer.getPlayerScore() >= this.winScore;
    }

    //RENAME IT!
    void gameOver() {
        this.users.clear();
        this.players.clear();
        this.index = 0;
        this.surrendered = 0;
        this.gameOver = true;
    }

    void gameOver(String text) {
        this.gameOverText = text;
        gameOver();
    }

    void nextPlayer() {
        currentPlayer.resetPlayerTurn();
        findNextPlayer();
    }

    private void findNextPlayer() {
        this.index++;
        if (this.index >= players.size()) this.index = 0;
        currentPlayer = players.get(this.index);
        if (!(currentPlayer.isInGame())) nextPlayer();  //It shouldn't be a problem but add check
    }

    void rollAction() {
        this.menu.rollMenuCheck = false;
        dices.rollAndAnalyzeDice(this.currentPlayer, this.menu.numDiceInHand);

        //Move it to menu class?
        this.menu.rollMenuText = dices.rollResult + dices.rollText;
        this.menu.rollMenuCallBack = EnglishText.TAKE_ALL;
        this.menu.rollMenuCallBackText = EnglishText.TAKE_ALL;
        if (dices.zonk) {
            this.menu.rollMenuCallBack = EnglishText.END_TURN;
            this.menu.rollMenuCallBackText = EnglishText.END_TURN;
        }
    }

    void setPlayersList() {
        this.players.clear();
        this.index = 0;
        this.users.forEach(n -> this.players.add(new Player(n)));
        currentPlayer = players.get(this.index);
        this.gameOver = false;
    }

    void addUser(User user) {
        users.add(user);
        userIdMap.put(user.getUserName(), user);
    }

    //ADD index check. Should be 0-4
    void take(int index) {
        this.menu.rollMenuCheck = true;
        currentPlayer.setTurnScore(dices.diceRollCounts.get(index).points);
        currentPlayer.setNumDiceInUse(dices.diceRollCounts.get(index).indices.size());
        dices.diceRollCounts.remove(dices.diceRollCounts.get(index));
        this.menu.rollMenuText = String.format(EnglishText.CURRENT_SCORE, currentPlayer.getName(), currentPlayer.getTurnScore());
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
        int userIndex = this.users.indexOf(from);
        if (this.players.get(userIndex).isInGame()) {
            this.players.get(userIndex).setInGame(false);
            this.surrendered++;
            checkNoPlayers();
        }
    }

    private void checkNoPlayers() {
        if (this.surrendered >= this.players.size()) {
            gameOver(EnglishText.NO_PLAYERS);
        }
    }

    public void setWinScore(String s) {
        try {
            int score = Integer.parseInt(s.trim());
            if (score >= 5000 && score <= 50000) this.winScore = score;
        }catch (NumberFormatException e){
            this.winScore = 10000;
        }
    }

    User getUser(String username) {
        return userIdMap.get(username);
    }

    public int getWinScore() {
        return winScore;
    }

    public int getActualMessageId(){return actualMessage.getMessageId();}

    public ArrayList<User> getUsers() {
        return users;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setActualMessage(Message actualMessage) {
        this.actualMessage = actualMessage;
    }

    public Message getActualMessage() {
        return actualMessage;
    }
}