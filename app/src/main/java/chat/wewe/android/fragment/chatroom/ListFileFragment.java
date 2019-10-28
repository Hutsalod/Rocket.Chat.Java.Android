package chat.wewe.android.fragment.chatroom;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import chat.wewe.android.R;

import chat.wewe.android.adapter.ExampleAdapterFile;
import chat.wewe.android.api.BaseApiService;
import chat.wewe.android.api.MethodCallHelper;
import chat.wewe.android.api.UtilsApiChat;
import chat.wewe.android.helper.AbsoluteUrlHelper;
import chat.wewe.android.widget.internal.RoomListItemView;
import chat.wewe.core.interactors.SessionInteractor;
import chat.wewe.persistence.realm.repositories.RealmRoomRepository;
import chat.wewe.persistence.realm.repositories.RealmServerInfoRepository;
import chat.wewe.persistence.realm.repositories.RealmSessionRepository;
import chat.wewe.persistence.realm.repositories.RealmUserRepository;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Url;

import static android.content.Context.MODE_PRIVATE;


public class ListFileFragment extends AbstractChatRoomFragment {

    private static final String HOSTNAME = "hostname";
    private static final String ROOM_ID = "roomId";
    public String token;
    public String userId;
    EditText editText;
    private String hostname;
    private String roomId;
    private RealmUserRepository userRepository;
    private MethodCallHelper methodCallHelper;
    private AbsoluteUrlHelper absoluteUrlHelper;
    SharedPreferences SipData;
    BaseApiService mApiServiceChat;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    ArrayList<ExampleItem> exampleList;


    public ListFileFragment() {
    }

    public static ListFileFragment create(String hostname, String roomId) {
        Bundle args = new Bundle();
        args.putString(HOSTNAME, hostname);
        args.putString(ROOM_ID, roomId);

        ListFileFragment fragment = new ListFileFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        SipData = getActivity().getSharedPreferences("SIP", MODE_PRIVATE);
        token = SipData.getString("TOKEN_RC", "");
        userId = SipData.getString("ID_RC", "");
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
        return R.layout.fragment_list_file;
    }

    @Override
    protected void onSetupView() {

        exampleList = new ArrayList<>();

        mRecyclerView = rootView.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new ExampleAdapterFile(exampleList);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        im_files(roomId);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    public static void loadImage(String imageUrl, SimpleDraweeView draweeView) {
        final DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(Uri.parse(imageUrl))
                .setAutoPlayAnimations(true)
                .setTapToRetryEnabled(true)
                .build();
        draweeView.setController(controller);
    }

    public void im_files(String search) {
        mApiServiceChat.im_files(token, userId, search)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        JsonObject object = response.body();
                       for (int i = 0; i < object.get("files").getAsJsonArray().size()-1; i++) {
                        Log.d("DEBUQ", "NAME " + object.get("files").getAsJsonArray().size());
                            String url = "file-upload/" + object.get("files").getAsJsonArray().get(1).getAsJsonObject().get("_id").getAsString() + "/" + object.get("files").getAsJsonArray().get(1).getAsJsonObject().get("name").getAsString();
                            image_set(url);
                       }

                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {

                    }
                });
    }

    public void image_set(String url) {
        mApiServiceChat.image_set(token, userId, url)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
                        exampleList.add(new ExampleItem(bmp, url));
                        mRecyclerView.setLayoutManager(mLayoutManager);
                        mRecyclerView.setAdapter(mAdapter);
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });
    }

}
