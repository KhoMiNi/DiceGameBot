package DiceGameBot;

import DiceGameBot.configuration.RussianText;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.ChatMember;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class MessageProcess implements Runnable {

    private Bot bot;

    MessageProcess(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void run() {
        while (true) {
            for (Message message = bot.processQueue.poll(); message != null; message = bot.processQueue.poll()) {
                try {
                    processMessage(message);
                } catch (Exception e) {
                    App.loggerWarn.error("Message queue error", e);
                }
            }
            Thread.yield();
        }
    }

    private void processMessage(Message message) {

        String messageText = message.getText();
        String commandText = "";
        ChatMember chatMember = new ChatMember();
        Long chatId = message.getChatId();
        String[] command = messageText.split(" ", 2);
        if (!(bot.games.containsKey(chatId))) bot.games.put(chatId, new Game(chatId));
        Game currentGame = bot.games.get(chatId);
        try {
            chatMember = bot.execute(new GetChatMember().setChatId(chatId).setUserId(message.getFrom().getId()));
        } catch (TelegramApiException e) {
            App.loggerWarn.error("Get chatMember at  " + chatId, e);
        }

        if (command.length >= 2) {
            if (command[1].length() > 0) commandText = command[1];
        }

        switch (command[0]) {
            case ("/start@ZonkGamebot"):
                if (currentGame.isGameOver() && currentGame.getUsers().isEmpty()) {
                    try {
                        currentGame.setWinScore(commandText);
                        currentGame.setActualMessage(bot.execute(currentGame.getMenu().startMenu()));
                        App.loggerGameInfo.info("Game launched at " + chatId);
                    } catch (TelegramApiException e) {
                        App.loggerWarn.error("Start game error at " + chatId, e);
                    }
                } else {
                    try {
                        bot.execute(new SendMessage().setChatId(chatId).setText("Already launched"));
                        currentGame.setActualMessage(bot.execute(new SendMessage().setChatId(chatId).setText(currentGame.getActualMessage().getText()).setReplyMarkup(currentGame.getActualMessage().getReplyMarkup())));
                    } catch (TelegramApiException e) {
                        App.loggerWarn.error("Start game error at " + chatId, e);
                    }

                }
                break;
            case ("/reset@ZonkGamebot"):
                if ((chatMember.getStatus().equals("administrator")) || (chatMember.getStatus().equals("creator"))) {
                    gameReset(currentGame);
                }
                break;
            case ("/rules@ZonkGamebot"):
                try {
                    bot.execute(new SendMessage().setChatId(chatId).setText(RussianText.RULES));
                } catch (TelegramApiException e) {
                    App.loggerWarn.error("Rules command at " + chatId, e);
                }
                break;
            case ("/surrender@ZonkGamebot"):
                if (currentGame.getUsers().contains(message.getFrom())) {
                    removePlayer(currentGame, message.getFrom());
                }
                break;
            case ("/kick@ZonkGamebot"):
                if ((chatMember.getStatus().equals("administrator")) || (chatMember.getStatus().equals("creator"))) {
                    if (commandText.length() > 0) {
                        String commandArg = commandText.substring(1);
                        User user = currentGame.getUser(commandArg);
                        if (currentGame.getUsers().contains(user)) {
                            removePlayer(currentGame, user);
                        } else {
                            try {
                                bot.execute(new SendMessage().setChatId(chatId).setText("Not in game"));
                            } catch (TelegramApiException e) {
                                App.loggerWarn.error("Remove player at " + chatId, e);
                            }
                        }

                    }

                }
                break;
            default:
                break;
        }
    }

    private void gameReset(Game currentGame) {
        if (currentGame.isGameOver()) {
            try {
                currentGame.gameOver();
                bot.execute(new SendMessage().setChatId(currentGame.getChatId()).setText("There is nothing to reset, but ok"));
                currentGame.setActualMessage(bot.execute(currentGame.getMenu().startMenu()));
            } catch (TelegramApiException e) {
                App.loggerWarn.error("Game reset error", e);
            }
        } else {
            try {
                currentGame.gameOver();
                bot.execute(new SendMessage().setChatId(currentGame.getChatId()).setText("This game is no more!"));
                currentGame.setActualMessage(bot.execute(currentGame.getMenu().startMenu()));
            } catch (TelegramApiException e) {
                App.loggerWarn.error("Game reset error", e);
            }

        }
    }

    private void removePlayer(Game currentGame, User user) {
        if(currentGame.getPlayers().size()< currentGame.getUsers().size()) return;
        currentGame.surrender(user);
        if (currentGame.isGameOver()) {
            try {
                currentGame.gameOver();
                bot.execute(new SendMessage().setChatId(currentGame.getChatId()).setText(currentGame.getGameOverText()));
                currentGame.setActualMessage(bot.execute(currentGame.getMenu().startMenu()));
            } catch (TelegramApiException e) {
                App.loggerWarn.error("Remove player error", e);
            }
        } else if (user.equals(currentGame.getCurrentPlayer().getUser())) {
            try {
                currentGame.nextPlayer();
                currentGame.setActualMessage(bot.execute(currentGame.getMenu().turnMenu()));
            } catch (TelegramApiException e) {
                App.loggerWarn.error("Remove player error", e);
            }
        }
    }
}
