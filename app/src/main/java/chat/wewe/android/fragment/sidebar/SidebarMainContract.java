package chat.wewe.android.fragment.sidebar;

import android.support.annotation.NonNull;

import java.util.List;
import chat.wewe.android.fragment.chatroom.RocketChatAbsoluteUrl;
import chat.wewe.android.shared.BaseContract;
import chat.wewe.core.models.Room;
import chat.wewe.core.models.SpotlightRoom;
import chat.wewe.core.models.User;

public interface SidebarMainContract {

  interface View extends BaseContract.View {

    void showScreen();

    void showEmptyScreen();

    void showRoomList(@NonNull List<Room> roomList);

    void show(User user, RocketChatAbsoluteUrl absoluteUrl);
  }

  interface Presenter extends BaseContract.Presenter<View> {

    void onRoomSelected(Room room);

    void onSpotlightRoomSelected(SpotlightRoom spotlightRoom);

    void onUserOnline();

    void onUserAway();

    void onUserBusy();

    void onUserOffline();

    void onLogout();
  }
}
