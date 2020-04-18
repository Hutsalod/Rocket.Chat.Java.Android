package chat.wewe.android.activity;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import chat.wewe.android.R;
import chat.wewe.android.Success;
import chat.wewe.android.api.BaseApiService;
import chat.wewe.android.api.UtilsApiChat;
import chat.wewe.android.service.PortSipService;
import chat.wewe.android.services.MyFirebaseMessagingService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Notification.VISIBILITY_PUBLIC;

public class Intro extends AppCompatActivity {
    private static final int NOTIFICATION_ID = 200;

    private static final String TAG = "Intro";
    private CountDownTimer countDownTimer;
    public static String UF_SIP_NUMBER,UF_SIP_PASSWORD,TOKEN_RC,TOKENWE;
    public static int callstatic = 0,callCout=0,StatusU = 4;
    public static boolean callSet,subscription;
    SharedPreferences SipData,sPrefs,sPref;
    BaseApiService mApiServiceChat;
    public static String[]ListGetStatus = new String[1];
    String code;
    private static final String CHANNEL_ID = "myChannel";
    private static final String CHANNEL_NAME = "WeWe";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.intro);





        SipData = getSharedPreferences("SIP", MODE_PRIVATE);
        mApiServiceChat = UtilsApiChat.getAPIService();
        getListStatus();
        UF_SIP_NUMBER = SipData.getString("UF_SIP_NUMBER", null);
        UF_SIP_PASSWORD = SipData.getString("UF_SIP_PASSWORD", null);
        TOKENWE = SipData.getString("TOKENWE", null);
        sPrefs = getSharedPreferences("pin", MODE_PRIVATE);
        code = sPrefs.getString("code", "");
        sPref = getSharedPreferences("Setting", MODE_PRIVATE);
        setLocale(SipData.getString("LANG_APP", ""));

        //SaveUserInfo();

        Log.d("getListStatusq", "NAME " + SipData.getString("LANG_APP", ""));
            countDownTimer = new CountDownTimer(800, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    cancel();
                    finish();

                    if(sPref.getInt("privary", '0')=='1'){
                        if(code=="") {
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        } else{
                            startActivity(new Intent(getApplicationContext(), PinCodeLong.class));
                        }
                    }else{
                        startActivity(new Intent(getApplicationContext(), PrivaryPolicy.class));
                    }

                }
            };
            countDownTimer.start();

    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                    return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }
    public void getListStatus(){
        mApiServiceChat.getListStatus()
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            try {
                                JSONObject jsonRESULTS = new JSONObject(response.body().string());
                                JSONArray values = jsonRESULTS.getJSONArray("users");
                                ListGetStatus = new String[40];
                                int s = 0;
                                for (int i = 0; i < values.length(); i++) {
                                    JSONObject jsonobject = values .getJSONObject(i);
                                    if(jsonobject.getString("status").equals("online")) {
                                        ListGetStatus[++s] = jsonobject.getString("username");
                                        Log.d("getListStatus", "NAME " + jsonobject.getString("username")+"="+i);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                    }
                });


    }

    public void setLocale(String language_code){
        Activity activity = this;
        Resources res = activity.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        android.content.res.Configuration conf = res.getConfiguration();
        conf.locale = new Locale(language_code.toLowerCase());
        res.updateConfiguration(conf, dm);
    }

    public void SaveUserInfo() {

        Intent onLineIntent = new Intent(getApplicationContext(), PortSipService.class);
        onLineIntent.putExtra(PortSipService.EXTRA_PUSHTOKEN, FirebaseInstanceId.getInstance().getToken());
        onLineIntent.setAction(PortSipService.ACTION_SIP_REGIEST);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplicationContext().startForegroundService(onLineIntent);
        }else{
            getApplicationContext().startService(onLineIntent);
        }
    }
}