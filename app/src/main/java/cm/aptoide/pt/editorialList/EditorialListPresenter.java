package cm.aptoide.pt.editorialList;

import cm.aptoide.accountmanager.Account;
import cm.aptoide.accountmanager.AptoideAccountManager;
import cm.aptoide.pt.crashreports.CrashReport;
import cm.aptoide.pt.presenter.Presenter;
import cm.aptoide.pt.presenter.View;
import rx.Observable;
import rx.Scheduler;
import rx.Single;
import rx.exceptions.OnErrorNotImplementedException;

public class EditorialListPresenter implements Presenter {

  private final EditorialListView view;
  private final EditorialListManager editorialListManager;
  private final AptoideAccountManager accountManager;
  private final EditorialListNavigator editorialListNavigator;
  private final EditorialListAnalytics editorialListAnalytics;
  private final CrashReport crashReporter;
  private final Scheduler viewScheduler;

  public EditorialListPresenter(EditorialListView editorialListView,
      EditorialListManager editorialListManager, AptoideAccountManager accountManager,
      EditorialListNavigator editorialListNavigator, EditorialListAnalytics editorialListAnalytics,
      CrashReport crashReporter, Scheduler viewScheduler) {
    this.view = editorialListView;
    this.editorialListManager = editorialListManager;
    this.accountManager = accountManager;
    this.editorialListNavigator = editorialListNavigator;
    this.editorialListAnalytics = editorialListAnalytics;
    this.crashReporter = crashReporter;
    this.viewScheduler = viewScheduler;
  }

  @Override public void present() {
    onCreateLoadViewModel();
    handleEditorialCardClick();
    handleRetryClick();
    handleBottomReached();
    handleUserImageClick();
    loadUserImage();
  }

  private void onCreateLoadViewModel() {
    view.getLifecycleEvent()
        .filter(lifecycleEvent -> lifecycleEvent.equals(View.LifecycleEvent.CREATE))
        .doOnNext(created -> view.showLoading())
        .flatMapSingle(created -> loadEditorialListViewModel(false))
        .subscribe(__ -> {
        }, crashReporter::log);
  }

  private void loadUserImage() {
    view.getLifecycleEvent()
        .filter(lifecycleEvent -> lifecycleEvent.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> accountManager.accountStatus())
        .flatMap(account -> getUserAvatar(account))
        .observeOn(viewScheduler)
        .doOnNext(userAvatarUrl -> {
          if (userAvatarUrl != null) {
            view.setUserImage(userAvatarUrl);
          } else {
            view.setDefaultUserImage();
          }
          view.showAvatar();
        })
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        });
  }

  private void handleEditorialCardClick() {
    view.getLifecycleEvent()
        .filter(lifecycleEvent -> lifecycleEvent.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> view.editorialCardClicked()
            .observeOn(viewScheduler)
            .doOnNext(click -> {
              editorialListAnalytics.sendEditorialInteractEvent(click.getCardId());
              editorialListNavigator.navigateToEditorial(click.getCardId());
            })
            .retry())
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(homeClick -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        });
  }

  private void handleRetryClick() {
    view.getLifecycleEvent()
        .filter(lifecycleEvent -> lifecycleEvent.equals(View.LifecycleEvent.CREATE))
        .flatMap(viewCreated -> view.retryClicked()
            .observeOn(viewScheduler)
            .doOnNext(bottom -> view.showLoading())
            .flatMapSingle(__ -> loadEditorialListViewModel(false)))
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(notificationUrl -> {
        }, crashReporter::log);
  }

  private void handleBottomReached() {
    view.getLifecycleEvent()
        .filter(lifecycleEvent -> lifecycleEvent.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> view.reachesBottom()
            .filter(__ -> editorialListManager.hasMore())
            .observeOn(viewScheduler)
            .doOnNext(bottomReached -> view.showLoadMore())
            .flatMapSingle(bottomReached -> loadEditorialListViewModel(true))
            .retry())
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(bundles -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        });
  }

  private Single<EditorialListViewModel> loadEditorialListViewModel(boolean loadMore) {
    return editorialListManager.loadEditorialListViewModel(loadMore)
        .observeOn(viewScheduler)
        .doOnSuccess(editorialListViewModel -> {
          if (!editorialListViewModel.isLoading()) {
            view.hideLoading();
          }
          if (editorialListViewModel.hasError()) {
            view.showError(editorialListViewModel.getError());
          } else {
            view.populateView(editorialListViewModel);
          }
          view.hideLoadMore();
        })
        .map(editorialViewModel -> editorialViewModel);
  }

  private void handleUserImageClick() {
    view.getLifecycleEvent()
        .filter(lifecycleEvent -> lifecycleEvent.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> view.imageClick()
            .observeOn(viewScheduler)
            .doOnNext(account -> editorialListNavigator.navigateToMyAccount())
            .retry())
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(__ -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        });
  }

  private Observable<String> getUserAvatar(Account account) {
    String userAvatarUrl = null;
    if (account != null && account.isLoggedIn()) {
      userAvatarUrl = account.getAvatar();
    }
    return Observable.just(userAvatarUrl);
  }
}