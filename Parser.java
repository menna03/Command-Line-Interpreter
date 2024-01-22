import java.io.*;
import java.nio.file.*;
import java.util.*;

import java.util.Arrays;


//THE PARSER CLASS :

class Parser {
    String[] commandArray;
    String commandName;

    public Parser(String[] commandArray) {
        this.commandArray = commandArray;
        if (commandArray.length > 0) {
            this.commandName = commandArray[0];
        } else {
            this.commandName = "";
        }
    }

    public String getCommandName() {
        return commandName;
    }

    public String[] getArguments() {
        if (commandArray.length > 1) {
            return Arrays.copyOfRange(commandArray, 1, commandArray.length);
        } else {
            return new String[0];
        }
    }

    public String getFirstArg() {
        if (commandArray.length > 1) {
            return commandArray[1];
        } else {
            return "";
        }
    }

    public boolean parse(String input) {
        String[] inputParts = input.split(" ");
        if (inputParts.length > 0) {
            commandArray = inputParts;
            commandName = inputParts[0];
            return true;
        }
        return false;
    }
}
