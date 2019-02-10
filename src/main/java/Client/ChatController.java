package Client;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class ChatController {

    public String Nick;

    public void updateUserList(String[] users) {
        for (int i = 0;i<users.length;i++){
            System.out.print(users[i] + " ");
        }
    }

    public void addMassege(String msg) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println(dtf.format(now)+ " : " + msg + "\n");
    }

    public void setNickLabel(String nick) {
        System.out.println("Nick -> " + nick);
        this.Nick = nick;

    }
}
