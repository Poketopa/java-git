package main.java.app;

import main.java.app.config.Appconfig;
import main.java.app.controller.GitController;

import java.nio.file.Path;
import java.nio.file.Paths;

// 애플리케이션 시작점
// - 현재 작업 디렉토리를 루트로 사용
// - 컨트롤러의 인터랙티브 콘솔 실행
public final class Main {
    public static void main(String[] args) {
        Path path = Paths.get(System.getProperty("user.dir"));
        GitController controller = new Appconfig(path).gitController();
        controller.runConsole();
    }
}


