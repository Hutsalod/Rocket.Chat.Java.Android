package chat.wewe.android.fragment.sidebar.dialog;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;


import bolts.Task;
import chat.wewe.android.R;

/**
 * add Channel, add Private-group.
 */
public class AddTaskFragment extends AbstractAddRoomDialogFragment {
  public String ActiveNumber = "";
  public AddTaskFragment() {
  }

  public static AddTaskFragment create(String hostname) {
    Bundle args = new Bundle();
    args.putString("hostname", hostname);

    AddTaskFragment fragment = new AddTaskFragment();
    fragment.setArguments(args);
    return fragment;
  }

  public static AddTaskFragment newInstance(String romid){
    AddTaskFragment fragment = new AddTaskFragment();

    Bundle args = new Bundle();
    args.putString("romid", romid);
    //args.putString(FrontActivity.EXTRA_SECTION_COUNTER_NUM, String.valueOf(sectionCounterNumber));

    fragment.setArguments(args);
    return fragment;
  }

  @Override
  protected int getLayout() {
    return R.layout.dialog_add_task;
  }

  @SuppressLint("RxLeakedSubscription")
  @Override
  protected void onSetupDialog() {
    View buttonAddChannel = getDialog().findViewById(R.id.btn_add_channel);
    TextView nameTaskin = getDialog().findViewById(R.id.nameTaskin);
    TextView msgTaskin = getDialog().findViewById(R.id.editor_channel_name);

    String textTasks  = nameTaskin.getText().toString();
    String msgTaskins = msgTaskin.getText().toString();
    ActiveNumber = getArguments().getString("romid");

  //  buttonAddChannel.setOnClickListener(view -> taskCreate(ActiveNumber,textTasks,msgTaskins,"hutsalod",""));

  }



  @Override
  protected Task<Void> getMethodCallForSubmitAction() {




      return methodCall.AddTask("","","","hutsalod","");

  }


}
