package cm.aptoide.pt.comment;

import android.support.annotation.VisibleForTesting;
import cm.aptoide.pt.crashreports.CrashReport;
import cm.aptoide.pt.presenter.Presenter;
import cm.aptoide.pt.presenter.View;
import rx.Scheduler;
import rx.exceptions.OnErrorNotImplementedException;

public class CommentsPresenter implements Presenter {

  private final CommentsView view;
  private final CommentsListManager commentsListManager;
  private final CommentsNavigator commentsNavigator;
  private final Scheduler viewScheduler;
  private final CrashReport crashReporter;

  public CommentsPresenter(CommentsView view, CommentsListManager commentsListManager,
      CommentsNavigator commentsNavigator, Scheduler viewScheduler, CrashReport crashReporter) {
    this.view = view;
    this.commentsListManager = commentsListManager;
    this.commentsNavigator = commentsNavigator;
    this.viewScheduler = viewScheduler;
    this.crashReporter = crashReporter;
  }

  @Override public void present() {
    showComments();

    pullToRefresh();

    reachesBottom();

    clickComment();

    postComment();
  }

  @VisibleForTesting public void postComment() {
    view.getLifecycleEvent()
        .filter(lifecycleEvent -> lifecycleEvent.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> view.commentPost())
        .flatMapCompletable(commentsListManager::postComment)
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(comment -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        });
  }

  @VisibleForTesting public void reachesBottom() {
    view.getLifecycleEvent()
        .filter(lifecycleEvent -> lifecycleEvent.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> view.reachesBottom())
        .observeOn(viewScheduler)
        .doOnNext(__ -> view.showLoadMore())
        .flatMapSingle(__ -> commentsListManager.loadMoreComments())
        .observeOn(viewScheduler)
        .doOnNext(this::addComments)
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(comments -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        });
  }

  private void addComments(CommentsListViewModel model) {
    if (!model.isLoading()) {
      view.hideLoadMore();
      view.addComments(model.getComments());
    }
  }

  @VisibleForTesting public void pullToRefresh() {
    view.getLifecycleEvent()
        .filter(lifecycleEvent -> lifecycleEvent.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> view.refreshes())
        .observeOn(viewScheduler)
        .doOnNext(__ -> view.showLoading())
        .flatMapSingle(__ -> commentsListManager.loadFreshComments())
        .observeOn(viewScheduler)
        .doOnNext(comments -> view.hideRefreshLoading())
        .doOnNext(this::showComments)
        .doOnError(throwable -> view.showGeneralError())
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(comments -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        });
  }

  @VisibleForTesting public void showComments() {
    view.getLifecycleEvent()
        .filter(lifecycleEvent -> lifecycleEvent.equals(View.LifecycleEvent.CREATE))
        .doOnNext(created -> view.showLoading())
        .flatMapSingle(created -> commentsListManager.loadComments())
        .observeOn(viewScheduler)
        .doOnNext(this::showComments)
        .doOnError(throwable -> view.showGeneralError())
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(comments -> {
        }, crashReporter::log);
  }

  @VisibleForTesting public void clickComment() {
    view.getLifecycleEvent()
        .filter(lifecycleEvent -> lifecycleEvent.equals(View.LifecycleEvent.CREATE))
        .flatMap(created -> view.commentClick())
        .doOnNext(commentsNavigator::navigateToCommentView)
        .compose(view.bindUntilEvent(View.LifecycleEvent.DESTROY))
        .subscribe(commentId -> {
        }, throwable -> {
          throw new OnErrorNotImplementedException(throwable);
        });
  }

  private void showComments(CommentsListViewModel model) {
    if (!model.isLoading()) {
      view.showComments(model);
      view.hideLoading();
    }
  }
}
