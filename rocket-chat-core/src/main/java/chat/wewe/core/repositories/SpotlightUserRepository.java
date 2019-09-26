package chat.wewe.core.repositories;

import io.reactivex.Flowable;

import java.util.List;
import chat.wewe.core.SortDirection;
import chat.wewe.core.models.SpotlightUser;

public interface SpotlightUserRepository {

  Flowable<List<SpotlightUser>> getSuggestionsFor(String name, SortDirection direction, int limit);
}
