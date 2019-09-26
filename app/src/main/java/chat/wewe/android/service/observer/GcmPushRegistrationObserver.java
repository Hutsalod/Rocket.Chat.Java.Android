package chat.wewe.android.service.observer;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import android.content.Context;
import io.realm.Realm;
import io.realm.RealmResults;

import java.io.IOException;
import java.util.List;
import bolts.Task;
import chat.wewe.android.R;
import chat.wewe.android.RocketChatCache;
import chat.wewe.android.api.RaixPushHelper;
import chat.wewe.android.helper.LogIfError;
import chat.wewe.core.SyncState;
import chat.wewe.persistence.realm.models.ddp.RealmPublicSetting;
import chat.wewe.core.PublicSettingsConstants;
import chat.wewe.persistence.realm.models.ddp.RealmUser;
import chat.wewe.persistence.realm.models.internal.GcmPushRegistration;
import chat.wewe.persistence.realm.RealmHelper;
import chat.wewe.android.service.DDPClientRef;

/**
 * call raix:push-update if needed.
 */
public class GcmPushRegistrationObserver extends AbstractModelObserver<GcmPushRegistration> {
  public GcmPushRegistrationObserver(Context context, String hostname,
                                     RealmHelper realmHelper,
                                     DDPClientRef ddpClientRef) {
    super(context, hostname, realmHelper, ddpClientRef);
  }

  @Override
  public RealmResults<GcmPushRegistration> queryItems(Realm realm) {
    return GcmPushRegistration.queryDefault(realm)
        .equalTo(GcmPushRegistration.SYNC_STATE, SyncState.NOT_SYNCED)
        .equalTo(GcmPushRegistration.GCM_PUSH_ENABLED, true)
        .findAll();
  }

  @Override
  public void onUpdateResults(List<GcmPushRegistration> results) {
    if (results.isEmpty()) {
      return;
    }

    realmHelper.executeTransaction(realm -> {
      GcmPushRegistration.queryDefault(realm).findFirst().setSyncState(SyncState.SYNCING);
      return null;
    }).onSuccessTask(_task -> registerGcmTokenForServer()
    ).onSuccessTask(_task ->
        realmHelper.executeTransaction(realm -> {
          GcmPushRegistration.queryDefault(realm).findFirst().setSyncState(SyncState.SYNCED);
          return null;
        })
    ).continueWith(task -> {
      if (task.isFaulted()) {
        realmHelper.executeTransaction(realm -> {
          GcmPushRegistration.queryDefault(realm).findFirst().setSyncState(SyncState.FAILED);
          return null;
        }).continueWith(new LogIfError());
      }
      return null;
    });
  }

  private Task<Void> registerGcmTokenForServer() throws IOException {
    final String gcmToken = getGcmToken(getSenderId());
    final RealmUser currentUser = realmHelper.executeTransactionForRead(realm ->
        RealmUser.queryCurrentUser(realm).findFirst());
    final String userId = currentUser != null ? currentUser.getId() : null;
    final String pushId = new RocketChatCache(context).getOrCreatePushId();

    return new RaixPushHelper(realmHelper, ddpClientRef)
        .pushUpdate(pushId, gcmToken, userId);
  }

  private String getGcmToken(String senderId) throws IOException {
    return InstanceID.getInstance(context)
        .getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
  }

  private String getSenderId() {
    final String senderId = RealmPublicSetting
        .getString(realmHelper, PublicSettingsConstants.Push.GCM_PROJECT_NUMBER, "").trim();

    if (senderId.length() != 0) {
      return senderId;
    }

    return context.getString(R.string.gcm_sender_id);
  }

}
