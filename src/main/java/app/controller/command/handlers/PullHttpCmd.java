package main.java.app.controller.command.handlers;

import main.java.app.service.remote.http.HttpPullService;
import main.java.app.view.OutputView;

import java.util.Objects;

public final class PullHttpCmd {
    private final HttpPullService httpPullService;
    private final OutputView outputView;

    public PullHttpCmd(HttpPullService httpPullService, OutputView outputView) {
        this.httpPullService = Objects.requireNonNull(httpPullService);
        this.outputView = Objects.requireNonNull(outputView);
    }

    public void execute(String[] args) {
        if (args.length != 3) {
            outputView.showPullHttpUsage();
            return;
        }
        String baseUrl = args[1];
        String branch = args[2];
        var result = httpPullService.pull(baseUrl, branch);
        switch (result) {
            case SUCCESS -> outputView.showPullHttpSuccess(branch);
            case ALREADY_UP_TO_DATE -> outputView.showPullHttpUpToDate();
            case REMOTE_NO_COMMITS -> outputView.showPullHttpRemoteNoCommits();
            case NOT_FAST_FORWARD -> outputView.showPullHttpNotFastForward();
        }
    }
}


