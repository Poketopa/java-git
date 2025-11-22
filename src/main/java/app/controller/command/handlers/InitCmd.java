package app.controller.command.handlers;

import app.service.InitService;
import app.view.OutputView;

import java.util.Objects;

public final class InitCmd {
    private final InitService initService;
    private final OutputView outputView;

    public InitCmd(InitService initService, OutputView outputView) {
        this.initService = Objects.requireNonNull(initService);
        this.outputView = Objects.requireNonNull(outputView);
    }

    public void execute(String[] args) {
        initService.init();
        outputView.showInitSuccess();
    }
}




