
package chat.wewe.android.service;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;

import chat.wewe.android.helper.GcmPushSettingHelper;
import chat.wewe.core.models.ServerInfo;
import chat.wewe.persistence.realm.RealmHelper;
import chat.wewe.persistence.realm.RealmStore;
import chat.wewe.persistence.realm.models.ddp.RealmPublicSetting;
import chat.wewe.persistence.realm.models.internal.GcmPushRegistration;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIdService";
    private static final String TOPIC_GLOBAL = "global";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        // now subscribe to `global` topic to receive app wide notifications
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_GLOBAL);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.

        sendRegistrationToServer(refreshedToken);
    }
    // [END refresh_token]

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        Intent srvIntent = new Intent(this, PortSipService.class);
        srvIntent.setAction(PortSipService.ACTION_PUSH_TOKEN);
        srvIntent.putExtra(PortSipService.EXTRA_PUSHTOKEN, token);
        startService(srvIntent);
    }

}

