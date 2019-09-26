package chat.wewe.android.renderer;

import android.support.annotation.DrawableRes;

import chat.wewe.android.R;
import chat.wewe.android.widget.helper.UserStatusProvider;
import chat.wewe.core.models.User;

public class RocketChatUserStatusProvider implements UserStatusProvider {

  private static RocketChatUserStatusProvider instance;

  private RocketChatUserStatusProvider() {
  }

  public static RocketChatUserStatusProvider getInstance() {
    if (instance == null) {
      instance = new RocketChatUserStatusProvider();
    }

    return instance;
  }

  @Override
  @DrawableRes
  public int getStatusResId(String status) {
    if (User.STATUS_ONLINE.equals(status)) {
      return R.drawable.userstatus_online;
    } else if (User.STATUS_AWAY.equals(status)) {
      return R.drawable.userstatus_away;
    } else if (User.STATUS_BUSY.equals(status)) {
      return R.drawable.userstatus_busy;
    } else if (User.STATUS_OFFLINE.equals(status)) {
      return R.drawable.userstatus_offline;
    }

    // unknown status is rendered as "offline" status.
    return R.drawable.userstatus_offline;
  }
}
