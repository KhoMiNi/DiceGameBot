package DiceGameBot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class CallbackQueryProcess implements Runnable {

    private Bot bot;

    CallbackQueryProcess(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void run() {
        while (true) {
            for (CallbackQuery callbackQuery = bot.processCallbackQueue.poll(); callbackQuery != null; callbackQuery = bot.processCallbackQueue.poll()) {
                try {
                    processCallbackQuery(callbackQuery);
                } catch (Exception e) {
                    App.loggerWarn.error("Callback query process error", e);
                }
            }
            Thread.yield();
        }
    }

    private void processCallbackQuery(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        String answer = callbackQuery.getData();
        int messageId = callbackQuery.getMessage().getMessageId();
        Game currentGame = bot.games.get(chatId);


        /* Check message. Ignore if not actual message*/
        if (messageId != currentGame.actualMessage.getMessageId()) {
            return;
        }


        /* Add new player and start game buttons */
        if (answer.equals("Start Game")) {
            if (currentGame.users.size() > 1) {
                currentGame.setPlayersList();
                App.loggerGameInfo.info("Game started at " + chatId + " with " + currentGame.users.size() + " players");
                recreateTurnMenu(currentGame, chatId, messageId);
            }
        }
        if (answer.equals("Join Game")) {
            if (!currentGame.users.contains(callbackQuery.getFrom())) {
                currentGame.addUser(callbackQuery.getFrom());
                recreateStartMenu(currentGame, chatId, messageId);
            }
        }

        /*Check player actions. Ignore if not current player*/
        if (!checkTurn(callbackQuery)) {
            return;
        }
        /*Process player actions */

        switch (answer) {
            case "Bank":
                currentGame.bankAction();
                if (currentGame.isGameOver()) {
                    createEndMenu(currentGame, chatId, messageId);
                    App.loggerGameInfo.info("Game finished at " + chatId);
                } else {
                    recreateTurnMenu(currentGame, chatId, messageId);
                }
                break;
            case "End Turn":
                currentGame.nextPlayer();
                recreateTurnMenu(currentGame, chatId, messageId);
                break;
            case "Roll dice":
                currentGame.rollAction();
                editRollMenu(currentGame, messageId);
                break;
            case "Take All":
                try {
                    currentGame.takeAll();
                    bot.execute(currentGame.menu.turnMenu(messageId));
                } catch (TelegramApiException e) {
                    App.loggerWarn.error("Turn menu error", e);
                }
                break;
            case "0":
            case "1":
            case "2":
            case "3":
            case "4":
                int index = Integer.parseInt(answer);
                currentGame.take(index);
                editRollMenu(currentGame, messageId);
                break;
            case "KeepRoll":
                try {
                    bot.execute(currentGame.menu.turnMenu(messageId));
                } catch (TelegramApiException e) {
                    App.loggerWarn.error("Turn menu error", e);
                }
                break;

            default:
                break;
        }

    }

    private void editRollMenu(Game currentGame, int messageId) {
        try {
            bot.execute(currentGame.menu.rollMenu(messageId));
        } catch (TelegramApiException e) {
            App.loggerWarn.error("Roll menu error", e);
        }
    }


    private boolean checkTurn(CallbackQuery callbackQuery) {
        boolean check = false;
        if (!(bot.games.containsKey(callbackQuery.getMessage().getChatId()))) {
            return false;
        }
        if (!(bot.games.get(callbackQuery.getMessage().getChatId()).isGameOver())) {
            check = callbackQuery.getFrom().equals(bot.games.get(callbackQuery.getMessage().getChatId()).getCurrentPlayer().getUser());
        }
        return check;
    }

    private void recreateTurnMenu(Game currentGame, Long chatId, int messageId) {
        try {
            if (bot.execute(new DeleteMessage(chatId, messageId))) {
                currentGame.actualMessage = bot.execute(currentGame.menu.turnMenu());
            }
        } catch (TelegramApiException e) {
            App.loggerWarn.error("Turn menu error", e);
        }
    }

    private void recreateStartMenu(Game currentGame, Long chatId, int messageId) {
        try {
            if (bot.execute(new DeleteMessage(chatId, messageId))) {
                currentGame.actualMessage = bot.execute(currentGame.menu.startMenu());
            }
        } catch (TelegramApiException e) {
            App.loggerWarn.error("Start menu error", e);
        }
    }

    private void createEndMenu(Game currentGame, Long chatId, int messageId) {
        try {
            if (bot.execute(new DeleteMessage(chatId, messageId))) {
                bot.execute(new SendMessage().setChatId(chatId).setText(currentGame.getGameOverText()));
                currentGame.actualMessage = bot.execute(currentGame.menu.startMenu());
            }
        } catch (TelegramApiException e) {
            App.loggerWarn.error("End menu error", e);
        }
    }

}