package chat.wewe.android.fragment.sidebar.dialog;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.jakewharton.rxbinding2.widget.RxTextView;

import org.json.JSONArray;

import java.text.SimpleDateFormat;

import bolts.Task;
import chat.wewe.android.R;
import chat.wewe.android.adapter.RecyclerViewTask;
import chat.wewe.android.helper.Logger;
import chat.wewe.android.helper.TextUtils;

/**
 * add Channel, add Private-group.
 */


public class AddUsersDialogFragment extends AbstractAddRoomDialogFragment {

    public String ActiveNumber = "", userId = "";
    private ListView lvMain;
  private JSONArray info;

  public AddUsersDialogFragment() {
  }

  public static AddUsersDialogFragment create(String hostname,String romid,String userId) {
    Bundle args = new Bundle();
    args.putString("hostname", hostname);
    args.putString("romid", romid);
    args.putString("userId", userId);

    AddUsersDialogFragment fragment = new AddUsersDialogFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  protected int getLayout() {
    return R.layout.dialog_add_users;
  }

  @SuppressLint("RxLeakedSubscription")
  @Override
  protected void onSetupDialog() {

    lvMain =  getDialog().findViewById(R.id.list_view);



    methodCall.getUserRoles().onSuccessTask(task -> {
         info = task.getResult();
       String[] add = new String[info.length()];
     for (int i = 0; i < info.length(); i++) {
          add[i] = info.getJSONObject(i).getString("username");

     }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                R.layout.list_item_users, add);

        lvMain.setAdapter(adapter);

        lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                mListener.onClick(lvMain.getItemAtPosition(position).toString());
            }});

      return null;});




  }



  @Override
  protected Task<Void> getMethodCallForSubmitAction() {

      return methodCall.createChannel("", "", true);

  }


    public interface ActionListener{
        void onClick(String uid);
    }

    private ActionListener mListener;

    public void setActionListener(ActionListener listener){
        mListener = listener;
    }


}


