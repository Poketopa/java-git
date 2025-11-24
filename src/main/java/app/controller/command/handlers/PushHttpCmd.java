package app.controller.command.handlers;

import app.service.remote.http.HttpPushService;
import app.view.OutputView;
import java.util.Objects;

public final class PushHttpCmd {
    private static final int EXPECTED_ARGUMENTS = 3;
    private static final int BASE_URL_INDEX = 1;
    private static final int BRANCH_NAME_INDEX = 2;

    private final HttpPushService httpPushService;
    private final OutputView outputView;

    public PushHttpCmd(HttpPushService httpPushService, OutputView outputView) {
        this.httpPushService = Objects.requireNonNull(httpPushService);
        this.outputView = Objects.requireNonNull(outputView);
    }

    public void execute(String[] args) {
        if (args.length != EXPECTED_ARGUMENTS) {
            outputView.showPushHttpUsage();
            return;
        }
        String baseUrl = args[BASE_URL_INDEX];
        String branch = args[BRANCH_NAME_INDEX];
        HttpPushService.Result result = httpPushService.push(baseUrl, branch);

        if (result == HttpPushService.Result.SUCCESS) {
            outputView.showPushHttpSuccess(branch);
            return;
        }
        if (result == HttpPushService.Result.ALREADY_UP_TO_DATE) {
            outputView.showPushHttpUpToDate();
            return;
        }
        if (result == HttpPushService.Result.REMOTE_REJECTED_NON_FF) {
            outputView.showPushHttpRejectedNonFastForward();
            return;
        }
        if (result == HttpPushService.Result.LOCAL_NO_COMMITS) {
            outputView.showPushHttpLocalNoCommits();
        }
    }
}
