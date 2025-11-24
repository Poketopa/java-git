package app.config;

import app.controller.GitController;
import app.view.OutputView;
import app.repository.FileIndexRepository;
import app.repository.FileObjectReader;
import app.repository.FileObjectWriter;
import app.repository.FileRefRepository;
import app.repository.IndexRepository;
import app.repository.ObjectReader;
import app.repository.ObjectWriter;
import app.repository.RefRepository;
import app.service.AddService;
import app.service.CommitService;
import app.service.FileSystemInitService;
import app.service.InitService;
import app.service.StatusService;
import app.service.LogService;
import app.service.BranchService;
import app.service.CheckoutService;
import app.service.MergeService;
import app.service.remote.fs.PushService;
import app.service.remote.fs.PullService;
import app.service.remote.fs.CloneService;
import app.service.remote.http.HttpPushService;
import app.service.remote.http.HttpPullService;

import java.nio.file.Path;
import java.util.Objects;



public final class Appconfig {
    private final Path rootDirectoryPath;

    public Appconfig(Path rootDirectoryPath) {
        this.rootDirectoryPath = Objects.requireNonNull(rootDirectoryPath, "rootDirectoryPath");
    }

    public GitController gitController() {
        return new GitController(initService(), addService(), commitService(), statusService(), logService(), branchService(), checkoutService(), mergeService(), pushService(), pullService(), cloneService(), httpPushService(), httpPullService(), outputView());
    }

    private InitService initService() {
        return new InitService(new FileSystemInitService(), rootDirectoryPath);
    }

    private AddService addService() {
        IndexRepository indexRepository = new FileIndexRepository(rootDirectoryPath);
        ObjectWriter objectWriter = new FileObjectWriter(rootDirectoryPath);
        return new AddService(objectWriter, indexRepository, rootDirectoryPath);
    }

    private CommitService commitService() {
        IndexRepository indexRepository = new FileIndexRepository(rootDirectoryPath);
        ObjectWriter objectWriter = new FileObjectWriter(rootDirectoryPath);
        RefRepository refRepository = new FileRefRepository(rootDirectoryPath);
        return new CommitService(indexRepository, objectWriter, refRepository, rootDirectoryPath);
    }

    private StatusService statusService() {
        IndexRepository indexRepository = new FileIndexRepository(rootDirectoryPath);
        RefRepository refRepository = new FileRefRepository(rootDirectoryPath);
        ObjectReader objectReader = new FileObjectReader(rootDirectoryPath);
        return new StatusService(indexRepository, refRepository, objectReader, rootDirectoryPath);
    }

    private LogService logService() {
        RefRepository refRepository = new FileRefRepository(rootDirectoryPath);
        ObjectReader objectReader = new FileObjectReader(rootDirectoryPath);
        return new LogService(refRepository, objectReader);
    }

    private OutputView outputView() {
        return new OutputView();
    }

    private BranchService branchService() {
        RefRepository refRepository = new FileRefRepository(rootDirectoryPath);
        return new BranchService(refRepository);
    }

    private CheckoutService checkoutService() {
        RefRepository refRepository = new FileRefRepository(rootDirectoryPath);
        return new CheckoutService(refRepository, statusService());
    }

    private MergeService mergeService() {
        RefRepository refRepository = new FileRefRepository(rootDirectoryPath);
        ObjectReader objectReader = new FileObjectReader(rootDirectoryPath);
        return new MergeService(refRepository, objectReader);
    }

    private PushService pushService() {
        RefRepository refRepository = new FileRefRepository(rootDirectoryPath);
        ObjectReader objectReader = new FileObjectReader(rootDirectoryPath);
        return new PushService(refRepository, objectReader, rootDirectoryPath);
    }

    private PullService pullService() {
        RefRepository refRepository = new FileRefRepository(rootDirectoryPath);
        ObjectReader objectReader = new FileObjectReader(rootDirectoryPath);
        return new PullService(refRepository, objectReader, rootDirectoryPath);
    }

    private CloneService cloneService() {
        return new CloneService();
    }

    private HttpPushService httpPushService() {
        RefRepository refRepository = new FileRefRepository(rootDirectoryPath);
        ObjectReader objectReader = new FileObjectReader(rootDirectoryPath);
        return new HttpPushService(refRepository, objectReader, rootDirectoryPath);
    }

    private HttpPullService httpPullService() {
        RefRepository refRepository = new FileRefRepository(rootDirectoryPath);
        ObjectReader objectReader = new FileObjectReader(rootDirectoryPath);
        return new HttpPullService(refRepository, objectReader, rootDirectoryPath);
    }
}


