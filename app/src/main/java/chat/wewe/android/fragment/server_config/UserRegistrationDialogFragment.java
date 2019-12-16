package chat.wewe.android.fragment.server_config;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import chat.wewe.android.R;
import chat.wewe.android.activity.MainActivity;
import chat.wewe.android.api.BaseApiService;
import chat.wewe.android.api.MethodCallHelper;
import chat.wewe.android.api.UtilsApi;
import chat.wewe.android.helper.TextUtils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static chat.wewe.android.fragment.server_config.LoginFragment.switchServer;

/**
 * Dialog for user registration.
 */
public class UserRegistrationDialogFragment extends DialogFragment {
  private String hostname;
  private String username;
  private String email;
  private String password;
  private String msg;
  static String model = "Android";
  Context mContext;
  BaseApiService mApiService;

  ProgressDialog loading;

  public UserRegistrationDialogFragment() {
    super();
  }

  /**
   * create UserRegistrationDialogFragment with auto-detect email/username.
   */
  public static UserRegistrationDialogFragment create(String hostname,
                                                      String usernameOrEmail, String password) {
    if (Patterns.EMAIL_ADDRESS.matcher(usernameOrEmail).matches()) {
      return create(hostname, null, usernameOrEmail, password);
    } else {
      return create(hostname, usernameOrEmail, null, password);
    }
  }

  /**
   * create UserRegistrationDialogFragment.
   */
  public static UserRegistrationDialogFragment create(String hostname,
                                                      String username, String email,
                                                      String password) {
    Bundle args = new Bundle();
    args.putString("hostname", hostname);
    if (!TextUtils.isEmpty(username)) {
      args.putString("username", username);
    }
    if (!TextUtils.isEmpty(email)) {
      args.putString("email", email);
    }
    if (!TextUtils.isEmpty(password)) {
      args.putString("password", password);
    }
    UserRegistrationDialogFragment dialog = new UserRegistrationDialogFragment();
    dialog.setArguments(args);
    return dialog;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle args = getArguments();
    if (args != null) {
      hostname = args.getString("hostname");
      username = args.getString("username");
      email = args.getString("email");
      password = args.getString("password");
    }
    model = Settings.Secure.getString(getContext().getContentResolver(),
            Settings.Secure.ANDROID_ID);
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    return new AlertDialog.Builder(getContext(), R.style.AppTheme_Dialog)
        .setView(createDialogView())
        .create();
  }

  private View createDialogView() {
    View dialog = LayoutInflater.from(getContext())
        .inflate(R.layout.dialog_user_registration, null, false);

    final TextView txtUsername = (TextView) dialog.findViewById(R.id.editor_username);
    final TextView txtEmail = (TextView) dialog.findViewById(R.id.editor_email);
    final TextView txtPasswd = (TextView) dialog.findViewById(R.id.editor_passwd);


    if (!TextUtils.isEmpty(username)) {
      txtUsername.setText(username);
    }
    if (!TextUtils.isEmpty(email)) {
      txtEmail.setText(email);
    }
    if (!TextUtils.isEmpty(password)) {
      txtPasswd.setText(password);
    }

    final View waitingView = dialog.findViewById(R.id.waiting);
    waitingView.setVisibility(View.GONE);

    dialog.findViewById(R.id.btn_register_user).setOnClickListener(view -> {

      waitingView.setVisibility(View.VISIBLE);

      username = txtUsername.getText().toString();
      email = txtEmail.getText().toString();
      password = txtPasswd.getText().toString();
      mApiService = UtilsApi.getAPIService();
      if(username.length()<=3)
        Toast.makeText(getActivity(), "Имя меньше 3 символов", Toast.LENGTH_SHORT).show();
      if(email.equals(password)){
      if (switchServer.isChecked()) {

        MethodCallHelper methodCallHelper = new MethodCallHelper(getContext(), hostname);
        methodCallHelper.registerUser(username, email, password, password)
                .onSuccessTask(task -> methodCallHelper.loginWithEmail(email, password))
                .onSuccessTask(task -> methodCallHelper.setUsername(username)) //TODO: should prompt!
                .onSuccessTask(task -> methodCallHelper.joinDefaultChannels())
                .onSuccessTask(task -> {
                  dismiss();
                  return task;
                })
                .continueWith(task -> {
                  if (task.isFaulted()) {
                    Exception exception = task.getError();
                    showError(exception.getMessage());
                    view.setEnabled(true);
                    waitingView.setVisibility(View.GONE);
                  }
                  return null;
                });
      } else {
        requestRegister();
        waitingView.setVisibility(View.GONE);
      }}else {
        msg = "Пароли не совпадают";
        Toast.makeText(getActivity(), ""+msg, Toast.LENGTH_SHORT).show();
      }
    });

    return dialog;
  }

  private void showError(String errMessage) {
    Toast.makeText(getContext(), errMessage, Toast.LENGTH_SHORT).show();
  }

  private void requestRegister(){
    mApiService.registerRequest(model,username,password).enqueue(new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        if (response.isSuccessful()){
          Log.i("debug", "onResponse: BERHASIL");
          try {
            JSONObject jsonRESULTS = new JSONObject(response.body().string());
            if (jsonRESULTS.getString("SUCCESS").equals("false")){
              msg = jsonRESULTS.getString("ERROR_CODE");
              switch (msg) {
                case "1": msg = "Не верные входные параметры";
                  break;
                case "2": msg = "Пользователь существует";
                  break;
                case "3": msg = "Ошибка регистрации на RocketChat";
                  break;
                case "4": msg = "Ошибка регистрации на SIP";
                  break;
                case "5": msg = "Логин не может содержать Кириллицу";
                  break;
              }
             Toast.makeText(getActivity(), ""+msg, Toast.LENGTH_SHORT).show();

            }else{
              dismiss();
              Toast.makeText(getActivity(), "Регистрация успешна, пожалуйста, авторизуйтесь", Toast.LENGTH_SHORT).show();
            }

          } catch (JSONException e) {
            e.printStackTrace();
          } catch (IOException e) {
            e.printStackTrace();
          }
        } else {
          Log.i("debug", "onResponse: GA BERHASIL");
        }
      }

      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t) {
        Log.e("debug", "onFailure: ERROR > " + t.getMessage());
        Toast.makeText(mContext, "Koneksi Internet Bermasalah", Toast.LENGTH_SHORT).show();
      }
    });
  }
}
