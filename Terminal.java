import java.io.*;
import java.nio.file.*;
import java.util.*;

import java.util.Arrays;


public class Terminal {
    private String currentDirectory = System.getProperty("user.dir");
    private List<String> commandHistory = new ArrayList<>();
    Parser parser ;
    //Implementation of each command:
    //Take one parameter and displays it
    public void echo(String argument) {
        System.out.println(argument);
    }
    //Displays the current path
    public void pwd() {
        System.out.println(currentDirectory);
    }
    //Change your current path to either the home or the path you want
    public void cd(String arg) {
        if (arg.equals("..")) {
            Path parent = Paths.get(currentDirectory).getParent();
            if (parent != null) {
                currentDirectory = parent.toString();
            } else {
                System.out.println("You are at the root directory.");
            }
        } else if (arg.isEmpty()) {
            String homeDirectory = System.getProperty("user.home");
            currentDirectory = homeDirectory;
        } else {
            Path newPath = Paths.get(arg);
            if (newPath.isAbsolute()) {
                if (Files.isDirectory(newPath)) {
                    currentDirectory = newPath.toString();
                } else {
                    System.out.println("Invalid path: " + arg);
                }
            } else {
                Path newRelativePath = Paths.get(currentDirectory, arg);
                if (Files.isDirectory(newRelativePath)) {
                    currentDirectory = newRelativePath.toString();
                } else {
                    System.out.println("Invalid path: " + newRelativePath);
                }
            }
        }
    }
    public void cdHome() {
        String homeDirectory = System.getProperty("user.home");
        currentDirectory = homeDirectory;
    }// To get the previous directory (path)
    public void cdParent() {
        Path parent = Paths.get(currentDirectory).getParent();
        if (parent != null) {
            currentDirectory = parent.toString();
        } else {
            System.out.println("You are at the root directory.");
        }
    }
    //Shows the content of your current directory (all files,folders,..) in a sorted way
    public void ls() {
        File current = new File(currentDirectory);
        File[] list = current.listFiles();
        Arrays.sort(list);
        for (File f : list) {
            System.out.println(f.getName());
        }
    }
    //shows the content of your current directory (all files,folders,..) but in reversed order
    public void lsR() {
        File current = new File(currentDirectory);
        File[] list = current.listFiles();

        if (list != null) {
            Arrays.sort(list, Collections.reverseOrder());

            for (File f : list) {
                System.out.println(f);
            }
        }
    }
    // creates new directory(directories) as much as you want to create
    public void mkdir(String[] args) {
        for (String arg : args) {
            Path newDir = Paths.get(arg);
            if (newDir.isAbsolute()) {
                try {
                    Files.createDirectories(newDir);
                    System.out.println("Directory created: " + newDir.toString());
                } catch (IOException e) {
                    System.out.println("Error creating directory: " + arg);
                }
            } else {
                try {
                    Files.createDirectories(Paths.get(currentDirectory, arg));
                    System.out.println("Directory created: " + Paths.get(currentDirectory, arg).toString());
                } catch (IOException e) {
                    System.out.println("Error creating directory: " + arg);
                }
            }
        }
    }
    //removes the directories either you define the one you want to delete or it deletes all of the empty ones
    public void rmdir(String arg) {
        if (arg.equals("*")) {
            // 1* Remove all empty directories in the current directory
            removeEmptyDirectories(currentDirectory);
        } else {
            // 2* Remove the given directory if it is empty
            Path targetDir = Paths.get(arg).toAbsolutePath();

            if (Files.exists(targetDir) && Files.isDirectory(targetDir)) {
                try {
                    //this line traverse the sub-directories first then the root
                    Files.walk(targetDir)
                            //sort the directory in reversed order
                            .sorted(Comparator.reverseOrder())
                            //it maps the path object to a file object
                            .map(Path::toFile)
                            //this line keeps only the files that are empty so that we can delete
                            .filter(file -> file.isDirectory() && isEmptyDirectory(file))
                            //Deletes the empty directories
                            .forEach(File::delete);

                    System.out.println("All empty directories are removed successfully.");
                } catch (IOException e) {
                    System.out.println("Error deleting directory: " + arg);
                }
            } else {
                System.out.println("Invalid directory or directory is not empty: " + arg);
            }
        }
    }
    //to check if the current directory empty to delete or not
    private boolean isEmptyDirectory(File directory) {
        File[] files = directory.listFiles();
        return files != null && files.length == 0;
    }
    private void removeEmptyDirectories(String path) {
        File directory = new File(path);

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        removeEmptyDirectories(file.getAbsolutePath());
                    }
                }
            }

            if (isEmptyDirectory(directory)) {
                System.out.println("Removing empty directory: " + directory.getAbsolutePath());
                directory.delete();
            }
        }
    }
    //creates a file in the path (directory)you want
    public void touch(String fileName) {
        Path filePath = Paths.get(fileName);
        if (!filePath.isAbsolute()) {
            filePath = Paths.get(currentDirectory, fileName);
        }

        try {
            Files.createFile(filePath);
            System.out.println("File created: " + filePath.toString());
        } catch (IOException e) {
            System.out.println("Error creating file: " + filePath.toString());
        }
    }
    //Takes 2 files as inputs then prints the first one into the second one
    public void cp(String sourcePath, String destinationPath) throws IOException {
        File src = makeAbsolute(sourcePath);
        if (!src.exists()) {
            throw new FileNotFoundException(src.getAbsolutePath() + " does not exist");
        }

        File dst = makeAbsolute(destinationPath);
        if (dst.isDirectory()) {
            // Build the destination path by combining the destination directory with the source file name
            File destinationFile = new File(dst, src.getName());
            Files.copy(src.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } else if (dst.exists()) {
            // If the destination is an existing file, copy to that file directly (deletes the old content then add the first's content)
            Files.copy(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } else {
            // If the second file does not exist it creates it then adds the contents of the first file into it
            Files.copy(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
    //Takes the file name as an input then deletes it
    public void rm(String fileName) {
        Path filePath = Paths.get(fileName);
        if (!filePath.isAbsolute()) {
            filePath = Paths.get(currentDirectory, fileName);
        }

        try {
            Files.delete(filePath);
            System.out.println("File removed: " + filePath.toString());
        } catch (IOException e) {
            System.out.println("Error removing file: " + filePath.toString());
        }
    }
    //if there's only one file then it shows what's inside it . if there are two files then it concatenates the content of the two files together
    public void cat(String[] args) {
        if (args.length == 1 || args.length == 2) {
            for (String fileName : args) {
                Path filePath = resolvePath(fileName);
                if (filePath != null) {
                    try {
                        List<String> lines = Files.readAllLines(filePath);
                        for (String line : lines) {
                            System.out.println(line);
                        }
                    } catch (IOException e) {
                        System.out.println("Error reading file: " + fileName);
                    }
                }
            }
        } else {
            System.out.println("Usage: cat <file> or cat <file1> <file2>");
        }
    }
    //displays how many lines,words, letters inside the file you entered
    public void wc(String fileName) {
        int lineCount = 0;
        int wordCount = 0;
        int charCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                lineCount++;
                charCount += line.length(); // Include spaces
                StringTokenizer tokenizer = new StringTokenizer(line);
                wordCount += tokenizer.countTokens();
            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
            return;
        }

        System.out.println(lineCount + " " + wordCount + " " + charCount + " " + fileName);
    }
    // this function is for the ">",">>" commands
    private void executeRedirectionCommand(String sourceCommand, String filePath, boolean append) {
        File file = makeAbsolute(filePath);
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", sourceCommand);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Read the output of the process
            try (InputStream inputStream = process.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                 FileWriter fileWriter = new FileWriter(file, append);
                 PrintWriter printWriter = new PrintWriter(fileWriter)) {

                String line;
                while ((line = reader.readLine()) != null) {
                    printWriter.println(line);
                }
            }

            // Wait for the process to complete
            if (process.waitFor() != 0) {
                System.err.println("Command not found: " + sourceCommand);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private File makeAbsolute(String filePath) {
        File file = new File(filePath);
        if (!file.isAbsolute()) {
            file = new File(currentDirectory, filePath);
        }
        return file;
    }
    // displays the commands you used in the execution time
    public void history() {
        for (int i = 0; i < commandHistory.size(); i++) {
            System.out.println(i + 1 + ") " + commandHistory.get(i));
        }
    }
    //this one is to define the command you want to use
    public void chooseCommandAction(String command) {
        if (command.contains(">>")) {
            String filePath = command.substring(command.indexOf(">>") + 2).trim();
            command = command.substring(0, command.indexOf(">>")).trim();
            executeRedirectionCommand(command, filePath, true); // true for append
        } else if (command.contains(">")) {
            String filePath = command.substring(command.indexOf(">") + 1).trim();
            command = command.substring(0, command.indexOf(">")).trim();
            executeRedirectionCommand(command, filePath, false); // false for overwrite
        } else {
            String[] splited = command.split("\\s+");
            // Parse and execute the command
            Parser parser = new Parser(splited);
            if (parser.parse(command)) { // Check the return value of the parse method
                String cmd = parser.getCommandName();
                String[] arguments = parser.getArguments();

                switch (cmd) {
                    case "exit":
                        System.out.println("END");
                        System.exit(0);
                        break;
                    case "history":
                        history();
                        break;
                    case "echo":
                        echo(String.join(" ", arguments));
                        break;
                    case "pwd":
                        pwd();
                        break;
                    case "cd":
                        if (arguments.length == 0) {
                            cdHome();
                        } else if (arguments.length == 1) {
                            if (arguments[0].equals("..")) {
                                cdParent();
                            } else {
                                cd(arguments[0]);
                            }
                        } else {
                            System.out.println("Usage: cd [<directory>|..]");
                        }
                        break;

                    case "ls":
                        ls();
                        break;
                    case "ls-r":
                        lsR();
                        break;
                    case "mkdir":
                        mkdir(arguments);
                        break;
                    case "rmdir":
                        if (arguments.length > 0) {
                            rmdir(arguments[0]);
                        } else {
                            System.out.println("Usage: rmdir <directory>");
                        }
                        break;
                    case "wc":
                        if (splited.length == 2) {
                            String fileName = parser.getFirstArg();
                            wc(fileName);
                        } else {
                            System.out.println("Usage: wc <file>");
                        }
                        break;
                    case "touch":
                        if (arguments.length > 0) {
                            touch(arguments[0]);
                        } else {
                            System.out.println("Usage: touch <file>");
                        }
                        break;
                    case "cp":
                        if (arguments.length == 2) {
                            try {
                                cp(arguments[0], arguments[1]);
                            } catch (IOException e) {
                                System.err.println("Error: " + e.getMessage());
                            }
                        } else {
                            System.out.println("Usage: cp <source> <destination>");
                        }
                        break;

                    case "rm":
                        if (arguments.length > 0) {
                            rm(arguments[0]);
                        } else {
                            System.out.println("Usage: rm <file>");
                        }
                        break;
                    case "cat":
                        if (arguments.length > 0) {
                            cat(arguments);
                        } else {
                            System.out.println("Usage: cat <file>");
                        }
                        break;

                    default:
                        System.out.println("Unknown command: " + cmd);
                        break;
                }
            }
        }
    }


    //This class is to make sure that the entered path is valid or not
    private Path resolvePath(String fileName) {
        Path filePath = Paths.get(fileName);
        if (!filePath.isAbsolute()) {
            filePath = Paths.get(currentDirectory, fileName);
        }

        if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
            return filePath;
        } else {
            System.out.println("Invalid file: " + fileName);
            return null;
        }
    }

    public static void main(String[] args) {
        Terminal terminal = new Terminal();
        Scanner in = new Scanner(System.in);
        String command;
        while (true) {
            System.out.print(terminal.currentDirectory + ":-$ ");
            command = in.nextLine().trim();
            terminal.commandHistory.add(command);

            terminal.chooseCommandAction(command);
        }
    }

}