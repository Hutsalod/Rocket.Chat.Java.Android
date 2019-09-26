package chat.wewe.core.repositories;

import com.fernandocejas.arrow.optional.Optional;
import io.reactivex.Flowable;

import java.util.List;
import chat.wewe.core.SortDirection;
import chat.wewe.core.models.User;

public interface UserRepository {

  Flowable<Optional<User>> getCurrent();

  Flowable<List<User>> getSortedLikeName(String name, SortDirection direction, int limit);
}
