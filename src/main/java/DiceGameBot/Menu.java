package DiceGameBot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

class Menu {

    private Game currentGame;
    boolean rollMenuCheck;
    private String turnMenuText;
    String rollMenuText, rollMenuCallBack, rollMenuCallBackText;
    private ArrayList<String> rollMenuButtonText;
    int numDiceInHand;


    Menu(Game currentGame) {
        this.currentGame = currentGame;
        rollMenuButtonText = new ArrayList<>();
    }

    private static InlineKeyboardMarkup createStartMenu() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineStartMenuButton1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineStartMenuButton2 = new InlineKeyboardButton();
        inlineStartMenuButton1.setText("Join Game");
        inlineStartMenuButton1.setCallbackData("Join Game");
        inlineStartMenuButton2.setText("Start Game");
        inlineStartMenuButton2.setCallbackData("Start Game");
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineStartMenuButton1);
        keyboardButtonsRow1.add(inlineStartMenuButton2);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    SendMessage startMenu() {
        String text = "Welcome to Zonk. Tap join to create new game.";
        if (currentGame.users.size() > 0) {
            String userlist = "";
            for (User user : currentGame.users) {
                userlist = userlist + "\n" + user.getFirstName();
            }
            text = "Welcome to Zonk." + userlist + "\n" + currentGame.users.size() + " players ready.\nJoin or start new game.";
        }
        return new SendMessage().setChatId(currentGame.getChatId()).setText(text).setReplyMarkup(createStartMenu());
    }

    private static InlineKeyboardMarkup createTurnMenu() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineTurnMenuButton1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineTurnMenuButton2 = new InlineKeyboardButton();
        inlineTurnMenuButton1.setText("Roll dice");
        inlineTurnMenuButton1.setCallbackData("Roll dice");
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineTurnMenuButton1);
        inlineTurnMenuButton2.setText("Save points and end turn");
        inlineTurnMenuButton2.setCallbackData("Bank");
        List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
        keyboardButtonsRow2.add(inlineTurnMenuButton2);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        rowList.add(keyboardButtonsRow2);
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    SendMessage turnMenu() {
        //String text = "Turn information";
        this.numDiceInHand = 6 - currentGame.getCurrentPlayer().getNumDiceInUse();
        String turnPointsList = "";
        if (currentGame.getPlayers().size() > 0) {
            String userlist = "";
            for (Player player : currentGame.getPlayers()) {
                userlist = userlist + "\n" + player.getName() + "'s points: " + player.getPlayerScore();
            }
            turnPointsList = userlist + "\n";
        }
        this.turnMenuText = turnPointsList + "\nIt is @" + currentGame.getCurrentPlayer().getUser().getUserName() + "'s turn\n" +
                currentGame.getCurrentPlayer().getName() + "'s total score: " + currentGame.getCurrentPlayer().getPlayerScore() + "\n" +
                currentGame.getCurrentPlayer().getName() + "'s current score for this turn: " + currentGame.getCurrentPlayer().getTurnScore() + "\n" +
                currentGame.getCurrentPlayer().getName() + " have " + numDiceInHand + " dice to roll\n";

        return new SendMessage().setChatId(currentGame.getChatId()).setText(this.turnMenuText).setReplyMarkup(createTurnMenu());
    }

    EditMessageText turnMenu(int messageId) {
        //Turn information
        this.numDiceInHand = 6 - currentGame.getCurrentPlayer().getNumDiceInUse();
        String turnPointsList = "";
        if (currentGame.getPlayers().size() > 0) {
            String userlist = "";
            for (Player player : currentGame.getPlayers()) {
                userlist = userlist + "\n" + player.getName() + "'s points: " + player.getPlayerScore();
            }
            turnPointsList = userlist + "\n";
        }
        this.turnMenuText = turnPointsList + "\nIt is @" + currentGame.getCurrentPlayer().getUser().getUserName() + "'s turn\n" +
                currentGame.getCurrentPlayer().getName() + "'s total score: " + currentGame.getCurrentPlayer().getPlayerScore() + "\n" +
                currentGame.getCurrentPlayer().getName() + "'s current score for this turn: " + currentGame.getCurrentPlayer().getTurnScore() + "\n" +
                currentGame.getCurrentPlayer().getName() + " have " + numDiceInHand + " dice to roll\n";
        return new EditMessageText().setChatId(currentGame.getChatId()).setMessageId(messageId).setText(this.turnMenuText).setReplyMarkup(createTurnMenu());
    }

    EditMessageText rollMenu(int messageId) {
        return new EditMessageText().setChatId(currentGame.getChatId()).setMessageId(messageId).setText(this.rollMenuText).setReplyMarkup(createRollMenu());
    }

    private InlineKeyboardMarkup createRollMenu() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineRollMenuButton1 = new InlineKeyboardButton();
        inlineRollMenuButton1.setText(this.rollMenuCallBackText);
        inlineRollMenuButton1.setCallbackData(this.rollMenuCallBack);
        if (this.rollMenuCheck) {
            inlineRollMenuButton1.setText("Continue turn");
            inlineRollMenuButton1.setCallbackData("KeepRoll");
        }
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineRollMenuButton1);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        this.checkRollMenuButtons();
        if (!this.rollMenuButtonText.isEmpty()) {
            List<InlineKeyboardButton> rollButtons1 = new ArrayList<>();
            List<InlineKeyboardButton> rollButtons2 = new ArrayList<>();
            List<InlineKeyboardButton> rollButtons3 = new ArrayList<>();
            List<InlineKeyboardButton> rollButtons4 = new ArrayList<>();
            List<InlineKeyboardButton> rollButtons5 = new ArrayList<>();
            List<List<InlineKeyboardButton>> rollButtonsList = new ArrayList<>();
            InlineKeyboardButton inlineRollMenuButtonTake1 = new InlineKeyboardButton();
            inlineRollMenuButtonTake1.setCallbackData("0");
            rollButtons1.add(inlineRollMenuButtonTake1);
            rollButtonsList.add(rollButtons1);
            InlineKeyboardButton inlineRollMenuButtonTake2 = new InlineKeyboardButton();
            inlineRollMenuButtonTake2.setCallbackData("1");
            rollButtons2.add(inlineRollMenuButtonTake2);
            rollButtonsList.add(rollButtons2);
            InlineKeyboardButton inlineRollMenuButtonTake3 = new InlineKeyboardButton();
            inlineRollMenuButtonTake3.setCallbackData("2");
            rollButtons3.add(inlineRollMenuButtonTake3);
            rollButtonsList.add(rollButtons3);
            InlineKeyboardButton inlineRollMenuButtonTake4 = new InlineKeyboardButton();
            inlineRollMenuButtonTake4.setCallbackData("3");
            rollButtons4.add(inlineRollMenuButtonTake4);
            rollButtonsList.add(rollButtons4);
            InlineKeyboardButton inlineRollMenuButtonTake5 = new InlineKeyboardButton();
            inlineRollMenuButtonTake5.setCallbackData("4");
            rollButtons5.add(inlineRollMenuButtonTake4);
            rollButtonsList.add(rollButtons5);
            for (int i = 0; i < this.rollMenuButtonText.size(); i++) {
                rollButtonsList.get(i).get(0).setText(this.rollMenuButtonText.get(i));
                rowList.add(rollButtonsList.get(i));
            }
        }
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    private void checkRollMenuButtons() {
        this.rollMenuButtonText.clear();
        for (int i = 0; i < currentGame.getDices().diceRollCounts.size(); i++) {
            String scoreType = currentGame.getDices().diceRollCounts.get(i).type;
            int scoreAmount = currentGame.getDices().diceRollCounts.get(i).points;
            this.rollMenuButtonText.add((i + 1) + ". " + scoreType + " - " + scoreAmount + " points");
        }
    }

}