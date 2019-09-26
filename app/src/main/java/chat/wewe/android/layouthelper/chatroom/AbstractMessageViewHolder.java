package chat.wewe.android.layouthelper.chatroom;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;

import chat.wewe.android.R;
import chat.wewe.android.helper.DateTime;
import chat.wewe.android.helper.TextUtils;
import chat.wewe.android.widget.AbsoluteUrl;
import chat.wewe.android.widget.internal.RoomListItemView;
import chat.wewe.core.SyncState;
import chat.wewe.android.widget.RocketChatAvatar;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public abstract class AbstractMessageViewHolder extends ModelViewHolder<PairedMessage> {
  protected final RocketChatAvatar avatar;
  protected final TextView username;
  protected final TextView subUsername;
  protected final TextView timestamp;
  protected final View userAndTimeContainer;
  protected final AbsoluteUrl absoluteUrl;
  protected final View newDayContainer;
  protected final TextView newDayText;

  /**
   * constructor WITH hostname.
   */
  public AbstractMessageViewHolder(View itemView, AbsoluteUrl absoluteUrl) {
    super(itemView);
    avatar = (RocketChatAvatar) itemView.findViewById(R.id.user_avatar);
    username = (TextView) itemView.findViewById(R.id.username);
    subUsername = (TextView) itemView.findViewById(R.id.sub_username);
    timestamp = (TextView) itemView.findViewById(R.id.timestamp);
    userAndTimeContainer = itemView.findViewById(R.id.user_and_timestamp_container);
    newDayContainer = itemView.findViewById(R.id.newday_container);
    newDayText = (TextView) itemView.findViewById(R.id.newday_text);

  this.absoluteUrl = absoluteUrl;
  }

  /**
   * bind the view model.
   */
  public final void bind(PairedMessage pairedMessage, boolean autoloadImages) {
    bindMessage(pairedMessage, autoloadImages);

    if (pairedMessage.target != null) {
      int syncState = pairedMessage.target.getSyncState();
      if (syncState == SyncState.NOT_SYNCED || syncState == SyncState.SYNCING) {
        itemView.setAlpha(0.6f);
      } else {
        itemView.setAlpha(1.0f);
      }
    }

    renderNewDayAndSequential(pairedMessage);
  }



  protected abstract void bindMessage(PairedMessage pairedMessage, boolean autoloadImages);

  private void renderNewDayAndSequential(PairedMessage pairedMessage) {
    //see Rocket.Chat:packages/rocketchat-livechat/app/client/views/message.coffee
    if (!pairedMessage.hasSameDate()) {
      setNewDay(DateTime.fromEpocMs(pairedMessage.target.getTimestamp(), DateTime.Format.DATE));
      setSequential(false);
    } else if (!pairedMessage.target.isGroupable() || !pairedMessage.nextSibling.isGroupable()
        || !pairedMessage.hasSameUser()) {
      setNewDay(null);
      setSequential(false);
    } else {
      setNewDay(null);
      setSequential(true);
    }
  }

  private void setSequential(boolean sequential) {
    if (avatar != null) {
      if (sequential) {
        avatar.setVisibility(GONE);
      } else {
        avatar.setVisibility(VISIBLE);
      }
    }

    if (userAndTimeContainer != null) {
      if (sequential) {
        userAndTimeContainer.setVisibility(GONE);
      } else {
        userAndTimeContainer.setVisibility(VISIBLE);
      }
    }
  }

  private void setNewDay(@Nullable String text) {
    if (newDayContainer != null) {
      if (TextUtils.isEmpty(text)) {
        newDayContainer.setVisibility(GONE);
      } else {
        newDayText.setText(text);
        newDayContainer.setVisibility(VISIBLE);
      }
    }
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
      if(result==null){
        imageView.setImageResource(chat.wewe.android.widget.R.drawable.list_user); }
      else{
        imageView.setImageBitmap(result);
      }
    }
  }
}
