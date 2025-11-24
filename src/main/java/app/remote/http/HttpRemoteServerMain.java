package app.remote.http;

import java.nio.file.Path;
import java.nio.file.Paths;


public final class HttpRemoteServerMain {
    public static void main(String[] args) {
        if (args == null || args.length != 1) {
            System.out.println("사용법: java ... HttpRemoteServerMain <port>");
            return;
        }
        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("포트는 숫자여야 합니다.");
            return;
        }
        Path root = Paths.get(System.getProperty("user.dir"));
        HttpRemoteServer server = new HttpRemoteServer(root);
        server.start(port);
        System.out.println("HTTP 원격 서버 시작: 포트=" + port + " 루트=" + root);
        
        try {
            Thread.currentThread().join();
        } catch (InterruptedException ignored) {
        }
    }
}





