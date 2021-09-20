package DiceGameBot;

import DiceGameBot.configuration.EnglishText;
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
                    App.loggerWarn.error(EnglishText.ERROR_CALLBACK, e);
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
        if (messageId != currentGame.getActualMessageId()) {
            return;
        }
        /* Add new player and start game buttons */
        checkStartJoin(callbackQuery);
        /*Check player actions. Ignore if not current player*/
        if (!checkTurn(callbackQuery)) {
            return;
        }
        /*Process player actions */
        switch (answer) {
            case EnglishText.BANK:
                answerBank(currentGame, chatId, messageId);
                break;
            case EnglishText.END_TURN:
                answerEndTurn(currentGame, chatId, messageId);
                break;
            case EnglishText.ROLL_DICE:
                answerRollDice(currentGame, messageId);
                break;
            case EnglishText.TAKE_ALL:
                answerTakeAll(currentGame, messageId);
                break;
            case "0":
            case "1":
            case "2":
            case "3":
            case "4":
                answerTake(answer,currentGame, messageId);
                break;
            case EnglishText.KEEP_ROLL:
                answerKeepRoll(currentGame, messageId);
                break;
            default:
                break;
        }

    }

    private void answerKeepRoll(Game currentGame, int messageId) {
        try {
            bot.execute(currentGame.getMenu().turnMenu(messageId));
        } catch (TelegramApiException e) {
            App.loggerWarn.error(EnglishText.ERROR_TURNMENU, e);
        }
    }

    private void answerTake(String answer, Game currentGame, int messageId) {
        int index = Integer.parseInt(answer);
        currentGame.take(index);
        editRollMenu(currentGame, messageId);
    }

    private void answerTakeAll(Game currentGame, int messageId) {
        try {
            currentGame.takeAll();
            bot.execute(currentGame.getMenu().turnMenu(messageId));
        } catch (TelegramApiException e) {
            App.loggerWarn.error(EnglishText.ERROR_TURNMENU, e);
        }
    }

    private void answerRollDice(Game currentGame, int messageId) {
        currentGame.rollAction();
        editRollMenu(currentGame, messageId);
    }

    private void answerEndTurn(Game currentGame, Long chatId, int messageId) {
        currentGame.nextPlayer();
        recreateTurnMenu(currentGame, chatId, messageId);
    }

    private void answerBank(Game currentGame, Long chatId, int messageId) {
        currentGame.bankAction();
        if (currentGame.isGameOver()) {
            createEndMenu(currentGame, chatId, messageId);
        } else {
            recreateTurnMenu(currentGame, chatId, messageId);
        }
    }

    private void checkStartJoin(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        String answer = callbackQuery.getData();
        int messageId = callbackQuery.getMessage().getMessageId();
        Game currentGame = bot.games.get(chatId);
        switch (answer){
            case EnglishText.START_GAME:
                startGame(currentGame, chatId, messageId);
                break;
            case EnglishText.JOIN_GAME:
                joinGame(currentGame, chatId, messageId, callbackQuery);
                break;
            default:
        }
    }

    private void joinGame(Game currentGame, Long chatId, int messageId, CallbackQuery callbackQuery) {
        if (!currentGame.getUsers().contains(callbackQuery.getFrom())) {
            currentGame.addUser(callbackQuery.getFrom());
            recreateStartMenu(currentGame, chatId, messageId);
        }
    }

    private void startGame(Game currentGame, Long chatId, int messageId) {
        if (currentGame.getUsers().size() > 1) {
            currentGame.setPlayersList();
            recreateTurnMenu(currentGame, chatId, messageId);
        }
    }

    private void editRollMenu(Game currentGame, int messageId) {
        try {
            bot.execute(currentGame.getMenu().rollMenu(messageId));
        } catch (TelegramApiException e) {
            App.loggerWarn.error(EnglishText.ERROR_ROLLMENU, e);
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
            if (isSuccessfullyDeleted(chatId, messageId)) {
                currentGame.setActualMessage(bot.execute(currentGame.getMenu().turnMenu()));
            }
        } catch (TelegramApiException e) {
            App.loggerWarn.error(EnglishText.ERROR_TURNMENU, e);
        }
    }

    private void recreateStartMenu(Game currentGame, Long chatId, int messageId) {
        try {
            if (isSuccessfullyDeleted(chatId, messageId)) {
                currentGame.setActualMessage(bot.execute(currentGame.getMenu().startMenu()));
            }
        } catch (TelegramApiException e) {
            App.loggerWarn.error(EnglishText.ERROR_STARTMENU, e);
        }
    }

    private void createEndMenu(Game currentGame, Long chatId, int messageId) {
        try {
            if (isSuccessfullyDeleted(chatId, messageId)) {
                bot.execute(new SendMessage().setChatId(chatId).setText(currentGame.getGameOverText()));
                currentGame.setActualMessage(bot.execute(currentGame.getMenu().startMenu()));
            }
        } catch (TelegramApiException e) {
            App.loggerWarn.error(EnglishText.ERROR_ENDMENU, e);
        }
    }

    private boolean isSuccessfullyDeleted(Long chatId, int messageId) throws TelegramApiException {
        return bot.execute(new DeleteMessage(chatId, messageId));
    }
}