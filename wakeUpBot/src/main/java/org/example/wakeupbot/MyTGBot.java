package org.example.wakeupbot;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import io.github.cdimascio.dotenv.Dotenv;
@Component
public class MyTGBot implements SpringLongPollingBot {
    private final UpdateConsumer updateConsumer;
    public MyTGBot(UpdateConsumer updateConsumer) {
        this.updateConsumer = updateConsumer;
    }
    @Override
    public String getBotToken() {
        return Dotenv.load().get("TELEGRAM_TOKEN");
        //add .env here
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return updateConsumer;
    }

}
