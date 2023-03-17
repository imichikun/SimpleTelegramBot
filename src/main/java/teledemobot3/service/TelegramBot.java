package teledemobot3.service;

import com.vdurmont.emoji.EmojiParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import teledemobot3.config.BotConfig;
import teledemobot3.model.User;
import teledemobot3.model.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final BotConfig botConfig;
    private final UserRepository userRepository;

    @Autowired
    public TelegramBot(BotConfig botConfig, UserRepository userRepository) {
        this.botConfig = botConfig;
        this.userRepository = userRepository;
        initializeBotMenu();
    }

    private static final String ABOUT = """
            This bot is developed for educational purposes.
            Try opting for other  actions to get more result.
            """;

    @Override
    public void onRegister() {
        super.onRegister();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public String getBotUsername() {
        return botConfig.getUsername();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()){
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.contains("/send ") && chatId==botConfig.getOwner()){
                String messageToAll = messageText.substring(messageText.indexOf(" "));
                userRepository.findAll().forEach(user -> sendPlainMessage(user.getChatId(),messageToAll));
            } else {

                switch (messageText) {
                    case "/start" -> startCommand(update.getMessage());
                    case "/about" -> sendPlainMessage(chatId, ABOUT);
                    case "/register" -> beforeRegister(update.getMessage());
                    case "/mydata" -> showUserData(chatId);
                    case "/deletedata" -> deleteUserData(chatId);
//                    case "/send" -> ;
                    default -> sendPlainMessage(chatId, "Sorry, this command is not recognized");
                }
            }
        } else if (update.hasCallbackQuery()){          // checking if user has pushed any buttons that were sent to him
            String callbackQuery = update.getCallbackQuery().getData();     // getting id of button (like "YES BUTTON")
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
                                                        // if you get messageId you could change message text at once
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            EditMessageText editMessageText = new EditMessageText();
            editMessageText.setChatId(String.valueOf(chatId));
            editMessageText.setMessageId((int) messageId);

            if (callbackQuery.equals("YES_BUTTON")) {
                if (userIsExist(chatId))
                    editMessageText.setText("You are already registered");
                else
                    editMessageText.setText("You are successfully registered");

                registerUser(update.getCallbackQuery().getMessage());
            } else if (callbackQuery.equals("NO_BUTTON")) {
                editMessageText.setText("You have refused to register");
            }

            try {
                execute(editMessageText);
            } catch (TelegramApiException e) {
                System.out.println("error in executing EditMessageText");
            }
        }
    }

    private void initializeBotMenu(){
        List<BotCommand> botCommands = new ArrayList<>(){{
            add(new BotCommand("/start", "main menu"));
            add(new BotCommand("/about", "information about bot"));
            add(new BotCommand("/register", "register your credentials"));
            add(new BotCommand("/mydata", "show your data"));
            add(new BotCommand("/deletedata", "delete all registered data"));
        }};

        try {
            execute(new SetMyCommands(botCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            System.out.println("Error in adding BotCommand");
        }
    }

    private void beforeRegister(Message message) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();   // creating basis for buttons to register
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();    // creating basis for row of buttons
        List<InlineKeyboardButton> row1 = new ArrayList<>();              // creating List of buttons for the first row

        InlineKeyboardButton buttonYES = new InlineKeyboardButton("YES");
        buttonYES.setCallbackData("YES_BUTTON");
        InlineKeyboardButton buttonNO = new InlineKeyboardButton("NO");
        buttonNO.setCallbackData("NO_BUTTON");

        row1.add(buttonYES);
        row1.add(buttonNO);
        rowsInLine.add(row1);                   // adding first row(comprises 'YES' and 'NO' buttons) into rowsInLine
        keyboardMarkup.setKeyboard(rowsInLine);

        SendMessage beforeRegister = preparedSendMessage(message.getChatId(), "Do you agree to register ?");
        beforeRegister.setReplyMarkup(keyboardMarkup);
        executeSendMessage(beforeRegister);
    }

    private void registerUser(Message message) {
        if (userIsExist(message.getChatId()))
            return;

        Chat chat = message.getChat();  // it's necessary to get all details about User in order to save (register) him
        User newUser = new User(message.getChatId(), chat.getFirstName(), chat.getLastName(), chat.getUserName(),
                LocalDateTime.now());
        userRepository.save(newUser);
    }

    private boolean userIsExist(long chatId){
        Optional<User> possibleUser = userRepository.findById(chatId);
        return possibleUser.isPresent();
    }

    private void showUserData(long chatId) {
        if(userIsExist(chatId) == false) {
            sendPlainMessage(chatId, "No data is found");
            return;
        }
        String userData = userRepository.findById(chatId).get().toString();
        sendPlainMessage(chatId, userData);
    }

    private void deleteUserData(long chatId) {
        if(userIsExist(chatId)) {
            userRepository.delete(userRepository.findById(chatId).get());
            return;
        }

        sendPlainMessage(chatId, "No data is found");
    }

    private void sendPlainMessage(long chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(message);                               // outgoing chatId should be String (dunno why :/)

        executeSendMessage(sendMessage);
    }

    private SendMessage preparedSendMessage(long chatId, String text){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(text);
        return sendMessage;
    }

    private void executeSendMessage(SendMessage sendMessage){
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println("Error in executing sending message");
        }
    }

    private void startCommand(Message message) {
        String helloToUser = EmojiParser.parseToUnicode("Hello, " + message.getChat().getFirstName() +
                ", welcome aboard " + ":blush:");       // emoji-java library for activating emoji in project
//        String helloToUser = "Hello, " + message.getChat().getFirstName() + ", welcome aboard !";     // plain hello
        sendPlainMessage(message.getChatId(), helloToUser);
    }
}