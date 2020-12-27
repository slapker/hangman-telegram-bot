package fr.slapker.hangmanbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TelegramBotConfig {

    @Value("${bot.name}")
    String botName;

    @Value("${bot.token}")
    String botToken;

    @Value("${admin.telegram.id}")
    String adminTelegramId;


    @Bean("BotConfigBean")
    public BotConfigBean botConfigBean() {
        BotConfigBean BotConfigBean = new BotConfigBean();
        BotConfigBean.setBotName(botName);
        BotConfigBean.setBotToken(botToken);
        BotConfigBean.setAdminId(adminTelegramId);
        return BotConfigBean;
    }

}
