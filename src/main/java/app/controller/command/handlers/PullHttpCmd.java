package app.controller.command.handlers;

import app.service.remote.http.HttpPullService;
import app.view.OutputView;
import java.util.Objects;

public final class PullHttpCmd {
    private static final int EXPECTED_ARGUMENTS = 3;
    private static final int BASE_URL_INDEX = 1;
    private static final int BRANCH_NAME_INDEX = 2;

    private final HttpPullService httpPullService;
    private final OutputView outputView;

    public PullHttpCmd(HttpPullService httpPullService, OutputView outputView) {
        this.httpPullService = Objects.requireNonNull(httpPullService);
        this.outputView = Objects.requireNonNull(outputView);
    }

    public void execute(String[] args) {
        if (args.length != EXPECTED_ARGUMENTS) {
            outputView.showPullHttpUsage();
            return;
        }
        String baseUrl = args[BASE_URL_INDEX];
        String branch = args[BRANCH_NAME_INDEX];
        HttpPullService.Result result = httpPullService.pull(baseUrl, branch);

        if (result == HttpPullService.Result.SUCCESS) {
            outputView.showPullHttpSuccess(branch);
            return;
        }
        if (result == HttpPullService.Result.ALREADY_UP_TO_DATE) {
            outputView.showPullHttpUpToDate();
            return;
        }
        if (result == HttpPullService.Result.REMOTE_NO_COMMITS) {
            outputView.showPullHttpRemoteNoCommits();
            return;
        }
        if (result == HttpPullService.Result.NOT_FAST_FORWARD) {
            outputView.showPullHttpNotFastForward();
        }
    }
}
