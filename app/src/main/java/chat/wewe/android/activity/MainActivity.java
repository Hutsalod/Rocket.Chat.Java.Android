package chat.wewe.android.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.util.ArrayMap;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fernandocejas.arrow.optional.Optional;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import bolts.Task;
import chat.wewe.android.LaunchUtil;
import chat.wewe.android.R;
import chat.wewe.android.RocketChatCache;
import chat.wewe.android.Success;
import chat.wewe.android.api.BaseApiService;
import chat.wewe.android.api.JoinWeWe;
import chat.wewe.android.api.MethodCallHelper;
import chat.wewe.android.api.UtilsApi;
import chat.wewe.android.api.UtilsApiChat;
import chat.wewe.android.api.model.sub;
import chat.wewe.android.fragment.chatroom.HomeFragment;
import chat.wewe.android.fragment.chatroom.ListFileFragment;
import chat.wewe.android.fragment.chatroom.ListMessageFragment;
import chat.wewe.android.fragment.chatroom.NotificationFragment;
import chat.wewe.android.fragment.chatroom.RocketChatAbsoluteUrl;
import chat.wewe.android.fragment.chatroom.RoomFragment;
import chat.wewe.android.fragment.sidebar.SidebarMainFragment;
import chat.wewe.android.fragment.sidebar.dialog.AddChannelDialogFragment;
import chat.wewe.android.helper.AbsoluteUrlHelper;
import chat.wewe.android.helper.KeyboardHelper;
import chat.wewe.android.layouthelper.chatroom.dialog.RoomUserAdapter;
import chat.wewe.android.layouthelper.sidebar.dialog.SuggestUserAdapter;
import chat.wewe.android.service.PortSipService;
import chat.wewe.core.SyncState;
import chat.wewe.core.interactors.CanCreateRoomInteractor;
import chat.wewe.core.interactors.RoomInteractor;
import chat.wewe.core.interactors.SessionInteractor;
import chat.wewe.android.service.ConnectivityManager;
import chat.wewe.android.widget.RoomToolbar;
import chat.wewe.persistence.realm.RealmAutoCompleteAdapter;
import chat.wewe.persistence.realm.models.ddp.RealmUser;
import chat.wewe.persistence.realm.models.internal.FileUploading;
import chat.wewe.persistence.realm.repositories.RealmRoomRepository;
import chat.wewe.persistence.realm.repositories.RealmServerInfoRepository;
import chat.wewe.persistence.realm.repositories.RealmSessionRepository;
import chat.wewe.persistence.realm.repositories.RealmUserRepository;
import hugo.weaving.DebugLog;
import io.reactivex.disposables.CompositeDisposable;
import io.realm.Case;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static chat.wewe.android.activity.ContactAdapter.a_chars;
import static chat.wewe.android.activity.Intro.StatusU;
import static chat.wewe.android.activity.Intro.TOKEN_RC;
import static chat.wewe.android.activity.Intro.UF_SIP_NUMBER;
import static chat.wewe.android.activity.Intro.UF_SIP_PASSWORD;
import static chat.wewe.android.activity.Intro.callCout;
import static chat.wewe.android.activity.Intro.callstatic;
import static chat.wewe.android.activity.Intro.callSet;
import static chat.wewe.android.activity.Intro.subscription;
import static chat.wewe.android.fragment.sidebar.SidebarMainFragment.adapter;
import static chat.wewe.android.fragment.sidebar.SidebarMainFragment.getName;
import static java.security.AccessController.getContext;

/**
 * Entry-point for Rocket.Chat.Android application.
 */
public class MainActivity extends AbstractAuthedActivity implements MainContract.View,AdapterView.OnItemClickListener,PopupMenu.OnMenuItemClickListener {

  private StatusTicker statusTicker;
  private MainContract.Presenter presenter;
  public static LinearLayout chat;
  private LinearLayout call,contacts, setting,kayboardLayout,openKey;
  public static LinearLayout callUsers;
  public static AppCompatAutoCompleteTextView editText;
  public static RecyclerView recyclerViews;
  public static  ImageView nazad,btnCreate,BtnCall,btnVideoCall,statusRoom,search_btn_users,task;
  public  ImageView statusUsers,statusUsers2,statusUsers3,statusUsers4, btnSearch;
  public static   TextView current_user_name;
  private CountDownTimer countDownTimer;
  public static  FrameLayout activity_main_container;
  private  EditText searchContact;
  private ContactAdapter mAdapter;
  public static String getUserNameCall;
  public static BottomNavigationView navigation;
  public static SwitchCompat switch2;
  private RecyclerView mRecyclerView;
  private ExampleAdapter mAdapters;
  private RecyclerView.LayoutManager mLayoutManager;
  static View waitingView;
  private CompositeDisposable compositeDisposable = new CompositeDisposable();
  private static String[] PERMISSION_CONTACT = {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS};
  private static final int REQUEST_CONTACT = 1;
  public static int setnupad=0,setContact=0;
  private List<ContactModel> contactModelList = new ArrayList<>();

  BaseApiService mApiServiceChat,mApiService;
  TextInputEditText EditTextName;
  Intent callInt;
  ArrayList<ExampleItem> mExampleList;
  SharedPreferences SipData;
  String name;
  AutoCompleteTextView autoCompleteTextView;
  ContactAdapter contactAdapter;
  RecyclerView recyclerView;
  String[] mCats;
  ArrayList<String> mCatsList;

  public static boolean active = false;

  @Override
  public void onStart() {
    super.onStart();
    active = false;
  }

  @Override
  public void onStop() {
    super.onStop();
    active = true;
  }


  @Override
  protected int getLayoutContainerForFragment() {
    return R.id.activity_main_container;
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mApiService = UtilsApi.getAPIService();
    mApiServiceChat = UtilsApiChat.getAPIService();
    SipData = getSharedPreferences("SIP", MODE_PRIVATE);
    callInt = new Intent(getApplicationContext(), chat.wewe.android.ui.MainActivity.class);



    navigation = (BottomNavigationView) findViewById(R.id.navigation);
    chat = (LinearLayout) findViewById(R.id.chat);
    call = (LinearLayout) findViewById(R.id.call);
    contacts = (LinearLayout) findViewById(R.id.contacts);
    setting = (LinearLayout) findViewById(R.id.setting);
    kayboardLayout = (LinearLayout) findViewById(R.id.kayboardLayout);
    recyclerViews = (RecyclerView) findViewById(R.id.room_list_container);
    openKey = (LinearLayout) findViewById(R.id.openKey);
    search_btn_users = (ImageView) findViewById(R.id.search_btn_users);
    nazad = (ImageView) findViewById(R.id.nazad);
    statusRoom = (ImageView) findViewById(R.id.statusRoom);
    btnCreate = (ImageView) findViewById(R.id.btnCreate);
    activity_main_container = (FrameLayout) findViewById(R.id.activity_main_container);
    editText = (AppCompatAutoCompleteTextView) findViewById(R.id.editText);
    current_user_name = (TextView) findViewById(R.id.current_user_name);
    callUsers = (LinearLayout) findViewById(R.id.callUsers);
    task = (ImageView) findViewById(R.id.task);
    BtnCall = (ImageView) findViewById(R.id.BtnCall);
    btnSearch = (ImageView) findViewById(R.id.btnSearch);
    statusUsers = (ImageView) findViewById(R.id.stanUsers);
    statusUsers2 = (ImageView) findViewById(R.id.stanUsers2);
    statusUsers3 = (ImageView) findViewById(R.id.stanUsers3);
    statusUsers4  = (ImageView) findViewById(R.id.stanUsers4);
    btnVideoCall = (ImageView) findViewById(R.id.btnVideoCall);
    searchContact =  (EditText) findViewById(R.id.searchContact);
    mAdapter = new ContactAdapter(this,contactModelList);
    EditTextName = (TextInputEditText)findViewById(R.id.EditTextName);
    switch2 = (SwitchCompat)findViewById(R.id.switch2);
    recyclerView = (RecyclerView)findViewById(R.id.rv);
    waitingView = findViewById(R.id.waiting_serch);
    navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);




    statusTicker = new StatusTicker();
    setupSidebar();
    setDataToAdapter();
    navigation.setSelectedItemId(R.id.action_chat);
    Log.d("XSWQAZ","ADD ");
    BtnCall.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        if (subscription == true) {
          callSet = false;
          setnupad = 1;
          startActivity(callInt);
          setInsertButton();
          Log.d("XSWQAZ","ADD ");
        }else{//INNER_GROUP Для теста
          if(SipData.getString("INNER_GROUP", "false").equals("false")){
            startActivity(new Intent(getApplicationContext(), Success.class));

          }else {
            callSet = false;
            setnupad = 1;
            startActivity(callInt);
              setInsertButton();

          }
        }
        saveData();
      }
    });
    btnVideoCall.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View view) {
        if (subscription == true) {
          callSet = true;
          setnupad = 2;
          startActivity(callInt);
          setInsertButton();
          Log.d("XSWQAZ","ADD ");
        }else{//INNER_GROUP Для теста
          if(SipData.getString("INNER_GROUP", "false").equals("false")){
            startActivity(new Intent(getApplicationContext(), Success.class));

          }else {
            callSet = true;
            setnupad = 2;
            startActivity(callInt);
              setInsertButton();

          }}

        saveData();
      }
    });

    btnSearch.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ListMessageFragment exampleDialog = ListMessageFragment.create(hostname,roomId);
        exampleDialog.show(getSupportFragmentManager(), "example dialog");
      }
    });

    loadData();
    buildRecyclerView();

    if(callCout==1) {
      setInsertButton();
      saveData();
      callCout=0;
    }
    btnCreate.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View view) {
     openDialog();
      }

    });
    search_btn_users.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View view) {
        waitingView.setVisibility(VISIBLE);
        createRoom();
        waitingView.setVisibility(GONE);
    }
    });

    autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.editText);

    AbsoluteUrlHelper absoluteUrlHelper = new AbsoluteUrlHelper(
            hostname,
            new RealmServerInfoRepository(),
            new RealmUserRepository(hostname),
            new SessionInteractor(new RealmSessionRepository(hostname))
    );

    autoCompleteTextView.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        if (s.length() > 0) {
          animateShow(search_btn_users);
          getList();
        } else {
          animateShow(search_btn_users);
          animateHide(search_btn_users);
        }
      }
    });

    autoCompleteTextView.setOnItemClickListener(this);
    UF_ORIGINAL_TRID();
    UserStatus();

    countDownTimer = new CountDownTimer(15000, 1000) {
      @Override
      public void onTick(long millisUntilFinished) {
          getStatus();
      }
      @Override
      public void onFinish() {
        countDownTimer.start();
      }
    };
    countDownTimer.start();


   if(SipData.getString("UF_SIP_NUMBER", "")!="" && callstatic==0 && StatusU>=4) {
     SaveUserInfo();
             Intent onLineIntent = new Intent(getBaseContext(), PortSipService.class);
        onLineIntent.setAction(PortSipService.ACTION_SIP_REGIEST);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getBaseContext().startForegroundService(onLineIntent);
        }else{
          getBaseContext().startService(onLineIntent);
        }
    }

    EditTextName.addTextChangedListener(new TextWatcher() {
      // ...


      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void afterTextChanged(Editable s) {
        if (EditTextName.getText().toString().indexOf("+")<0) {
          EditTextName.setText("+"+EditTextName.getText().toString());
        }
      }
    });

   Boolean startRoom = getIntent().getBooleanExtra("startRoom",false);
   if(startRoom) openPushRoom();

  }

  public void SaveUserInfo() {
    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplication()).edit();
    UF_SIP_NUMBER = SipData.getString("UF_SIP_NUMBER", null);
    UF_SIP_PASSWORD = SipData.getString("UF_SIP_PASSWORD", null);
    String UF_SIP_SERVER = SipData.getString("UF_SIP_SERVER", "sip.weltwelle.com");
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

  public void showPopup(View v) {
    if(nazad.getVisibility() == View.VISIBLE) {
      PopupMenu popup = new PopupMenu(this, v);
      popup.setOnMenuItemClickListener(this);
      popup.inflate(R.menu.menu_users);
      setForceShowIcon(popup);
      popup.show();
    }
  }

  public static void setForceShowIcon(PopupMenu popupMenu) {
    try {
      Field[] fields = popupMenu.getClass().getDeclaredFields();
      for (Field field : fields) {
        if ("mPopup".equals(field.getName())) {
          field.setAccessible(true);
          Object menuPopupHelper = field.get(popupMenu);
          Class<?> classPopupHelper = Class.forName(menuPopupHelper
                  .getClass().getName());
          Method setForceIcons = classPopupHelper.getMethod(
                  "setForceShowIcon", boolean.class);
          setForceIcons.invoke(menuPopupHelper, true);
          break;
        }
      }
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }


  @Override
  public boolean onMenuItemClick(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.item1:
        getBlacklistAdd(getName);
        return true;
      case R.id.item2:
        showFragment(NotificationFragment.create(hostname, roomId));
        return true;
      case R.id.item3:
        showFragment(ListFileFragment.create(hostname, roomId));
        return true;

      case R.id.item5:
        setContact = 1;
        navigation.setSelectedItemId(R.id.action_group);
       getSharedPreferences("NameConntact", MODE_PRIVATE)
                .edit()
                .putString(getName, "")
                .commit();
        recyclerViews.setAdapter(adapter);
        return true;
      default:
        return false;
    }
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    createRoom();
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
  public void openDialog() {
    AddChannelDialogFragment exampleDialog = new AddChannelDialogFragment();
    exampleDialog.show(getSupportFragmentManager(), "example dialog");
  }


  private void saveData() {
    SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    Gson gson = new Gson();
    String json = gson.toJson(mExampleList);
    editor.putString("task list", json);
    editor.apply();
  }

  private void loadData() {
    SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
    Gson gson = new Gson();
    String json = sharedPreferences.getString("task list", null);
    Type type = new TypeToken<ArrayList<ExampleItem>>() {}.getType();
    mExampleList = gson.fromJson(json, type);
    if (mExampleList == null) {
      mExampleList = new ArrayList<>();
    }
  }

  private void buildRecyclerView() {
    mRecyclerView = findViewById(R.id.recyclerv_view);
    mRecyclerView.setHasFixedSize(true);
    mLayoutManager = new LinearLayoutManager(this);
    mAdapters = new ExampleAdapter(mExampleList);

    mRecyclerView.setLayoutManager(mLayoutManager);
    mRecyclerView.setAdapter(mAdapters);
  }

  private void setInsertButton() {
    long date = System.currentTimeMillis();
    SimpleDateFormat datas = new SimpleDateFormat("H:m:s yyyy-MM-dd");
    String time = datas.format(date);
        insertItem(""+getName, ""+time);
    Log.d("XSWQAZ","ADD ");
  }

  private void insertItem(String line1, String line2) {
    mExampleList.add(new ExampleItem(line1, line2));
    mAdapters.notifyItemInserted(mExampleList.size());

  }

  public void helpB(View v) {
    Button clickedButton = (Button) v;
    switch (clickedButton.getId()) {
      case R.id.btn1:
        EditTextName.setText(EditTextName.getText()+"1");
        EditTextName.setSelection(EditTextName.getText().length());
        break;
      case R.id.btn2:
        EditTextName.setText(EditTextName.getText()+"2");
        EditTextName.setSelection(EditTextName.getText().length());
        break;
      case R.id.btn3:
        EditTextName.setText(EditTextName.getText()+"3");
        EditTextName.setSelection(EditTextName.getText().length());
        break;
      case R.id.btn4:
        EditTextName.setText(EditTextName.getText()+"4");
        EditTextName.setSelection(EditTextName.getText().length());
        break;
      case R.id.btn5:
        EditTextName.setText(EditTextName.getText()+"5");
        EditTextName.setSelection(EditTextName.getText().length());
        break;
      case R.id.btn6:
        EditTextName.setText(EditTextName.getText()+"6");
        EditTextName.setSelection(EditTextName.getText().length());
        break;
      case R.id.btn7:
        EditTextName.setText(EditTextName.getText()+"7");
        EditTextName.setSelection(EditTextName.getText().length());
        break;
      case R.id.btn8:
        EditTextName.setText(EditTextName.getText()+"8");
        EditTextName.setSelection(EditTextName.getText().length());
        break;
      case R.id.btn9:
        EditTextName.setText(EditTextName.getText()+"9");
        EditTextName.setSelection(EditTextName.getText().length());
        break;
      case R.id.btn10:
        EditTextName.setText(EditTextName.getText()+"*");
        EditTextName.setSelection(EditTextName.getText().length());
        break;
      case R.id.btn11:
        EditTextName.setText(EditTextName.getText()+"0");
        EditTextName.setSelection(EditTextName.getText().length());
        break;
      case R.id.btn12:
        EditTextName.setText(EditTextName.getText()+"#");
        EditTextName.setSelection(EditTextName.getText().length());
        break;
      case R.id.btn13:
        navigation.setSelectedItemId(R.id.action_group);
        break;
      case R.id.btn14:
        getName = EditTextName.getText().toString().replace("+", "").replaceAll("[^0-9\\.]", "");
          if(!EditTextName.getText().toString().substring(1,2).equals("0") && EditTextName.getText().toString().length()>3) {
            if (subscription==true) {
              if(EditTextName.getText().toString().indexOf("+")<0)
                  EditTextName.setText("+"+EditTextName.getText().toString());
            callSet = false;
            startActivity(callInt);
            setInsertButton();
            setnupad = 1;
          }else {
            Toast.makeText(getApplicationContext(),"У вас нет доступа!",Toast.LENGTH_LONG).show();
          }
        }else {
            Toast.makeText(getApplicationContext(), "Неверный формат международного телефонного номера",
                    Toast.LENGTH_SHORT).show();

          }

        break;
      case R.id.btn15:
        if(!EditTextName.getText().toString().equals(""))
        EditTextName.setText(new StringBuffer(EditTextName.getText().toString()).delete(EditTextName.getText().toString().length()-1,EditTextName.getText().toString().length()));
        break;
    }



  }

  @Override
  protected Task<Void> getMethodCallForSubmitAction() {
    String username =
            ((AppCompatAutoCompleteTextView) findViewById(R.id.editText)).getText().toString();
    return methodCall.createDirectMessage(username);
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (presenter != null) {
      presenter.bindViewOnly(this);
    }


  }

  @Override
  protected void onPause() {
    if (presenter != null) {
      presenter.release();
    }

    super.onPause();
  }

  private void setupSidebar() {
    SlidingPaneLayout pane = (SlidingPaneLayout) findViewById(R.id.sliding_pane);
    if (pane == null) {
      return;
    }

    final SlidingPaneLayout subPane = (SlidingPaneLayout) findViewById(R.id.sub_sliding_pane);
    pane.setPanelSlideListener(new SlidingPaneLayout.SimplePanelSlideListener() {
      @Override
      public void onPanelClosed(View panel) {
        super.onPanelClosed(panel);
        if (subPane != null) {
          subPane.closePane();
        }
      }
    });

    final DrawerArrowDrawable drawerArrowDrawable = new DrawerArrowDrawable(this);
    Toolbar toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
    toolbar.setNavigationIcon(drawerArrowDrawable);
    toolbar.setNavigationOnClickListener(view -> {
      if (pane.isSlideable() && !pane.isOpen()) {
        pane.openPane();
      }
    });

    //ref: ActionBarDrawerToggle#setProgress
    pane.setPanelSlideListener(new SlidingPaneLayout.PanelSlideListener() {
      @Override
      public void onPanelSlide(View panel, float slideOffset) {
        drawerArrowDrawable.setProgress(slideOffset);
      }

      @Override
      public void onPanelOpened(View panel) {
        drawerArrowDrawable.setVerticalMirror(true);
      }

      @Override
      public void onPanelClosed(View panel) {
        drawerArrowDrawable.setVerticalMirror(false);
      }
    });
  }

  private boolean closeSidebarIfNeeded() {
    // REMARK: Tablet UI doesn't have SlidingPane!
    SlidingPaneLayout pane = (SlidingPaneLayout) findViewById(R.id.sliding_pane);
    if (pane != null && pane.isSlideable() && pane.isOpen()) {
      pane.closePane();
      return true;
    }
    return false;
  }

  @DebugLog
  @Override
  protected void onHostnameUpdated() {
    super.onHostnameUpdated();

    if (presenter != null) {
      presenter.release();
    }

    RoomInteractor roomInteractor = new RoomInteractor(new RealmRoomRepository(hostname));

    CanCreateRoomInteractor createRoomInteractor = new CanCreateRoomInteractor(
            new RealmUserRepository(hostname),
            new SessionInteractor(new RealmSessionRepository(hostname))
    );

    SessionInteractor sessionInteractor = new SessionInteractor(
            new RealmSessionRepository(hostname)
    );

    presenter = new MainPresenter(
            roomInteractor,
            createRoomInteractor,
            sessionInteractor,
            new MethodCallHelper(this, hostname),
            ConnectivityManager.getInstance(getApplicationContext()),
            new RocketChatCache(this)
    );

    updateSidebarMainFragment();

    presenter.bindView(this);
  }

  private void updateSidebarMainFragment() {
    getSupportFragmentManager().beginTransaction()
            .replace(R.id.sidebar_fragment_container, SidebarMainFragment.create(hostname))
            .commit();
  }

  @Override
  protected void onRoomIdUpdated() {
    super.onRoomIdUpdated();
    presenter.onOpenRoom(hostname, roomId);
  }

  @Override
  protected boolean onBackPress() {
    return closeSidebarIfNeeded() || super.onBackPress();
  }

  @Override
  public void showHome() {
    showFragment(RoomFragment.create(hostname, roomId));
  }



  @Override
  public void showRoom(String hostname, String roomId) {
    showFragment(RoomFragment.create(hostname, roomId));
    closeSidebarIfNeeded();
    KeyboardHelper.hideSoftKeyboard(this);
  }

  private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
          = new BottomNavigationView.OnNavigationItemSelectedListener() {

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

      switch (item.getItemId()) {
        case R.id.action_chat:
          chat.setVisibility(VISIBLE);
          call.setVisibility(GONE);
          contacts.setVisibility(GONE);
          setting.setVisibility(GONE);
          setContact = 0;
          return true;
        case R.id.action_call:
          chat.setVisibility(GONE);
          call.setVisibility(VISIBLE);
          contacts.setVisibility(GONE);
          setting.setVisibility(GONE);
          if(SipData.getString("INNER_GROUP", "false").equals("false")){
            startActivity(new Intent(getApplicationContext(), Success.class));
          }
          setContact = 0;
          return true;
        case R.id.action_group:
          chat.setVisibility(GONE);
          call.setVisibility(GONE);
          contacts.setVisibility(VISIBLE);
          setting.setVisibility(GONE);
          contactModelList.clear();
          requestContactsPermissions();
          if(SipData.getString("INNER_GROUP", "false").equals("false")){
            startActivity(new Intent(getApplicationContext(), Success.class));
          }

          return true;
        case R.id.action_setting:
          chat.setVisibility(GONE);
          call.setVisibility(GONE);
          contacts.setVisibility(GONE);
          setting.setVisibility(VISIBLE);


          SwitchCompat switch3 = (SwitchCompat)findViewById(R.id.switch3);
          if(SipData.getBoolean("VIDEO_C", true)==true){
            switch3.setChecked(true);
          }else{
           switch3.setChecked(false);
          }

          Log.d("TEST22",""+SipData.getBoolean("VIDEO_C", true));
          TextView textView6 = (TextView)findViewById(R.id.textView6);
          if(callstatic==1)
            textView6.setText("Chat: Онлайн SIP: Онлайн");
          else
            textView6.setText("Chat: Онлайн SIP: Офлайн");
          setContact = 0;
          return true;
      }

      return false;
    }
  };

  public void kayboard(View view) {
    animateShow(kayboardLayout);
    animateShow(mRecyclerView);
    animateHide(mRecyclerView);
    animateShow(openKey);
    animateHide(openKey);
  }

  public void closeKey(View view) {
    animateShow(kayboardLayout);
    animateHide(kayboardLayout);
    animateShow(mRecyclerView);
    animateShow(openKey);
}

  public void nazad(View view) {
 /*   RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) chat
            .getLayoutParams();

    layoutParams.setMargins(0, 0, 0, 60);
    chat.setLayoutParams(layoutParams);
    navigation.setVisibility(VISIBLE);*/
    showFragment(RoomFragment.create(hostname,roomId));
    animateHide(activity_main_container);
    animateShow(recyclerViews);
    animateShow(editText);
    animateHide(nazad);
    animateShow(btnCreate);
    animateHide(callUsers);
    animateHide(statusRoom);

    current_user_name.setText("Сообщения");


  }


  @Override
  public void showUnreadCount(long roomsCount, int mentionsCount) {
    RoomToolbar toolbar = (RoomToolbar) findViewById(R.id.activity_main_toolbar);
    if (toolbar != null) {
      toolbar.setUnreadBudge((int) roomsCount, mentionsCount);
    }
  }

  @Override
  public void showAddServerScreen() {
    LaunchUtil.showAddServerActivity(this);
  }

  @Override
  public void showLoginScreen() {
    LaunchUtil.showLoginActivity(this, hostname);
    statusTicker.updateStatus(StatusTicker.STATUS_DISMISS, null);
  }

  @Override
  public void showConnectionError() {
    statusTicker.updateStatus(StatusTicker.STATUS_CONNECTION_ERROR,
            Snackbar.make(findViewById(getLayoutContainerForFragment()),
                    R.string.fragment_retry_login_error_title, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.fragment_retry_login_retry_title, view ->
                            presenter.onRetryLogin()));
  }

  @Override
  public void showConnecting() {
    StatusU=7;
    UserStatus();
   /* statusTicker.updateStatus(StatusTicker.STATUS_TOKEN_LOGIN,
            Snackbar.make(findViewById(getLayoutContainerForFragment()),
                    R.string.server_config_activity_authenticating, Snackbar.LENGTH_INDEFINITE));*/
  }

  @Override
  public void showConnectionOk() {
    statusTicker.updateStatus(StatusTicker.STATUS_DISMISS, null);
  }

  //TODO: consider this class to define in layouthelper for more complicated operation.
  private static class StatusTicker {
    public static final int STATUS_DISMISS = 0;
    public static final int STATUS_CONNECTION_ERROR = 1;
    public static final int STATUS_TOKEN_LOGIN = 2;

    private int status;
    private Snackbar snackbar;

    public StatusTicker() {
      status = STATUS_DISMISS;
    }

    public void updateStatus(int status, Snackbar snackbar) {
      if (status == this.status) {
        return;
      }
      this.status = status;
      if (this.snackbar != null) {
        this.snackbar.dismiss();
      }
      if (status != STATUS_DISMISS) {
        this.snackbar = snackbar;
        if (this.snackbar != null) {
          this.snackbar.show();
        }
      }
    }
  }

  private void setDataToAdapter(){
    contactAdapter = new ContactAdapter(this,contactModelList);
    initRecyclerView();
  }

  private void initRecyclerView(){
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    recyclerView.setHasFixedSize(true);
    recyclerView.setAdapter(contactAdapter);
  }

  private void getContactInfo(){
    Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
    String ID = ContactsContract.Contacts._ID;
    String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
    String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

    Uri PHONE_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
    String PHONE_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
    String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

    ContentResolver contentResolver = getContentResolver();
    Cursor cursor = contentResolver.query(CONTENT_URI,null,null,null,DISPLAY_NAME);

    if (cursor.getCount() > 0){
      while (cursor.moveToNext()){
        String CONTACT_ID = cursor.getString(cursor.getColumnIndex(ID));
        String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));

        int hasPhoneNumber = cursor.getInt(cursor.getColumnIndex(HAS_PHONE_NUMBER));
        ContactModel contactModel = new ContactModel();
        if (hasPhoneNumber > 0){
          contactModel.setName(name);

          Cursor phoneCursor = contentResolver.query(PHONE_URI, new String[]{NUMBER},PHONE_ID+" = ?",new String[]{CONTACT_ID},null);
          List<String> contactList = new ArrayList<>();
          phoneCursor.moveToFirst();
          while (!phoneCursor.isAfterLast()){
            String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER)).replace(" ","");
            contactList.add(phoneNumber);
            phoneCursor.moveToNext();
          }
          contactModel.setNumber(contactList);
          contactModelList.add(contactModel);
          phoneCursor.close();
        }
      }
      contactAdapter.notifyDataSetChanged();
    }
  }

  public void requestContactsPermissions(){
    a_chars=0;
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED){

      if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_CONTACTS) ||
              ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CONTACTS)){

        Snackbar.make(recyclerView, "permission Contact", Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", new View.OnClickListener() {
                  @Override
                  public void onClick(View view) {
                    ActivityCompat.requestPermissions(MainActivity.this,PERMISSION_CONTACT,REQUEST_CONTACT);
                  }
                }).show();
      } else {
        ActivityCompat.requestPermissions(MainActivity.this,PERMISSION_CONTACT,REQUEST_CONTACT);
      }
    } else {
      getContactInfo();
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    for (int result : grantResults){
      if (result == PackageManager.PERMISSION_GRANTED){
        getContactInfo();
      }
    }
  }




  public void getList(){
    mApiServiceChat.getList(autoCompleteTextView.getText().toString())
            .enqueue(new Callback<ResponseBody>() {
              @Override
              public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()){
                  try {
                    JSONObject json = new JSONObject(response.body().string());
                    JSONArray values = json.getJSONArray("users");
                    mCats = new String[values .length()];

                    for (int i = 0; i < values .length(); i++) {
                      JSONObject jsonobject = values .getJSONObject(i);
                      name = jsonobject.getString("username");
                      mCats[i] = ""+name;
                    }
                    mCatsList = new ArrayList<String>(Arrays.asList(mCats));
                    autoCompleteTextView.setAdapter(new ArrayAdapter<String>(MainActivity.this,
                            android.R.layout.simple_dropdown_item_1line, mCats));
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
              }
            });
  }

    private void UF_ORIGINAL_TRID(){
      Map<String, Object> jsonParams = new ArrayMap<>();
      jsonParams.put("UF_ORIGINAL_TRID", "tt");
        jsonParams.put("GET_USER", "1");
        mApiService.subscription("KEY:"+SipData.getString("TOKENWE",""),"application/json",jsonParams)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject > response) {
                      try{
                        if (response.body().getAsJsonObject("result").get("SUCCESS").equals("false")){
                          Toast.makeText(getApplication(), "Покупка привязна к другому пользователю, нужно зайти под другим пользователем WeWe",
                                  Toast.LENGTH_SHORT).show();
                        }
                      }catch (Exception e){
                        e.printStackTrace();
                      }
                    }
                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        Log.e("debug", "onFailure: ERROR > " + t.toString());
                    }
                });
    }

  public void getStatus(){
   mApiService.getStatusUsers()
            .enqueue(new Callback<ResponseBody>() {
              @Override
              public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()){
                  StatusU=8;
                } else {
                  StatusU=0;
                }
              }
              @Override
              public void onFailure(Call<ResponseBody> call, Throwable t) {
                //   Log.e("debug", "onFailure: ERROR > " + t.toString());
                StatusU=0;

              }
            });
    UserStatus();
  }

  public void getBlacklistAdd(String UF_ROCKET_LOGIN){
    SipData = getSharedPreferences("SIP", MODE_PRIVATE);
    Map<String, Object> jsonParams = new ArrayMap<>();
    jsonParams.put("UF_ROCKET_ID", "null");
    jsonParams.put("UF_ROCKET_LOGIN", "null");
    jsonParams.put("UF_USER_ID_BLOCKED", "null");
    jsonParams.put("UF_ROCKET_ID_BLOCKED", "null");
    jsonParams.put("UF_ROCKET_LOGIN_BLOC", UF_ROCKET_LOGIN);
    mApiService.getBlacklistAdd("KEY:"+SipData.getString("TOKENWE",""),jsonParams)
            .enqueue(new Callback<ResponseBody>() {
              @Override
              public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()){
                  try {
                    JSONObject jsonRESULTS = new JSONObject(response.body().string());
                    showFragment(RoomFragment.create(hostname,roomId));
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
              }
            });
  }




  private void UserStatus(){
    switch (StatusU) {
      case 0:statusUsers.setImageResource(R.drawable.s000);
        statusUsers2.setImageResource(R.drawable.s000);
        statusUsers3.setImageResource(R.drawable.s000);
        statusUsers4.setImageResource(R.drawable.s000);
        break;
      case 1:statusUsers.setImageResource(R.drawable.s011);
        statusUsers2.setImageResource(R.drawable.s011);
        statusUsers3.setImageResource(R.drawable.s011);
        statusUsers4.setImageResource(R.drawable.s011);
        break;
      case 2:    statusUsers.setImageResource(R.drawable.s012);
        statusUsers2.setImageResource(R.drawable.s012);
        statusUsers3.setImageResource(R.drawable.s012);
        statusUsers4.setImageResource(R.drawable.s012);
        break;
      case 3: statusUsers.setImageResource(R.drawable.s110);
        statusUsers2.setImageResource(R.drawable.s110);
        statusUsers3.setImageResource(R.drawable.s110);
        statusUsers4.setImageResource(R.drawable.s110);
        break;
      case 4:statusUsers.setImageResource(R.drawable.s111);
        statusUsers2.setImageResource(R.drawable.s111);
        statusUsers3.setImageResource(R.drawable.s111);
        statusUsers4.setImageResource(R.drawable.s111);
        break;
      case 5:statusUsers.setImageResource(R.drawable.s112);
        statusUsers2.setImageResource(R.drawable.s112);
        statusUsers3.setImageResource(R.drawable.s112);
        statusUsers4.setImageResource(R.drawable.s112);
        break;
      case 6:statusUsers.setImageResource(R.drawable.s210);
        statusUsers2.setImageResource(R.drawable.s210);
        statusUsers3.setImageResource(R.drawable.s210);
        statusUsers4.setImageResource(R.drawable.s210);
        break;
      case 7:statusUsers.setImageResource(R.drawable.s211);
        statusUsers2.setImageResource(R.drawable.s211);
        statusUsers3.setImageResource(R.drawable.s211);
        statusUsers4.setImageResource(R.drawable.s211);
        break;
      case 8:statusUsers.setImageResource(R.drawable.s222);
        statusUsers2.setImageResource(R.drawable.s222);
        statusUsers3.setImageResource(R.drawable.s222);
        statusUsers4.setImageResource(R.drawable.s222);
        break;

    }

  }



  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    if (event.getAction() == KeyEvent.ACTION_DOWN) {
      switch (event.getKeyCode()) {
        case KeyEvent.KEYCODE_BACK:
          return true;
      }
    }
    return super.dispatchKeyEvent(event);
  }

  public void openPushRoom(){
    CountDownTimer countDownTimer = new CountDownTimer(2000, 1000) {
      @Override
      public void onTick(long millisUntilFinished) {
      }

      @Override
      public void onFinish() {
        recyclerViews.findViewHolderForAdapterPosition(0).itemView.performClick();
        cancel();
      }
    };
    countDownTimer.start();
  }

}
