import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Bot extends TelegramLongPollingBot {

    private final String TEXT_CATEGORY_USER = "Выберете нужную категорию для сортировки по вашим записям за месяц";
    private final String TEXT_CATEGORY = "Выберете нужную категорию";
    private final String TEXT_ENTER = "Выберете категорию для которой вводите расход";
    private boolean income = false;
    private boolean command_enter = false;
    String selectedCategory;
    static Logger LOGGER = LoggerFactory.getLogger(Bot.class);

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            try {
                handleMessage(update.getMessage());
            } catch (TelegramApiException e) {
                LOGGER.error(e.toString());
            }
        } else if (update.hasCallbackQuery()) {
            try {
                handleCallback(update.getCallbackQuery());
            } catch (TelegramApiException | SQLException e) {
                LOGGER.error(e.toString());
            }
        }
    }

    private void handleCallback(CallbackQuery callbackQuery) throws TelegramApiException, SQLException {

        Message message = callbackQuery.getMessage();
        String buttonName = callbackQuery.getData().toLowerCase();
        SQLTable table = new SQLTable();

        switch (callbackQuery.getMessage().getText()) {
            case (TEXT_CATEGORY_USER):
                String user = callbackQuery.getFrom().getFirstName();
                execute(SendMessage.builder().text("Данные для категории " + buttonName
                        + " в формате: № сумма комментарий дата")
                        .chatId(message.getChatId().toString()).build());
                for (String answer :
                        table.showCatFromBase(table.getConnectionBD(), buttonName, message.getDate() * 1000L, user)) {
                    execute(SendMessage.builder().text(answer).chatId(message.getChatId().toString()).build());
                }
                return;
            case (TEXT_CATEGORY):
                execute(SendMessage.builder().text("Данные для категории " + buttonName
                        + " в формате: № сумма комментарий пользователь дата")
                        .chatId(message.getChatId().toString()).build());
                for (String answer :
                        table.showCatFromBase(table.getConnectionBD(), buttonName, message.getDate() * 1000L, null)) {
                    execute(SendMessage.builder().text(answer).chatId(message.getChatId().toString()).build());
                }
                return;
            case (TEXT_ENTER):
                selectedCategory = buttonName;
        }
    }

    private void handleMessage(Message message) throws TelegramApiException {
        SQLTable table = new SQLTable();
        String messageFromBot = message.getText();
        Date dateM = new Date(message.getDate() * 1000L);
        SimpleDateFormat jdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateForBase = jdf.format(dateM);
        LOGGER.debug("dateForBase = " + dateForBase);
        String userName = message.getChat().getFirstName();
        Parsing pars = new Parsing();

        if (message.hasText() & !message.hasEntities() & !command_enter) {

            SendMessage messageOut = new SendMessage(); // Create a SendMessage object with mandatory fields
            messageOut.setChatId(message.getChatId().toString());
            LOGGER.debug("Message:" + message.getText());

            Double sum = 0.0d;
            String category = null;
            String sql;
//-----------------------------------------Ввод дохода
            if (income) {

                Double summ = pars.calculate(pars.detectNumbers(pars.detect(messageFromBot)),
                        pars.detectOperators(pars.detect(messageFromBot)));
                sql = String.format(Locale.ENGLISH,
                        "INSERT INTO finance(income,summ,comments,dates, person) VALUES (5,%.2f,'%s','%s','%s');",
                        summ, messageFromBot, dateForBase, userName);

                if (table.insertInTable(table.getConnectionBD(), sql)) {
                    execute(SendMessage.builder()
                            .text("Ура! Ввели сумму поступления: " + summ)
                            .chatId(message.getChatId().toString()).build());
                } else {
                    execute(SendMessage.builder()
                            .text("Ошибка при попытке ввести сумму поступления! ")
                            .chatId(message.getChatId().toString()).build());
                    LOGGER.error("Ошибка при попытке ввести сумму поступления: ");
                }
                income=false;
                return;
            }
//-----------------------------------Ввод расходов через сообщение боту от пользователя
            if (pars.detectGoodMessage(messageFromBot)) {
                sum = pars.calculate(pars.detectNumbers(pars.detect(messageFromBot)),
                        pars.detectOperators(pars.detect(messageFromBot)));
                category = Category.findCategory(pars.detectWords(messageFromBot));

                sql = String.format(Locale.ENGLISH,
                        "INSERT INTO finance(income,summ,%s,comments,dates, person) VALUES (2,%.2f,%.2f,'%s','%s','%s');",
                        category, sum, sum, messageFromBot, dateForBase, userName);
                if (table.insertInTable(table.getConnectionBD(), sql)) {
                    messageOut.setText("Ура! Запись внесена в таблицу! Категория: " + category + " , сумма: " + sum);

                } else {
                    messageOut.setText("Ошибка при вставке записи в таблицу");
                    LOGGER.error("Ошибка при вставке записи в таблицу. SQL= " + sql);
                }
            } else {
                LOGGER.debug("Введенная фраза не содержит необходимых данных!");
                messageOut.setText("Не могу определить категорию или сумму.");
            }
            try {
                execute(messageOut); // Call method to send the message
            } catch (TelegramApiException e) {
                LOGGER.error("Ошибка при попытке отправить сообщение в бот: " + e.toString());
            }
        }

//-----------------------------------------------Обрабатыем ввод расходов через меню
        if (command_enter) {
            if (!selectedCategory.equals("")) {

                Double summ = pars.calculate(pars.detectNumbers(pars.detect(messageFromBot)),
                        pars.detectOperators(pars.detect(messageFromBot)));

                String sql = String.format(Locale.ENGLISH,
                        "INSERT INTO finance(income,summ,%s,comments,dates, person) VALUES (2,%.2f,%.2f,'%s','%s','%s');",
                        selectedCategory, summ, summ, messageFromBot, dateForBase, userName);

                if (table.insertInTable(table.getConnectionBD(), sql)) {
                    execute(SendMessage.builder()
                            .text("Ура!!! Запись внесена в таблицу! Категория: " + selectedCategory + " , сумма: " + summ)
                            .chatId(message.getChatId().toString()).build());
                } else {
                    execute(SendMessage.builder()
                            .text("Не удалось внести запись в таблицу (")
                            .chatId(message.getChatId().toString()).build());
                }
                command_enter = false;
            }

        }
//--------------------------------- Обрабатываем команды
        if (message.hasEntities()) {
            Optional<MessageEntity> commandEntity = message.getEntities().stream().filter(e -> "bot_command".equals(e.getType())).findFirst();

            if (commandEntity.isPresent()) {
                String command = message.getText().substring(commandEntity.get().getOffset(), commandEntity.get().getLength());

                switch (command) {
                    case "/delete_insert":
                        if (table.deleteFromTableLastInsert(table.getConnectionBD(), userName)) {
                            execute(SendMessage.builder().text("Последняя запись удалена из таблицы!")
                                    .chatId(message.getChatId().toString()).build());
                        } else {
                            execute(SendMessage.builder().text("Не удалось удалить последнюю запись!")
                                    .chatId(message.getChatId().toString()).build());
                        }
                        return;
                    case "/open_category":
                        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
                        for (Category category : Category.values()) {
                            buttons.add(
                                    Arrays.asList(InlineKeyboardButton.builder().text(category.name()).callbackData(String.valueOf(category)).build()));
                        }
                        execute(SendMessage.builder().text(TEXT_CATEGORY)
                                .chatId(message.getChatId().toString())
                                .replyMarkup(InlineKeyboardMarkup.builder().keyboard(buttons).build()).build());
                        return;
                    case "/open_my_category":
                        List<List<InlineKeyboardButton>> button = new ArrayList<>();
                        for (Category category : Category.values()) {
                            button.add(
                                    Arrays.asList(InlineKeyboardButton.builder().text(category.name()).callbackData(String.valueOf(category)).build()));
                        }
                        execute(SendMessage.builder().text(TEXT_CATEGORY_USER)
                                .chatId(message.getChatId().toString())
                                .replyMarkup(InlineKeyboardMarkup.builder().keyboard(button).build()).build());
                        return;
                    case "/open_table":
                        try {
                            execute(SendMessage.builder()
                                    .text("Итоги по столбцам за месяц: " + table.showTableFromBase(table.getConnectionBD(), message.getDate() * 1000L, null))
                                    .chatId(message.getChatId().toString()).build());

                        } catch (Exception e) {
                            execute(SendMessage.builder().text(e.getMessage()).chatId(message.getChatId().toString()).build());
                            LOGGER.error("Ошибка при попытке вывести итоги по столбцам: " + e.toString());
                        }
                        return;
                    case "/open_my_table":
                        execute(SendMessage.builder()
                                .text("Итоги по столбцам за месяц по вашим записям: " + table.showTableFromBase(table.getConnectionBD(), message.getDate() * 1000L, userName))
                                .chatId(message.getChatId().toString()).build());
                        return;
                    case "/insert_income":
                        try {
                            execute(SendMessage.builder()
                                    .text("Введите полученную сумму и комментарий")
                                    .chatId(message.getChatId().toString()).build());
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException ex) {
                                LOGGER.debug("Не получилась задержка!");
                            }
                            income = true;
                        } catch (Exception e) {
                            execute(SendMessage.builder().text(e.getMessage()).chatId(message.getChatId().toString()).build());
                            LOGGER.error("Ошибка при попытке ввести поступление: " + e.toString());
                        }
                        return;
                    case "/see_rest":
                        try {
                            execute(SendMessage.builder()
                                    .text("Остаток за месяц: " + table.seeRest(table.getConnectionBD(), message.getDate() * 1000L, null))
                                    .chatId(message.getChatId().toString()).build());

                        } catch (Exception e) {
                            execute(SendMessage.builder().text(e.getMessage()).chatId(message.getChatId().toString()).build());
                            LOGGER.error("Ошибка при попытке посмотреть остаток: " + e.toString());
                        }
                        return;
                    case "/enter_expense": // ввод расходов через меню
                        List<List<InlineKeyboardButton>> but = new ArrayList<>();
                        int i = 1;
                        InlineKeyboardButton button1 = new InlineKeyboardButton();
                        InlineKeyboardButton button2 = new InlineKeyboardButton();
                        for (Category category : Category.values()) {
                            if (i % 2 == 0) {
                                button2 = InlineKeyboardButton.builder().text(category.name()).callbackData(String.valueOf(category)).build();
                                but.add(Arrays.asList(button1, button2));
                                i++;
                            } else {
                                button1 = InlineKeyboardButton.builder().text(category.name()).callbackData(String.valueOf(category)).build();
                                if (i == Category.values().length) {
                                    but.add(Arrays.asList(button1));
                                }
                                i++;
                            }
                        }
                        execute(SendMessage.builder().text(TEXT_ENTER)
                                .chatId(message.getChatId().toString())
                                .replyMarkup(InlineKeyboardMarkup.builder().keyboard(but).build()).build());
                        command_enter = true;
                        return;
                    case "/unload_table":
                        ToExcel exportTable = new ToExcel();
                        try {
                            if (!exportTable.createFile()) {
                                execute(SendMessage.builder().text("Предыдущую таблицу не удалось удалить! Не могу экспортировать таблицу (")
                                        .chatId(message.getChatId().toString()).build());
                            } else {
                                SendDocument document = new SendDocument();
                                document.setChatId(message.getChatId().toString());
                                document.setCaption("Ваша таблица");
                                document.setDocument(new InputFile(exportTable.saveFile, "temp.xlsx"));
                                execute(document);
                            }
                        } catch (Exception e) {
                            execute(SendMessage.builder().text(e.getMessage()).chatId(message.getChatId().toString()).build());
                            LOGGER.error("Ошибка при попытке выгрузить таблицу в файл: " + e.toString());
                        }
                        return;
                    case "/key_words":
                        execute(SendMessage.builder().text("Список ключевых слов для категорий:").chatId(message.getChatId().toString()).build());
                        int q = 0;
                        for (Category category : Category.values()) {
                            execute(SendMessage.builder().text(Category.keyWords()[q]).chatId(message.getChatId().toString()).build());
                            q++;
                        }
                }
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "FinanceToBot";
    }

    @Override
    public String getBotToken() {
        return "5546030768:AAGF6IqSKSjJ_LkewKUXTanns1AMFUDTyZ0";
    }
}