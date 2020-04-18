package chat.wewe.android.fragment.chatroom;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.os.BuildCompat;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.fernandocejas.arrow.optional.Optional;
import com.jakewharton.rxbinding2.support.v4.widget.RxDrawerLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


import chat.wewe.android.BackgroundLooper;
import chat.wewe.android.R;
import chat.wewe.android.activity.MainActivity;
import chat.wewe.android.api.BaseApiService;
import chat.wewe.android.api.MethodCallHelper;
import chat.wewe.android.api.UtilsApi;
import chat.wewe.android.api.UtilsApiChat;
import chat.wewe.android.fragment.chatroom.dialog.FileUploadProgressDialogFragment;
import chat.wewe.android.fragment.chatroom.dialog.MessageOptionsDialogFragment;
import chat.wewe.android.fragment.chatroom.dialog.UsersOfRoomDialogFragment;
import chat.wewe.android.fragment.sidebar.dialog.AddChannelDialogFragment;
import chat.wewe.android.fragment.sidebar.dialog.AddTaskFragment;
import chat.wewe.android.fragment.sidebar.dialog.AddUsersDialogFragment;
import chat.wewe.android.helper.AbsoluteUrlHelper;
import chat.wewe.android.helper.FileUploadHelper;
import chat.wewe.android.helper.KeyboardHelper;
import chat.wewe.android.helper.LoadMoreScrollListener;
import chat.wewe.android.helper.Logger;
import chat.wewe.android.helper.OnBackPressListener;
import chat.wewe.android.helper.RecyclerViewAutoScrollManager;
import chat.wewe.android.helper.RecyclerViewScrolledToBottomListener;
import chat.wewe.android.helper.TextUtils;
import chat.wewe.android.layouthelper.chatroom.AbstractNewMessageIndicatorManager;
import chat.wewe.android.layouthelper.chatroom.MessageFormManager;
import chat.wewe.android.layouthelper.chatroom.MessageListAdapter;
import chat.wewe.android.layouthelper.chatroom.ModelListAdapter;
import chat.wewe.android.layouthelper.chatroom.PairedMessage;
import chat.wewe.android.layouthelper.extra_action.AbstractExtraActionItem;
import chat.wewe.android.layouthelper.extra_action.MessageExtraActionBehavior;
import chat.wewe.android.layouthelper.extra_action.upload.AbstractUploadActionItem;
import chat.wewe.android.layouthelper.extra_action.upload.AudioUploadActionItem;
import chat.wewe.android.layouthelper.extra_action.upload.FileUploadActionItem;
import chat.wewe.android.layouthelper.extra_action.upload.ImageUploadActionItem;
import chat.wewe.android.layouthelper.extra_action.upload.VideoUploadActionItem;
import chat.wewe.android.log.RCLog;
import chat.wewe.android.renderer.RocketChatUserStatusProvider;
import chat.wewe.android.service.ConnectivityManager;
import chat.wewe.android.service.temp.DeafultTempSpotlightRoomCaller;
import chat.wewe.android.service.temp.DefaultTempSpotlightUserCaller;
import chat.wewe.android.widget.internal.ExtraActionPickerDialogFragment;
import chat.wewe.android.widget.message.MessageFormLayout;
import chat.wewe.android.widget.message.autocomplete.AutocompleteManager;
import chat.wewe.android.widget.message.autocomplete.channel.ChannelSource;
import chat.wewe.android.widget.message.autocomplete.user.UserSource;
import chat.wewe.core.interactors.AutocompleteChannelInteractor;
import chat.wewe.core.interactors.AutocompleteUserInteractor;
import chat.wewe.core.interactors.MessageInteractor;
import chat.wewe.core.interactors.SessionInteractor;
import chat.wewe.core.models.Message;
import chat.wewe.core.models.Room;
import chat.wewe.persistence.realm.RealmStore;
import chat.wewe.persistence.realm.repositories.RealmMessageRepository;
import chat.wewe.persistence.realm.repositories.RealmRoomRepository;
import chat.wewe.persistence.realm.repositories.RealmServerInfoRepository;
import chat.wewe.persistence.realm.repositories.RealmSessionRepository;
import chat.wewe.persistence.realm.repositories.RealmSpotlightRoomRepository;
import chat.wewe.persistence.realm.repositories.RealmSpotlightUserRepository;
import chat.wewe.persistence.realm.repositories.RealmUserRepository;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static chat.wewe.android.activity.MainActivity.BtnCall;
import static chat.wewe.android.activity.MainActivity.btnVideoCall;
import static chat.wewe.android.activity.MainActivity.current_user_name;
import static chat.wewe.android.activity.MainActivity.nazad;
import static chat.wewe.android.activity.MainActivity.statusRoom;
import static chat.wewe.android.activity.MainActivity.task;
import static chat.wewe.android.activity.MainActivity.user_add;
import static chat.wewe.android.ui.VideoFragment.callSed;

/**
 * Chat room screen.
 */
@RuntimePermissions
public class RoomFragment extends AbstractChatRoomFragment implements
        OnBackPressListener,
        ExtraActionPickerDialogFragment.Callback,
        ModelListAdapter.OnItemClickListener<PairedMessage>,
        ModelListAdapter.OnItemLongClickListener<PairedMessage>,
        RoomContract.View {

  private static final int DIALOG_ID = 1;
  private static final String HOSTNAME = "hostname";
  private static final String ROOM_ID = "roomId";
  public static MethodCallHelper onUsersMessage;

  public SharedPreferences SipDataVideo;
  public static ArrayList<Uri> ListImage = new ArrayList<Uri>();
  public static int sq = 0;
  public static boolean updat = true;
  private String hostname;
  private String roomId;
  private LoadMoreScrollListener scrollListener;
  private MessageFormManager messageFormManager;
  private RecyclerViewAutoScrollManager recyclerViewAutoScrollManager;
  protected AbstractNewMessageIndicatorManager newMessageIndicatorManager;
  protected Snackbar unreadIndicator;
  private boolean previousUnreadMessageExists;
  private MessageListAdapter messageListAdapter;
  private AutocompleteManager autocompleteManager;

  private List<AbstractExtraActionItem> extraActionItems;

  private CompositeDisposable compositeDisposable = new CompositeDisposable();

  protected RoomContract.Presenter presenter;

  private RealmRoomRepository roomRepository;
  private RealmUserRepository userRepository;
  private MethodCallHelper methodCallHelper;
  private AbsoluteUrlHelper absoluteUrlHelper;
  SharedPreferences SipData;
  private Message edittingMessage = null;
  List<String> imagesEncodedList;


  public RoomFragment() {}

  public static String token;
  public static String userId;
  private String roomType;
  BaseApiService mApiServiceChat;
  /**
   * create fragment with roomId.
   */

  public static RoomFragment create(String hostname, String roomId) {
    Bundle args = new Bundle();
    args.putString(HOSTNAME, hostname);
    args.putString(ROOM_ID, roomId);

    RoomFragment fragment = new RoomFragment();
    fragment.setArguments(args);

    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    SipDataVideo = getActivity().getSharedPreferences("SIP", MODE_PRIVATE);
    Bundle args = getArguments();
    hostname = args.getString(HOSTNAME);
    roomId = args.getString(ROOM_ID);

    roomRepository = new RealmRoomRepository(hostname);

    MessageInteractor messageInteractor = new MessageInteractor(
        new RealmMessageRepository(hostname),
        roomRepository
    );

    userRepository = new RealmUserRepository(hostname);

    absoluteUrlHelper = new AbsoluteUrlHelper(
        hostname,
        new RealmServerInfoRepository(),
        userRepository,
        new SessionInteractor(new RealmSessionRepository(hostname))
    );

    methodCallHelper = new MethodCallHelper(getContext(), hostname);

    presenter = new RoomPresenter(
        roomId,
        userRepository,
        messageInteractor,
        roomRepository,
        absoluteUrlHelper,
        methodCallHelper,
        ConnectivityManager.getInstance(getContext())
    );

    if (savedInstanceState == null) {
      presenter.loadMessages();
    }



    SipData = getActivity().getSharedPreferences("SIP", MODE_PRIVATE);
    mApiServiceChat = UtilsApiChat.getAPIService();

  }

  @Override
  protected int getLayout() {
    return R.layout.fragment_room;
  }

  @Override
  protected void onSetupView() {
    RecyclerView messageRecyclerView = (RecyclerView) rootView.findViewById(R.id.messageRecyclerView);

    messageListAdapter = new MessageListAdapter(getContext());
    messageRecyclerView.setAdapter(messageListAdapter);
    messageListAdapter.setOnItemClickListener(this);
    messageListAdapter.setOnItemLongClickListener(this);

    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, true);
    messageRecyclerView.setLayoutManager(linearLayoutManager);

    recyclerViewAutoScrollManager = new RecyclerViewAutoScrollManager(linearLayoutManager) {
      @Override
      protected void onAutoScrollMissed() {
        if (newMessageIndicatorManager != null) {
          presenter.onUnreadCount();
        }
      }
    };
    messageListAdapter.registerAdapterDataObserver(recyclerViewAutoScrollManager);

    scrollListener = new LoadMoreScrollListener(linearLayoutManager, 40) {
      @Override
      public void requestMoreItem() {
        presenter.loadMoreMessages();
      }
    };
    messageRecyclerView.addOnScrollListener(scrollListener);
    messageRecyclerView.addOnScrollListener(
        new RecyclerViewScrolledToBottomListener(linearLayoutManager, 1, this::markAsReadIfNeeded));

    newMessageIndicatorManager = new AbstractNewMessageIndicatorManager() {
      @Override
      protected void onShowIndicator(int count, boolean onlyAlreadyShown) {
        if ((onlyAlreadyShown && unreadIndicator != null && unreadIndicator.isShown())
            || !onlyAlreadyShown) {
          unreadIndicator = getUnreadCountIndicatorView(count);
          unreadIndicator.show();
        }
      }

      @Override
      protected void onHideIndicator() {
        if (unreadIndicator != null && unreadIndicator.isShown()) {
          unreadIndicator.dismiss();
        }
      }
    };


    task.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        openDialog();
      }
    });

    user_add.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        openDialogUsers();
      }
    });



    setupSideMenu();
    setupMessageComposer();
    setupMessageActions();
  }

  private void setupMessageActions() {
    extraActionItems = new ArrayList<>(4); // fixed number as of now
    extraActionItems.add(new ImageUploadActionItem());
    extraActionItems.add(new AudioUploadActionItem());
    extraActionItems.add(new VideoUploadActionItem(SipDataVideo.getBoolean("VIDEO_C", true)));
    extraActionItems.add(new FileUploadActionItem());
  }

  private void scrollToLatestMessage() {
    RecyclerView listView = (RecyclerView) rootView.findViewById(R.id.messageRecyclerView);
    if (listView != null) {
      listView.scrollToPosition(0);
    }
  }

  protected Snackbar getUnreadCountIndicatorView(int count) {
    // TODO: replace with another custom View widget, not to hide message composer.
    final String caption = getResources().getString(
        R.string.fmt_dialog_view_latest_message_title, count);

    return Snackbar.make(rootView, caption, Snackbar.LENGTH_LONG)
        .setAction(R.string.dialog_view_latest_message_action, view -> scrollToLatestMessage());
  }

  @Override
  public void onDestroyView() {
    RecyclerView listView = (RecyclerView) rootView.findViewById(R.id.messageRecyclerView);
    if (listView != null) {
      RecyclerView.Adapter adapter = listView.getAdapter();
      if (adapter != null) {
        adapter.unregisterAdapterDataObserver(recyclerViewAutoScrollManager);
      }
    }

    compositeDisposable.clear();

    if (autocompleteManager != null) {
      autocompleteManager.dispose();
      autocompleteManager = null;
    }

    super.onDestroyView();
  }

  @Override
  public void onItemClick(PairedMessage pairedMessage) {
    presenter.onMessageSelected(pairedMessage.target);
  }

  @Override
  public boolean onItemLongClick(PairedMessage pairedMessage) {
    MessageOptionsDialogFragment messageOptionsDialogFragment = MessageOptionsDialogFragment
        .create(pairedMessage.target);

    messageOptionsDialogFragment.setOnMessageOptionSelectedListener(message -> {
      messageOptionsDialogFragment.dismiss();
      onEditMessage(message);

    });

    messageOptionsDialogFragment.setOnMessageOptionSelectedListeners(message -> {
      messageOptionsDialogFragment.dismiss();
     onDeleteMessage(message);
    });

    messageOptionsDialogFragment.setOnMessageOptionSelectedListenerss(message -> {
      messageOptionsDialogFragment.dismiss();
      onCopy(message);

    });

    messageOptionsDialogFragment.setOnMessageOptionSelectedListenersss(message -> {
      messageOptionsDialogFragment.dismiss();
      forwardMessage(message);

    });


    messageOptionsDialogFragment.show(getChildFragmentManager(), "MessageOptionsDialogFragment");
    return true;
  }

  private void setupSideMenu() {
    View sideMenu = rootView.findViewById(R.id.room_side_menu);
    sideMenu.findViewById(R.id.btn_users).setOnClickListener(view -> {
      UsersOfRoomDialogFragment.create(roomId, hostname)
          .show(getFragmentManager(), "UsersOfRoomDialogFragment");
      closeSideMenuIfNeeded();
    });

    DrawerLayout drawerLayout = (DrawerLayout) rootView.findViewById(R.id.drawer_layout);
    SlidingPaneLayout pane = (SlidingPaneLayout) getActivity().findViewById(R.id.sliding_pane);
    if (drawerLayout != null && pane != null) {
      compositeDisposable.add(RxDrawerLayout.drawerOpen(drawerLayout, GravityCompat.END)
          .compose(bindToLifecycle())
          .subscribe(
              opened -> {
                try {
                  Field fieldSlidable = pane.getClass().getDeclaredField("mCanSlide");
                  fieldSlidable.setAccessible(true);
                  fieldSlidable.setBoolean(pane, !opened);
                } catch (Exception exception) {
                  RCLog.w(exception);
                }
              },
              Logger::report
          )
      );
    }
  }

  private boolean closeSideMenuIfNeeded() {
    DrawerLayout drawerLayout = (DrawerLayout) rootView.findViewById(R.id.drawer_layout);
    if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.END)) {
      drawerLayout.closeDrawer(GravityCompat.END);
      return true;
    }
    return false;
  }

  private void setupMessageComposer() {
    final MessageFormLayout messageFormLayout = (MessageFormLayout) rootView.findViewById(R.id.messageComposer);
    messageFormManager = new MessageFormManager(messageFormLayout, this::showExtraActionSelectionDialog);
    messageFormManager.setSendMessageCallback(this::sendMessage);
    messageFormLayout.setEditTextCommitContentListener(this::onCommitContent);

    autocompleteManager = new AutocompleteManager((ViewGroup) rootView.findViewById(R.id.messageListRelativeLayout));

    autocompleteManager.registerSource(
        new ChannelSource(
            new AutocompleteChannelInteractor(
                roomRepository,
                new RealmSpotlightRoomRepository(hostname),
                new DeafultTempSpotlightRoomCaller(methodCallHelper)
            ),
            AndroidSchedulers.from(BackgroundLooper.get()),
            AndroidSchedulers.mainThread()
        )
    );

    Disposable disposable = Single.zip(
        absoluteUrlHelper.getRocketChatAbsoluteUrl(),
        roomRepository.getById(roomId).first(Optional.absent()),
        Pair::create
    )
        .subscribe(
            pair -> {
              if (pair.first.isPresent() && pair.second.isPresent()) {
                autocompleteManager.registerSource(
                    new UserSource(
                        new AutocompleteUserInteractor(
                            pair.second.get(),
                            userRepository,
                            new RealmMessageRepository(hostname),
                            new RealmSpotlightUserRepository(hostname),
                            new DefaultTempSpotlightUserCaller(methodCallHelper)
                        ),
                        pair.first.get(),
                        RocketChatUserStatusProvider.getInstance(),
                        AndroidSchedulers.from(BackgroundLooper.get()),
                        AndroidSchedulers.mainThread()
                    )
                );
              }
            },
            throwable -> {
            }
        );

    compositeDisposable.add(disposable);

    autocompleteManager.bindTo(
        messageFormLayout.getEditText(),
        messageFormLayout
    );
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    sq=0;
    Log.d("ImageUploadActionItem", "TE"+data.getData()+"QQ" +requestCode+"RR"+ resultCode+"ff"+data);
    if (requestCode != AbstractUploadActionItem.RC_UPL || resultCode != Activity.RESULT_OK) {
      Log.d("ImageUploadActionItem1", "Yes");
      return;
    }
    if (data == null || data.getData() == null) {

      ClipData mClipData = data.getClipData();
      ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
      ListImage.clear();
      sq=0;
      for (int i = 0; i < mClipData.getItemCount(); i++) {

        ClipData.Item item = mClipData.getItemAt(i);
        Uri uri = item.getUri();
        Log.d("ImageUploadActionItem2", "Yes"+uri);
        ListImage.add(uri);

      }
      if(ListImage.size()<=20) {
        Timer t = new Timer();
        t.schedule(new TimerTask() {
          public void run() {
            if (updat && ListImage.size()-1>=sq) {
              uploadFile(ListImage.get(sq));
              updat = false;
              sq++;
            } else {
              Log.d("ImageUploadActionItem2", "Nice!");
            }
            if(ListImage.size()<=sq) {
              t.cancel();
              t.purge();
              Log.d("ImageUploadActionItem2", "STOP!");
            }
          }
        }, 0, 4000);

      }else{
        Toast.makeText(getActivity(), "Доступно 20 фото",
                Toast.LENGTH_SHORT).show();
      }
    }else{
      Log.d("ImageUploadActionItem2", "FACK!");
      uploadFile(data.getData());
    }

  }

  private void uploadFile(Uri uri) {
    String uplId = new FileUploadHelper(getContext(), RealmStore.get(hostname))
        .requestUploading(roomId, uri);
    if (!TextUtils.isEmpty(uplId)) {
      FileUploadProgressDialogFragment.create(hostname, roomId, uplId)
          .show(getFragmentManager(), "FileUploadProgressDialogFragment");
    } else {
      // show error.
    }
  }



  public static boolean hasOpenedDialogs(FragmentActivity activity) {
    List<Fragment> fragments = activity.getSupportFragmentManager().getFragments();
    if (fragments != null) {
      for (Fragment fragment : fragments) {
        if (fragment instanceof DialogFragment) {
          return true;
        }
      }
    }

    return false;
  }

  private void markAsReadIfNeeded() {
    presenter.onMarkAsRead();
  }

  @Override
  public void onResume() {
    super.onResume();
    presenter.bindView(this);
    closeSideMenuIfNeeded();



    if (callSed == 2) {
      callSed = 0;
      methodCallHelper.usersMessage(true, "0", userId, roomId.replaceAll(userId, ""), roomId);
    } else if (callSed == 1) {
      callSed = 0;
      methodCallHelper.usersMessage(false, "0", userId, roomId.replaceAll(userId, ""), roomId);

    }

  }



  @Override
  public void onPause() {
    presenter.release();
    super.onPause();
  }

  private void showExtraActionSelectionDialog() {
    final DialogFragment fragment = ExtraActionPickerDialogFragment
        .create(new ArrayList<>(extraActionItems));
    fragment.setTargetFragment(this, DIALOG_ID);
    fragment.show(getFragmentManager(), "ExtraActionPickerDialogFragment");
  }

  @Override
  public void onItemSelected(int itemId) {
    for (AbstractExtraActionItem extraActionItem : extraActionItems) {
      if (extraActionItem.getItemId() == itemId) {
        RoomFragmentPermissionsDispatcher.onExtraActionSelectedWithCheck(RoomFragment.this, extraActionItem);
        return;
      }
    }
  }

  @Override
  public boolean onBackPressed() {
    if (edittingMessage != null) {
      edittingMessage = null;
      messageFormManager.clearComposingText();
      return true;
    }
    return closeSideMenuIfNeeded();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    RoomFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
  }

  @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
  protected void onExtraActionSelected(MessageExtraActionBehavior action) {
    action.handleItemSelectedOnFragment(RoomFragment.this);
  }

  private boolean onCommitContent(InputContentInfoCompat inputContentInfo, int flags,
                                  Bundle opts, String[] supportedMimeTypes) {
    boolean supported = false;
    for (final String mimeType : supportedMimeTypes) {
      if (inputContentInfo.getDescription().hasMimeType(mimeType)) {
        supported = true;
        break;
      }
    }

    if (!supported) {
      return false;
    }

    if (BuildCompat.isAtLeastNMR1()
        && (flags & InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0) {
      try {
        inputContentInfo.requestPermission();
      } catch (Exception e) {
        return false;
      }
    }

    Uri linkUri = inputContentInfo.getLinkUri();
    if (linkUri == null) {
      return false;
    }

    sendMessage(linkUri.toString());

    try {
      inputContentInfo.releasePermission();
    } catch (Exception e) {
    }

    return true;
  }

  private void sendMessage(String messageText) {
    if (edittingMessage == null) {
      presenter.sendMessage(messageText);
    } else {
      presenter.updateMessage(edittingMessage, messageText);
    }
  }

  @Override
  public void setupWith(@NonNull RocketChatAbsoluteUrl rocketChatAbsoluteUrl) {
    messageListAdapter.setAbsoluteUrl(rocketChatAbsoluteUrl);

    if (rocketChatAbsoluteUrl != null) {
      token = rocketChatAbsoluteUrl.getToken();
      userId = rocketChatAbsoluteUrl.getUserId();
      messageListAdapter.setAbsoluteUrl(rocketChatAbsoluteUrl);

      getActivity().getSharedPreferences("SIP", MODE_PRIVATE)
              .edit()
              .putString("TOKEN_RC", token)
              .putString("ID_RC", userId)
              .putString("RM_ID", roomId)
              .putString("hostname", hostname)
              .commit();
    }
 }

  @Override
  public void render(Room room) {
    String type = room.getType();
      if(nazad.getVisibility()==VISIBLE)
      current_user_name.setText(" " +room.getName());

    getStatus(room.getName(),token,userId);
    roomType = room.getType();
    if (Room.TYPE_CHANNEL.equals(type)) {
      animateShow(user_add);
      animateShow(task);
      animateHide(BtnCall);
      animateHide(btnVideoCall);
      Log.d("yhntgb","TYPE  TYPE_CHANNEL"+roomId);
    } else if (Room.TYPE_PRIVATE.equals(type)) {
      animateShow(task);
      animateShow(user_add);
      animateHide(BtnCall);
      animateHide(btnVideoCall);
      Log.d("yhntgb","TYPE  TYPE_PRIVATE"+roomId);
    } else if (Room.TYPE_DIRECT_MESSAGE.equals(type)) {
      animateShow(BtnCall);
      animateShow(btnVideoCall);
      animateHide(task);
      animateHide(user_add);
      Log.d("yhntgb","TYPE  TYPE_DIRECT_MESSAGE"+roomId);

    } else {
      Log.d("yhntgb","TYPE  ELSE");
      setToolbarRoomIcon(0);
    }



    boolean unreadMessageExists = room.isAlert();
    if (newMessageIndicatorManager != null && previousUnreadMessageExists && !unreadMessageExists) {
      newMessageIndicatorManager.reset();
    }
    previousUnreadMessageExists = unreadMessageExists;


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

  @Override
  public void updateHistoryState(boolean hasNext, boolean isLoaded) {
    RecyclerView listView = (RecyclerView) rootView.findViewById(R.id.messageRecyclerView);
    if (listView == null || !(listView.getAdapter() instanceof MessageListAdapter)) {
      return;
    }

    MessageListAdapter adapter = (MessageListAdapter) listView.getAdapter();
    if (isLoaded) {
      scrollListener.setLoadingDone();
    }
    adapter.updateFooter(hasNext, isLoaded);
  }

  @Override
  public void onMessageSendSuccessfully() {
    scrollToLatestMessage();
    messageFormManager.onMessageSend();
    edittingMessage = null;
  }

  @Override
  public void showUnreadCount(int count) {
    newMessageIndicatorManager.updateNewMessageCount(count);
  }

  @Override
  public void showMessages(List<Message> messages) {
    if (messageListAdapter == null) {
      return;
    }
    messageListAdapter.updateData(messages);
  }

  @Override
  public void showMessageSendFailure(Message message) {
    new AlertDialog.Builder(getContext())
        .setPositiveButton(R.string.resend,
            (dialog, which) -> presenter.resendMessage(message))
        .setNegativeButton(android.R.string.cancel, null)
        .setNeutralButton(R.string.discard,
            (dialog, which) -> presenter.deleteMessage(message))
        .show();

  }

  @Override
  public void autoloadImages() {
    messageListAdapter.setAutoloadImages(true);
  }

  @Override
  public void manualLoadImages() {
    messageListAdapter.setAutoloadImages(false);
  }

  private void onEditMessage(Message message) {
    edittingMessage = message;
    messageFormManager.setEditMessage(message.getMessage());
  }

  public void onDeleteMessage(Message message) {
    methodCallHelper.deleteMessage(message.getId());
  }

  public void forwardMessage(Message message) {

    openDialogUserss(message);

  }

  public void onCopy(Message message) {

    try {
      ClipboardManager clipboardManager =
              (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
      clipboardManager.setPrimaryClip(ClipData.newPlainText("linkURL", message.getMessage()));

    } catch (Exception exception) {
    }

  }

  public void openDialog() {
    AddTaskFragment task = new AddTaskFragment().create(hostname,roomId,userId);
    task.setActionListener(new AddTaskFragment.ActionListener() {
      @Override
      public void onClick(String uid) {
        showRoom(uid);
      }
    });
    task.show(getActivity().getSupportFragmentManager(), "example dialog");
  }

  public void openDialogUserss(Message message) {

    AddUsersDialogFragment di = new AddUsersDialogFragment().create(hostname,roomId,userId,1);
    di.setActionListener(new AddUsersDialogFragment.ActionListener() {
      @Override
      public void onClick(String uid) {
       methodCallHelper.forwardMessage(message.getId(),uid,roomId);
        showRoom(uid);

      }
    });
    di.show(getActivity().getSupportFragmentManager(), "example dialog");


  }

  public void openDialogUsers() {

    AddUsersDialogFragment di = new AddUsersDialogFragment().create(hostname,roomId,userId,0);
    di.setActionListener(new AddUsersDialogFragment.ActionListener() {
      @Override
      public void onClick(String uid) {
     sendUsers(uid);
      }
    });
    di.show(getActivity().getSupportFragmentManager(), "example dialog");


  }

  public void getStatus(String roomName,String token,String id){
    Log.d("Status3","true"+roomName + token +id);
    statusRoom.setImageResource(R.drawable.ic_at_white_24dp);
    mApiServiceChat.getStatus(roomName,token,id)
            .enqueue(new Callback<ResponseBody>() {
              @Override
              public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()){
                  try {
                    JSONObject jsonRESULTS = new JSONObject(response.body().string());
                    Log.d("Status3","true");
                    if(jsonRESULTS.getJSONObject("user").getString("status").equals("online")){
                      statusRoom.setImageResource(R.drawable.ic_at_gray_24dp);
                      Log.d("Status3","true");}
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

  public void showRoom(String roomId){
    ((MainActivity)getActivity()).showRoom(hostname,roomId);
  }



  public void sendUsers(String msg){
    presenter.sendMessage("@"+msg + " Добавить");
  }
}
