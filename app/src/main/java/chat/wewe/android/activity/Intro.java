package chat.wewe.android.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import chat.wewe.android.R;
import chat.wewe.android.Success;
import chat.wewe.android.api.BaseApiService;
import chat.wewe.android.api.UtilsApiChat;
import chat.wewe.android.services.MyFirebaseMessagingService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Intro extends AppCompatActivity {


    private static final String TAG = "Intro";
    private CountDownTimer countDownTimer;
    public static String UF_SIP_NUMBER,UF_SIP_PASSWORD,TOKEN_RC,TOKENWE;
    public static int callstatic = 0,callCout=0,StatusU = 4;
    public static boolean callSet,subscription;
    SharedPreferences SipData;
    BaseApiService mApiServiceChat;
    public static String[]ListGetStatus = new String[1];
    public int tabIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro);
        SipData = getSharedPreferences("SIP", MODE_PRIVATE);
        mApiServiceChat = UtilsApiChat.getAPIService();
        getListStatus();
        SharedPreferences.Editor ed =  SipData.edit();
        ed.commit();
        UF_SIP_NUMBER = SipData.getString("UF_SIP_NUMBER", null);
        ed.commit();
        UF_SIP_PASSWORD = SipData.getString("UF_SIP_PASSWORD", null);
        TOKENWE = SipData.getString("TOKENWE", null);

        Intent intent = getIntent();
        tabIndex = intent.getIntExtra(MyFirebaseMessagingService.EXTRA_TAB_INDEX, 0);

        if (tabIndex==1)
            finish();
        startActivity(new Intent(getApplicationContext(), MainActivity.class));


            countDownTimer = new CountDownTimer(2000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    startActivity(new Intent(getApplicationContext(), PrivaryPolicy.class));
                    finish();
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
}