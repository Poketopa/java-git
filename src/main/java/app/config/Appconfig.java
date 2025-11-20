package main.java.app.config;

import main.java.app.controller.GitController;
import main.java.app.view.OutputView;
import main.java.app.repository.FileIndexRepository;
import main.java.app.repository.FileObjectReader;
import main.java.app.repository.FileObjectWriter;
import main.java.app.repository.FileRefRepository;
import main.java.app.repository.IndexRepository;
import main.java.app.repository.ObjectReader;
import main.java.app.repository.ObjectWriter;
import main.java.app.repository.RefRepository;
import main.java.app.service.AddService;
import main.java.app.service.CommitService;
import main.java.app.service.FileSystemInitService;
import main.java.app.service.InitService;
import main.java.app.service.StatusService;
import main.java.app.service.LogService;
import main.java.app.service.BranchService;
import main.java.app.service.CheckoutService;
import main.java.app.service.MergeService;
import main.java.app.service.remote.fs.PushService;
import main.java.app.service.remote.fs.PullService;
import main.java.app.service.remote.fs.CloneService;
import main.java.app.service.remote.http.HttpPushService;
import main.java.app.service.remote.http.HttpPullService;

import java.nio.file.Path;
import java.util.Objects;

// 수동 DI 구성
// - Repository / Service 인스턴스 생성 및 주입
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


