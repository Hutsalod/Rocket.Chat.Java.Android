
package chat.wewe.android.service;
import android.content.Intent;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.List;

import chat.wewe.android.helper.GcmPushSettingHelper;
import chat.wewe.core.models.ServerInfo;
import chat.wewe.persistence.realm.RealmHelper;
import chat.wewe.persistence.realm.RealmStore;
import chat.wewe.persistence.realm.models.ddp.RealmPublicSetting;
import chat.wewe.persistence.realm.models.internal.GcmPushRegistration;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        List<ServerInfo> serverInfoList = ConnectivityManager.getInstance(getApplicationContext())
                .getServerList();
        for (ServerInfo serverInfo : serverInfoList) {
            RealmHelper realmHelper = RealmStore.get(serverInfo.getHostname());
            if (realmHelper != null) {
                updateGcmToken(realmHelper);
            }
        }
    }

    private void updateGcmToken(RealmHelper realmHelper) {
        final List<RealmPublicSetting> results = realmHelper.executeTransactionForReadResults(
                GcmPushSettingHelper::queryForGcmPushEnabled);
        final boolean gcmPushEnabled = GcmPushSettingHelper.isGcmPushEnabled(results);

        if (gcmPushEnabled) {
            realmHelper.executeTransaction(realm ->
                    GcmPushRegistration.updateGcmPushEnabled(realm, gcmPushEnabled));
        }
    }
}
