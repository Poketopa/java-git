package main.java.app.controller.command.handlers;

import main.java.app.service.remote.http.HttpPushService;
import main.java.app.view.OutputView;

import java.util.Objects;

public final class PushHttpCmd {
    private final HttpPushService httpPushService;
    private final OutputView outputView;

    public PushHttpCmd(HttpPushService httpPushService, OutputView outputView) {
        this.httpPushService = Objects.requireNonNull(httpPushService);
        this.outputView = Objects.requireNonNull(outputView);
    }

    public void execute(String[] args) {
        if (args.length != 3) {
            outputView.showPushHttpUsage();
            return;
        }
        String baseUrl = args[1];
        String branch = args[2];
        var result = httpPushService.push(baseUrl, branch);
        switch (result) {
            case SUCCESS -> outputView.showPushHttpSuccess(branch);
            case ALREADY_UP_TO_DATE -> outputView.showPushHttpUpToDate();
            case REMOTE_REJECTED_NON_FF -> outputView.showPushHttpRejectedNonFastForward();
            case LOCAL_NO_COMMITS -> outputView.showPushHttpLocalNoCommits();
        }
    }
}


