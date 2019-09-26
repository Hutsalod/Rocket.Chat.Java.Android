package chat.wewe.android.fragment.sidebar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding2.support.v7.widget.RxSearchView;
import com.jakewharton.rxbinding2.widget.RxCompoundButton;

import org.json.JSONException;
import org.json.JSONObject;

import chat.wewe.android.activity.Intro;
import chat.wewe.android.activity.MainActivity;
import chat.wewe.android.activity.PinCode;
import chat.wewe.android.api.BaseApiService;
import chat.wewe.android.api.UtilsApi;
import chat.wewe.android.api.UtilsApiChat;
import chat.wewe.android.service.PortSipService;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import chat.wewe.android.BuildConfig;
import chat.wewe.android.R;
import chat.wewe.android.RocketChatCache;
import chat.wewe.android.api.MethodCallHelper;
import chat.wewe.android.fragment.AbstractFragment;
import chat.wewe.android.fragment.chatroom.RocketChatAbsoluteUrl;
import chat.wewe.android.helper.AbsoluteUrlHelper;
import chat.wewe.android.helper.Logger;
import chat.wewe.android.helper.TextUtils;
import chat.wewe.android.layouthelper.chatroom.roomlist.FavoriteRoomListHeader;
import chat.wewe.android.layouthelper.chatroom.roomlist.RoomListAdapter;
import chat.wewe.android.layouthelper.chatroom.roomlist.RoomListHeader;
import chat.wewe.android.layouthelper.chatroom.roomlist.UnreadRoomListHeader;
import chat.wewe.core.SortDirection;
import chat.wewe.core.interactors.RoomInteractor;
import chat.wewe.core.interactors.SessionInteractor;
import chat.wewe.core.models.Room;
import chat.wewe.core.models.SpotlightRoom;
import chat.wewe.core.models.User;
import chat.wewe.android.renderer.UserRenderer;
import chat.wewe.persistence.realm.repositories.RealmRoomRepository;
import chat.wewe.persistence.realm.repositories.RealmServerInfoRepository;
import chat.wewe.persistence.realm.repositories.RealmSessionRepository;
import chat.wewe.persistence.realm.repositories.RealmSpotlightRoomRepository;
import chat.wewe.persistence.realm.repositories.RealmUserRepository;
import chat.wewe.android.widget.RocketChatAvatar;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;
import static android.content.Context.MODE_PRIVATE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static chat.wewe.android.activity.Intro.callstatic;
import static chat.wewe.android.activity.MainActivity.activity_main_container;
import static chat.wewe.android.activity.MainActivity.btnCreate;
import static chat.wewe.android.activity.MainActivity.callUsers;
import static chat.wewe.android.activity.MainActivity.current_user_name;
import static chat.wewe.android.activity.MainActivity.editText;
import static chat.wewe.android.activity.MainActivity.nazad;
import static chat.wewe.android.activity.MainActivity.recyclerViews;
import static chat.wewe.android.activity.MainActivity.search_btn_users;
import static chat.wewe.android.activity.MainActivity.statusRoom;
import static chat.wewe.android.activity.MainActivity.switch2;
import static chat.wewe.android.fragment.server_config.LoginFragment.TOKENwe;

public class SidebarMainFragment extends AbstractFragment implements SidebarMainContract.View {

  private static final String HOSTNAME = "hostname";

  private SidebarMainContract.Presenter presenter;

  public static RoomListAdapter adapter;
  SwipeController swipeController = null;
  private String hostname;
  SharedPreferences SipData;
  public static MethodCallHelper methodCallHelper;
  private RealmSpotlightRoomRepository realmSpotlightRoomRepository;
  private SearchView searchView;
  private  SwitchCompat switch1;
  public static String getName;
  SharedPreferences.Editor ed;
  TextView exetGoogle;
  NestedScrollView actionContainers,languageLayaut,secyrityLayaut;
  String getRoom;
  Context mContext;
  BaseApiService mApiServiceChat;
  String device = "0";
  public static   Bitmap bmp;
  public SidebarMainFragment() {
  }

  /**
   * create SidebarMainFragment with hostname.
   */
  public static SidebarMainFragment create(String hostname) {
    Bundle args = new Bundle();
    args.putString(HOSTNAME, hostname);

    SidebarMainFragment fragment = new SidebarMainFragment();
    fragment.setArguments(args);

    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle args = getArguments();
    hostname = args == null ? null : args.getString(HOSTNAME);

    methodCallHelper = new MethodCallHelper(getContext(), hostname);
    realmSpotlightRoomRepository = new RealmSpotlightRoomRepository(hostname);

    RealmUserRepository userRepository = new RealmUserRepository(hostname);

    AbsoluteUrlHelper absoluteUrlHelper = new AbsoluteUrlHelper(
        hostname,
        new RealmServerInfoRepository(),
        userRepository,
        new SessionInteractor(new RealmSessionRepository(hostname))
    );

    presenter = new SidebarMainPresenter(
        hostname,
        new RoomInteractor(new RealmRoomRepository(hostname)),
        userRepository,
        new RocketChatCache(getContext()),
        absoluteUrlHelper,
        TextUtils.isEmpty(hostname) ? null : new MethodCallHelper(getContext(), hostname)
    );

    SipData = getActivity().getSharedPreferences("SIP", MODE_PRIVATE);
    ed = SipData.edit();


    bmp = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.delete);

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

  @Override
  protected int getLayout() {
    return R.layout.fragment_sidebar_main;
  }

  @SuppressLint("RxLeakedSubscription")
  @Override
  protected void onSetupView() {
    setupUserActionToggle();
    setupUserStatusButtons();
    setupLogoutButton();
    setupVersionInfo();


    mApiServiceChat = UtilsApiChat.getAPIService();
    adapter = new RoomListAdapter();
    adapter.setOnItemClickListener(new RoomListAdapter.OnItemClickListener() {

      @Override
      public void onItemClick(Room room) {
        getRoom = room.getRoomId();
        searchView.clearFocus();
        presenter.onRoomSelected(room);
        Log.i("Test","Yo project");
        animateShow(recyclerViews);
        animateHide(recyclerViews);
        animateShow(editText);
        animateHide(editText);
        animateShow(nazad);
        animateShow(callUsers);
        animateShow(btnCreate);
        animateHide(btnCreate);
        animateShow(search_btn_users);
        animateHide(search_btn_users);
        current_user_name.setText(" " +room.getName());
        animateShow(statusRoom);
        getName=room.getName();

        SipData = getContext().getSharedPreferences("NameMessage", MODE_PRIVATE);
        SharedPreferences.Editor ed = SipData.edit();
        ed.putString("getName", room.getName());
        ed.commit();
        SipData = getContext().getSharedPreferences("TimeMessage", MODE_PRIVATE);
        SharedPreferences.Editor eds = SipData.edit();
        eds.putString("getName", room.getName());
        eds.commit();
        getStatus(getName);
        animateShow(activity_main_container);

      }

      @Override
      public void onItemClick(SpotlightRoom spotlightRoom) {
        searchView.setQuery(null, false);
        searchView.clearFocus();
        methodCallHelper.joinRoom(spotlightRoom.getId())
            .onSuccessTask(task -> {
              presenter.onSpotlightRoomSelected(spotlightRoom);
              return null;
            });
      }


    });


    switch1 = (SwitchCompat) rootView.findViewById(R.id.deviceprivat);
    exetGoogle = (TextView) rootView.findViewById(R.id.exetGoogle);
    actionContainers = (NestedScrollView) rootView.findViewById(R.id.user_action_outer_container);
    languageLayaut = (NestedScrollView) rootView.findViewById(R.id.languageLayaut);
    secyrityLayaut = (NestedScrollView) rootView.findViewById(R.id.secyrityLayaut);

    recyclerViews.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
   // new ItemTouchHelper(swipeController).attachToRecyclerView(recyclerViews);
    recyclerViews.setAdapter(adapter);

    swipeController = new SwipeController(new SwipeControllerActions() {
      @Override
      public void onRightClicked(int position) {

        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, adapter.getItemCount());
      }
    });

    ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
    itemTouchhelper.attachToRecyclerView(recyclerViews);

    recyclerViews.addItemDecoration(new RecyclerView.ItemDecoration() {
      @Override
      public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        swipeController.onDraw(c);
      }
    });


    searchView = (SearchView) rootView.findViewById(R.id.search);
    RxSearchView.queryTextChanges(searchView)
        .compose(bindToLifecycle())
        .debounce(300, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .switchMap(it -> {
          if (it.length() == 0) {
            adapter.setMode(RoomListAdapter.MODE_ROOM);
            return Observable.just(Collections.<SpotlightRoom>emptyList());
          }

          adapter.setMode(RoomListAdapter.MODE_SPOTLIGHT_ROOM);

          final String queryString = it.toString();

          methodCallHelper.searchSpotlightRooms(queryString);

          return realmSpotlightRoomRepository.getSuggestionsFor(queryString, SortDirection.DESC, 10)
              .toObservable();
        })
        .subscribe(
            this::showSearchSuggestions,
            Logger::report
        );


  }

  @SuppressLint("RxLeakedSubscription")
  private void setupUserActionToggle() {
    final CompoundButton toggleUserAction =
        ((CompoundButton) rootView.findViewById(R.id.toggle_user_action));
    toggleUserAction.setFocusableInTouchMode(false);

    rootView.findViewById(R.id.user_info_container)
        .setOnClickListener(view -> toggleUserAction.toggle());

    RxCompoundButton.checkedChanges(toggleUserAction)
        .compose(bindToLifecycle())
        .subscribe(
            aBoolean -> rootView.findViewById(R.id.user_action_outer_container)
                .setVisibility(View.VISIBLE),
            Logger::report
        );
  }

  private void setupUserStatusButtons() {
    rootView.findViewById(R.id.btn_status_online).setOnClickListener(view -> {
      presenter.onUserOnline();
      closeUserActionContainer();
    });
    rootView.findViewById(R.id.btn_status_away).setOnClickListener(view -> {
      presenter.onUserAway();
      closeUserActionContainer();
    });
    rootView.findViewById(R.id.btn_status_busy).setOnClickListener(view -> {
      presenter.onUserBusy();
      closeUserActionContainer();
    });
    rootView.findViewById(R.id.btn_status_invisible).setOnClickListener(view -> {
      presenter.onUserOffline();
      closeUserActionContainer();
    });
    rootView.findViewById(R.id.deviceprivat).setOnClickListener(view -> {
      if(switch1.isChecked())
        device = "1";
      else
        device = "0";
      Log.i("Test",device);
      postDevice();
    });



    rootView.findViewById(R.id.exetGoogle).setOnClickListener(view -> {
      Toast.makeText(getActivity(), "Текущий пользователь WeWe успешно отвязан от Appleid" ,
              Toast.LENGTH_SHORT).show();
    });

    rootView.findViewById(R.id.supportConnect).setOnClickListener(view -> {
      Intent email = new Intent(Intent.ACTION_SEND);
      email.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@weltwelle.com"});
      email.putExtra(Intent.EXTRA_SUBJECT, "Support on Android application");
      email.putExtra(Intent.EXTRA_TEXT, "Support");
      email.setType("message/rfc822");
      startActivity(Intent.createChooser(email, ""));
    });

    rootView.findViewById(R.id.policy_mss).setOnClickListener(view -> {
    Intent i = new Intent(Intent.ACTION_VIEW,
            Uri.parse("https://weltwelle.com/privpolicy.html"));
    startActivity(i);
    });
      rootView.findViewById(R.id.btn_language).setOnClickListener(view -> {
      actionContainers.setVisibility(View.GONE);
        languageLayaut.setVisibility(View.VISIBLE);
        secyrityLayaut.setVisibility(View.GONE);
  });
    rootView.findViewById(R.id.switch2).setOnClickListener(view -> {
      SharedPreferences.Editor ed = SipData.edit();
      if(SipData.getString("INNER_GROUP", "false").equals("true"))
        ed.putString("INNER_GROUP", "false");
      else
        ed.putString("INNER_GROUP", "true");
      ed.commit();
    });

    rootView.findViewById(R.id.nazadq).setOnClickListener(view -> {
      languageLayaut.setVisibility(View.GONE);
      actionContainers.setVisibility(View.VISIBLE);
      secyrityLayaut.setVisibility(View.GONE);
    });

    rootView.findViewById(R.id.securityBtn).setOnClickListener(view -> {
   /*   languageLayaut.setVisibility(View.GONE);
      actionContainers.setVisibility(View.GONE);
      secyrityLayaut.setVisibility(View.VISIBLE);*/
      startActivity(new Intent(getContext(), PinCode.class));
    });

    rootView.findViewById(R.id.noKey).setOnClickListener(view -> {
      languageLayaut.setVisibility(View.GONE);
      actionContainers.setVisibility(View.VISIBLE);
      secyrityLayaut.setVisibility(View.GONE);
    });

    rootView.findViewById(R.id.okKey).setOnClickListener(view -> {
      languageLayaut.setVisibility(View.GONE);
      actionContainers.setVisibility(View.VISIBLE);
      secyrityLayaut.setVisibility(View.GONE);
    });

    rootView.findViewById(R.id.languagede).setOnClickListener(view -> {
      Locale locale = new Locale("en");
      Locale.setDefault(locale);
      Configuration configuration = new Configuration();
      configuration.locale = locale;
      getActivity().getBaseContext().getResources().updateConfiguration(configuration, null);
    });
  }

  private void onRenderCurrentUser(User user, RocketChatAbsoluteUrl absoluteUrl) {
    if (user != null && absoluteUrl != null) {
      new UserRenderer(getContext(), user)
          .avatarInto((RocketChatAvatar) rootView.findViewById(R.id.current_user_avatar),
              absoluteUrl)
          .usernameInto((TextView) rootView.findViewById(R.id.current_user_name))
          .statusColorInto((ImageView) rootView.findViewById(R.id.current_user_status));
    }
  }

  private void updateRoomListMode(User user) {
    final List<RoomListHeader> roomListHeaders = new ArrayList<>();

    if (user != null && user.getSettings() != null && user.getSettings().getPreferences() != null
        && user.getSettings().getPreferences().isUnreadRoomsMode()) {
      roomListHeaders.add(new UnreadRoomListHeader(
          getString(R.string.fragment_sidebar_main_unread_rooms_title)
      ));
    }

    roomListHeaders.add(new FavoriteRoomListHeader(
        getString(R.string.fragment_sidebar_main_favorite_title)
    ));
    /*  roomListHeaders.add(new DirectMessageRoomListHeader(
            getString(R.string.fragment_sidebar_main_direct_messages_title),
            () -> showAddRoomDialog(AddDirectMessageDialogFragment.create(hostname))

    ));
   roomListHeaders.add(new ChannelRoomListHeader(getString(R.string.fragment_sidebar_main_direct_messages_title),
        () -> showAddRoomDialog(AddChannelDialogFragment.create(hostname))
    ));*/


    adapter.setRoomListHeaders(roomListHeaders);
  }

  private void setupLogoutButton() {
    rootView.findViewById(R.id.btn_logout).setOnClickListener(view -> {
      Intent offLineIntent = new Intent(getActivity(), PortSipService.class);
      offLineIntent.setAction(PortSipService.ACTION_SIP_UNREGIEST);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        getActivity().startForegroundService(offLineIntent);
      }else{
        getActivity().startService(offLineIntent);
      }
      ed.putString("UF_SIP_NUMBER", null);
      ed.commit();
      presenter.onLogout();
      closeUserActionContainer();
      // destroy Activity on logout to be able to recreate most of the environment
  //    this.getActivity().finish();
      startActivity(new Intent(getActivity(),Intro.class));
      finish();
      callstatic=0;

    });


  }

  private void closeUserActionContainer() {
    final CompoundButton toggleUserAction =
        ((CompoundButton) rootView.findViewById(R.id.toggle_user_action));
    if (toggleUserAction != null && toggleUserAction.isChecked()) {
      toggleUserAction.setChecked(false);
    }
  }

  private void setupVersionInfo() {
    TextView versionInfoView = (TextView) rootView.findViewById(R.id.version_info);
    versionInfoView.setText(getString(R.string.version_info_text, BuildConfig.VERSION_NAME));
  }

  private void showAddRoomDialog(DialogFragment dialog) {
    dialog.show(getFragmentManager(), "AbstractAddRoomDialogFragment");
  }

  @Override
  public void showScreen() {
    rootView.setVisibility(View.VISIBLE);
  }

  @Override
  public void showEmptyScreen() {
    rootView.setVisibility(View.INVISIBLE);
  }

  @Override
  public void showRoomList(@NonNull List<Room> roomList) {
    adapter.setRooms(roomList);
  }

  @Override
  public void show(User user, RocketChatAbsoluteUrl absoluteUrl) {
    onRenderCurrentUser(user, absoluteUrl);
    updateRoomListMode(user);
  }

  private void showSearchSuggestions(List<SpotlightRoom> spotlightRooms) {
    adapter.setSpotlightRoomList(spotlightRooms);
  }

  private void postDevice(){
    mApiServiceChat.postDevice(" KEY:"+TOKENwe,device)
            .enqueue(new Callback<ResponseBody>() {
              @Override
              public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()){
                  try {
                    JSONObject jsonRESULTS = new JSONObject(response.body().string());
                    if (jsonRESULTS.getString("status").equals("200")){
                      String UF_ROCKET_LOGIN = jsonRESULTS.getJSONObject("result").getString("UF_ROCKET_LOGIN");
                      String UF_ROCKET_PASSWORD = jsonRESULTS.getJSONObject("result").getString("UF_ROCKET_PASSWORD");
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

  private void animateHide(final View view) {
    view.animate().scaleX(0).scaleY(0).setDuration(150).withEndAction(new Runnable() {
      @Override
      public void run() {
        view.setVisibility(GONE);
      }
    });
  }

  private void animateShow(final View view) {
    view.animate().scaleX(1).scaleY(1).setDuration(150).withStartAction(new Runnable() {
      @Override
      public void run() {
        view.setVisibility(VISIBLE);
      }
    });
  }

  public void getStatus(String roomName){
      statusRoom.setImageResource(R.drawable.ic_at_white_24dp);
    mApiServiceChat.getStatus(roomName)
            .enqueue(new Callback<ResponseBody>() {
              @Override
              public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()){
                  try {
                    JSONObject jsonRESULTS = new JSONObject(response.body().string());
                    if(jsonRESULTS.getJSONObject("user").getString("status").equals("online")){

                        statusRoom.setImageResource(R.drawable.ic_at_gray_24dp);
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
                //   Log.e("debug", "onFailure: ERROR > " + t.toString());

              }
            });
  }





}
