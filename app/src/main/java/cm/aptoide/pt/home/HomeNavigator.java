package cm.aptoide.pt.home;

import cm.aptoide.pt.app.view.AppViewFragment;
import cm.aptoide.pt.navigator.FragmentNavigator;
import cm.aptoide.pt.view.app.Application;

/**
 * Created by jdandrade on 13/03/2018.
 */

public class HomeNavigator {
  private final FragmentNavigator fragmentNavigator;

  public HomeNavigator(FragmentNavigator fragmentNavigator) {
    this.fragmentNavigator = fragmentNavigator;
  }

  public void navigateToAppView(Application app) {
    fragmentNavigator.navigateTo(AppViewFragment.newInstance(app.getAppId(), app.getPackageName(),
        AppViewFragment.OpenType.OPEN_ONLY, ""), true);
  }
}
