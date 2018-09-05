package cm.aptoide.pt.download;

import cm.aptoide.pt.downloadmanager.Constants;
import cm.aptoide.pt.downloadmanager.FileDownloadCallback;
import cm.aptoide.pt.downloadmanager.FileDownloader;
import com.liulishuo.filedownloader.BaseDownloadTask;
import rx.Completable;
import rx.Observable;

/**
 * Created by filipegoncalves on 7/31/18.
 */

public class FileDownloadManager implements FileDownloader {

  public static final int RETRY_TIMES = 3;
  private static final int APTOIDE_DOWNLOAD_TASK_TAG_KEY = 888;
  private static final int PROGRESS_MAX_VALUE = 100;
  private final String mainDownloadPath;
  private final int fileType;
  private final String packageName;
  private final int versionCode;
  private final String fileName;
  private com.liulishuo.filedownloader.FileDownloader fileDownloader;
  private FileDownloadTask fileDownloadTask;
  private String downloadsPath;
  private int downloadId;

  public FileDownloadManager(com.liulishuo.filedownloader.FileDownloader fileDownloader,
      FileDownloadTask fileDownloadTask, String downloadsPath, String mainDownloadPath,
      int fileType, String packageName, int versionCode, String fileName) {
    this.fileDownloader = fileDownloader;
    this.fileDownloadTask = fileDownloadTask;
    this.downloadsPath = downloadsPath;
    this.mainDownloadPath = mainDownloadPath;
    this.fileType = fileType;
    this.packageName = packageName;
    this.versionCode = versionCode;
    this.fileName = fileName;
  }

  @Override public Completable startFileDownload() {
    return Completable.fromCallable(() -> {
      if (mainDownloadPath == null || mainDownloadPath.isEmpty()) {
        throw new IllegalArgumentException("The url for the download can not be empty");
      } else {
        createBaseDownloadTask(mainDownloadPath, versionCode, packageName, fileType, fileName);
        return fileDownloader.start(fileDownloadTask, false);
      }
    });
  }

  @Override public Completable pauseDownload() {
    return Completable.fromAction(() -> fileDownloader.pause(fileDownloadTask));
  }

  @Override public Completable removeDownloadFile() {
    return Completable.fromAction(() -> fileDownloader.clear(downloadId, mainDownloadPath));
  }

  @Override public Observable<FileDownloadCallback> observeFileDownloadProgress() {
    return fileDownloadTask.onDownloadStateChanged();
  }

  private void createBaseDownloadTask(String mainDownloadPath, int versionCode, String packageName,
      int fileType, String fileName) {

    BaseDownloadTask baseDownloadTask = fileDownloader.create(mainDownloadPath);
    baseDownloadTask.setAutoRetryTimes(RETRY_TIMES);

    baseDownloadTask.addHeader(Constants.VERSION_CODE, String.valueOf(versionCode));
    baseDownloadTask.addHeader(Constants.PACKAGE, packageName);
    baseDownloadTask.addHeader(Constants.FILE_TYPE, String.valueOf(fileType));
    baseDownloadTask.setTag(APTOIDE_DOWNLOAD_TASK_TAG_KEY, fileDownloadTask);
    baseDownloadTask.setListener(fileDownloadTask);
    baseDownloadTask.setCallbackProgressTimes(PROGRESS_MAX_VALUE);
    baseDownloadTask.setPath(downloadsPath + fileName);
    this.downloadId = baseDownloadTask.asInQueueTask()
        .enqueue();
  }
}