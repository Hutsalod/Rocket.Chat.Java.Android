package chat.wewe.android.layouthelper.sidebar.dialog;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import java.util.Iterator;
import java.util.List;
import chat.wewe.android.R;
import chat.wewe.android.widget.AbsoluteUrl;
import chat.wewe.persistence.realm.models.ddp.RealmUser;
import chat.wewe.persistence.realm.RealmAutoCompleteAdapter;
import chat.wewe.android.renderer.UserRenderer;
import chat.wewe.android.widget.RocketChatAvatar;

/**
 * adapter to suggest user names.
 */
public class SuggestUserAdapter extends RealmAutoCompleteAdapter<RealmUser> {
  private final AbsoluteUrl absoluteUrl;

  public SuggestUserAdapter(Context context, AbsoluteUrl absoluteUrl) {
    super(context, R.layout.listitem_room_user, R.id.room_user_name);
    this.absoluteUrl = absoluteUrl;
  }

  @Override
  protected void onBindItemView(View itemView, RealmUser user) {
    new UserRenderer(itemView.getContext(), user.asUser())
        .statusColorInto((ImageView) itemView.findViewById(R.id.room_user_status))
        .avatarInto((RocketChatAvatar) itemView.findViewById(R.id.room_user_avatar), absoluteUrl);
  }

  @Override
  protected void filterList(List<RealmUser> users, String text) {
    Iterator<RealmUser> itUsers = users.iterator();
    final String prefix = text.toLowerCase();
    while (itUsers.hasNext()) {
      RealmUser user = itUsers.next();
      if (!user.getUsername().toLowerCase().startsWith(prefix)) {
        itUsers.remove();
      }
    }
  }

  @Override
  protected String getStringForSelectedItem(RealmUser user) {
    return user.getUsername();
  }
}
