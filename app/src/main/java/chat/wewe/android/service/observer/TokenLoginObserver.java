package chat.wewe.android.service.observer;

import android.content.Context;
import io.realm.Realm;
import io.realm.RealmResults;

import java.util.List;
import chat.wewe.android.api.MethodCallHelper;
import chat.wewe.android.helper.LogIfError;
import chat.wewe.persistence.realm.models.internal.RealmSession;
import chat.wewe.persistence.realm.RealmHelper;
import chat.wewe.android.service.DDPClientRef;

public class TokenLoginObserver extends AbstractModelObserver<RealmSession> {

  private final MethodCallHelper methodCall;

  public TokenLoginObserver(Context context, String hostname,
                            RealmHelper realmHelper, DDPClientRef ddpClientRef) {
    super(context, hostname, realmHelper, ddpClientRef);
    methodCall = new MethodCallHelper(realmHelper, ddpClientRef);
  }

  @Override
  public RealmResults<RealmSession> queryItems(Realm realm) {
    return realm.where(RealmSession.class)
        .isNotNull(RealmSession.TOKEN)
        .equalTo(RealmSession.TOKEN_VERIFIED, false)
        .isNull(RealmSession.ERROR)
        .findAll();

  }

  @Override
  public void onUpdateResults(List<RealmSession> results) {
    if (results.isEmpty()) {
      return;
    }

    RealmSession session = results.get(0);
    methodCall.loginWithToken(session.getToken()).continueWith(new LogIfError());
  }
}
