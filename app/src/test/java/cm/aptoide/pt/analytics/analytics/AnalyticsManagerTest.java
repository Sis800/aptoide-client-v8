package cm.aptoide.pt.analytics.analytics;

import cm.aptoide.analytics.AnalyticsManager;
import cm.aptoide.analytics.EventLogger;
import cm.aptoide.analytics.implementation.AnalyticsEventParametersNormalizer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AnalyticsManagerTest {
  @Test public void sendSupportedEvent() throws Exception {
    String eventName = "TestEvent";
    EventLogger eventLogger = mock(EventLogger.class);
    HttpKnockEventLogger knockEventLogger = mock(HttpKnockEventLogger.class);
    AnalyticsEventParametersNormalizer analyticsNormalizer =
        mock(AnalyticsEventParametersNormalizer.class);

    AnalyticsManager analyticsManager =
        new AnalyticsManager.Builder().addLogger(eventLogger, Arrays.asList(eventName))
            .setKnockLogger(knockEventLogger)
            .setAnalyticsNormalizer(analyticsNormalizer)
            .build();

    Map<String, Object> data = new HashMap<>();
    AnalyticsManager.Action action = AnalyticsManager.Action.OPEN;
    String context = "timeline";
    analyticsManager.logEvent(data, eventName, action, context);
    verify(eventLogger).log(eventName, data, action, context);
  }

  @Test public void sendUnsupportedEvent() throws Exception {
    String eventName = "TestEvent";
    EventLogger eventLogger = mock(EventLogger.class);
    HttpKnockEventLogger knockEventLogger = mock(HttpKnockEventLogger.class);
    AnalyticsEventParametersNormalizer analyticsNormalizer =
        mock(AnalyticsEventParametersNormalizer.class);

    AnalyticsManager analyticsManager =
        new AnalyticsManager.Builder().addLogger(eventLogger, Arrays.asList(eventName))
            .setKnockLogger(knockEventLogger)
            .setAnalyticsNormalizer(analyticsNormalizer)
            .build();

    Map<String, Object> data = new HashMap<>();
    AnalyticsManager.Action action = AnalyticsManager.Action.OPEN;
    String context = "timeline";
    analyticsManager.logEvent(data, "Unsupported event", action, context);
    verify(eventLogger, times(0)).log(eventName, data, action, context);
  }
}