package DiceGameBot;

import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.ChatMember;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class MessageProcess implements Runnable {

    private Bot bot;
    private String rulesText;


    MessageProcess(Bot bot) {
        this.bot = bot;
        /////
        rulesText = "Для игры нужно минимум два участника.\n" +
                "Свой ход игрок начинает с броска 6 костей. \n" +
                "Затем он должен выбрать комбинации, которые принесут ему очки.\n" +
                "После чего можно либо закончить ход и сохранить очки, либо повторить бросок оставшимися костями.\n" +
                "Если собраны все кости, то количество доступных костей снова становится 6.\n" +
                "Если во время броска не выпадает ни одной приносящей очки комбинации, то все очки за этот ход сгорают, а ход переходит к следующему игроку.\n" +
                "Игра завершается, когда один из игроков достиг 10000 очков.\n\n" +
                "Комбинации:\n" +
                "Стрейт - шесть разных - 2000 очков\n" +
                "Три пары - 1500 очков\n" +
                "Три одинаковых - значение*100 \n" +
                "Три единицы - 1000\n" +
                "Одинаковые начиная с четвертой умножают результат на 2(три тройки - 300, четыре тройки - 600, пять троек - 1200)  \n" +
                "Единица - 100\n" +
                "Пятерка - 50\n\n" +
                "- команда /start@ZonkGamebot - начать новую игру\n" +
                "- команда /surrender@ZonkGamebot - выйти из игры\n" +
                "Только для админов чата:\n" +
                "- команда /reset@ZonkGamebot - сбросить игру\n" +
                "- команда /kick@ZonkGamebot @username - исключить из игры игрока @username";
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
            case ("/start@TestDiceGamebot"):
            case ("/Zonk"):
                if (currentGame.isGameOver() && currentGame.users.isEmpty()) {
                    try {
                        currentGame.actualMessage = bot.execute(currentGame.menu.startMenu());
                        App.loggerGameInfo.info("Game launched at " + chatId);
                    } catch (TelegramApiException e) {
                        App.loggerWarn.error("Start game error at " + chatId, e);
                    }
                } else {
                    try {
                        bot.execute(new SendMessage().setChatId(chatId).setText("Already launched"));
                        currentGame.actualMessage = bot.execute(new SendMessage().setChatId(chatId).setText(currentGame.actualMessage.getText()).setReplyMarkup(currentGame.actualMessage.getReplyMarkup()));
                    } catch (TelegramApiException e) {
                        App.loggerWarn.error("Start game error at " + chatId, e);
                    }

                }
                break;
            case ("/reset@ZonkGamebot"):
            case ("/Zonkreset"):
                if ((chatMember.getStatus().equals("administrator")) || (chatMember.getStatus().equals("creator"))) {
                    gameReset(currentGame);
                }
                break;
            case ("/rules"):
            case ("/rules@ZonkGamebot"):
                try {
                    bot.execute(new SendMessage().setChatId(chatId).setText(rulesText));
                } catch (TelegramApiException e) {
                    App.loggerWarn.error("Rules command at " + chatId, e);
                }
                break;
            case ("/exitzonk"):
            case ("/surrender@ZonkGamebot"):
                if (currentGame.users.contains(message.getFrom())) {
                    removePlayer(currentGame, message.getFrom());
                }
                break;
            case ("/kickzonk"):
            case ("/kick@ZonkGamebot"):
                if ((chatMember.getStatus().equals("administrator")) || (chatMember.getStatus().equals("creator"))) {
                    if (commandText.length() > 0) {
                        String commandArg = commandText.substring(1);
                        User user = currentGame.getUser(commandArg);
                        if (currentGame.users.contains(user)) {
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
                App.loggerGameInfo.info("Try to reset game at " + currentGame.getChatId());
                bot.execute(new SendMessage().setChatId(currentGame.getChatId()).setText("There is nothing to reset, but ok"));
                currentGame.actualMessage = bot.execute(currentGame.menu.startMenu());
            } catch (TelegramApiException e) {
                App.loggerWarn.error("Game reset error", e);
            }
        } else {
            try {
                currentGame.gameOver();
                App.loggerGameInfo.info("Game reset at " + currentGame.getChatId());
                bot.execute(new SendMessage().setChatId(currentGame.getChatId()).setText("This game is no more!"));
                currentGame.actualMessage = bot.execute(currentGame.menu.startMenu());
            } catch (TelegramApiException e) {
                App.loggerWarn.error("Game reset error", e);
            }

        }
    }

    private void removePlayer(Game currentGame, User user) {
        if(currentGame.getPlayers().size()< currentGame.users.size()) return;
        currentGame.surrender(user);
        if (currentGame.isGameOver()) {
            try {
                currentGame.gameOver();
                bot.execute(new SendMessage().setChatId(currentGame.getChatId()).setText(currentGame.getGameOverText()));
                currentGame.actualMessage = bot.execute(currentGame.menu.startMenu());
            } catch (TelegramApiException e) {
                App.loggerWarn.error("Remove player error", e);
            }
        } else if (user.equals(currentGame.getCurrentPlayer().getUser())) {
            try {
                currentGame.nextPlayer();
                currentGame.actualMessage = bot.execute(currentGame.menu.turnMenu());
            } catch (TelegramApiException e) {
                App.loggerWarn.error("Remove player error", e);
            }
        }
    }
}
