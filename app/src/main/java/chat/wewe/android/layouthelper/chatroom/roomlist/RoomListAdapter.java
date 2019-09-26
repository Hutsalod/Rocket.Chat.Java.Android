package chat.wewe.android.layouthelper.chatroom.roomlist;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import chat.wewe.android.R;
import chat.wewe.android.fragment.sidebar.SidebarMainFragment;
import chat.wewe.android.widget.internal.RoomListItemView;
import chat.wewe.core.models.Room;
import chat.wewe.core.models.SpotlightRoom;

public class RoomListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  public static final int MODE_ROOM = 0;
  public static final int MODE_SPOTLIGHT_ROOM = 1;

  private static final int VIEW_TYPE_HEADER = 0;
  private static final int VIEW_TYPE_ROOM = 1;
  public static  ArrayList<String> arrayListList = new ArrayList<>();
  private List<Room> roomList = Collections.emptyList();
  private List<SpotlightRoom> spotlightRoomList = Collections.emptyList();
  private List<RoomListHeader> roomListHeaders = Collections.emptyList();
  public Map<Integer, RoomListHeader> headersPosition = new HashMap<>();

  private int mode = MODE_ROOM;

  private OnItemClickListener externalListener;
  private OnItemClickListener listener = new OnItemClickListener() {
    @Override
    public void onItemClick(Room room) {
      if (externalListener != null) {
        externalListener.onItemClick(room);
      }
    }

    @Override
    public void onItemClick(SpotlightRoom spotlightRoom) {
      if (externalListener != null) {
        externalListener.onItemClick(spotlightRoom);
      }
    }
  };

  public void setRoomListHeaders(@NonNull List<RoomListHeader> roomListHeaders) {
    this.roomListHeaders = roomListHeaders;

    updateRoomList();
  }

  public void setRooms(@NonNull List<Room> roomList) {
    this.roomList = roomList;

    updateRoomList();


  }

  public void setSpotlightRoomList(@NonNull List<SpotlightRoom> spotlightRoomList) {
    this.spotlightRoomList = spotlightRoomList;

    updateRoomList();

  }

  public void setMode(int mode) {
    this.mode = mode;

    if (mode == MODE_ROOM) {
      // clean up
      spotlightRoomList.clear();
        arrayListList.clear();
    }
  }

  public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
    externalListener = onItemClickListener;
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    if (viewType == VIEW_TYPE_HEADER) {
      return new RoomListHeaderViewHolder(
          LayoutInflater.from(parent.getContext())
              .inflate(R.layout.room_list_header, parent, false)
      );
    }
    return new RoomListItemViewHolder(new RoomListItemView(parent.getContext()), listener);
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    if (mode == MODE_ROOM) {
      if (getItemViewType(position) == VIEW_TYPE_HEADER) {
        ((RoomListHeaderViewHolder) holder)
            .bind(headersPosition.get(position));
        return;
      }

      ((RoomListItemViewHolder) holder)
          .bind(roomList.get(position - getTotalHeadersBeforePosition(position)));
      Log.d("SWIPE", "spotlightRoomList " + roomList.get(position - getTotalHeadersBeforePosition(position)).getName());
      arrayListList.add(roomList.get(position - getTotalHeadersBeforePosition(position)).getRoomId());
    } else if (mode == MODE_SPOTLIGHT_ROOM) {
      ((RoomListItemViewHolder) holder)
          .bind(spotlightRoomList.get(position));
    }
  }

  @Override
  public int getItemCount() {
    if (mode == MODE_SPOTLIGHT_ROOM) {
      return spotlightRoomList.size();
    }
    return roomList.size() + headersPosition.size();
  }

  @Override
  public int getItemViewType(int position) {
    if (mode == MODE_SPOTLIGHT_ROOM) {
      return VIEW_TYPE_ROOM;
    }

    if (headersPosition.containsKey(position)) {
      return VIEW_TYPE_HEADER;
    }
    return VIEW_TYPE_ROOM;
  }

  private void updateRoomList() {
    if (mode == MODE_ROOM) {

      sortRoomList();
   calculateHeadersPosition();
    }
    Log.d("SWIPE", "YES! ");
    notifyDataSetChanged();

  }

  private void sortRoomList() {

    int totalHeaders = roomListHeaders.size();

    Collections.sort(roomList, (anotherRoom, room) -> {

      for (int i = 0; i < totalHeaders; i++) {
        arrayListList.clear();
        final RoomListHeader header = roomListHeaders.get(i);
        if (header.owns(room) && !header.owns(anotherRoom)) {
          return -1;
        } else if (!header.owns(room) && header.owns(anotherRoom)) {
          return 1;
        }

      }
     Collections.reverseOrder();

      return room.getUpdatedAt().compareTo(anotherRoom.getUpdatedAt());


    });
  }

  private void calculateHeadersPosition() {
    headersPosition.clear();


    int roomIdx = 0;
    int totalRooms = roomList.size();
    int totalHeaders = roomListHeaders.size();
    for (int i = 0; i < totalHeaders; i++) {
      final RoomListHeader header = roomListHeaders.get(i);
      if (!header.shouldShow(roomList)) {
        continue;
      }

      headersPosition.put(roomIdx + headersPosition.size(), header);

      for (; roomIdx < totalRooms; roomIdx++) {
        final Room room = roomList.get(roomIdx);

        if (!header.owns(room)) {

          break;
        }
      }
    }

  }

  private int getTotalHeadersBeforePosition(int position) {
    int totalHeaders = headersPosition.size();
    Integer[] keySet = headersPosition.keySet().toArray(new Integer[totalHeaders]);

    int totalBefore = 0;
    for (int i = 0; i < totalHeaders; i++) {
      if (keySet[i] <= position) {
        totalBefore++;
      }
    }


    return totalBefore;
  }

  public interface OnItemClickListener {
    void onItemClick(Room room);

    void onItemClick(SpotlightRoom spotlightRoom);
  }

}
