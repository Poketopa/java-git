package app.controller.command.handlers;

import app.service.AddService;
import app.util.CommandLineParser;
import app.view.OutputView;
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
