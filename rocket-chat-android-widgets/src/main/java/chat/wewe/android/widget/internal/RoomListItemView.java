package chat.wewe.android.widget.internal;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import chat.wewe.android.widget.R;
import chat.wewe.core.BaseApiService;
import chat.wewe.core.UtilsApi;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * Room list-item view used in sidebar.
 */
public class RoomListItemView extends FrameLayout {
  private static HashMap<String, Integer> ICON_TABLE = new HashMap<String, Integer>() {
    {
      put("c", R.string.fa_hashtag);
      put("p", R.string.fa_lock);
      put("d", R.string.fa_at);
    }
  };
  BaseApiService mApiService = UtilsApi.getAPIService();
  private String roomId;
  private String roomName;
  private MediaPlayer song;
 SharedPreferences SipData, SipDataq;
  public ArrayList<String> arrayListLists = new ArrayList<>();
    TextView text;

  public RoomListItemView(Context context) {
    super(context);
    initialize(context);
  }

  public RoomListItemView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialize(context);
  }

  public RoomListItemView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize(context);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public RoomListItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initialize(context);
  }



  private void initialize(Context context) {
    setLayoutParams(new LinearLayout.LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

    TypedArray array2 = context.getTheme().obtainStyledAttributes(new int[]{
            R.attr.selectableItemBackground
    });
    setBackground(array2.getDrawable(0));
    array2.recycle();


    View.inflate(context, R.layout.room_list_item, this);
  }

  public String getRoomId() {
    return roomId;
  }


  public RoomListItemView setRoomId(String roomId) {
    this.roomId = roomId;
    return this;
  }

  public RoomListItemView setRoomType(String type) {
    if (ICON_TABLE.containsKey(type)) {
      TextView icon = (TextView) findViewById(R.id.icon);
      icon.setText(ICON_TABLE.get(type));
    }

    return this;
  }

  public RoomListItemView setUnreadCount(int count) {

    View alertCountContainer = findViewById(R.id.alert_count_container);
    song = (MediaPlayer) MediaPlayer.create(getContext(), R.raw.msg);
    TextView alertCount = (TextView) findViewById(R.id.alert_count);

    if (count > 0) {
      alertCount.setText(Integer.toString(count));
      alertCountContainer.setVisibility(View.VISIBLE);

    } else {
      alertCountContainer.setVisibility(View.GONE);

    }

    return this;
  }

  public RoomListItemView setAlert(boolean alert) {
    // setAlpha(alert ? 1.0f : 0.62f);

    return this;
  }

  public void soungPlay(MediaPlayer sound) {
    sound.start();
  }

  public String getRoomName() {
    return roomName;
  }

  public RoomListItemView setRoomName(final String roomName) {
    this.roomName = roomName;
   // getStart(roomName);
    SipData = getContext().getSharedPreferences("NameMessage", MODE_PRIVATE);
    SipDataq = getContext().getSharedPreferences("TimeMessage", MODE_PRIVATE);
    TextView message_out = (TextView) findViewById(R.id.message_out);
    text = (TextView) findViewById(R.id.text);
    TextView timemessage = (TextView) findViewById(R.id.textView2);
    TextView iconText = (TextView) findViewById(R.id.iconText);
    message_out.setText(SipData.getString(roomName, ""));
    timemessage.setText(SipDataq.getString(roomName, ""));
    text.setText(roomName);
      ImageView statusConnects =  findViewById(R.id.statusConnect);
      statusConnects.setImageResource(R.drawable.userstatus_offline);



    new DownloadImageFromInternet((ImageView)findViewById(R.id.avatarUser)).execute("https://chat.weltwelle.com/avatar/"+roomName);

    char a_char = roomName.charAt(0);
    iconText.setText(""+String.valueOf(a_char).substring(0, 1).toUpperCase());
    return this;
  }

    public final void  showOnlineUserStatusIcon() {
        ImageView statusConnects =  findViewById(R.id.statusConnect);
        statusConnects.setImageResource(R.drawable.userstatus_online);

    }
    public void  showOfflineUserStatusIcon() {
        ImageView statusConnects =  findViewById(R.id.statusConnect);
        statusConnects.setImageResource(R.drawable.userstatus_offline);

    }



  private class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
    ImageView imageView;
    public DownloadImageFromInternet(ImageView imageView) {
      this.imageView = imageView;
    }
    protected Bitmap doInBackground(String... urls) {
      String imageURL = urls[0];
      Bitmap bimage = null;
      try {
        InputStream in = new java.net.URL(imageURL).openStream();
        bimage = BitmapFactory.decodeStream(in);
      } catch (Exception e) {
        Log.e("Error Message", e.getMessage());
        e.printStackTrace();
      }
      return bimage;
    }
    protected void onPostExecute(Bitmap result) {
      TextView iconText = (TextView) findViewById(R.id.iconText);
      if(result==null){
        imageView.setImageResource(R.drawable.list_user);
        iconText.setVisibility(VISIBLE);}
      else{
        imageView.setImageBitmap(result);
        iconText.setVisibility(GONE);
      }
    }
  }


}
