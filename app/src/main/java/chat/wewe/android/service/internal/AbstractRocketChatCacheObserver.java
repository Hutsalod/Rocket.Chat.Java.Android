package chat.wewe.android.service.internal;

import android.content.Context;

import io.reactivex.disposables.CompositeDisposable;

import chat.wewe.android.RocketChatCache;
import chat.wewe.android.helper.TextUtils;
import chat.wewe.persistence.realm.models.ddp.RealmRoom;
import chat.wewe.persistence.realm.RealmHelper;
import chat.wewe.android.service.Registrable;

public abstract class AbstractRocketChatCacheObserver implements Registrable {
  private final Context context;
  private final RealmHelper realmHelper;
  private String roomId;
  private CompositeDisposable compositeDisposable = new CompositeDisposable();

  protected AbstractRocketChatCacheObserver(Context context, RealmHelper realmHelper) {
    this.context = context;
    this.realmHelper = realmHelper;
  }

  private void updateRoomIdWith(String roomId) {
    if (!TextUtils.isEmpty(roomId)) {
      RealmRoom room = realmHelper.executeTransactionForRead(realm ->
          realm.where(RealmRoom.class).equalTo("rid", roomId).findFirst());
      if (room != null) {
        if (this.roomId == null || !this.roomId.equals(roomId)) {
          this.roomId = roomId;
          onRoomIdUpdated(roomId);
        }
        return;
      }
    }

    if (this.roomId != null) {
      this.roomId = null;
      onRoomIdUpdated(null);
    }
  }

  protected abstract void onRoomIdUpdated(String roomId);

  @Override
  public final void register() {
    compositeDisposable.add(
        new RocketChatCache(context)
            .getSelectedRoomIdPublisher()
            .subscribe(this::updateRoomIdWith)
    );
  }

  @Override
  public final void unregister() {
    compositeDisposable.clear();
  }
}
