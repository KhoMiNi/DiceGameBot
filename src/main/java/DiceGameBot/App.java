package DiceGameBot;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

/**
 * Zonk game bot
 */
public class App {
    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        Bot bot = new Bot();
        try {
            telegramBotsApi.registerBot(bot);
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }

        MessageProcess messageQueue = new MessageProcess(bot);
        CallbackQueryProcess callbackQueue = new CallbackQueryProcess(bot);

        Thread messageReceiver = new Thread(messageQueue);
        messageReceiver.setDaemon(true);
        messageReceiver.setName("MessageProcessing");
        messageReceiver.start();

        Thread callbackReceiver = new Thread(callbackQueue);
        callbackReceiver.setDaemon(true);
        callbackReceiver.setName("CallbackProcessing");
        callbackReceiver.start();


    }
}