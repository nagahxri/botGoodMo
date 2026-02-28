package org.example.wakeupbot;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import io.github.cdimascio.dotenv.Dotenv;



@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {
    Dotenv dotenv = Dotenv.load();
    private static TelegramClient telegramClient;

    //real tg token is not allowed to give here in the project (security risk)
    public UpdateConsumer() {
        this.telegramClient = new OkHttpTelegramClient(dotenv.get("TELEGRAM_TOKEN"));
        //added .env here
        //registerBotCommands();
    }

    public class LocationResponse {
        List<Location> locations;
        public void setLocations(List<Location> locations) {
            this.locations = locations;
        }
        public List<Location> getLocations() {
            return locations;
        }

        static class Location {
            String id;
            String name;

            public void setId(String id) {
                this.id = id;
            }
            public String getId() {
                return id;
            }
            public void setName(String name) {
                this.name = name;
            }
            public String getName() {
                return name;
            }
        }
    }
    public class DepartureResponse {
        List<Departure> departures;

        public void setDepartures(List<Departure> departures) {
            this.departures = departures;
        }
        public List<Departure> getDepartures() {
            return departures;
        }

        static class Departure {
            String when;
            int delay;
            Line line;
            String direction;

            public void setDirection(String direction) {
                this.direction = direction;
            }
            public String getDirection() {
                return direction;
            }
            public void setLine(Line line) {
                this.line = line;
            }
            public Line getLine() {
                return line;
            }
            public void setWhen(String when) {
                this.when = when;
            }
            public String getWhen() {
                return when;
            }
            public void setDelay(int delay) {
                this.delay = delay;
            }
            public int getDelay() {
                return delay;
            }
        }

        static class Line {
            String name;
        }
    }

public void ostanovka(Update update) throws URISyntaxException, IOException, InterruptedException {
    HttpClient client = HttpClient.newHttpClient();
     Gson gson = new Gson();

sendMessage(update.getMessage().getChatId(), "Hello, u sent " + update.getMessage().getText());

        String stopName = "Hauptmann-Hermann-Platz";


        // GET DEPARTURES
        String departuresUrl = dotenv.get("stop_api");
        //added .env here
        HttpRequest depRequest = HttpRequest.newBuilder()
                .uri(URI.create(departuresUrl))
                .GET()
                .build();

        HttpResponse<String> depResponse =
                client.send(depRequest, HttpResponse.BodyHandlers.ofString());
    //System.out.println(depResponse.body());
        DepartureResponse departures =
                gson.fromJson(depResponse.body(), DepartureResponse.class);


        OffsetDateTime now = OffsetDateTime.now();

        for (DepartureResponse.Departure d : departures.departures) {

            OffsetDateTime departureTime = OffsetDateTime.parse(d.when);
            long minutes = Duration.between(now, departureTime).toMinutes();

            if (minutes < 0) continue;

            System.out.println(
                    d.line.name + " → " + d.direction + " — " + minutes + " min"
            );
//            switch (d.line.name) {
//                case "Bus 4": sendMessage(update.getMessage().getChatId(), "next departure is " + d.line.name + " in " + minutes + " minutes" + " with delay of " + d.delay + " minutes");
//                case "Bus 6": sendMessage(update.getMessage().getChatId(), "next departure is " + d.line.name + " in " + minutes + " minutes" + " with delay of " + d.delay + " minutes");
//                default: sendMessage(update.getMessage().getChatId(), "next hour there is no buses to the university");
//            }

            if (d.line.name.equals("Bus 4") || d.line.name.equals("Bus 6")){
                sendMessage(update.getMessage().getChatId(), "next departure is " + d.line.name + " in " + minutes + " minutes" + " with delay of " + d.delay + " minutes");
            } else {
                sendMessage(update.getMessage().getChatId(), "next hour there is no buses to the university");
            }
            //sendMessage(update.getMessage().getChatId(), "next departure is " + d.line.name + " in " + minutes + " minutes");

        }
}

    public void weather(Update update) throws URISyntaxException, IOException, InterruptedException {
        Long chatId = update.getMessage().getChatId();
        String url = dotenv.get("weather_api");
        //add .env here
        //Gson gson = new Gson();
        //String jsonReq = gson.toJson(city);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .GET()
                .header("Accept", "application/json")
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());
        //response = (HttpResponse<String>) gson.fromJson(response.body(), City.class);
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(response.body(), JsonObject.class);

        double tempC = jsonObject
                .getAsJsonObject("current")
                .get("temp_c")
                .getAsDouble();
        String timeLoc = jsonObject
                .getAsJsonObject("location")
                .get("localtime")
                .getAsString();
        double windKmh = jsonObject
                .getAsJsonObject("current")
                .get("wind_kph").
                getAsDouble();
        sendMessage(chatId, "Temperature in Klagenfurt is: " + tempC +
                "; Wind speed is: " + windKmh + "; At local time: " + timeLoc);
    }

    @SneakyThrows
    @Override
    public void consume (Update update){
        if (update.hasMessage()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            messageText = messageText.toLowerCase();
            messageText = messageText.strip();
            System.out.println(messageText + " " + chatId);
//            if(messageText.equals("ostanovka")){
//                ostanovka(update);
//            }
//            if(messageText.equals("weather")){
//                weather(update);
//            } else {
//                //sendMessage(chatId, "Hello, u sent " + messageText);
//            }
            if(messageText.contains("good morning")){
                sendMessage(chatId, "Good morning, roman! ");
                weather(update);
                ostanovka(update);
            }
        }
    }
    @SneakyThrows
    public static void sendMessage(
            Long chatId,
            String messageText
    ) {
        SendMessage message = SendMessage.builder().text(messageText).chatId(chatId).build();
        telegramClient.execute(message);
    }



}
