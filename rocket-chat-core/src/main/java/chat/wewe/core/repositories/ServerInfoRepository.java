package chat.wewe.core.repositories;

import com.fernandocejas.arrow.optional.Optional;

import io.reactivex.Flowable;

import chat.wewe.core.models.ServerInfo;

public interface ServerInfoRepository {

  Flowable<Optional<ServerInfo>> getByHostname(String hostname);
}
