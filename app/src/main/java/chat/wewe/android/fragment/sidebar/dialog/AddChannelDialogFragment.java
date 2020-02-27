package chat.wewe.android.fragment.sidebar.dialog;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.jakewharton.rxbinding2.widget.RxTextView;

import java.util.Random;

import bolts.Task;
import chat.wewe.android.R;
import chat.wewe.android.helper.Logger;
import chat.wewe.android.helper.TextUtils;

/**
 * add Channel, add Private-group.
 */


public class AddChannelDialogFragment extends AbstractAddRoomDialogFragment {

  public AddChannelDialogFragment() {
  }

  public static AddChannelDialogFragment create(String hostname) {
    Bundle args = new Bundle();
    args.putString("hostname", hostname);

    AddChannelDialogFragment fragment = new AddChannelDialogFragment();
    fragment.setArguments(args);
    return fragment;
  }



  @Override
  protected int getLayout() {
    return R.layout.dialog_add_channel;
  }

  @SuppressLint("RxLeakedSubscription")
  @Override
  protected void onSetupDialog() {
    View buttonAddChannel = getDialog().findViewById(R.id.btn_add_channel);
    TextView channelNameId = (TextView) getDialog().findViewById(R.id.editor_channel_id);

    String symbols = "weid1234qaz";
    StringBuilder randString = new StringBuilder();
    for(int i=0;i<10;i++)
      randString.append(symbols.charAt((int)(Math.random()*symbols.length())));

    channelNameId.setText(""+randString);

    RxTextView.textChanges((TextView) getDialog().findViewById(R.id.editor_channel_name))
            .map(text -> !TextUtils.isEmpty(text))
            .compose(bindToLifecycle())
            .subscribe(
                    buttonAddChannel::setEnabled,
                    Logger::report
            );

       buttonAddChannel.setOnClickListener(view -> createRoom());
  }

  private boolean isChecked(int viewId) {
    CompoundButton check = (CompoundButton) getDialog().findViewById(viewId);
    return check.isChecked();
  }

  @Override
  protected Task<Void> getMethodCallForSubmitAction() {
    TextView channelNameText = (TextView) getDialog().findViewById(R.id.editor_channel_name);
    TextView channelNameId = (TextView) getDialog().findViewById(R.id.editor_channel_name);
    String channelName = channelNameText.getText().toString();
    String channelId = channelNameId.getText().toString();
    boolean isPrivate = isChecked(R.id.checkbox_private);
    boolean isReadOnly = isChecked(R.id.checkbox_read_only);

    if (isPrivate) {
      return methodCall.createPrivateGroup(channelName, isReadOnly);
    } else {
      return methodCall.createChannel(channelName, channelId,isReadOnly);
    }
  }
}


