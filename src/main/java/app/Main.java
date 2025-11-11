package app;

import app.config.Appconfig;
import app.controller.GitController;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class Main {
    public static void main(String[] args) {
        // 현재 작업 디렉토리 경로를 반환
        Path path = Paths.get(System.getProperty("user.dir"));
        GitController controller = new Appconfig(path).gitController();
        controller.run(args);
    }
}

