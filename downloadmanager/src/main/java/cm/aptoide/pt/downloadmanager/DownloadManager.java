package cm.aptoide.pt.downloadmanager;

import cm.aptoide.pt.database.realm.Download;
import java.util.List;
import rx.Completable;
import rx.Observable;

/**
 * Created by filipegoncalves on 7/27/18.
 */

public interface DownloadManager {

  void start();

  void stop();

  Completable startDownload(Download download);

  Observable<Download> getDownload(String md5);

  Observable<Download> getDownloadsByMd5(String md5);

  Observable<List<Download>> getDownloadsList();

  Observable<Download> getCurrentInProgressDownload();

  Observable<List<Download>> getCurrentActiveDownloads();

  Completable pauseAllDownloads();

  Completable pauseDownload(String md5);

  Observable<Integer> getDownloadStatus(String md5);

  Completable removeDownload(String md5);

  Completable invalidateDatabase();
}
