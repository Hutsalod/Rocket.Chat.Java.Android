package chat.wewe.android.fragment.sidebar.dialog;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import bolts.Task;
import chat.wewe.android.R;
import chat.wewe.android.adapter.RecyclerViewTask;
import chat.wewe.android.helper.LogIfError;
import chat.wewe.persistence.realm.models.ddp.RealmRoom;
import chat.wewe.persistence.realm.models.internal.GetUsersOfRoomsProcedure;

/**
 * add Channel, add Private-group.
 */
public class AddTaskFragment extends AbstractAddRoomDialogFragment {
  public String ActiveNumber = "", userId = "";
  private RecyclerView recyclerView;
  private Button buttonBlackList;
  private LinearLayout addTask,getTask;
  private Button closed,red;
  private Spinner spinner;
  private JSONArray info;
  private int position;
  private ListView list_view;
  private TextView task_name,_taskText,_createdBy,date;
  private ArrayList<String> mNames = new ArrayList<>();
  private ArrayList<String> mCreatedBy= new ArrayList<>();
  private ArrayList<String> mPosition = new ArrayList<>();
  private ArrayList<String> mData = new ArrayList<>();
  private ArrayList<String> mMessage = new ArrayList<>();
  private ArrayList<Integer> mNumberId = new ArrayList<>();
  private ArrayList<Boolean> mClosed = new ArrayList<>();

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
    buttonBlackList = getDialog().findViewById(R.id.buttonBlackList);
    recyclerView = getDialog().findViewById(R.id.recyclerv_view);
    addTask = getDialog().findViewById(R.id.addTask);
    spinner = getDialog().findViewById(R.id.spinner);
    getTask = getDialog().findViewById(R.id.getTask);

    task_name = getDialog().findViewById(R.id.task_name);
    _taskText = getDialog().findViewById(R.id._taskText);
    _createdBy = getDialog().findViewById(R.id._createdBy);
    date = getDialog().findViewById(R.id.date);
    closed = getDialog().findViewById(R.id.closed);
    red = getDialog().findViewById(R.id.red);

    list_view = getDialog().findViewById(R.id.list_view);

    ActiveNumber = getArguments().getString("romid");

    methodCall.getTask(ActiveNumber).onSuccessTask(task -> {
              info = task.getResult();
      for (int i = 0; i < info.length(); i++) {
        add(info.getJSONObject(i).isNull("_name") ? " " : info.getJSONObject(i).getString("_name"),info.getJSONObject(i).isNull("_taskText") ? " " : info.getJSONObject(i).getString("_taskText"),info.getJSONObject(i).isNull("_createdBy") ? " " : info.getJSONObject(i).getString("_createdBy"),new SimpleDateFormat("dd.MM.yyyy hh:mm").format(new java.util.Date((long)info.getJSONObject(i).getJSONObject("_date").getLong("$date"))),info.getJSONObject(i).isNull("_numberId") ? 0 : info.getJSONObject(i).getInt("_numberId"),info.getJSONObject(i).isNull("_closed") ? false : info.getJSONObject(i).getBoolean("_closed"));
      }
      initRecyclerView();

      return null;});

      methodCall.getUsersByRoomId(ActiveNumber).onSuccessTask(task -> {
      JSONArray info = task.getResult();
      String[] data = new String[info.length()+1];
      data[0] = "Нет";

      for (int i = 1; i < info.length()+1; i++) {
        data[i] = info.getJSONObject(i-1).isNull("username") ? "" : info.getJSONObject(i-1).getString("username");

      }


      ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, data);
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

      spinner.setAdapter(adapter);
      return null;});

    buttonBlackList.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
          if(getTask.getVisibility()==View.GONE) {
            addTask.setVisibility(addTask.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            recyclerView.setVisibility(addTask.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            buttonBlackList.setText(addTask.getVisibility() == View.VISIBLE ? "Отмена" : "Добавить задачу");
          }else{
              addTask.setVisibility(View.GONE);
              getTask.setVisibility(View.GONE);
              recyclerView.setVisibility(View.VISIBLE);
              buttonBlackList.setText("Добавить задачу");
        }

      }
    });

    closed.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        methodCall.closeTask(ActiveNumber, position ,"hutsalod").continueWith(task -> {
          Toast.makeText(getContext(), task.getError().getMessage(), Toast.LENGTH_SHORT).show();

          return null;
        });
      dismiss();
      }
    });


      red.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            addTask.setVisibility(View.VISIBLE);
            getTask.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            buttonBlackList.setText("Отмена");
          }
      });

    buttonAddChannel.setOnClickListener(view -> createRoom());

    initRecyclerView();
  }

  private void add(String name,String uid,String createdBy,String data,Integer numberId,Boolean closed){
    mNames.add(name);
    mPosition.add(uid);
    mCreatedBy.add(createdBy);
    mData.add(data);
    mNumberId.add(numberId);
    mClosed.add(closed);
  }

  public void initRecyclerView(){
    RecyclerViewTask adapter = new RecyclerViewTask(getContext(), mNames,mPosition,mCreatedBy,mData,mNumberId,mClosed);
    recyclerView.setAdapter(adapter);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    adapter.setActionListener(new RecyclerViewTask.ActionListener() {
      @Override
      public void onClick(String uid,int position) {
        Log.d("MOSTTEST", "NO" + uid);
        methodCall.closeTask(ActiveNumber, position ,"hutsalod").continueWith(task -> {
          Toast.makeText(getContext(), task.getError().getMessage(), Toast.LENGTH_SHORT).show();

          return null;
        });
      }

      @Override
      public void onRed(String uid, int position) {
        addTask.setVisibility(addTask.getVisibility()==View.VISIBLE ? View.GONE : View.VISIBLE);
        recyclerView.setVisibility(addTask.getVisibility()==View.VISIBLE ? View.GONE : View.VISIBLE);
        buttonBlackList.setText(addTask.getVisibility()==View.VISIBLE ? "Отмена" : "Добавить задачу");
      }

      @Override
      public void onGet(int position) {
        getTask(position);
      }
    });
  }



  @Override
  protected Task<Void> getMethodCallForSubmitAction() {


    TextView nameTaskin = getDialog().findViewById(R.id.nameTaskin);
    TextView msgTaskin = getDialog().findViewById(R.id.msgTaskin);
    ActiveNumber = getArguments().getString("romid");
    userId = getArguments().getString("userId");
    //methodCall.AddTask(ActiveNumber, userId , nameTaskin.getText().toString() , msgTaskin.getText().toString() ,"hutsalod","");

    Log.d("TASKENTW","TYPE  TYPE_PRIVATE"+ActiveNumber + "" + userId + "" + nameTaskin.getText().toString() + msgTaskin.getText().toString() +"hutsalod");

    if (nameTaskin.getText().length()>0) {
      return   methodCall.AddTask(ActiveNumber, userId , nameTaskin.getText().toString() , msgTaskin.getText().toString() ,"hutsalod",spinner.getSelectedItem().toString());
    } else {
      return   methodCall.AddTask(ActiveNumber, userId , nameTaskin.getText().toString() , msgTaskin.getText().toString() ,"hutsalod",spinner.getSelectedItem().toString());
    }

  }


  public void getTask(int tab){
    try {
      for (int i = 0; i < info.length(); i++) {
        if(tab==info.getJSONObject(i).getInt("_numberId")) {
          task_name.setText("Задача #" + (info.getJSONObject(i).isNull("_numberId") ? " " : info.getJSONObject(i).getString("_numberId")) + (info.getJSONObject(i).isNull("_name") ? " " : info.getJSONObject(i).getString("_name")));
          _taskText.setText(info.getJSONObject(i).isNull("_taskText") ? " " : info.getJSONObject(i).getString("_taskText"));
          _createdBy.setText("Создан: " + (info.getJSONObject(i).isNull("_createdBy") ? " " : info.getJSONObject(i).getString("_createdBy")));
          date.setText(new SimpleDateFormat("dd.MM.yyyy hh:mm").format(new java.util.Date((long) info.getJSONObject(i).getJSONObject("_date").getLong("$date"))));

          if(info.getJSONObject(i).getJSONArray("_messages").length()>0) {
            String[] myArray = new String[info.getJSONObject(i).getJSONArray("_messages").length()];
            myArray[0] = "";
             for (int m = 0; m < info.getJSONObject(i).getJSONArray("_messages").length(); m++) {
             myArray[0] = info.getJSONObject(i).getJSONArray("_messages").getJSONObject(0).getJSONObject("u").getString("username")+": "+info.getJSONObject(i).getJSONArray("_messages").getJSONObject(m).getString("msg");
            }

            list_view.setAdapter(new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_list_item_1, myArray));
          }else {
            String[] myArray = new String[0];
            list_view.setAdapter(new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_list_item_1, myArray));
          }
          position = tab;
          break;
        }
      }
      addTask.setVisibility(View.GONE);
      recyclerView.setVisibility(View.GONE);
      getTask.setVisibility(View.VISIBLE);
      buttonBlackList.setText("Назад");

    } catch (JSONException e) {
      e.printStackTrace();
    }

  }


}
