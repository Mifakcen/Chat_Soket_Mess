package Server;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

public class BaseAuthService implements IAuthService {
    private static final String DATABASE_NAME = "chat.db";

    private Connection connection;

    private void connect(String filename) throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
//        String url = "jdbc:sqlite:C:/Users/Mifa/Documents/java_socket_chat/" + filename;
        String url = "jdbc:sqlite:C:/Users/Mifa/Documents/Chat_Message/" + filename;
        this.connection = DriverManager.getConnection(url);
        //Если таблицы нет то создаем эту таблицу
        if(this.connection!=null){
            //Таблица которая будет хранить пользователей
            this.connection.createStatement().execute("CREATE TABLE IF NOT EXISTS users ("
            +"id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE  NOT NULL,"
            +"login VARCHAR(255) UNIQUE,"
                    + "pass  TEXT)");
            //Таблица которая будет хранить сообщения Общего чата
            this.connection.createStatement().execute("CREATE TABLE IF NOT exists GeneralChatMessages ("
                    +"id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE  NOT NULL,"
                    +"MessageTime TEXT,"
                    +"MessageDate TEXT,"
                    +"NicUser VARCHAR(255),"
                        +"TextMess TEXT)");
        }
    }

    private void disconnect(){
        try {
            this.connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void start() {
        try {
            this.connect(DATABASE_NAME);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        disconnect();
    }

    //Возвращает список пользователей
    //Массив Стринг
    @Override
    public ArrayList<String> getUserList() {
        ArrayList<String> users = new ArrayList<>();
        try {
            ResultSet rs = connection.createStatement().executeQuery("SELECT  login FROM users");
            while (rs.next()){
                users.add(rs.getString("login"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public String addLoginPass(String login, String pass) {
        int count = 0;
        try{
            PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM users WHERE login = ? LIMIT 1");
            ps.setString(1,login);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                count = rs.getInt(1);
            }
            if(count == 0){
                ps = connection.prepareStatement("INSERT INTO users (login, pass) VALUES (?, ?)");
                ps.setString(1,login);
//                ps.setString(2,this.stringToMd5(pass));
                ps.setString(2,pass);
                ps.execute();
                return login;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Выборка сообщений за текущую дату
    @Override
    public ArrayList<String> getTextGlobChat() {
        ArrayList<String> MessText = new ArrayList<>();
        LocalDateTime dateTime = LocalDateTime.now();
        String date = dateTime.format( DateTimeFormatter.ofPattern("dd-L-yyyy"));
        try {
            ResultSet rs = connection.createStatement().executeQuery("SELECT TextMess FROM GeneralChatMessages WHERE MessageDate = '"+date+"'");
            while (rs.next()){
                MessText.add(rs.getString("TextMess"));
                //MessText.add(rs.getString("NucUser"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return MessText;
    }

    private String stringToMd5(String string) {
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(string.getBytes());
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }

    @Override
    public void deleteByLogin(String login) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE  FROM users WHERE  login = ?");
            ps.setString(1,login);
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getNicByLoginPass(String login, String pass) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT login FROM users WHERE login = ? AND pass = ? LIMIT 1");
            ps.setString(1,login);
//            ps.setString(2,this.stringToMd5(pass));
            ps.setString(2,pass);
            ResultSet rs = ps.executeQuery();

            while (rs.next()){
                return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    //Добавление сообщений глабального чата в базу
    @Override
    public void addMessGlobChat(String mess,String nick) {
        if (!mess.startsWith("/")) {
            Date date = new Date();
        /*DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-L-yyyy : HH:mm:ss ");
           LocalDateTime now = LocalDateTime.now();*/
            LocalDateTime dateTime = LocalDateTime.now();
            try {
                PreparedStatement ps = connection.prepareStatement("INSERT INTO GeneralChatMessages (MessageTime,MessageDate, NicUser, TextMess) VALUES (?, ?, ?, ?)");
                ps.setString(2, (dateTime.format(DateTimeFormatter.ofPattern("dd-L-yyyy"))));
                ps.setString(1, (dateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
                ps.setString(3, nick);
                ps.setString(4, mess);
                ps.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
