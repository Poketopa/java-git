package main.java.app.controller.command.handlers;

import main.java.app.service.AddService;
import main.java.app.util.CommandLineParser;
import main.java.app.view.OutputView;

import java.util.List;
import java.util.Objects;

public final class AddCmd {
    private final AddService addService;
    private final OutputView outputView;

    public AddCmd(AddService addService, OutputView outputView) {
        this.addService = Objects.requireNonNull(addService);
        this.outputView = Objects.requireNonNull(outputView);
    }

    public void execute(String[] args) {
        List<String> filePaths = CommandLineParser.extractPaths(args);
        if (filePaths.isEmpty()) {
            outputView.showAddMissingPath();
            return;
        }
        addService.add(filePaths);
        outputView.showAddedToIndex(filePaths.size());
    }
}




