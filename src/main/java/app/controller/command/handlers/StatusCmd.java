package main.java.app.controller.command.handlers;

import main.java.app.service.StatusService;
import main.java.app.view.OutputView;

import java.util.Objects;

public final class StatusCmd {
    private final StatusService statusService;
    private final OutputView outputView;

    public StatusCmd(StatusService statusService, OutputView outputView) {
        this.statusService = Objects.requireNonNull(statusService);
        this.outputView = Objects.requireNonNull(outputView);
    }

    public void execute(String[] args) {
        OutputView view = this.outputView;
        view.showStatus(statusService.status());
    }
}




