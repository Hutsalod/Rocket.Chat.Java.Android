package chat.wewe.android.layouthelper.chatroom;

import android.util.Log;

import chat.wewe.android.activity.SettingActivity;
import chat.wewe.android.widget.message.MessageFormLayout;

import static chat.wewe.android.fragment.sidebar.SidebarMainFragment.getName;

/**
 * handling MessageForm.
 */
public class MessageFormManager {
  private final MessageFormLayout messageFormLayout;
  private SendMessageCallback sendMessageCallback;
  public  static String  nameBlack = "";

  public MessageFormManager(MessageFormLayout messageFormLayout,
                            MessageFormLayout.ExtraActionSelectionClickListener callback) {
    this.messageFormLayout = messageFormLayout;

    init(callback);
  }

  private void init(MessageFormLayout.ExtraActionSelectionClickListener listener) {
    messageFormLayout.setExtraActionSelectionClickListener(listener);
    messageFormLayout.setSubmitTextListener(this::sendMessage);
    messageFormLayout.setBlocingUsers(this::sendBlocking);
  /*  new SettingActivity().getBlacklist();
    Log.d("TEST23",""+getName+"ку"+nameBlack);
   if(getName.equals(nameBlack)) {
     messageFormLayout.setBlocing(true);
   }else
   {messageFormLayout.setBlocing(false);}*/
  }

  public void setSendMessageCallback(SendMessageCallback sendMessageCallback) {
    this.sendMessageCallback = sendMessageCallback;
  }

  public void clearComposingText() {
    messageFormLayout.setText("");
  }

  public void onMessageSend() {
    clearComposingText();
    messageFormLayout.setEnabled(true);
  }

  public void setEditMessage(String message) {
    clearComposingText();
    messageFormLayout.setText(message);
  }

  private void sendMessage(String message) {
    if (sendMessageCallback == null) {
      return;
    }

    messageFormLayout.setEnabled(false);
    sendMessageCallback.onSubmitText(message);
  }

  public interface SendMessageCallback {
    void onSubmitText(String messageText);
  }



  private void sendBlocking() {
    Log.d("QAZX","TRUE");
    new SettingActivity().UF_ROCKET_LOGIN_BLOC(getName);
  }


}
