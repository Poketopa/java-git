package app.controller.command.handlers;

import app.remote.http.HttpRemoteServer;
import app.view.OutputView;

import java.nio.file.Paths;
import java.util.Objects;

public final class ServeHttpCmd {
    private static final int EXPECTED_ARGUMENTS = 2;
    private static final int PORT_ARGUMENT_INDEX = 1;
    private static final String USER_DIR_PROPERTY = "user.dir";

    private final OutputView outputView;

    public ServeHttpCmd(OutputView outputView) {
        this.outputView = Objects.requireNonNull(outputView);
    }

    public void execute(String[] args) {
        if (args.length != EXPECTED_ARGUMENTS) {
            outputView.showServeHttpUsage();
            return;
        }
        int port;
        try {
            port = Integer.parseInt(args[PORT_ARGUMENT_INDEX]);
        } catch (NumberFormatException e) {
            outputView.showServeHttpUsage();
            return;
        }
        HttpRemoteServer server = new HttpRemoteServer(Paths.get(System.getProperty(USER_DIR_PROPERTY)));
        server.start(port);
        outputView.showServeHttpStarted(port);
        
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
