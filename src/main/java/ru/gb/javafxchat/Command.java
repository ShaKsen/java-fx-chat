package ru.gb.javafxchat;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum Command {
    AUTH("/auth") {
        @Override
        public String[] parse(String commandText) {
            final String[] split = commandText.split(TOKEN_DELIMITER);
            return new String[]{split[1], split[2]};
        }
    },
    AUTHOK("/authok") {
        @Override
        public String[] parse(String commandText) {
            final String[] split = commandText.split(TOKEN_DELIMITER);
            return new String[]{split[1]};
        }
    },
    END("/end") {
        @Override
        public String[] parse(String commandText) {
            return new String[0];
        }
    },
    PRIVATE_MESSAGE("/w") {
        @Override
        public String[] parse(String commandText) {
            final String[] split = commandText.split(TOKEN_DELIMITER, 3);
            return new String[]{split[1], split[2]};
        }
    },
    CLIENTS("/clients") {
        @Override
        public String[] parse(String commandText) {
            final String[] split = commandText.split(TOKEN_DELIMITER);
            final String[] nicks = new String[split.length - 1];
            for (int i = 0; i < nicks.length; i++) {
                nicks[i] = split[i + 1];
            }
            return nicks;
            }
        },
    ERROR("/error") {
        @Override
        public String[] parse(String commandText) {
            final String[] split = commandText.split(TOKEN_DELIMITER, 2);
            return new String[]{split[1]};
        }
    },
    MESSAGE("/message"){
        @Override
        public String[] parse(String commandText) {
            final String[] split = commandText.split(TOKEN_DELIMITER, 2);
            return new String[]{split[1]};
        }
    },
    STOP("/stop"){
        @Override
        public String[] parse(String commandText) {
            return new String[0];
        }
    }
   ;
    private final String command;

    static final String TOKEN_DELIMITER = "\\p{Blank}+";

    static final Map<String, Command> commandMap = Arrays.stream(values())
                .collect(Collectors.toMap(Command::getCommand, Function.identity()));


    Command(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
    private static boolean isCommand(String message){
        return message.startsWith("/");
    }

    public static Command getCommand(String message){
        if (!isCommand(message)){
            throw new RuntimeException("'" + message + "' is not a command");
        }
        final String cmd = message.split(TOKEN_DELIMITER, 2)[0];
        final Command command = commandMap.get(cmd);
        if(command == null){
        throw new RuntimeException("Unknown command '" + cmd + "'")  ;
        }
        return command;

    }

    public abstract String[] parse(String commandText);

    public String collectMessage (String...params){
      return this.command + " " + String.join(" ", params);
    }
}
