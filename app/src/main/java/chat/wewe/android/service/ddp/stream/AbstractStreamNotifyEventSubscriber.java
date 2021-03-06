package chat.wewe.android.service.ddp.stream;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import chat.wewe.android.helper.LogIfError;
import chat.wewe.android.log.RCLog;
import chat.wewe.persistence.realm.RealmHelper;
import chat.wewe.android.service.DDPClientRef;
import chat.wewe.android.service.ddp.AbstractDDPDocEventSubscriber;
import chat.wewe.android_ddp.DDPSubscription;

abstract class AbstractStreamNotifyEventSubscriber extends AbstractDDPDocEventSubscriber {
  protected AbstractStreamNotifyEventSubscriber(Context context, String hostname,
                                                RealmHelper realmHelper,
                                                DDPClientRef ddpClientRef) {
    super(context, hostname, realmHelper, ddpClientRef);
  }

  @Override
  protected final boolean shouldTruncateTableOnInitialize() {
    return false;
  }

  @Override
  protected final boolean isTarget(String callbackName) {
    return getSubscriptionName().equals(callbackName);
  }

  protected abstract String getSubscriptionParam();

  @Override
  protected final JSONArray getSubscriptionParams() throws JSONException {
    return new JSONArray().put(getSubscriptionParam()).put(false);
  }

  protected abstract String getPrimaryKeyForModel();

  @Override
  protected final void onDocumentAdded(DDPSubscription.Added docEvent) {
    // do nothing.
  }

  @Override
  protected final void onDocumentRemoved(DDPSubscription.Removed docEvent) {
    // do nothing.
  }

  @Override
  protected final void onDocumentChanged(DDPSubscription.Changed docEvent) {
    try {
      if (!docEvent.fields.getString("eventName").equals(getSubscriptionParam())) {
        return;
      }

      handleArgs(docEvent.fields.getJSONArray("args"));
    } catch (Exception exception) {
      RCLog.w(exception, "failed to save stream-notify event.");
    }
  }

  protected void handleArgs(JSONArray args) throws JSONException {
    String msg = args.length() > 0 ? args.getString(0) : null;
    JSONObject target = args.getJSONObject(args.length() - 1);
    if ("removed".equals(msg)) {
      realmHelper.executeTransaction(realm ->
          realm.where(getModelClass())
              .equalTo(getPrimaryKeyForModel(), target.getString(getPrimaryKeyForModel()))
              .findAll().deleteAllFromRealm()
      ).continueWith(new LogIfError());
    } else { //inserted, updated
      realmHelper.executeTransaction(realm ->
          realm.createOrUpdateObjectFromJson(getModelClass(), customizeFieldJson(target))
      ).continueWith(new LogIfError());
    }
  }
}
