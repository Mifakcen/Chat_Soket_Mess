package Server;

import java.util.ArrayList;

public interface IAuthService {
    void start();
    void stop();
    ArrayList<String> getUserList();
    String addLoginPass(String login,String pass);
    void deleteByLogin(String login);
    String getNicByLoginPass(String login,String pass);
    void addMessGlobChat(String mess,String nick);
    ArrayList<String> getTextGlobChat();

}
