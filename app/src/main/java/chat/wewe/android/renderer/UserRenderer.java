package chat.wewe.android.renderer;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import chat.wewe.android.helper.Avatar;
import chat.wewe.android.helper.TextUtils;
import chat.wewe.android.widget.AbsoluteUrl;
import chat.wewe.core.models.User;
import chat.wewe.android.widget.RocketChatAvatar;

/**
 * Renderer for RealmUser model.
 */
public class UserRenderer extends AbstractRenderer<User> {

  public UserRenderer(Context context, User user) {
    super(context, user);
  }

  /**
   * show Avatar image
   */
  public UserRenderer avatarInto(RocketChatAvatar rocketChatAvatar, AbsoluteUrl absoluteUrl) {
    if (!shouldHandle(rocketChatAvatar)) {
      return this;
    }

    if (!TextUtils.isEmpty(object.getUsername())) {
      new Avatar(absoluteUrl, object.getUsername()).into(rocketChatAvatar);
    }
    return this;
  }

  /**
   * show Username in textView
   */
  public UserRenderer usernameInto(TextView textView) {
    if (!shouldHandle(textView)) {
      return this;
    }

    textView.setText(object.getUsername());

    return this;
  }

  /**
   * show user's status color into imageView.
   */
  public UserRenderer statusColorInto(ImageView imageView) {
    if (!shouldHandle(imageView)) {
      return this;
    }

    String status = object.getStatus();
    imageView.setImageResource(RocketChatUserStatusProvider.getInstance().getStatusResId(status));

    return this;
  }
}
