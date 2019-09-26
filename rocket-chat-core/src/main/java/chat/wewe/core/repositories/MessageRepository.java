package chat.wewe.core.repositories;

import com.fernandocejas.arrow.optional.Optional;
import io.reactivex.Flowable;
import io.reactivex.Single;

import java.util.List;
import chat.wewe.core.models.Message;
import chat.wewe.core.models.Room;
import chat.wewe.core.models.User;

public interface MessageRepository {

  Single<Optional<Message>> getById(String messageId);

  Single<Boolean> save(Message message);

  Single<Boolean> delete(Message message);

  Flowable<List<Message>> getAllFrom(Room room);

  Single<Integer> unreadCountFor(Room room, User user);
}
