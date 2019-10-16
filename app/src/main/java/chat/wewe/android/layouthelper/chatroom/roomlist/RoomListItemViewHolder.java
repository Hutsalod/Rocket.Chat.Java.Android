package chat.wewe.android.layouthelper.chatroom.roomlist;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import chat.wewe.android.activity.MainActivity;
import chat.wewe.android.widget.AbsoluteUrl;
import chat.wewe.android.widget.internal.RoomListItemView;
import chat.wewe.core.BaseApiService;
import chat.wewe.core.UtilsApi;
import chat.wewe.core.models.Room;
import chat.wewe.core.models.SpotlightRoom;
import chat.wewe.core.models.User;
import chat.wewe.persistence.realm.RealmHelper;
import chat.wewe.persistence.realm.models.ddp.RealmUser;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoomListItemViewHolder extends RecyclerView.ViewHolder {
  private final RealmHelper realmHelper;
  
  

  
  BaseApiService mApiService = UtilsApi.getAPIService();
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
    realmHelper = null;
  }


  public void bind(Room room) {
    ((RoomListItemView) itemView)
        .setRoomId(room.getRoomId())
        .setRoomName(room.getName())
        .setRoomType(room.getType())
        .setAlert(room.isAlert())
        .setUnreadCount(room.getUnread())
        .setTag(room);
  getStart(room.getName());///Статуси
    Log.d("Status55", "r " + room.getName());



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


  private void showUserStatusIcon(Boolean userStatus) {
    if (userStatus) {
      ((RoomListItemView) itemView). showOnlineUserStatusIcon();
    } else {
      ((RoomListItemView) itemView).showOfflineUserStatusIcon();
    }
  }

  public void getStart(String roomNames){
    mApiService.getList(roomNames)
            .enqueue(new Callback<ResponseBody>() {
              @Override
              public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                  try {
                    JSONObject jsonRESULTS = new JSONObject(response.body().string());
                    if (jsonRESULTS.getJSONObject("user").getString("status").equals("online")) {
                      Log.d("Status5", "" + roomNames);
                        showUserStatusIcon(true);
                    }else{
                      showUserStatusIcon(false);
                    }
                  } catch (JSONException e) {
                    e.printStackTrace();
                  } catch (IOException e) {
                    e.printStackTrace();
                  }
                } else {
                }
              }

              @Override
              public void onFailure(Call<ResponseBody> call, Throwable t) {
              }
            });
  }
}
