package ru.gb.javafxchat.server;

import java.io.Closeable;

public interface AuthService extends Closeable {
    String getNickByLoginAndPassword(String login, String password);

}
