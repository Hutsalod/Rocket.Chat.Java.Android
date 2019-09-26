package chat.wewe.android.service.ddp.stream;

import android.content.Context;
import io.realm.RealmObject;
import org.json.JSONException;
import org.json.JSONObject;

import chat.wewe.persistence.realm.models.ddp.RealmRoom;
import chat.wewe.persistence.realm.RealmHelper;
import chat.wewe.android.service.DDPClientRef;

public class StreamNotifyUserSubscriptionsChanged extends AbstractStreamNotifyUserEventSubscriber {
  public StreamNotifyUserSubscriptionsChanged(Context context, String hostname,
                                              RealmHelper realmHelper, DDPClientRef ddpClientRef,
                                              String userId) {
    super(context, hostname, realmHelper, ddpClientRef, userId);
  }

  @Override
  protected String getSubscriptionSubParam() {
    return "subscriptions-changed";
  }

  @Override
  protected Class<? extends RealmObject> getModelClass() {
    return RealmRoom.class;
  }

  @Override
  protected JSONObject customizeFieldJson(JSONObject json) throws JSONException {
    return RealmRoom.customizeJson(super.customizeFieldJson(json));
  }

  @Override
  protected String getPrimaryKeyForModel() {
    return "rid";
  }
}
