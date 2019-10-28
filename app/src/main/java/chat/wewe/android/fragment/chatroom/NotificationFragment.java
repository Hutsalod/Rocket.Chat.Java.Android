package chat.wewe.android.fragment.chatroom;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import chat.wewe.android.R;
import chat.wewe.android.api.BaseApiService;
import chat.wewe.android.api.MethodCallHelper;
import chat.wewe.android.api.UtilsApiChat;
import chat.wewe.android.helper.AbsoluteUrlHelper;
import chat.wewe.core.interactors.SessionInteractor;
import chat.wewe.persistence.realm.repositories.RealmRoomRepository;
import chat.wewe.persistence.realm.repositories.RealmServerInfoRepository;
import chat.wewe.persistence.realm.repositories.RealmSessionRepository;
import chat.wewe.persistence.realm.repositories.RealmUserRepository;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;


public class NotificationFragment extends AbstractChatRoomFragment {
    SwitchCompat notification;
    BaseApiService mApiServiceChat;
    private static final String HOSTNAME = "hostname";
    private static final String ROOM_ID = "roomId";
    public String token;
    public String userId;
    EditText editText;
    SharedPreferences SipData;

    private String hostname;
    private String roomId;

    private RealmUserRepository userRepository;

    private AbsoluteUrlHelper absoluteUrlHelper;
    public NotificationFragment() {
    }

    public static NotificationFragment create(String hostname, String roomId) {
        Bundle args = new Bundle();
        args.putString(HOSTNAME, hostname);
        args.putString(ROOM_ID, roomId);

        NotificationFragment fragment = new NotificationFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        SipData = getActivity().getSharedPreferences("SIP", MODE_PRIVATE);
        token = SipData.getString("TOKEN_RC","");
        userId = SipData.getString("ID_RC","");
        hostname = args.getString(HOSTNAME);
        roomId = args.getString(ROOM_ID);
        mApiServiceChat = UtilsApiChat.getAPIService();
        userRepository = new RealmUserRepository(hostname);

        absoluteUrlHelper = new AbsoluteUrlHelper(
                hostname,
                new RealmServerInfoRepository(),
                userRepository,
                new SessionInteractor(new RealmSessionRepository(hostname))
        );

    }

    @Override
    protected int getLayout() {
        return R.layout.notification_fragment;
    }

    @Override
    protected void onSetupView() {
        notification = (SwitchCompat)rootView.findViewById(R.id.notification);

        notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(notification.isChecked()) {
                   chat_ignoreUserh(true);
               }else{
                   chat_ignoreUserh(false);
               }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

    }


    public  void chat_ignoreUserh(Boolean ignore){
        mApiServiceChat.chat_ignoreUserh(token, userId, userId,roomId,ignore)
                .enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Log.d("DEBUQ", "TT"+response.toString());
                        if (response.isSuccessful()) {
                            try {
                                JSONObject jsonRESULTS = new JSONObject(response.body().string());
                                Log.d("DEBUQ", "TT"+jsonRESULTS.getJSONArray("messages").getJSONObject(0).getString("username"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.d("DEBUQ", "NO" + call + t);
                    }
                });
    }
}
