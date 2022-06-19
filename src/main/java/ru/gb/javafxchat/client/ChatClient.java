package ru.gb.javafxchat.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatClient {

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private final ChatController controller;

    public ChatClient(ChatController controller) {
        this.controller = controller;

    }
    public void openConnection() throws IOException {
        socket = new Socket("localhost", 8189);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            try {
                waitAuth();
                readMessages();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnection();
            }

        }).start();
    }

    private void waitAuth() throws IOException {
        while (true){
           final String message = in.readUTF();
            if (message.startsWith("/authok")){
                final String[] split = message.split("\\p{Blank}+");
               final String nick = split[1];
               controller.setAuth(true);
               controller.addMessage("Успешная авторизация под ником " + nick);
            break;
            }
        }
    }

    private void closeConnection() {
        if (in != null){
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null){
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket != null){
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void readMessages() throws IOException {
        while (true){
          final String message = in.readUTF();
          if("/end".equals(message)){
              controller.setAuth(false);
              break;
          }
          controller.addMessage(message);
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
