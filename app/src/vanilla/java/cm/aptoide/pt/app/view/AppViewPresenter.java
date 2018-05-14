package cm.aptoide.pt.app.view;

import android.text.TextUtils;
import cm.aptoide.accountmanager.AptoideAccountManager;
import cm.aptoide.pt.account.AccountAnalytics;
import cm.aptoide.pt.account.view.AccountNavigator;
import cm.aptoide.pt.app.AppViewAnalytics;
import cm.aptoide.pt.app.AppViewManager;
import cm.aptoide.pt.crashreports.CrashReport;
import cm.aptoide.pt.presenter.Presenter;
import cm.aptoide.pt.presenter.View;
import rx.Completable;
import rx.Observable;
import rx.Scheduler;
import rx.Single;
import rx.exceptions.OnErrorNotImplementedException;

/**
 * Created by franciscocalado on 08/05/18.
 */

public class AppViewPresenter implements Presenter {

  private AppViewView view;
  private AccountNavigator accountNavigator;
  private AppViewAnalytics appViewAnalytics;
  private AppViewNavigator appViewNavigator;
  private AppViewManager appViewManager;
  private AptoideAccountManager accountManager;
  private Scheduler scheduler;
  private CrashReport crashReport;
  private long appId;
  private String packageName;

  public AppViewPresenter(AppViewView view, AccountNavigator accountNavigator,
      AppViewAnalytics appViewAnalytics, AppViewNavigator appViewNavigator,
      AppViewManager appViewManager, AptoideAccountManager accountManager, Scheduler scheduler,
      CrashReport crashReport, long appId, String packageName) {
    this.view = view;
    this.accountNavigator = accountNavigator;
    this.appViewAnalytics = appViewAnalytics;
    this.appViewNavigator = appViewNavigator;
    this.appViewManager = appViewManager;
    this.accountManager = accountManager;
    this.scheduler = scheduler;
    this.crashReport = crashReport;
    this.appId = appId;
    this.packageName = packageName;
  }

  @Override public void present() {
    handleFirstLoad();
    handleClickOnScreenshot();
    handleClickOnVideo();
    handleClickOnDescriptionReadMore();
    handleClickOnDeveloperWebsite();
    handleClickOnDeveloperEmail();
    handleClickOnDeveloperPrivacy();
    handleClickOnDeveloperPermissions();
    handleClickOnStoreLayout();
    handleClickOnFollowStore();
    handleClickOnOtherVersions();
    handleClickOnTrustedBadge();
    handleClickOnRateApp();
    handleClickReadComments();
    handleClickFlags();
    handleClickLoginSnack();
    handleClickOnSimilarApps();
  }

  private void handleFirstLoad() {
    view.getLifecycle()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .doOnNext(__ -> view.showLoading())
        .flatMapSingle(__ -> appViewManager.getDetailedAppViewModel(appId, packageName))
        .observeOn(scheduler)
        .doOnNext(appViewModel -> view.populateAppDetails(appViewModel))
        .flatMapSingle(appViewModel -> Single.zip(appViewManager.getReviewsViewModel(
            appViewModel.getDetailedApp()
                .getStore()
                .getName(), packageName, 5, view.getLanguageFilter())
                .observeOn(scheduler),
            appViewManager.loadSimilarApps(packageName, appViewModel.getDetailedApp()
                .getMedia()
                .getKeywords(), 2)
                .observeOn(scheduler),
            (reviews, similar) -> view.populateReviewsAndAds(reviews, similar,
                appViewModel.getDetailedApp())))
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
        }, throwable -> crashReport.log(throwable));
  }

  private void handleClickOnScreenshot() {
    view.getLifecycle()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> view.getScreenshotClickEvent())
        .filter(event -> !event.isVideo())
        .doOnNext(imageClick -> {
          appViewAnalytics.sendOpenScreenshotEvent();
          appViewNavigator.navigateToScreenshots(imageClick.getImagesUris(),
              imageClick.getImagesIndex());
        })
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
        }, err -> crashReport.log(err));
  }

  private void handleClickOnVideo() {
    view.getLifecycle()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> view.getScreenshotClickEvent())
        .filter(event -> event.isVideo())
        .doOnNext(videoClick -> {
          appViewAnalytics.sendOpenVideoEvent();
          appViewNavigator.navigateToUri(videoClick.getUri());
        })
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
        }, err -> crashReport.log(err));
  }

  private void handleClickOnDescriptionReadMore() {
    view.getLifecycle()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> view.clickedReadMore())
        .doOnNext(readMoreClickEvent -> {
          appViewAnalytics.sendReadMoreEvent();
          appViewNavigator.navigateToDescriptionReadMore(readMoreClickEvent.getStoreName(),
              readMoreClickEvent.getDescription(), readMoreClickEvent.getStoreTheme());
        })
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
        }, err -> crashReport.log(err));
  }

  private void handleClickOnDeveloperWebsite() {
    view.getLifecycle()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> view.clickDeveloperWebsite())
        .flatMapSingle(__ -> appViewManager.getDetailedAppViewModel(appId, packageName))
        .filter(app -> !TextUtils.isEmpty(app.getDetailedApp()
            .getDeveloper()
            .getWebsite()))
        .doOnNext(app -> view.navigateToDeveloperWebsite(app.getDetailedApp()))
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
        }, err -> crashReport.log(err));
  }

  private void handleClickOnDeveloperEmail() {
    view.getLifecycle()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> view.clickDeveloperEmail())
        .flatMapSingle(__ -> appViewManager.getDetailedAppViewModel(appId, packageName))
        .filter(app -> !TextUtils.isEmpty(app.getDetailedApp()
            .getDeveloper()
            .getEmail()))
        .doOnNext(app -> view.navigateToDeveloperEmail(app.getDetailedApp()))
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
        }, err -> crashReport.log(err));
  }

  private void handleClickOnDeveloperPrivacy() {
    view.getLifecycle()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> view.clickDeveloperPrivacy())
        .flatMapSingle(__ -> appViewManager.getDetailedAppViewModel(appId, packageName))
        .filter(app -> !TextUtils.isEmpty(app.getDetailedApp()
            .getDeveloper()
            .getPrivacy()))
        .doOnNext(app -> view.navigateToDeveloperPrivacy(app.getDetailedApp()))
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
        }, err -> crashReport.log(err));
  }

  private void handleClickOnDeveloperPermissions() {
    view.getLifecycle()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> view.clickDeveloperPermissions())
        .flatMapSingle(__ -> appViewManager.getDetailedAppViewModel(appId, packageName))
        .doOnNext(app -> view.navigateToDeveloperPermissions(app.getDetailedApp()))
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
        }, err -> crashReport.log(err));
  }

  private void handleClickOnStoreLayout() {
    view.getLifecycle()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> view.clickStoreLayout())
        .flatMapSingle(__ -> appViewManager.getDetailedAppViewModel(appId, packageName))
        .doOnNext(app -> appViewNavigator.navigateToStore(app.getDetailedApp()
            .getStore()))
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
        }, err -> crashReport.log(err));
  }

  private void handleClickOnFollowStore() {
    view.getLifecycle()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> view.clickFollowStore())
        .flatMapSingle(__ -> appViewManager.getDetailedAppViewModel(appId, packageName))
        .observeOn(scheduler)
        .flatMapCompletable(model -> {
          if (model.isStoreFollowed()) {
            view.setFollowButton(true);
            appViewAnalytics.sendOpenStoreEvent();
            appViewNavigator.navigateToStore(model.getDetailedApp()
                .getStore());
            return Completable.complete();
          } else {
            view.setFollowButton(false);
            appViewAnalytics.sendFollowStoreEvent();
            view.displayStoreFollowedSnack(model.getDetailedApp()
                .getStore()
                .getName());
            return appViewManager.subscribeStore(model.getDetailedApp()
                .getStore()
                .getName());
          }
        })
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
        }, err -> {
          throw new OnErrorNotImplementedException(err);
        });
  }

  private void handleClickOnOtherVersions() {
    view.getLifecycle()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> view.clickOtherVersions())
        .flatMapSingle(__ -> appViewManager.getDetailedAppViewModel(appId, packageName))
        .doOnNext(model -> {
          appViewAnalytics.sendOtherVersionsEvent();
          appViewNavigator.navigateToOtherVersions(model.getDetailedApp()
              .getName(), model.getDetailedApp()
              .getIcon(), model.getDetailedApp()
              .getPackageName());
        })
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
        }, err -> crashReport.log(err));
  }

  private void handleClickOnTrustedBadge() {
    view.getLifecycle()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> view.clickTrustedBadge())
        .flatMapSingle(__ -> appViewManager.getDetailedAppViewModel(appId, packageName))
        .doOnNext(model -> view.showTrustedDialog(model.getDetailedApp()))
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
        }, err -> crashReport.log(err));
  }

  private void handleClickOnRateApp() {
    view.getLifecycle()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> Observable.merge(view.clickRateApp(), view.clickRateAppLarge(),
            view.clickRateAppLayout()))
        .flatMapSingle(__ -> appViewManager.getDetailedAppViewModel(appId, packageName))
        .observeOn(scheduler)
        .flatMap(model -> view.showRateDialog(model.getAppName(), model.getPackageName(),
            model.getStore()
                .getName()))
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
        }, err -> crashReport.log(err));
  }

  private void handleClickReadComments() {
    view.getLifecycle()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> Observable.merge(view.clickCommentsLayout(), view.clickReadAllComments()))
        .flatMapSingle(__ -> appViewManager.getDetailedAppViewModel(appId, packageName))
        .doOnNext(
            model -> appViewNavigator.navigateToRateAndReview(model.getAppId(), model.getAppName(),
                model.getStore()
                    .getName(), model.getPackageName(), model.getStore()
                    .getAppearance()
                    .getTheme()))
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
        }, err -> crashReport.log(err));
  }

  private void handleClickFlags() {
    view.getLifecycle()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> Observable.merge(view.clickVirusFlag(), view.clickLicenseFlag(),
            view.clickWorkingFlag(), view.clickFakeFlag()))
        .doOnNext(type -> view.disableFlags())
        .flatMap(type -> accountManager.accountStatus()
            .observeOn(scheduler)
            .flatMap(account -> {
              if (!account.isLoggedIn()) {
                view.enableFlags();
                view.displayNotLoggedInSnack();
                return Observable.just(false);
              } else {
                return Observable.just(true);
              }
            })
            .filter(isLoggedIn -> isLoggedIn)
            .flatMapSingle(__ -> appViewManager.getDetailedAppViewModel(appId, packageName))
            .flatMapSingle(model -> appViewManager.addApkFlagRequestAction(model.getStore()
                .getName(), model.getFile()
                .getMd5sum(), type))
            .filter(result -> result)
            .observeOn(scheduler)
            .doOnNext(__ -> {
              view.incrementFlags(type);
              view.showFlagVoteSubmittedMessage();
            }))
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
        }, err -> {
          view.enableFlags();
          crashReport.log(err);
        });
  }

  private void handleClickLoginSnack() {
    view.getLifecycle()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> view.clickLoginSnack())
        .doOnNext(__ -> accountNavigator.navigateToAccountView(
            AccountAnalytics.AccountOrigins.APP_VIEW_FLAG))
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
        }, err -> crashReport.log(err));
  }

  private void handleClickOnSimilarApps() {
    view.getLifecycle()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> view.clickSimilarApp())
        .doOnNext(similarAppClickEvent -> {
          if (similarAppClickEvent.getSimilar()
              .isAd()) {
            appViewNavigator.navigateToAd(similarAppClickEvent.getSimilar()
                .getAd());
          } else {
            appViewNavigator.navigateToAppView(similarAppClickEvent.getSimilar()
                .getApp()
                .getAppId(), similarAppClickEvent.getSimilar()
                .getApp()
                .getPackageName(), "");
          }
        })
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
        }, err -> crashReport.log(err));
  }
}
