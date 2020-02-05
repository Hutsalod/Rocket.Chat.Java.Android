package chat.wewe.android.services;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import chat.wewe.android.activity.Intro;
import chat.wewe.android.activity.MainActivity;
import chat.wewe.android.fragment.sidebar.dialog.AddChannelDialogFragment;
import chat.wewe.android.service.PortSipService;
import chat.wewe.android.utils.NotificationUtils;
import chat.wewe.android.vo.NotificationVO;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import static android.view.View.VISIBLE;
import static chat.wewe.android.activity.Intro.UF_SIP_NUMBER;
import static chat.wewe.android.activity.Intro.UF_SIP_PASSWORD;
import static chat.wewe.android.service.PortSipService.ACTION_PUSH_MESSAGE;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgingService";
    private static final String TITLE = "title";
    private static final String EMPTY = "";
    private static final String MESSAGE = "message";
    private static final String IMAGE = "image";
    private static final String ACTION = "action";
    private static final String DATA = "notification";
    private static final String ACTION_DESTINATION = "action_destination";


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            Map<String, String> data = remoteMessage.getData();
           handleData(data);



        } else if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            handleNotification(remoteMessage.getNotification());
        }// Check if message contains a notification payload.




    }



    private void handleNotification(RemoteMessage.Notification RemoteMsgNotification) {
        String message = RemoteMsgNotification.getBody();
        String title = RemoteMsgNotification.getTitle();
        NotificationVO notificationVO = new NotificationVO();
        notificationVO.setTitle(title);
        notificationVO.setMessage(message);

        Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
        NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
        notificationUtils.displayNotification(notificationVO, resultIntent);

    }

    private void handleData(Map<String, String> data) {

    //    if(MainActivity.active) {



            String title = data.get(TITLE);
            String message = data.get(MESSAGE);
            String iconUrl = data.get(IMAGE);
            String action = data.get(ACTION);
            String actionDestination = data.get(ACTION_DESTINATION);



            NotificationVO notificationVO = new NotificationVO();
            notificationVO.setTitle(title);
            notificationVO.setMessage(message);
            notificationVO.setIconUrl(iconUrl);
            notificationVO.setAction(action);


            notificationVO.setActionDestination(actionDestination);

            Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
            resultIntent.putExtra("startRoom",true);
            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());

            notificationUtils.displayNotification(notificationVO, resultIntent);

      if(message.equals("Видеозвонок") || message.equals("Аудеозвонок")) {
         //   startActivity(new Intent(getApplicationContext(), Intro.class));
          SaveUserInfo();
          Intent onLineIntent = new Intent(getBaseContext(), PortSipService.class);
          onLineIntent.setAction(PortSipService.ACTION_SIP_REGIEST);

          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
              getBaseContext().startForegroundService(onLineIntent);
          }else{
              getBaseContext().startService(onLineIntent);
          }
      }
    }
    public void SaveUserInfo() {
        SharedPreferences SipData = getSharedPreferences("SIP", MODE_PRIVATE);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplication()).edit();
        UF_SIP_NUMBER = SipData.getString("UF_SIP_NUMBER", null);
        UF_SIP_PASSWORD = SipData.getString("UF_SIP_PASSWORD", null);
        String UF_SIP_SERVER = SipData.getString("UF_SIP_SERVER", "sip.weltwelle.com");
        editor.putString(PortSipService.USER_NAME, UF_SIP_NUMBER);
        editor.putString(PortSipService.USER_PWD, UF_SIP_PASSWORD);
        editor.putString(PortSipService.SVR_HOST, UF_SIP_SERVER);
        editor.putString(PortSipService.SVR_PORT, "5061");

        editor.putString(PortSipService.USER_DISPALYNAME, null);
        editor.putString(PortSipService.USER_DOMAIN, null);
        editor.putString(PortSipService.USER_AUTHNAME, null);
        editor.putString(PortSipService.STUN_HOST, null);
        editor.putString(PortSipService.STUN_PORT, "3478");

        editor.commit();
    }

}
