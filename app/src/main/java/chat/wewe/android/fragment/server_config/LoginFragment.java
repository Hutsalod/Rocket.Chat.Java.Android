package chat.wewe.android.fragment.server_config;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

import java.util.HashMap;
import java.util.List;
import chat.wewe.android.R;
import chat.wewe.android.Success;
import chat.wewe.android.api.BaseApiService;
import chat.wewe.android.api.MethodCallHelper;
import chat.wewe.android.layouthelper.oauth.OAuthProviderInfo;
import chat.wewe.android.log.RCLog;
import chat.wewe.android.service.PortSipService;
import chat.wewe.core.models.LoginServiceConfiguration;
import chat.wewe.persistence.realm.repositories.RealmLoginServiceConfigurationRepository;
import chat.wewe.persistence.realm.repositories.RealmPublicSettingRepository;

import chat.wewe.android.api.UtilsApi;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;
import static chat.wewe.android.activity.Intro.UF_SIP_NUMBER;
import static chat.wewe.android.activity.Intro.UF_SIP_PASSWORD;
import static chat.wewe.android.activity.Intro.callstatic;

/**
 * Login screen.
 */
public class LoginFragment extends AbstractServerConfigFragment implements LoginContract.View {

  private LoginContract.Presenter presenter;
  private ConstraintLayout container;
  private View waitingView;
  private TextView txtUsername;
  private TextView txtPasswd;
  private String idmodel,model;
  public static  SwitchCompat switchServer;
  public static String TOKENwe,UF_SIP_SERVER,UF_SIP_LOGIN,UF_ACCESS_OTH_DEVICE,INNER_GROUP,UF_ROCKET_SERVER;
  ProgressDialog loading;
  SharedPreferences SipData;
  Context mContext;
  BaseApiService mApiService;

  @Override
  protected int getLayout() {
    return R.layout.fragment_login;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    presenter = new LoginPresenter(
        new RealmLoginServiceConfigurationRepository(hostname),
        new RealmPublicSettingRepository(hostname),
        new MethodCallHelper(getContext(), hostname)
    );

    idmodel = Settings.Secure.getString(getContext().getContentResolver(),
            Settings.Secure.ANDROID_ID);
    model = Build.MODEL;

    mApiService = UtilsApi.getAPIService(); // meng-init yang ada di package apihelper
    SipData = getActivity().getSharedPreferences("SIP", MODE_PRIVATE);


    Log.d("model",""+FirebaseInstanceId.getInstance().getToken()+ " "+model+" "+idmodel);
  }

  @Override
  protected void onSetupView() {
    container = (ConstraintLayout) rootView.findViewById(R.id.container);

    View btnEmail = rootView.findViewById(R.id.btn_login_with_email);
    txtUsername = (TextView) rootView.findViewById(R.id.editor_username);
    txtPasswd = (TextView) rootView.findViewById(R.id.editor_passwd);
    switchServer = (SwitchCompat) rootView.findViewById(R.id.switchServer);
    waitingView = rootView.findViewById(R.id.waiting);
   btnEmail.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View view) {
        if(switchServer.isChecked()) {
          presenter.login(txtUsername.getText().toString(), txtPasswd.getText().toString());


        }else {
          //  loading = ProgressDialog.show(mContext, null, "Harap Tunggu...", true, false);
          loginRequest();

        }

      }

    });
    final View btnUserRegistration = rootView.findViewById(R.id.btn_user_registration);
    btnUserRegistration.setOnClickListener(view -> UserRegistrationDialogFragment.create(hostname,
        txtUsername.getText().toString(), txtPasswd.getText().toString())
        .show(getFragmentManager(), "UserRegistrationDialogFragment"));
  }

  @Override
  public void showLoader() {
    container.setVisibility(View.GONE);
    waitingView.setVisibility(View.VISIBLE);
  }

  @Override
  public void hideLoader() {
    waitingView.setVisibility(View.GONE);
   // container.setVisibility(View.VISIBLE);
  }

  @Override
  public void showError(String message) {
    Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show();
  }

  @Override
  public void showLoginServices(List<LoginServiceConfiguration> loginServiceList) {
    HashMap<String, View> viewMap = new HashMap<>();
    HashMap<String, Boolean> supportedMap = new HashMap<>();
    for (OAuthProviderInfo info : OAuthProviderInfo.LIST) {
      viewMap.put(info.serviceName, rootView.findViewById(info.buttonId));
      supportedMap.put(info.serviceName, false);
    }

    for (LoginServiceConfiguration authProvider : loginServiceList) {
      for (OAuthProviderInfo info : OAuthProviderInfo.LIST) {
        if (!supportedMap.get(info.serviceName)
            && info.serviceName.equals(authProvider.getService())) {
          supportedMap.put(info.serviceName, true);
          viewMap.get(info.serviceName).setOnClickListener(view -> {
            Fragment fragment = null;
            try {
              fragment = info.fragmentClass.newInstance();
            } catch (Exception exception) {
              RCLog.w(exception, "failed to create new Fragment");
            }
            if (fragment != null) {
              Bundle args = new Bundle();
              args.putString("hostname", hostname);
              fragment.setArguments(args);
              showFragmentWithBackStack(fragment);
            }
          });
          viewMap.get(info.serviceName).setVisibility(View.VISIBLE);
        }
      }
    }

    for (OAuthProviderInfo info : OAuthProviderInfo.LIST) {
      if (!supportedMap.get(info.serviceName)) {
        viewMap.get(info.serviceName).setVisibility(View.GONE);
      }
    }
  }

  @Override
  public void showTwoStepAuth() {
    showFragmentWithBackStack(TwoStepAuthFragment.create(
        hostname, txtUsername.getText().toString(), txtPasswd.getText().toString()
    ));
  }

  @Override
  public void onResume() {
    super.onResume();
    presenter.bindView(this);
  }

  @Override
  public void onPause() {
    presenter.release();
    super.onPause();
  }

  private void loginRequest(){
    mApiService.loginRequest(txtUsername.getText().toString(), txtPasswd.getText().toString(),idmodel,model,FirebaseInstanceId.getInstance().getToken())
            .enqueue(new Callback<ResponseBody>() {
              @Override
              public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()){
                  try {
                    JSONObject jsonRESULTS = new JSONObject(response.body().string());
                    if (jsonRESULTS.getString("SUCCESS").equals("true")){
                      TOKENwe = jsonRESULTS.getString("TOKEN");
                      SharedPreferences.Editor ed = SipData.edit();
                      ed.putString("TOKENWE", TOKENwe);
                      ed.commit();
                      Log.i("MSG","TEST" +TOKENwe);
                      getSettings();
                    }else {
                      Toast.makeText(getActivity(), "Пользователь не найден" ,
                              Toast.LENGTH_SHORT).show();
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
                Log.e("debug", "onFailure: ERROR > " + t.toString());

              }
            });
  }

  private void getSettings(){
    mApiService.getSettings(" KEY:"+TOKENwe)
            .enqueue(new Callback<ResponseBody>() {
              @Override
              public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()){
                  try {
                    JSONObject jsonRESULTS = new JSONObject(response.body().string());
                    if (jsonRESULTS.getString("status").equals("200")){
                      String UF_ROCKET_LOGIN = jsonRESULTS.getJSONObject("result").getString("UF_ROCKET_LOGIN");
                      String UF_ROCKET_PASSWORD = jsonRESULTS.getJSONObject("result").getString("UF_ROCKET_PASSWORD");
                      UF_SIP_SERVER = jsonRESULTS.getJSONObject("result").getString("UF_SIP_SERVER");
                      UF_SIP_LOGIN = jsonRESULTS.getJSONObject("result").getString("UF_SIP_LOGIN");
                      UF_SIP_PASSWORD = jsonRESULTS.getJSONObject("result").getString("UF_SIP_PASSWORD");
                      UF_SIP_NUMBER = jsonRESULTS.getJSONObject("result").getString("UF_SIP_NUMBER");
                      UF_ACCESS_OTH_DEVICE = jsonRESULTS.getJSONObject("result").getString("UF_ACCESS_OTH_DEVICE");
                      INNER_GROUP = jsonRESULTS.getJSONObject("result").getString("INNER_GROUP");
                      UF_ROCKET_SERVER = jsonRESULTS.getJSONObject("result").getString("UF_ROCKET_SERVER");
                      presenter.login(UF_ROCKET_LOGIN, UF_ROCKET_PASSWORD);
                      SharedPreferences.Editor ed = SipData.edit();
                      ed.putString("UF_SIP_NUMBER", UF_SIP_NUMBER);
                      ed.commit();
                      ed.putString("UF_SIP_PASSWORD", UF_SIP_PASSWORD);
                      ed.commit();
                      ed.putString("INNER_GROUP", INNER_GROUP);
                      ed.commit();
                      if(switchServer.isChecked()) {
                        ed.putString("UF_ROCKET_SERVER", UF_ROCKET_SERVER);
                        ed.commit();
                        ed.putString("UF_SIP_SERVER", UF_SIP_SERVER);
                        ed.commit();
                      }else {
                        ed.putString("UF_ROCKET_SERVER", "chat.weltwelle.com");
                        ed.commit();
                        ed.putString("UF_SIP_SERVER", "sip.weltwelle.com");
                        ed.commit();
                      }

                      if(SipData.getString("INNER_GROUP", "false").equals("false")){
                        startActivity(new Intent(getActivity(), Success.class));
                        finish();
                      }
                     if(SipData.getString("UF_SIP_NUMBER", null)!=null & callstatic==0) {
                       SaveUserInfo();
                       Intent onLineIntent = new Intent(getContext(), PortSipService.class);
                       onLineIntent.putExtra(PortSipService.EXTRA_PUSHTOKEN, FirebaseInstanceId.getInstance().getToken());
                       onLineIntent.setAction(PortSipService.ACTION_SIP_REGIEST);

                      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                         getContext().startForegroundService(onLineIntent);
                       }else{
                         getContext().startService(onLineIntent);
                       }
                      }
                     // getLoginChat();
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
                Log.e("debug", "onFailure: ERROR > " + t.toString());
                loading.dismiss();
              }
            });
  }

  public void SaveUserInfo() {
    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
    UF_SIP_NUMBER = SipData.getString("UF_SIP_NUMBER", null);
    UF_SIP_PASSWORD = SipData.getString("UF_SIP_PASSWORD", null);
    UF_SIP_SERVER = SipData.getString("UF_SIP_SERVER", "sip.weltwelle.com");
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
