package Server;

import Client.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler {
   // private Scanner scanner = new Scanner(System.in);
  //  private Socket socket;
    private Server server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String name;

    public ClientHandler(Socket socket, final Server server) {
        try {
           // this.socket = socket;
            this.server = server;
            this.in = new ObjectInputStream(socket.getInputStream());
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.name = "";
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(()-> {
            try {
                while (true) {
                    //цикл регистрации
                    while (true) {
                        Message str =(Message) in.readObject();
                        System.out.println("<-Клиент: " + str);
                        if (str.getMessText().startsWith("/auth ")) {
                            String[] elements = str.getMessText().split(" ");
                            if (elements.length == 3) {
                                String nick = server.getAuthService().getNicByLoginPass(elements[1], elements[2]);
                                if (nick != null) {
                                    if (!server.isNickBusy(nick)) {
                                        sendMessage(new Message("/auth_ok " + nick));
                                        this.name = nick;
                                        setAuthorized(true);
                                        break;
                                    } else {
                                        sendMessage(new Message("/auth_fail Учетная запись уже используется"));
                                    }
                                } else {
                                    sendMessage(new Message("/auth_fail Неверный логин/пароль"));
                                }
                            } else {
                                sendMessage(new Message("/auth_fail Неверное количество параметров"));
                            }
                        } else if (str.getMessText().startsWith("/register ")) {
                            String[] elements = str.getMessText().split(" ");
                            if (elements.length == 3) {
                                String nick = server.getAuthService().addLoginPass(elements[1], elements[2]);

                                if (nick != null) {
                                    sendMessage(new Message("/register_ok " + nick));
                                    this.name = nick;
                                    setAuthorized(true);
                                    break;
                                } else {
                                    sendMessage(new Message("/register_fail Этот логин уже занят"));
                                }
                            } else {
                                sendMessage(new Message("/register_fail Неверное количество параметров"));
                            }
                        } else {
                            sendMessage(new Message("/auth_fail Для начала нужна авторизация"));
                        }
                    }
                    //цикл для приема сообщений с клиента
                    while (true) {
                        Message str = (Message) in.readObject();
                        System.out.println("<-Клиент " + name + " : " + str);
                        server.getAuthService().addMessGlobChat(str.getMessText(),name);
                        if(str.getMessText().startsWith("/user_list")){
                            server.UserList(this);
                            //server.broadcastMessList();
                            continue;
                        }
                        if (str.getMessText().startsWith("/commands" )){
                            sendMessage(new Message("Commands : \n" +
                                    "/user_list"+
                                    "/end \n"+
                                    "/delete\n"+
                                    "/w 'nic' 'mess' \n"));
                            continue;
                        }
                        if (str.getMessText().equalsIgnoreCase("/end")) {
                            server.broadcast(str, name);
                            setAuthorized(false);
                            break;
                        } else if (str.getMessText().startsWith("/w ")) {
                            String[] elements = str.getMessText().split(" ");
                            server.broadcast(new Message(name + "->" + elements[1] + "(DM): " + elements[2]), name, elements[1]);
                        } else if (str.getMessText().equalsIgnoreCase("/delete")) {
                            server.getAuthService().deleteByLogin(name);
                            server.broadcast(str, name);
                           // setAuthorized(false);
                            break;
                        } else {
                            server.broadcast(new Message(name + " : " + str));
                        }
                    }
                    setAuthorized(false);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                setAuthorized(false);
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    //Отправка сообщений пользователям о входе и выходе из чата
    private void setAuthorized(boolean isAuthorizen) {
        if(isAuthorizen){
            server.subscribe(this);
            if (!name.isEmpty()){
                server.broadcast(new Message(" Пользователь " + name + " зашел в чат "));
                server.broadcastUserList();
            }
        }else {
            server.ubsubscrine(this);

            if(!name.isEmpty()){
                server.broadcast(new Message(" Пользователь " + name + " вышел из чата "));
                server.broadcastUserList();
            }
        }
    }
    //Отправка сообщений клиенту
    public void sendMessage(Message message) {
        try {
            System.out.println("->Клиент" + (this.name!=null? " " + this.name : "") + message);
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getName() {
        return this.name;
    }


}
