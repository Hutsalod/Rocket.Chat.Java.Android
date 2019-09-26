package chat.wewe.android.service.observer;

import android.content.Context;
import io.realm.Realm;
import io.realm.RealmResults;
import org.json.JSONObject;

import java.util.List;
import chat.wewe.android.api.MethodCallHelper;
import chat.wewe.android.helper.LogIfError;
import chat.wewe.android.log.RCLog;
import chat.wewe.core.SyncState;
import chat.wewe.persistence.realm.models.ddp.RealmMessage;
import chat.wewe.persistence.realm.RealmHelper;
import chat.wewe.android.service.DDPClientRef;

/**
 * Observe messages for sending.
 */
public class NewMessageObserver extends AbstractModelObserver<RealmMessage> {

  private final MethodCallHelper methodCall;

  public NewMessageObserver(Context context, String hostname, RealmHelper realmHelper, DDPClientRef ddpClientRef) {
    super(context, hostname, realmHelper, ddpClientRef);
    methodCall = new MethodCallHelper(realmHelper, ddpClientRef);

    realmHelper.executeTransaction(realm -> {
      // resume pending operations.
      RealmResults<RealmMessage> pendingMethodCalls = realm.where(RealmMessage.class)
          .equalTo(RealmMessage.SYNC_STATE, SyncState.SYNCING)
          .findAll();
      for (RealmMessage message : pendingMethodCalls) {
        message.setSyncState(SyncState.NOT_SYNCED);
      }

      return null;
    }).continueWith(new LogIfError());
  }

  @Override
  public RealmResults<RealmMessage> queryItems(Realm realm) {
    return realm.where(RealmMessage.class)
        .equalTo(RealmMessage.SYNC_STATE, SyncState.NOT_SYNCED)
        .isNotNull(RealmMessage.ROOM_ID)
        .findAll();
  }

  @Override
  public void onUpdateResults(List<RealmMessage> results) {
    if (results.isEmpty()) {
      return;
    }

    final RealmMessage message = results.get(0);
    final String messageId = message.getId();
    final String roomId = message.getRoomId();
    final String msg = message.getMessage();
    final long editedAt = message.getEditedAt();

    realmHelper.executeTransaction(realm ->
        realm.createOrUpdateObjectFromJson(RealmMessage.class, new JSONObject()
            .put(RealmMessage.ID, messageId)
            .put(RealmMessage.SYNC_STATE, SyncState.SYNCING)
        )
    ).onSuccessTask(task -> methodCall.sendMessage(messageId, roomId, msg, editedAt)
    ).continueWith(task -> {
      if (task.isFaulted()) {
        RCLog.w(task.getError());
        realmHelper.executeTransaction(realm ->
            realm.createOrUpdateObjectFromJson(RealmMessage.class, new JSONObject()
                .put(RealmMessage.ID, messageId)
                .put(RealmMessage.SYNC_STATE, SyncState.FAILED)));
      } else {
        realmHelper.executeTransaction(realm ->
                realm.createOrUpdateObjectFromJson(RealmMessage.class, new JSONObject()
                        .put(RealmMessage.ID, messageId)
                        .put(RealmMessage.SYNC_STATE, SyncState.SYNCED)));
      }
      return null;
    });
  }
}
