package Server;

import Client.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

public class Server {
    private final int PORT = 8080; //Нужно задавать из файла

    private Vector<ClientHandler> clients;
   // private ServerSocket server;
    private IAuthService authService;

    public Server(){
        clients = new Vector<>();
        Socket socket = null;
        try (ServerSocket server = new ServerSocket(PORT)){
            authService = new BaseAuthService();
            authService.start();
            System.out.println("Сервер запущен");
            /*dtf = DateTimeFormatter.ofPattern("dd-L-yyyy : HH:mm:ss ");
            now = LocalDateTime.now();*/
//            System.out.println(dtf.format(now)+"\n"); \
            Date date = new Date();

//            System.out.println(date);
            while (true){
               // System.out.println(authService.getUserList().toString());
                System.out.println("Сервер ожидает подключения");
                socket = server.accept();
                new ClientHandler(socket,this);
                System.out.println("Клиент подключился");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Не удалось запустить сервер");
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        authService.stop();
    }

    //сообщение всем пользователям
    public synchronized void broadcast(Message msg) {
        for (ClientHandler c: clients) {
            c.sendMessage(msg);
        }
    }
    //сообщение одному или нескольким пользователям
    public synchronized void broadcast(Message msg,String...nicks){
        int countCurrent = 0;
        int countALL = nicks.length;

        for (ClientHandler c: clients){
            for (String nick : nicks){
                if(c.getName().equals(nick)){
                    c.sendMessage(msg);
                    if(countCurrent == countALL){
                        return;
                    }
                }
            }
        }
    }
    //Проверяемм есть ли человек с введенным ником в базе
    public synchronized boolean isNickBusy(String nick){
        for (ClientHandler c:clients){
            if (c.getName().equals(nick)){
                return true;
            }
        }
        return false;
    }
    //Добаляем пользователя в список
    public synchronized void subscribe(ClientHandler client){
        clients.add(client);
    }
    //Удаляем пользователя из списка
    public synchronized void ubsubscrine(ClientHandler client){
        clients.remove(client);
    }

    public IAuthService getAuthService(){
        return this.authService;
    }

    //Отправка списка всех сообщений за текущую дату
    public void broadcastMessList(){
        Message sb = new Message(("/user_list"));
        ArrayList<String> Mess = this.getAuthService().getTextGlobChat();
        for (String m : Mess) {
            sb.setMessText(sb.getMessText() + m + "\n");
        }
        for (ClientHandler client: clients){
            client.sendMessage(sb);
        }
    }
    private Message message(){
        Message sb = new Message(("/user_list"));
        ArrayList<String> logins = this.getAuthService().getUserList();
        for (ClientHandler client: clients){
            sb.setMessText(sb.getMessText() +  " " + client.getName()+":on");
            logins.remove(client.getName());
        }
        for (String login: logins){
            sb.setMessText(sb.getMessText() +" " + login + ":off");
        }
        sb.setMessText(sb.getMessText() + "\n");
        return sb;

    }//создает список пользователей сервера
    //Отправить список всех пользователей
    public void broadcastUserList(){
        Message sb  = message();
        for (ClientHandler client: clients){
            client.sendMessage(sb);
        }
    }
    //отправляем список пользователй только тому кто запросил команду
    public void UserList(ClientHandler clientHandler){
        Message sb = message();
        clientHandler.sendMessage(sb);

    }
}
