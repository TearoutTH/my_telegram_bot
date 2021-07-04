package by.legan.lesons.telegrambot.telegrambotgitea.Service;

import by.legan.lesons.telegrambot.telegrambotgitea.Model.CryptoControl;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class CryptoScheduler {

    @Autowired
    Bot bot;

    private String lastCourse = "";

    private final static String fallCurseMessage = "Курс упал: ";
    private final static String currentCurseMessage = "Курс изменился ";

    @SneakyThrows
    @Scheduled(cron = "0/3 * * * * ?")
    public void scheduleTaskUsingCronExpression() {
        try {
            for (int i = 0; i < Bot.userList.size(); i++) {
                List<CryptoControl> list = Bot.userCryptoInfo.get(Bot.userList.get(i));
                Bot.builder.chatId(Bot.userList.get(i));
                for (int j = 0; j < list.size(); j++) {
                    String coinName = list.get(j).getCoinName();
                    String courseFromApi = CryptoApiService.getCourseFromApi(coinName);
                    if (!list.get(j).getLaslCourse().equals(courseFromApi)) {
                        list.get(j).setLaslCourse(courseFromApi);
                        Bot.builder.text(currentCurseMessage + ", " + coinName + " : " + courseFromApi);
                        bot.execute(Bot.builder.build());
                    }
                }

            }
        } catch (Exception e) {
            return;
        }
//        String courseFromApi = CryptoApiService.getCourseFromApi("BTC");
//        if (checkPercentForSendMessage(courseFromApi)) {
//            Bot.builder.text(fallCurseMessage + courseFromApi);
//            bot.execute(Bot.builder.build());
//        } else if (!Bot.userList.isEmpty() && !Bot.userCryptoInfo.isEmpty() && Bot.userCryptoInfo.get(Bot.userList.get(0)) != null
//         && !Bot.userCryptoInfo.get(Bot.userList.get(0)).isEmpty()) {
//            if (!lastCourse.equals(courseFromApi)) {
//                lastCourse = courseFromApi;
//                Bot.builder.text(currentCurseMessage + courseFromApi);
//                bot.execute(Bot.builder.build());
//            }
//        }
    }

    private boolean checkPercentForSendMessage(String courseFromApi) {
        try {
            List<CryptoControl> cryptoControlList = Bot.userCryptoInfo.get(Bot.userList.get(0));
            CryptoControl cryptoControl = cryptoControlList.stream().filter(e -> e.getCoinName().equals("BTC")).findFirst().get();
            double savePercent = cryptoControl.getPercent();
            String saveCourse = cryptoControl.getSaveCourse();
            double newPercent = getPercentFromSaveAndNewCourse(courseFromApi, saveCourse);
            return savePercent < newPercent;
        } catch (Exception e) {
            return false;
        }
    }

    private double getPercentFromSaveAndNewCourse(String courseFromApi, String saveCourse) {
        double res = 1 - Double.parseDouble(saveCourse) / Double.parseDouble(courseFromApi);
        return res;
    }

}


