package chat.wewe.android.service;

import rx.Single;

public interface ConnectivityServiceInterface {
  Single<Boolean> ensureConnectionToServer(String hostname);

  Single<Boolean> disconnectFromServer(String hostname);
}
