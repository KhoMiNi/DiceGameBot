package DiceGameBot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

/**
 * Zonk game bot
 */
public class App {
    static final Logger loggerGameInfo = LoggerFactory.getLogger("infoFileLog");
    static final Logger loggerWarn = LoggerFactory.getLogger("warnFileLog");

    public static void main( String[] args )
    {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        Bot bot = new Bot();
        loggerGameInfo.info("Log started");
        loggerWarn.info("Log started");
        try {
            telegramBotsApi.registerBot(bot);
            loggerGameInfo.info("Bot launched");
        } catch (TelegramApiRequestException e) {
            loggerWarn.error("Connection failed", e);
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
