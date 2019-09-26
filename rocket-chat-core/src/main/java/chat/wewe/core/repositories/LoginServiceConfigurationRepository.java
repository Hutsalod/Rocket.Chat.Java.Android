package chat.wewe.core.repositories;

import com.fernandocejas.arrow.optional.Optional;
import io.reactivex.Flowable;
import io.reactivex.Single;

import java.util.List;
import chat.wewe.core.models.LoginServiceConfiguration;

public interface LoginServiceConfigurationRepository {

  Single<Optional<LoginServiceConfiguration>> getByName(String serviceName);

  Flowable<List<LoginServiceConfiguration>> getAll();
}
