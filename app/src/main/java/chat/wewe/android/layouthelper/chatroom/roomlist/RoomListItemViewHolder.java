package chat.wewe.android.layouthelper.chatroom.roomlist;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

import chat.wewe.android.activity.MainActivity;
import chat.wewe.android.widget.internal.RoomListItemView;
import chat.wewe.core.models.Room;
import chat.wewe.core.models.SpotlightRoom;

public class RoomListItemViewHolder extends RecyclerView.ViewHolder {
  public RoomListItemViewHolder(RoomListItemView itemView,
                                RoomListAdapter.OnItemClickListener listener) {

    super(itemView);


    itemView.setOnClickListener(view -> {
      if (listener != null) {
        Object tag = view.getTag();

        if (tag instanceof Room) {
          listener.onItemClick((Room) view.getTag());
        } else if (tag instanceof SpotlightRoom) {
          listener.onItemClick((SpotlightRoom) view.getTag());
        }
      }
    });
  }


  public void bind(Room room) {
    ((RoomListItemView) itemView)
        .setRoomId(room.getRoomId())
        .setRoomName(room.getName())
        .setRoomType(room.getType())
        .setAlert(room.isAlert())
        .setUnreadCount(room.getUnread())
        .setTag(room);

  }

  public void bind(SpotlightRoom spotlightRoom) {
    ((RoomListItemView) itemView)
        .setRoomId(spotlightRoom.getId())
        .setRoomName(spotlightRoom.getName())
        .setRoomType(spotlightRoom.getType())
        .setAlert(false)
        .setUnreadCount(0)
        .setTag(spotlightRoom);
  }
}
