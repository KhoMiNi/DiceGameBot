package DiceGameBot;

import DiceGameBot.configuration.Constants;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Bot extends TelegramLongPollingBot {
    Map<Long, Game> games = new HashMap<>();
    final Queue<Message> processQueue = new ConcurrentLinkedQueue<>();
    final Queue<CallbackQuery> processCallbackQueue = new ConcurrentLinkedQueue<>();

    @Override
    public String getBotUsername() {
        return Constants.BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return Constants.BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            processQueue.add(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            processCallbackQueue.add(update.getCallbackQuery());
        }
    }
}
