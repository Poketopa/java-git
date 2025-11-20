package main.java.app.controller.command.handlers;

import main.java.app.remote.http.HttpRemoteServer;
import main.java.app.view.OutputView;

import java.nio.file.Paths;
import java.util.Objects;

public final class ServeHttpCmd {
    private final OutputView outputView;

    public ServeHttpCmd(OutputView outputView) {
        this.outputView = Objects.requireNonNull(outputView);
    }

    public void execute(String[] args) {
        if (args.length != 2) {
            outputView.showServeHttpUsage();
            return;
        }
        int port;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            outputView.showServeHttpUsage();
            return;
        }
        HttpRemoteServer server = new HttpRemoteServer(Paths.get(System.getProperty("user.dir")));
        Thread t = new Thread(() -> server.start(port));
        t.setDaemon(true);
        t.start();
        outputView.showServeHttpStarted(port);
    }
}


