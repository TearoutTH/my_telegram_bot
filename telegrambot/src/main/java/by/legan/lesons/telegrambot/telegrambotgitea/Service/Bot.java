package by.legan.lesons.telegrambot.telegrambotgitea.Service;

import by.legan.lesons.telegrambot.telegrambotgitea.Config.BotConfig;
import by.legan.lesons.telegrambot.telegrambotgitea.Model.CryptoControl;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.stream.Collectors;

import static by.legan.lesons.telegrambot.telegrambotgitea.Service.CryptoApiService.getAllCoinsFromApi;


@Component
@Slf4j
public class Bot extends TelegramLongPollingBot {

    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
    Set<String> allCoins = new HashSet<>();

    final BotConfig config;

    public Bot(BotConfig config) {
        this.config = config;
        if (allCoins.isEmpty()) {
            allCoins.addAll(getAllCoinsFromApi());
        }
    }

    private String coinNameBuffer = "";

    private String messageGroupFlag = "";

    public static SendMessage.SendMessageBuilder builder = SendMessage.builder();

    public static ArrayList<String> userList = new ArrayList<>();
    public static Map<String, List<CryptoControl>> userCryptoInfo = new HashMap<>();

    public void onUpdatesReceivedMessageGroup(Update update) {
        String messageText = update.getMessage().getText();
        String chatId = update.getMessage().getChatId().toString();
        List<CryptoControl> cryptoControl = userCryptoInfo.get(chatId);

        if (messageText.contains("Я передумал!")) {
            builder.text("Хорошо, вы снова в главном меню :)");
            try {
                execute(builder.build());
            } catch (TelegramApiException e) {
                log.debug(e.toString());
            }
            messageGroupFlag = "";
        }

        if (messageGroupFlag.equals("Подписка на криптовалюту")) {
            if (allCoins.contains(messageText)) {
                userList.add(chatId);
                if (cryptoControl == null || cryptoControl.isEmpty()) {
                    userCryptoInfo.put(chatId, new ArrayList<>(Arrays.asList(new CryptoControl(messageText))));
                } else if (cryptoControl.stream().map(e->e.getCoinName().equals(messageText)).findFirst().get()) {
                    builder.text("Вы уже подписаны на эту валюту на постоянной основе :)");
                    try {
                        execute(builder.build());
                    } catch (TelegramApiException e) {
                        log.debug(e.toString());
                    }
                    return;
                }else {
                    userCryptoInfo.get(chatId).add(new CryptoControl(messageText));
                }
                builder.text("Укажите интервал оповещения согласно паттерну: s(секунды) mm(минуты) h(часы) d(дни) m(месяцы).\n" +
                        "Укажите 1 или несколько параметров из паттерна.\n" +
                        "Пример: 3 * * * * (эквивалентно оповещениям каждую 3ю секунду)");
                messageGroupFlag += 2;
                coinNameBuffer = messageText;
            } else {
                builder.text("Извините, но такая криптовалюта не поддерживается.");
            }
            try {
                execute(builder.build());
            } catch (TelegramApiException e) {
                log.debug(e.toString());
            }
        }

        if (messageGroupFlag.equals("Подписка на криптовалюту2")) {
            userCryptoInfo.get(chatId).stream().filter(e -> e.getCoinName().equals(coinNameBuffer))
                    .forEach(e -> e.setTimeNotification("3"));
            messageGroupFlag = "";
            builder.text("Подписка на криптовалюту " + coinNameBuffer +
                    " с интервалом каждые 3 секунды успешно активирована.\n " +
                    "Курс будет отображаться только в случае изменения.");
            try {
                execute(builder.build());
            } catch (TelegramApiException e) {
                log.debug(e.toString());
            }
        }

        if (messageGroupFlag.equals("Подписка на события")) {
            if (allCoins.contains(messageText)) {
                userList.add(chatId);
                if (cryptoControl == null || cryptoControl.isEmpty()) {
                    userCryptoInfo.put(chatId, new ArrayList<>(Arrays.asList(new CryptoControl(messageText))));
                } else {
                    userCryptoInfo.get(chatId).add(new CryptoControl(messageText));
                }
                builder.text("Укажите процент, на который измениться валюта.\n" +
                        "Диапазон: 0,001% - 0,999%\n" +
                        "Варианты значений: -XX (падение), XX (любое изменение), +XX (рост)\n" +
                        "Пример: -0,3 (эквивалентно оповещению о падении курса на 3%)");
                messageGroupFlag += 2;
                coinNameBuffer = messageText;
            } else {
                builder.text("Извините, но такая криптовалюта не поддерживается.");
            }
            try {
                execute(builder.build());
            } catch (TelegramApiException e) {
                log.debug(e.toString());
            }
        }
        if (messageGroupFlag.equals("Подписка на события2")) {
            userCryptoInfo.get(chatId).stream().filter(e -> e.getCoinName().equals(coinNameBuffer))
                    .forEach(e -> e.setTimeNotification("3"));
            messageGroupFlag = "";
            builder.text("Подписка на оповещение об изменении курса криптовалюты " + coinNameBuffer +
                    " на " + 0.3 + ". Успешно активирована.");
            try {
                execute(builder.build());
            } catch (TelegramApiException e) {
                log.debug(e.toString());
            }
        }

        if (messageGroupFlag.equals("Отписаться от конкретных валют")) {
            String[] list = messageText.split(" ");

            List<CryptoControl> newCryptoControls = null;
            if (cryptoControl != null) {
                newCryptoControls = cryptoControl.stream()
                        .filter(e -> Arrays.stream(list).noneMatch(k -> e.getCoinName().equals(k)))
                        .collect(Collectors.toList());
            }

            userCryptoInfo.put(chatId, newCryptoControls);
            messageGroupFlag = "";
            builder.text("Вы успешно отписались от валют: " + messageText);
            try {
                execute(builder.build());
            } catch (TelegramApiException e) {
                log.debug(e.toString());
            }
        }
    }

    @SneakyThrows
    public void onUpdateReceived(Update update) {
//        if (update.getMessage().getChat().getFirstName().equals("Vladislav"))
//            return;
        update.getUpdateId();

        String messageText;
        String chatId;
        // клавиатура
        builder.replyMarkup(replyKeyboardMarkup);

        ArrayList<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardRow keyboardSecondRow = new KeyboardRow();

        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        keyboardFirstRow.add("Охват криптовалют");
        keyboardFirstRow.add("Криптовалюта");
        keyboardFirstRow.add("База знаний");
        keyboardSecondRow.add("Help");
        keyboardSecondRow.add("Обратная связь");
        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        replyKeyboardMarkup.setKeyboard(keyboard);

        if (update.getMessage() != null) {
            chatId = update.getMessage().getChatId().toString();
            builder.chatId(chatId);
            messageText = update.getMessage().getText();
        } else {
            chatId = update.getCallbackQuery().getMessage().getChatId().toString();
            builder.chatId(chatId);
            messageText = " ";
        }
//        } else {
//            chatId = update.getChannelPost().getChatId().toString();
//            builder.chatId(chatId);
//            messageText = update.getChannelPost().getText();
//        }

        if (!messageGroupFlag.equals("")) {
            onUpdatesReceivedMessageGroup(update);
        }

        if (update.hasCallbackQuery()) {
            if (update.getCallbackQuery().getData().equals("/yes")) {
                userCryptoInfo.remove(chatId);
                userList.remove(chatId);
                builder.text("Все подписки были обнулены\n");
            }
            if (update.getCallbackQuery().getData().equals("/no")) {
                builder.text("Хорошо, продолжаю следить :)");
            }
            try {
                execute(builder.build());
            } catch (TelegramApiException e) {
                log.debug(e.toString());
            }
        }

        if (messageText.contains("Охват криптовалют")) {
            builder.text("Криптончик поддерживает работу со всеми актуальными на данный момент криптовалютами.\n " +
                    "Их число сейчас: " + allCoins.size());
            try {
                execute(builder.build());
            } catch (TelegramApiException e) {
                log.debug(e.toString());
            }
        }

        if (messageText.contains("Криптовалюта")) {
            keyboard.clear();
            keyboardFirstRow.clear();
            keyboardSecondRow.clear();
            keyboardFirstRow.add("Мои подписки");
            keyboardFirstRow.add("Подписка на криптовалюту");
            keyboardFirstRow.add("Подписка на события");
            keyboardSecondRow.add("Отписаться от конкретных валют");
            keyboardSecondRow.add("Обнуление подписок");
            keyboardSecondRow.add("Возврат");
            keyboard.add(keyboardFirstRow);
            keyboard.add(keyboardSecondRow);
            replyKeyboardMarkup.setKeyboard(keyboard);

            builder.text("Выберите действие. (PS: Кнопки в помощь)");
            try {
                execute(builder.build());
            } catch (TelegramApiException e) {
                log.debug(e.toString());
            }
        }

        if (messageText.contains("Возврат")) {
            keyboard.clear();
            keyboardFirstRow.clear();
            keyboardSecondRow.clear();
            keyboardFirstRow.add("Охват криптовалют");
            keyboardFirstRow.add("Криптовалюта");
            keyboardFirstRow.add("База знаний");
            keyboardSecondRow.add("Help");
            keyboardSecondRow.add("Обратная связь");
            keyboard.add(keyboardFirstRow);
            keyboard.add(keyboardSecondRow);
            replyKeyboardMarkup.setKeyboard(keyboard);
            builder.text("Вы снова в главном меню :)");
            try {
                execute(builder.build());
            } catch (TelegramApiException e) {
                log.debug(e.toString());
            }
        }
// .replaceAll("\\ds\\s\\dmm\\s\\dh\\s\\dd(\\s\\dm)?", "

        //тут лист инициализируется подписок пользователя
        if (messageText.contains("Мои подписки")) {
            List<CryptoControl> cryptoControlList = userCryptoInfo.get(chatId);
            if (cryptoControlList == null || cryptoControlList.isEmpty()) {
                builder.text("Список ваших подписок пуст.");
                try {
                    execute(builder.build());
                } catch (TelegramApiException e) {
                    log.debug(e.toString());
                }
            } else {
                cryptoControlList.stream().map(CryptoControl::getCoinName).reduce((a, b) -> a + ", " + b)
                        .ifPresent(e -> builder.text("Список ваших подписок: " + e));
                try {
                    execute(builder.build());
                } catch (TelegramApiException e) {
                    log.debug(e.toString());
                }
            }
        }

        if (messageText.contains("Подписка на криптовалюту")) {
            keyboard.clear();
            keyboardFirstRow.clear();
            keyboardSecondRow.clear();
            keyboardFirstRow.add("Я передумал!");
            keyboard.add(keyboardFirstRow);
            replyKeyboardMarkup.setKeyboard(keyboard);
            builder.text("Укажите интересующую вас криптовалюту.");
            messageGroupFlag = messageText;
            try {
                execute(builder.build());
            } catch (TelegramApiException e) {
                log.debug(e.toString());
            }
        }

        if (messageText.contains("Подписка на события")) {
            keyboard.clear();
            keyboardFirstRow.clear();
            keyboardSecondRow.clear();
            keyboardFirstRow.add("Я передумал!");
            keyboard.add(keyboardFirstRow);
            replyKeyboardMarkup.setKeyboard(keyboard);
            builder.text("Укажите интересующую вас криптовалюту.");
            messageGroupFlag = messageText;
            try {
                execute(builder.build());
            } catch (TelegramApiException e) {
                log.debug(e.toString());
            }
        }

        if (messageText.contains("Отписаться от конкретных валют")) {
            List<CryptoControl> cryptoControlList = userCryptoInfo.get(chatId);
            if (cryptoControlList == null) {
                builder.text("Список ваших подписок пуст.");
                try {
                    execute(builder.build());
                } catch (TelegramApiException e) {
                    log.debug(e.toString());
                }
            } else {
                keyboard.clear();
                keyboardFirstRow.clear();
                keyboardSecondRow.clear();
                keyboardFirstRow.add("Я передумал!");
                keyboard.add(keyboardFirstRow);
                replyKeyboardMarkup.setKeyboard(keyboard);
                //String list = cryptoControlList.stream().map(e->e.getCoinName()).reduce((a,b)->a + " " + b).get();
                builder.text("Укажите 1 или несколько криптовалют через пробел, от которых Вы хотите отписаться.");
                messageGroupFlag = messageText;
                try {
                    execute(builder.build());
                } catch (TelegramApiException e) {
                    log.debug(e.toString());
                }
            }
        }

        if (messageText.contains("Обнуление подписок")) {
            builder.text("Внимание! Вы действительно хотите удалить все, имеющиеся у Вас подписки?\n");
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            InlineKeyboardButton buttonYes = new InlineKeyboardButton();
            buttonYes.setText("Да");
            buttonYes.setCallbackData("/yes");
            InlineKeyboardButton buttonNo = new InlineKeyboardButton();
            buttonNo.setText("Нет");
            buttonNo.setCallbackData("/no");
            rowInline.add(buttonYes);
            rowInline.add(buttonNo);
            // Set the keyboard to the markup
            rowsInline.add(rowInline);
            // Add it to the message
            inlineKeyboardMarkup.setKeyboard(rowsInline);
            builder.replyMarkup(inlineKeyboardMarkup);

//            userCryptoInfo.clear();
//            userList.clear();
//            try {
//                execute(builder.build());
//            } catch (TelegramApiException e) {
//                log.debug(e.toString());
//            }
//            builder.text("Все подписки были обнулены\n");
            try {
                execute(builder.build());
            } catch (TelegramApiException e) {
                log.debug(e.toString());
            }
        }

        if (messageText.contains("Help")) {
            builder.text("Список команд:\n" +
                    "/start - если хотите со мной ещё раз поздароваться :)\n" +
                    "Все основные взаимодействия выполняются с помощью кнопок.\n" +
                    "Если что-то всё равно будет не понятно, спросите у моего разработчика.\n " +
                    "PS: надеюсь она сама ещё помнит что тут и как :D ");
            try {
                execute(builder.build());
            } catch (TelegramApiException e) {
                log.debug(e.toString());
            }
        }

        if (messageText.contains("Обратная связь")) {
            builder.text("Мы знаем, что вам всё понравилось, ну по крайней мере надеемся ^^");
            try {
                execute(builder.build());
            } catch (TelegramApiException e) {
                log.debug(e.toString());
            }
        }

        if (messageText.contains("База знаний")) {
            builder.text("Тут можно почитать про криптовалюту:\n" +
                    "https://www.rbc.ru/crypto/news/5f95b6d79a7947d04d2375e0");
            try {
                execute(builder.build());
            } catch (TelegramApiException e) {
                log.debug(e.toString());
            }
        }

        if (messageText.contains("/start")) {
            String name = update.getMessage().getChat().getFirstName();
            builder.text("Приветствую, " + name + ", я Криптончик - бот, помогающий следить за криптовалютой, рад знакомству :)");
            try {
                execute(builder.build());
            } catch (TelegramApiException e) {
                log.debug(e.toString());
            }
        }
    }

    public String getBotUsername() {
        return config.getBotUserName();
    }

    public String getBotToken() {
        return config.getToken();
    }
}
