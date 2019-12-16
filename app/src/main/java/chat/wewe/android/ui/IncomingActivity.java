package chat.wewe.android.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.portsip.PortSipErrorcode;
import chat.wewe.android.R;
import chat.wewe.android.RocketChatApplication;
import chat.wewe.android.activity.MyAdmin;
import chat.wewe.android.receiver.PortMessageReceiver;
import chat.wewe.android.service.PortSipService;
import chat.wewe.android.util.CallManager;
import chat.wewe.android.util.Ring;
import chat.wewe.android.util.Session;

public class IncomingActivity extends Activity implements PortMessageReceiver.BroadcastListener, View.OnClickListener {

    public PortMessageReceiver receiver = null;
    RocketChatApplication application;
    TextView tips;
    Button btnVideo;
    long mSessionid;
    boolean exit = false;

    static final int RESULT_ENABLE = 1;

    DevicePolicyManager deviceManger;
    ActivityManager activityManager;
    ComponentName compName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        deviceManger = (DevicePolicyManager)getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        activityManager = (ActivityManager)getSystemService(
                Context.ACTIVITY_SERVICE);
        compName = new ComponentName(this, MyAdmin.class);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.incomingview);


        tips = findViewById(R.id.sessiontips);
        btnVideo = findViewById(R.id.answer_video);
        receiver = new PortMessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PortSipService.REGISTER_CHANGE_ACTION);
        filter.addAction(PortSipService.CALL_CHANGE_ACTION);
        filter.addAction(PortSipService.PRESENCE_CHANGE_ACTION);
        registerReceiver(receiver, filter);
        receiver.broadcastReceiver =this;
        Intent intent = getIntent();
        mSessionid = intent.getLongExtra("incomingSession", PortSipErrorcode.INVALID_SESSION_ID);
        Session session = CallManager.Instance().findSessionBySessionID(mSessionid);
        if(mSessionid==PortSipErrorcode.INVALID_SESSION_ID||session ==null||session.state!= Session.CALL_STATE_FLAG.INCOMING){
            this.finish();
        }

        application = (RocketChatApplication) getApplication();
        tips.setText(session.displayName);
        setVideoAnswerVisibility(session);

        findViewById(R.id.hangup_call).setOnClickListener(this);
		findViewById(R.id.answer_audio).setOnClickListener(this);
        btnVideo.setOnClickListener(this);

        boolean active = deviceManger.isAdminActive(compName);
        if (active) {
            deviceManger.lockNow();
        }
        Intent intents = new Intent(DevicePolicyManager
                .ACTION_ADD_DEVICE_ADMIN);
        intents.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                compName);
        intents.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                compName);
        intents.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Additional text explaining why this needs to be added.");
        startActivityForResult(intents, RESULT_ENABLE);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        long sessionid = intent.getLongExtra("incomingSession", PortSipErrorcode.INVALID_SESSION_ID);
        Session session = CallManager.Instance().findSessionBySessionID(sessionid);
        if(mSessionid!=PortSipErrorcode.INVALID_SESSION_ID&&session !=null){
            mSessionid = sessionid;
            setVideoAnswerVisibility(session);
            tips.setText(session.lineName+"   "+session.remote );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        if(exit==true)
        startActivity(new Intent(this,MainActivity.class));
    }

    @Override
    public void onBroadcastReceiver(Intent intent) {
        String action = intent.getAction();
        if (PortSipService.CALL_CHANGE_ACTION.equals(action))
        {
            long sessionId = intent.getLongExtra(PortSipService.EXTRA_CALL_SEESIONID, Session.INVALID_SESSION_ID);
            String status = intent.getStringExtra(PortSipService.EXTRA_CALL_DESCRIPTION);
            Session session = CallManager.Instance().findSessionBySessionID(sessionId);
            if (session != null)
            {
                switch (session.state)
                {
                    case INCOMING:
                        break;
                    case TRYING:
                        break;
                    case CONNECTED:
                    case FAILED:
                    case CLOSED:
                        Session anOthersession = CallManager.Instance().findIncomingCall();
                        if(anOthersession==null) {
                            this.finish();
                        }else{
                            setVideoAnswerVisibility(anOthersession);
                            tips.setText(anOthersession.displayName);
                            mSessionid = anOthersession.sessionID;
                        }
                        break;

                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        if(application.mEngine!=null){

            Session currentLine = CallManager.Instance().findSessionBySessionID(mSessionid);
            switch (view.getId()){
                case R.id.answer_audio:
                case R.id.answer_video:
                    if (currentLine.state != Session.CALL_STATE_FLAG.INCOMING) {
                    //    Toast.makeText(this,currentLine.lineName + "No incoming call on current line",Toast.LENGTH_SHORT);
                        return;
                    }
                    Ring.getInstance(this).stopRingTone();
                    currentLine.state = Session.CALL_STATE_FLAG.CONNECTED;
                    application.mEngine.answerCall(mSessionid,view.getId()==R.id.answer_video);
                    if(application.mConference){
                        application.mEngine.joinToConference(currentLine.sessionID);
                    }
                    exit=true;
                    break;
                case R.id.hangup_call:
                    Ring.getInstance(this).stop();
                    if (currentLine.state == Session.CALL_STATE_FLAG.INCOMING) {
                        application.mEngine.rejectCall(currentLine.sessionID, 486);
                        currentLine.Reset();
                       // Toast.makeText(this,currentLine.lineName + ": Rejected call",Toast.LENGTH_SHORT);
                    }
                    break;
            }
        }

        Session anOthersession = CallManager.Instance().findIncomingCall();
        if(anOthersession==null) {
            this.finish();
        }else{
            mSessionid = anOthersession.sessionID;
            setVideoAnswerVisibility(anOthersession);
        }

    }
	
	private void setVideoAnswerVisibility(Session session){
		if(session == null)
			return;
		if(session.hasVideo){
            btnVideo.setVisibility(View.VISIBLE);
        }else{
            btnVideo.setVisibility(View.GONE);
        }
	}
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_ENABLE:
                if (resultCode == Activity.RESULT_OK) {
                    Log.i("DeviceAdminSample", "Admin enabled!");
                } else {
                    Log.i("DeviceAdminSample", "Admin enable FAILED!");
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
