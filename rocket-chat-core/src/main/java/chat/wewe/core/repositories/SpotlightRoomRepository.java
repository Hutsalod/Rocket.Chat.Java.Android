package chat.wewe.core.repositories;

import io.reactivex.Flowable;

import java.util.List;
import chat.wewe.core.SortDirection;
import chat.wewe.core.models.SpotlightRoom;

public interface SpotlightRoomRepository {

  Flowable<List<SpotlightRoom>> getSuggestionsFor(String name, SortDirection direction, int limit);
}
