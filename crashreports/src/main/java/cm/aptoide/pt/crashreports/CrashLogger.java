package cm.aptoide.pt.crashreports;

/**
 * Created by neuro on 09-12-2016.
 */

public interface CrashLogger {

  /**
   * logs exception in crashes
   *
   * @param throwable exception you want to send
   */
  void logException(Throwable throwable);

  /**
   * logs string in crashes
   *
   * @param key unique key to send on crash
   * @param value value you want associated with the key
   */
  void logString(String key, String value);
}
