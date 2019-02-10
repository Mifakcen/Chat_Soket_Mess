package Client;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

import static java.lang.Thread.sleep;

public class ClientConnection implements Runnable {
    private static Socket socket;
    private static ObjectInputStream in;
    private static ObjectOutputStream out;

    private static String nick;
    private static String login;
    private static String pass;
    private Boolean register;//Флаг показывает нужна ли регистрация

    private ChatController chatController;
    Scanner scanner = new Scanner(System.in);

    public ClientConnection(ChatController chatController,Boolean register) {
        System.out.println("Введите логин / пароль");
        ClientConnection.login = scanner.nextLine();
        ClientConnection.pass=scanner.nextLine();
        this.register=register;
        this.chatController = chatController;
    }
    @Override
    public void run() {
        try {
            socket = new Socket("localhost",8080);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            if(this.register){
                this.register();
            } else {
                this.login();
            }
            //Принятие сообщений с сервера
            Thread thread;
            while (socket.isConnected()){
                Message msg = (Message) in.readObject();//Сообщение с сервера
//                System.out.println(msg);
                if(nick != null){
                    if(msg.getMessText().startsWith("/")){
                        if(msg.getMessText().equalsIgnoreCase("/end")){
                            nick=null;
                            socket=null;
                            break;
                            //this.chatController.showLogin();
                        } else if(msg.getMessText().equalsIgnoreCase("/delete")){
                           // this.chatController.showLogin();
                        } else if (msg.getMessText().startsWith("/user_list ")){
                            String[] users = msg.getMessText().split(" ");
                          this.chatController.updateUserList(Arrays.copyOfRange(users,1,users.length));
                        }
                    }else {
                        if(!msg.getMessText().isEmpty()){
                            this.chatController.addMassege(msg.getMessText());
                        }
                    }
                }else{
                    if(msg.getMessText().startsWith("/auth_ok")){
                        String[] elements = msg.getMessText().split(" ");
                        nick = elements[1];
                        this.chatController.setNickLabel(nick);
                        thread=new Thread(new mess());
                        thread.start();
                    } else if(msg.getMessText().startsWith("/auth_fail ")){
                        String response = msg.getMessText().substring(11);
                        System.out.println(response);
                         break;
                    } else if(msg.getMessText().startsWith("/register_ok ")){
                        String[] elements = msg.getMessText().split(" ");
                        nick = elements[1];

                        //this.chatController.setNickLabel(nick);
                       thread = new Thread(new mess());
                       thread.start();
                    }else if(msg.getMessText().startsWith("/register_fail")){
                        String response = msg.getMessText().substring(11);
                        System.out.println(response);
                        break;
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    //Отправка сообщений на сервер
    public static void sendMessage(Message mess){
        try {
            out.writeObject(mess);
            out.flush();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    //Отправляем команду для регистрации
    private void register(){
        sendMessage(new Message("/register " + ClientConnection.login + " " + ClientConnection.pass));
    }
    //Отправляем команду для входа
    private void login(){
        sendMessage(new Message("/auth " + ClientConnection.login + " " + ClientConnection.pass));
    }

//    public  static  void logout(){
//        nick = null;
//
//        sendMessage(new Message("/delete"));
//    }
//
//    public static void delete(){
//        nick = null;
//        sendMessage(new Message("/delete"));
//    }
}
//Поток для обмена сообщениями с сервером
class mess implements Runnable {
    Scanner scanner = new Scanner(System.in);
    @Override
    public void run() {
        while (true) {
            try {
                sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Введите сообщение\n");
            Message message = new Message(scanner.nextLine());
            ClientConnection.sendMessage(message);
            if(message.getMessText().startsWith("/end")){
                break;
            }
        }
    }
}
