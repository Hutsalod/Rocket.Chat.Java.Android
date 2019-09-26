package chat.wewe.core.repositories;

import com.fernandocejas.arrow.optional.Optional;
import io.reactivex.Flowable;
import io.reactivex.Single;

import java.util.List;
import chat.wewe.core.SortDirection;
import chat.wewe.core.models.Room;
import chat.wewe.core.models.RoomHistoryState;

public interface RoomRepository {

  Flowable<List<Room>> getAll();

  Flowable<Optional<Room>> getById(String roomId);

  Flowable<Optional<RoomHistoryState>> getHistoryStateByRoomId(String roomId);

  Single<Boolean> setHistoryState(RoomHistoryState roomHistoryState);

  Flowable<List<Room>> getSortedLikeName(String name, SortDirection direction, int limit);

  Flowable<List<Room>> getLatestSeen(int limit);
}
