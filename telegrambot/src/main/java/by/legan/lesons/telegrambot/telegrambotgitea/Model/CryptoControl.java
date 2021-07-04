package by.legan.lesons.telegrambot.telegrambotgitea.Model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class CryptoControl {

    public CryptoControl(String coinName) {
        this.coinName = coinName;
    }

    String coinName;
    String saveCourse;
    String laslCourse = "0";
    double percent;
    String timeNotification;
}
