package chat.wewe.android.layouthelper.chatroom.roomlist;

import android.support.annotation.NonNull;

import java.util.List;
import chat.wewe.core.models.Room;

public interface RoomListHeader {

  String getTitle();

  boolean owns(Room room);

  boolean shouldShow(@NonNull List<Room> roomList);

  ClickListener getClickListener();

  interface ClickListener {
    void onClick();
  }
}
