package chat.wewe.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.portsip.PortSIPVideoRenderer;
import com.portsip.PortSipErrorcode;
import com.portsip.PortSipSdk;

import java.util.Timer;
import java.util.TimerTask;

import chat.wewe.android.R;
import chat.wewe.android.RocketChatApplication;
import chat.wewe.android.receiver.PortMessageReceiver;
import chat.wewe.android.service.PortSipService;
import chat.wewe.android.util.CallManager;
import chat.wewe.android.util.Ring;
import chat.wewe.android.util.Session;

import static chat.wewe.android.activity.Intro.callCout;
import static chat.wewe.android.activity.MainActivity.current_user_name;

public class VideoFragment extends BaseFragment implements View.OnClickListener ,PortMessageReceiver.BroadcastListener{
	RocketChatApplication application;
	MainActivity activity;
	int t = 0,m;
	String c;
	private PortSIPVideoRenderer remoteRenderScreen = null;
	private PortSIPVideoRenderer localRenderScreen = null;
	private PortSIPVideoRenderer.ScalingType scalingType = PortSIPVideoRenderer.ScalingType.SCALE_ASPECT_BALANCED;// SCALE_ASPECT_FIT or SCALE_ASPECT_FILL;
	private ImageView imgSwitchCamera = null;
	private ImageView imgScaleType = null,finish_car= null,ic_video_call= null,ic_audio_call= null;

	public TextView usersName,statucConnect;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		activity = (MainActivity)getActivity();
		application = (RocketChatApplication)activity.getApplication();

		return inflater.inflate(R.layout.video, container, false);


	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		imgSwitchCamera = (ImageView)view.findViewById(R.id.ibcamera);
		imgScaleType = (ImageView)view.findViewById(R.id.ibscale);
		finish_car = (ImageView)view.findViewById(R.id.finish_car);
		ic_video_call = (ImageView)view.findViewById(R.id.ic_video_call);
		ic_audio_call = (ImageView)view.findViewById(R.id.ic_audio_call);
		Timer time = new Timer();
		imgScaleType.setOnClickListener(this);
		imgSwitchCamera.setOnClickListener(this);
		finish_car.setOnClickListener(this);
		ic_video_call.setOnClickListener(this);
		ic_audio_call.setOnClickListener(this);

		usersName = (TextView) view.findViewById(R.id.usersName);
		usersName.setText("" +current_user_name.getText());
		statucConnect = (TextView)view.findViewById(R.id.statucConnect);

		localRenderScreen = (PortSIPVideoRenderer)view.findViewById(R.id.local_video_view);
		remoteRenderScreen = (PortSIPVideoRenderer)view.findViewById(R.id.remote_video_view);

		scalingType = PortSIPVideoRenderer.ScalingType.SCALE_ASPECT_FIT;//
		remoteRenderScreen.setScalingType(scalingType);
		activity.receiver.broadcastReceiver =this ;


		Handler handler = new Handler();
				handler.post(new Runnable() {
					@Override
					public void run() {
						++t;
						if(t>59){
							t=0;
							++m;
						}
						statucConnect.setText(""+m+":"+((t>=10)? "" : "0")+t);
						handler.postDelayed(this, 1000);
					}
				});
	}


	@Override
	public void onDestroyView() {
		super.onDestroyView();

		PortSipSdk portSipLib = application.mEngine;
		if(localRenderScreen!=null){
			if(portSipLib!=null) {
				portSipLib.displayLocalVideo(false);
			}
			localRenderScreen.release();
		}

		CallManager.Instance().setRemoteVideoWindow(application.mEngine,-1,null);//set
		if(remoteRenderScreen!=null){
			remoteRenderScreen.release();
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);

		if (hidden) {
			localRenderScreen.setVisibility(View.INVISIBLE);
			stopVideo(application.mEngine);
		}
		else
		{
			updateVideo(application.mEngine);
			activity.receiver.broadcastReceiver = this;
			localRenderScreen.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onClick(View v)
	{
		Intent Home = new Intent(getActivity(), chat.wewe.android.activity.MainActivity.class);
		PortSipSdk portSipLib = application.mEngine;
		Session currentLine = CallManager.Instance().getCurrentSession();
		switch (v.getId())
		{
			case R.id.ibcamera:
				application.mUseFrontCamera = !application.mUseFrontCamera;
				SetCamera(portSipLib, application.mUseFrontCamera);
				break;
			case R.id.ibscale:
				if (scalingType == PortSIPVideoRenderer.ScalingType.SCALE_ASPECT_FIT)
				{
					imgScaleType.setImageResource(R.drawable.aspect_fill);
					scalingType = PortSIPVideoRenderer.ScalingType.SCALE_ASPECT_FILL;
				}
				else if (scalingType == PortSIPVideoRenderer.ScalingType.SCALE_ASPECT_FILL)
				{
					imgScaleType.setImageResource(R.drawable.aspect_balanced);
					scalingType = PortSIPVideoRenderer.ScalingType.SCALE_ASPECT_BALANCED;
				}
				else
				{
					imgScaleType.setImageResource(R.drawable.aspect_fit);
					scalingType = PortSIPVideoRenderer.ScalingType.SCALE_ASPECT_FIT;
				}

				localRenderScreen.setScalingType(scalingType);
				remoteRenderScreen.setScalingType(scalingType);
				updateVideo(portSipLib);
				break;

			case R.id.finish_car:
				Ring.getInstance(getActivity()).stop();
				switch (currentLine.state) {
					case INCOMING:
						portSipLib.rejectCall(currentLine.sessionID, 486);
						break;
					case CONNECTED:
					case TRYING:
						portSipLib.hangUp(currentLine.sessionID);
						break;
				}
				//startActivity(Home);
				getActivity().finish();
				currentLine.Reset();


				break;
			case R.id.ic_video_call:
			{
				if (currentLine.bMute) {
					portSipLib.muteSession(currentLine.sessionID, false,
							false, false, false);
					currentLine.bMute = false;
					ic_video_call.setImageResource(R.drawable.ic_video_call);
				} else {
					portSipLib.muteSession(currentLine.sessionID, true,
							true, true, true);
					currentLine.bMute = true;
					ic_video_call.setImageResource(R.drawable.ic_video_call_off);
				}
			}
			break;
			case R.id.ic_audio_call:
				if (scalingType == PortSIPVideoRenderer.ScalingType.SCALE_ASPECT_FIT)
				{
					ic_audio_call.setImageResource(R.drawable.ic_audio_call);
					scalingType = PortSIPVideoRenderer.ScalingType.SCALE_ASPECT_FILL;
				}
				else if (scalingType == PortSIPVideoRenderer.ScalingType.SCALE_ASPECT_FILL)
				{
					ic_audio_call.setImageResource(R.drawable.ic_audio_call_off);
					scalingType = PortSIPVideoRenderer.ScalingType.SCALE_ASPECT_BALANCED;
				}
				break;
		}
	}



	private void SetCamera(PortSipSdk portSipLib,boolean userFront)
	{
		if (userFront)
		{
			portSipLib.setVideoDeviceId(1);
		}
		else
		{
			portSipLib.setVideoDeviceId(0);
		}
	}

	private void stopVideo(PortSipSdk portSipLib)
	{
		Session cur = CallManager.Instance().getCurrentSession();
		if(portSipLib!=null) {
			portSipLib.displayLocalVideo(false);
			portSipLib.setLocalVideoWindow(null);
			CallManager.Instance().setRemoteVideoWindow(portSipLib,cur.sessionID,null);
			CallManager.Instance().setConferenceVideoWindow(portSipLib,null);
		}
	}

	public void updateVideo(PortSipSdk portSipLib)
	{
		CallManager callManager = CallManager.Instance();
		if (application.mConference)
		{
			callManager.setConferenceVideoWindow(portSipLib,remoteRenderScreen);
		}else {
			Session cur = CallManager.Instance().getCurrentSession();
			if (cur != null && !cur.IsIdle()
					&& cur.sessionID != PortSipErrorcode.INVALID_SESSION_ID
					&& cur.hasVideo) {

				callManager.setRemoteVideoWindow(portSipLib,cur.sessionID, remoteRenderScreen);

				portSipLib.setLocalVideoWindow(localRenderScreen);
				portSipLib.displayLocalVideo(true); // display Local video
				portSipLib.sendVideo(cur.sessionID, true);
			} else {
				portSipLib.displayLocalVideo(false);
				callManager.setRemoteVideoWindow(portSipLib,cur.sessionID, null);
				portSipLib.setLocalVideoWindow(null);
			}
		}
	}

	public void onBroadcastReceiver(Intent intent)
	{
		String action = intent == null ? "" : intent.getAction();
		if (PortSipService.CALL_CHANGE_ACTION.equals(action))
		{
			long sessionId = intent.getLongExtra(PortSipService.EXTRA_CALL_SEESIONID, Session.INVALID_SESSION_ID);
			String status = intent.getStringExtra(PortSipService.EXTRA_CALL_DESCRIPTION);
			if(status.equals("1"))
				getActivity().finish();
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
						updateVideo(application.mEngine);
						break;

				}
			}
		}
	}
}
