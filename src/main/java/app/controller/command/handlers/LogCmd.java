package app.controller.command.handlers;

import app.service.LogService;
import app.view.OutputView;

import java.util.Objects;

public final class LogCmd {
    private final LogService logService;
    private final OutputView outputView;

    public LogCmd(LogService logService, OutputView outputView) {
        this.logService = Objects.requireNonNull(logService);
        this.outputView = Objects.requireNonNull(outputView);
    }

    public void execute(String[] args) {
        outputView.showLog(logService.list());
    }
}





