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
  public String ActiveNumber = "", userId = "";
  public AddTaskFragment() {
  }

  public static AddTaskFragment create(String hostname,String romid,String userId) {
    Bundle args = new Bundle();
    args.putString("hostname", hostname);
    args.putString("romid", romid);
    args.putString("userId", userId);

    AddTaskFragment fragment = new AddTaskFragment();
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
    ActiveNumber = getArguments().getString("romid");

    buttonAddChannel.setOnClickListener(view -> createRoom());
    //methodCall.getTask(ActiveNumber);

  }



  @Override
  protected Task<Void> getMethodCallForSubmitAction() {


    TextView nameTaskin = getDialog().findViewById(R.id.nameTaskin);
    TextView msgTaskin = getDialog().findViewById(R.id.msgTaskin);
    ActiveNumber = getArguments().getString("romid");
    userId = getArguments().getString("userId");


    Log.d("TASKENTW","TYPE  TYPE_PRIVATE"+ActiveNumber + "" + userId + "" + nameTaskin.getText().toString() + msgTaskin.getText().toString() +"hutsalod");

    if (nameTaskin.getText().length()>0) {
      return  methodCall.AddTask(ActiveNumber,userId,nameTaskin.getText().toString(), msgTaskin.getText().toString(),"hutsalod","");
    } else {
      return  methodCall.AddTask(ActiveNumber,userId,nameTaskin.getText().toString(), msgTaskin.getText().toString(),"hutsalod","");
    }

  }


}
