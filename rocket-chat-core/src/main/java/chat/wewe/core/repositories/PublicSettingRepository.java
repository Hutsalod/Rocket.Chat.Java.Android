package chat.wewe.core.repositories;

import com.fernandocejas.arrow.optional.Optional;
import io.reactivex.Single;

import chat.wewe.core.models.PublicSetting;

public interface PublicSettingRepository {

  Single<Optional<PublicSetting>> getById(String id);
}
