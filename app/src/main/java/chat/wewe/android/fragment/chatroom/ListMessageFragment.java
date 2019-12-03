package chat.wewe.android.fragment.chatroom;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import chat.wewe.android.R;
import chat.wewe.android.adapter.ExampleAdapterMessage;
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


public class ListMessageFragment extends DialogFragment  {

    private static final String HOSTNAME = "hostname";
    private static final String ROOM_ID = "roomId";
    public String token;
    public String userId,m,n;
    EditText editText;
    private String hostname;
    private String roomId;
    public TextView name,message;
    private RealmUserRepository userRepository;
    private AbsoluteUrlHelper absoluteUrlHelper;
    SharedPreferences SipData;
    BaseApiService mApiServiceChat;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    ArrayList<ExampleItemM> exampleList;

    public ListMessageFragment() {
    }

    public static ListMessageFragment create(String hostname, String roomId) {
        Bundle args = new Bundle();
        args.putString(HOSTNAME, hostname);
        args.putString(ROOM_ID, roomId);

        ListMessageFragment fragment = new ListMessageFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        SipData = getActivity().getSharedPreferences("SIP", MODE_PRIVATE);
        token = SipData.getString("TOKEN_RC","");
        userId = SipData.getString("ID_RC","");
        Bundle args = getArguments();
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


        View v = inflater.inflate(R.layout.fragment_list_message, null);
        exampleList = new ArrayList<>();

        mRecyclerView = v.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new ExampleAdapterMessage(exampleList);

        editText = (EditText) v.findViewById(R.id.editText3);



        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    listMessage(editText.getText().toString());

                }
            }
        });
        return v;
    }

    public  void listMessage(String search){
        mApiServiceChat.chat_search(token, userId,roomId,search)
                .enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Log.d("DEBUQ", "TT"+response.toString());
                        exampleList = new ArrayList<>();
                        mAdapter = new ExampleAdapterMessage(exampleList);
                        try {

                            JSONObject jsonRESULTS = new JSONObject(response.body().string());
                            for (int i = 0; i < jsonRESULTS.getJSONArray("messages").length(); i++) {
                                Log.d("DEBUQ", "NAME " + jsonRESULTS.getJSONArray("messages").getJSONObject(i).getJSONObject("u").getString("name"));
                                exampleList.add(new ExampleItemM(jsonRESULTS.getJSONArray("messages").getJSONObject(i).getJSONObject("u").getString("name"), jsonRESULTS.getJSONArray("messages").getJSONObject(i).getString("msg")));
                            }

                            mRecyclerView.setLayoutManager(mLayoutManager);
                            mRecyclerView.setAdapter(mAdapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.d("DEBUQ", "NO" + call + t);
                    }
                });
    }
}
