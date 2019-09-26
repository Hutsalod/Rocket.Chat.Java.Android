package chat.wewe.android.fragment.chatroom;

import android.support.annotation.Nullable;

import java.util.List;
import chat.wewe.android.shared.BaseContract;
import chat.wewe.core.models.Message;
import chat.wewe.core.models.Room;

public interface RoomContract {

  interface View extends BaseContract.View {

    void setupWith(RocketChatAbsoluteUrl rocketChatAbsoluteUrl);

    void render(Room room);

    void updateHistoryState(boolean hasNext, boolean isLoaded);

    void onMessageSendSuccessfully();

    void showUnreadCount(int count);

    void showMessages(List<Message> messages);

    void showMessageSendFailure(Message message);

    void autoloadImages();

    void manualLoadImages();
  }

  interface Presenter extends BaseContract.Presenter<View> {

    void loadMessages();

    void loadMoreMessages();

    void onMessageSelected(@Nullable Message message);

    void sendMessage(String messageText);

    void resendMessage(Message message);

    void updateMessage(Message message, String content);

    void deleteMessage(Message message);

    void onUnreadCount();

    void onMarkAsRead();
  }
}
