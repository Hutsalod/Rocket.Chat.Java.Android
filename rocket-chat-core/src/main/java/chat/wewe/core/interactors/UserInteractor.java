package chat.wewe.core.interactors;

import io.reactivex.Flowable;

import java.util.List;
import chat.wewe.core.SortDirection;
import chat.wewe.core.models.User;
import chat.wewe.core.repositories.UserRepository;

public class UserInteractor {

  private final UserRepository userRepository;

  public UserInteractor(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public Flowable<List<User>> getUserAutocompleteSuggestions(String name) {
    return userRepository.getSortedLikeName(name, SortDirection.DESC, 5);
  }
}
