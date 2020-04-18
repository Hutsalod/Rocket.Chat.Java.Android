package chat.wewe.android.service;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import static chat.wewe.android.service.PortSipService.ACTION_PUSH_MESSAGE;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private int notifyID = 0;

    public void handleIntent(Intent intent){
        String action = intent.getAction();
        if(action.equals("com.google.android.c2dm.intent.RECEIVE")&&intent.getStringExtra("message_type")==null) {
            Bundle bundle = intent.getExtras();

            if ("call".equals(bundle.getString("msg_type")))
            {
                Intent srvIntent = new Intent(this, PortSipService.class);
                srvIntent.setAction(ACTION_PUSH_MESSAGE);
                startService(srvIntent);
            }

            if ("im".equals(bundle.getString("msg_type")))
            {
                String content = bundle.getString("msg_content");
                String from = bundle.getString("send_from");
                String to = bundle.getString("send_to");
                String pushid = bundle.getString("portsip-push-id");
                Intent srvIntent = new Intent(this, PortSipService.class);
                srvIntent.setAction(ACTION_PUSH_MESSAGE);
                startService(srvIntent);
            }
        }
    }
}
