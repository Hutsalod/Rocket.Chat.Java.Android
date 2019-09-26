package chat.wewe.android.service.ddp.base;

import android.content.Context;
import org.json.JSONArray;

import chat.wewe.persistence.realm.RealmHelper;
import chat.wewe.android.service.DDPClientRef;
import chat.wewe.android.service.ddp.AbstractDDPDocEventSubscriber;

abstract class AbstractBaseSubscriber extends AbstractDDPDocEventSubscriber {
  protected AbstractBaseSubscriber(Context context, String hostname, RealmHelper realmHelper,
                                   DDPClientRef ddpClientRef) {
    super(context, hostname, realmHelper, ddpClientRef);
  }

  @Override
  protected final JSONArray getSubscriptionParams() {
    return null;
  }

  @Override
  protected final boolean shouldTruncateTableOnInitialize() {
    return true;
  }

  protected abstract String getSubscriptionCallbackName();

  @Override
  protected final boolean isTarget(String callbackName) {
    return getSubscriptionCallbackName().equals(callbackName);
  }
}
