package chat.wewe.android.layouthelper.chatroom.dialog;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import chat.wewe.android.R;
import chat.wewe.android.widget.RocketChatAvatar;

/**
 * ViewHolder for UsersOfRoom.
 */
public class RoomUserViewHolder extends RecyclerView.ViewHolder {
  ImageView status;
  RocketChatAvatar avatar;
  TextView username;


  /**
   * Constructor.
   */
  public RoomUserViewHolder(View itemView) {
    super(itemView);
    status = (ImageView) itemView.findViewById(R.id.room_user_status);
    avatar = (RocketChatAvatar) itemView.findViewById(R.id.room_user_avatar);
    username = (TextView) itemView.findViewById(R.id.room_user_name);
  }


}
