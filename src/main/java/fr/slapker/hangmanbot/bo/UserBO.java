package fr.slapker.hangmanbot.bo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.User;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserBO {

    public UserBO(User telegramUser) {
        telegramId=telegramUser.getId();
        firstName=telegramUser.getFirstName();
        username=telegramUser.getUserName();
    }
    private Integer idUser;
    private String firstName;
    private Integer telegramId;
    private String username;
    private boolean superUser;

    public String getVisibleName() {
        if (username != null && !username.equals("")) {
            return username;
        }
        return firstName;
    }
}
