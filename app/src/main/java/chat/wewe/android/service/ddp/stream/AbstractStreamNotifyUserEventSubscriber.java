package chat.wewe.android.service.ddp.stream;

import android.content.Context;

import chat.wewe.persistence.realm.RealmHelper;
import chat.wewe.android.service.DDPClientRef;

abstract class AbstractStreamNotifyUserEventSubscriber extends AbstractStreamNotifyEventSubscriber {
  protected final String userId;

  protected AbstractStreamNotifyUserEventSubscriber(Context context, String hostname,
                                                    RealmHelper realmHelper,
                                                    DDPClientRef ddpClientRef, String userId) {
    super(context, hostname, realmHelper, ddpClientRef);
    this.userId = userId;
  }

  @Override
  protected final String getSubscriptionName() {
    return "stream-notify-user";
  }

  @Override
  protected final String getSubscriptionParam() {
    return userId + "/" + getSubscriptionSubParam();
  }

  protected abstract String getSubscriptionSubParam();
}
