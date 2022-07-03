package ru.gb.javafxchat.server;

import ru.gb.javafxchat.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;



public class ClientHandler {

    private static final int AUTH_TIMEOUT = 120_000;
    private Socket socket;
    private ChatServer server;
    private DataInputStream in;
    private DataOutputStream out;
    private String nick;
    private AuthService authService;
    private Thread timeoutThread;


    public ClientHandler(Socket socket, ChatServer server, AuthService authService) {

        try {

            this.socket = socket;
            this.server = server;
            this.authService = authService;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

        this.timeoutThread = new Thread(() -> {
            try {
                Thread.sleep(AUTH_TIMEOUT);
                sendMessage(Command.STOP);
            } catch (InterruptedException e) {
                System.out.println("Успешная авторизация");
            }
        });
        timeoutThread.start();

            new Thread(() -> {
                try {
                  if (authenticate()) {
                    readMessages();
                 }
                } finally {
                    closeConnection();
                }

            }).start();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    private boolean authenticate() {
        while (true) {
            try {

                    final String message = in.readUTF();
                    final Command command = Command.getCommand(message);

                    if (command == Command.END) {
                        return false;
                    }
                    if (command == Command.AUTH){
                        final String[] params = command.parse(message);
                        final String login = params[0];
                        final String password = params[1];
                        final String nick = authService.getNickByLoginAndPassword(login, password);
                        if (nick != null) {
                            if (server.isNickBusy(nick)) {
                                sendMessage(Command.ERROR, "Пользователь уже авторизован");
                                continue;
                            }
                            sendMessage(Command.AUTHOK, nick);
                            this.nick = nick;
                            server.broadcast(Command.MESSAGE, "Пользователь " + nick + " зашёл в чат");
                            server.subscribe(this);
                            return true;
                        } else {
                            sendMessage(Command.ERROR, "Неверные логин и пароль");
                        }
                    }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void sendMessage(Command command, String... params) {
        sendMessage(command.collectMessage(params));
    }

    private void closeConnection() {
        sendMessage(Command.END);
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            server.unsubscribe(this);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMessages() {
        while (true) {
            try {
                final String message = in.readUTF();
                final Command command = Command.getCommand(message);
                if (command == Command.END) {
                    break;

                }
                if (command == Command.PRIVATE_MESSAGE) {
                    final String[] params = command.parse(message);
                    server.sendPrivateMessage(this, params[0], params[1]);
                    continue;
                }
                server.broadcast(Command.MESSAGE, nick + ": " + command.parse(message)[0]);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getNick() {
        return nick;
    }


}
