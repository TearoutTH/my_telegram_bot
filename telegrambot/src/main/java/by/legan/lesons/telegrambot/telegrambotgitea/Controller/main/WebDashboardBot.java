package by.legan.lesons.telegrambot.telegrambotgitea.Controller.main;


import by.legan.lesons.telegrambot.telegrambotgitea.Config.BotConfig;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class WebDashboardBot {

    final
    BotConfig botConfig;

    public WebDashboardBot(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @GetMapping(value = "/")
    public @ResponseBody
    String hello() {
        return "Hello this " + botConfig.getBotUserName();
    }
}
