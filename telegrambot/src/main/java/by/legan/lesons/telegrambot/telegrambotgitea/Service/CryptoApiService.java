package by.legan.lesons.telegrambot.telegrambotgitea.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CryptoApiService {

    @SneakyThrows
    public static Set<String> getAllCoinsFromApi() {
        URL url = new URL("https://web-api.coinmarketcap.com/v1/cryptocurrency/map");
        URLConnection request = url.openConnection();
        request.connect();

        JsonParser jp = new JsonParser();
        JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
        JsonObject rootobj = root.getAsJsonObject();
        JsonArray data = rootobj.get("data").getAsJsonArray();
        Set<String> allCoins = StreamSupport.stream(data.spliterator(), false)
                .map(e->e.getAsJsonObject().get("symbol").getAsString())
                .collect(Collectors.toSet());
        return allCoins;
    }

    @SneakyThrows
    public static String getCourseFromApi(String crypt) {
        URL url = new URL("https://api.bittrex.com/api/v1.1/public/getticker?market=USD-" + crypt);
        URLConnection request = url.openConnection();
        request.connect();

        JsonParser jp = new JsonParser();
        JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
        JsonObject rootobj = root.getAsJsonObject();
        JsonObject result = rootobj.get("result").getAsJsonObject();
        String course = result.get("Last").getAsString();
        return course;
    }
}
