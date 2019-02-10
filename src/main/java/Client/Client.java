package Client;

import java.util.Scanner;

public  class Client {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ChatController chatController = new ChatController();
        boolean flag_register = false;
        String s = " ";
        while (!flag_register) {
            do {
                System.out.println("Войти(E) или рарегестрироваться(R) ?");
                s =  scanner.nextLine();
            }while (!(s.equals("E"))&&!(s.equals("R")));

            if (s.equals("E")) {
                flag_register = false;
            } else {flag_register=true;}
            new ClientConnection(chatController,flag_register).run();
        }
    }
}