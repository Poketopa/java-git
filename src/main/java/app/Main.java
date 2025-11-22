package main.java.app;

import main.java.app.config.Appconfig;
import main.java.app.controller.GitController;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class Main {
    public static void main(String[] args) {
        Path path = Paths.get(System.getProperty("user.dir"));
        GitController controller = new Appconfig(path).gitController();
        if (args.length > 0) {
            controller.run(args);
        } else {
            controller.runConsole();
        }
    }
}


