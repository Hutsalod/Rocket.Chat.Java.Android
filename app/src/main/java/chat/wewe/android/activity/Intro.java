package chat.wewe.android.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import chat.wewe.android.R;
import chat.wewe.android.Success;

public class Intro extends AppCompatActivity {
    private static final String TAG = "Intro";
    private CountDownTimer countDownTimer;
    public static String UF_userId,UF_authToken,UF_SIP_NUMBER,UF_SIP_PASSWORD,presence,TOKEN_RC;
    public static int callstatic = 0,callCout=0;
    public static boolean callSet;
    SharedPreferences SipData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro);
        SipData = getSharedPreferences("SIP", MODE_PRIVATE);
        SharedPreferences.Editor ed =  SipData.edit();
        ed.commit();
        UF_SIP_NUMBER = SipData.getString("UF_SIP_NUMBER", null);
        ed.commit();
        UF_SIP_PASSWORD = SipData.getString("UF_SIP_PASSWORD", null);


            countDownTimer = new CountDownTimer(3000, 1000) {
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

}