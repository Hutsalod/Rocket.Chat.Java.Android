package chat.wewe.android.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import chat.wewe.android.R;
import chat.wewe.android.RocketChatApplication;
import chat.wewe.android.receiver.PortMessageReceiver;
import chat.wewe.android.service.PortSipService;
import chat.wewe.android.util.CallManager;

import static android.content.Context.MODE_PRIVATE;
import static chat.wewe.android.activity.Intro.callstatic;
import static chat.wewe.android.activity.Intro.UF_SIP_NUMBER;
import static chat.wewe.android.activity.Intro.UF_SIP_PASSWORD;
import static chat.wewe.android.service.PortSipService.EXTRA_REGISTER_STATE;
import static chat.wewe.android.activity.Intro.StatusU;
public class LoginFragment extends BaseFragment implements AdapterView.OnItemSelectedListener, View.OnClickListener,PortMessageReceiver.BroadcastListener{
    RocketChatApplication application;
    MainActivity activity;
    private EditText etUsername = null;
    private EditText etPassword = null;
    private EditText etSipServer = null;
    private EditText etSipServerPort = null;

    private EditText etDisplayname = null;

    private EditText etUserdomain = null;
    private EditText etAuthName = null;
    private EditText etStunServer = null;
    private EditText etStunPort = null;
    private Spinner spTransport;
    private Spinner spSRTP;
    private TextView mtxStatus;
    SharedPreferences SipData;
    Intent callInt;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();
        application = (RocketChatApplication) activity.getApplicationContext();
        View view = inflater.inflate(R.layout.login, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mtxStatus = (TextView) view.findViewById(R.id.txtips);

        etUsername = (EditText) view.findViewById(R.id.etusername);
        etPassword = (EditText) view.findViewById(R.id.etpwd);
        etSipServer = (EditText) view.findViewById(R.id.etsipsrv);
        etSipServerPort = (EditText) view.findViewById(R.id.etsipport);

        etDisplayname = (EditText) view.findViewById(R.id.etdisplayname);
        etUserdomain = (EditText) view.findViewById(R.id.etuserdomain);
        etAuthName = (EditText) view.findViewById(R.id.etauthName);
        etStunServer = (EditText) view.findViewById(R.id.etStunServer);
        etStunPort = (EditText) view.findViewById(R.id.etStunPort);

        spTransport = (Spinner) view.findViewById(R.id.spTransport);
        spSRTP = (Spinner) view.findViewById(R.id.spSRTP);

        spTransport.setAdapter(ArrayAdapter.createFromResource(getActivity(), R.array.transports, android.R.layout.simple_list_item_1));
        spSRTP.setAdapter(ArrayAdapter.createFromResource(getActivity(), R.array.srtp, android.R.layout.simple_list_item_1));

        spSRTP.setOnItemSelectedListener(this);
        spTransport.setOnItemSelectedListener(this);

        LoadUserInfo();
        setOnlineStatus(null);
        SipData = getActivity().getSharedPreferences("SIP", MODE_PRIVATE);
        SharedPreferences.Editor ed =  SipData.edit();
        ed.commit();
        UF_SIP_NUMBER = SipData.getString("UF_SIP_NUMBER", null);
        ed.commit();
        UF_SIP_PASSWORD = SipData.getString("UF_SIP_PASSWORD", null);

        activity.receiver.broadcastReceiver = this;
        view.findViewById(R.id.btonline).setOnClickListener(this);
        view.findViewById(R.id.btoffline).setOnClickListener(this);

        if(callstatic==0) {

            SaveUserInfo();
            Intent onLineIntent = new Intent(getActivity(), PortSipService.class);
            onLineIntent.setAction(PortSipService.ACTION_SIP_REGIEST);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getActivity().startForegroundService(onLineIntent);
            } else {
                getActivity().startService(onLineIntent);
            }
            mtxStatus.setText("RegisterServer..");

        }

    }



    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden){
            activity.receiver.broadcastReceiver = this;
            setOnlineStatus(null);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        activity.receiver.broadcastReceiver =null;
    }


    private void LoadUserInfo() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        etUsername.setText(preferences.getString(PortSipService.USER_NAME, UF_SIP_PASSWORD));
        etPassword.setText(preferences.getString(PortSipService.USER_PWD, UF_SIP_PASSWORD));
        etSipServer.setText(preferences.getString(PortSipService.SVR_HOST, "sip.weltwelle.com"));
        etSipServerPort.setText(preferences.getString(PortSipService.SVR_PORT, "5061"));

        etDisplayname.setText(preferences.getString(PortSipService.USER_DISPALYNAME, null));
        etUserdomain.setText(preferences.getString(PortSipService.USER_DOMAIN, null));
        etAuthName.setText(preferences.getString(PortSipService.USER_AUTHNAME, null));
        etStunServer.setText(preferences.getString(PortSipService.STUN_HOST, null));
        etStunPort.setText(preferences.getString(PortSipService.STUN_PORT, "3478"));

        spTransport.setSelection(preferences.getInt(PortSipService.TRANS, 0));
        spSRTP.setSelection(preferences.getInt(PortSipService.SRTP, 0));
    }

    public void SaveUserInfo() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        editor.putString(PortSipService.USER_NAME, UF_SIP_NUMBER);
        editor.putString(PortSipService.USER_PWD, UF_SIP_PASSWORD);
        editor.putString(PortSipService.SVR_HOST, "sip.weltwelle.com");
        editor.putString(PortSipService.SVR_PORT, "5061");

        editor.putString(PortSipService.USER_DISPALYNAME, null);
        editor.putString(PortSipService.USER_DOMAIN, null);
        editor.putString(PortSipService.USER_AUTHNAME, null);
        editor.putString(PortSipService.STUN_HOST, null);
        editor.putString(PortSipService.STUN_PORT, "3478");

        editor.commit();
    }


    public void onBroadcastReceiver(Intent intent) {
        String action = intent == null ? "" : intent.getAction();
        if (PortSipService.REGISTER_CHANGE_ACTION.equals(action)) {
            String tips  =intent.getStringExtra(EXTRA_REGISTER_STATE);
            setOnlineStatus(tips);
        } else if (PortSipService.CALL_CHANGE_ACTION.equals(action)) {
            //long sessionId = intent.GetLongExtra(PortSipService.EXTRA_CALL_SEESIONID, Session.INVALID_SESSION_ID);
            //callStatusChanged(sessionId);
        }
    }

    private void setOnlineStatus(String tips) {
        if (CallManager.Instance().regist) {
            mtxStatus.setText(TextUtils.isEmpty(tips)?getString(R.string.online):tips);
            if (callstatic==0)
         startActivity(new Intent(getActivity(), chat.wewe.android.activity.MainActivity.class));
            StatusU = 8;
            callstatic=1;
        } else {
            mtxStatus.setText(TextUtils.isEmpty(tips)?getString(R.string.offline):tips);
        startActivity(new Intent(getActivity(), chat.wewe.android.activity.MainActivity.class));
        }
        if(mtxStatus.getText().equals("No DNS results")){
            callstatic=1;
            startActivity(new Intent(getActivity(), chat.wewe.android.activity.MainActivity.class));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btonline:
                SaveUserInfo();
                Intent onLineIntent = new Intent(getActivity(), PortSipService.class);
                onLineIntent.setAction(PortSipService.ACTION_SIP_REGIEST);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    getActivity().startForegroundService(onLineIntent);
                }else{
                    getActivity().startService(onLineIntent);
                }
                mtxStatus.setText("RegisterServer..");
                break;
            case R.id.btoffline:
                Intent offLineIntent = new Intent(getActivity(), PortSipService.class);
                offLineIntent.setAction(PortSipService.ACTION_SIP_UNREGIEST);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    getActivity().startForegroundService(offLineIntent);
                }else{
                    getActivity().startService(offLineIntent);
                }
                mtxStatus.setText("unRegisterServer");
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        if (adapterView == null)
            return;
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        switch (adapterView.getId()) {
            case R.id.spSRTP:
                editor.putInt(PortSipService.SRTP, position).commit();
                break;
            case R.id.spTransport:
                editor.putInt(PortSipService.TRANS, position).commit();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
