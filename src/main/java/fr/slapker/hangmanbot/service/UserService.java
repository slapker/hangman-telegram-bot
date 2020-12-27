package fr.slapker.hangmanbot.service;

import fr.slapker.hangmanbot.bo.UserBO;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Service
@Slf4j
public class UserService {
    private List<UserBO> userList=new ArrayList<>();

    public UserBO getUser(User telegramUser) {
        UserBO myUser = userList.stream().filter(userBO -> userBO.getTelegramId().equals(telegramUser.getId())).findFirst().orElse(null);
        if (myUser == null) {
            myUser =new UserBO(telegramUser);
            userList.add(myUser);
        }
        return myUser;
    }

    public UserBO getUserByTelegramId(Integer telegramId) {
        return userList.stream().filter(userBO -> userBO.getTelegramId().equals(telegramId)).findFirst().orElse(null);
    }

    public int getNbUsers() {
        return userList.size();
    }
}
