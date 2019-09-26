package chat.wewe.core.repositories;

import com.fernandocejas.arrow.optional.Optional;
import io.reactivex.Single;

import chat.wewe.core.models.Room;
import chat.wewe.core.models.RoomRole;
import chat.wewe.core.models.User;

public interface RoomRoleRepository {

  Single<Optional<RoomRole>> getFor(Room room, User user);
}
