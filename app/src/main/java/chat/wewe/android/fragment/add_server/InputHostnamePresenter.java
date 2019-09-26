package chat.wewe.android.fragment.add_server;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import chat.wewe.android.RocketChatCache;
import chat.wewe.android.api.rest.DefaultServerPolicyApi;
import chat.wewe.android.api.rest.ServerPolicyApi;
import chat.wewe.android.helper.OkHttpHelper;
import chat.wewe.android.helper.ServerPolicyApiValidationHelper;
import chat.wewe.android.helper.ServerPolicyHelper;
import chat.wewe.android.service.ConnectivityManagerApi;
import chat.wewe.android.shared.BasePresenter;

public class InputHostnamePresenter extends BasePresenter<InputHostnameContract.View>
    implements InputHostnameContract.Presenter {

  private final RocketChatCache rocketChatCache;
  private final ConnectivityManagerApi connectivityManager;

  public InputHostnamePresenter(RocketChatCache rocketChatCache,
                                ConnectivityManagerApi connectivityManager) {
    this.rocketChatCache = rocketChatCache;
    this.connectivityManager = connectivityManager;
  }

  @Override
  public void connectTo(final String hostname) {
    view.showLoader();

    connectToEnforced(ServerPolicyHelper.enforceHostname(hostname));
  }

  public void connectToEnforced(final String hostname) {
    final ServerPolicyApi serverPolicyApi =
        new DefaultServerPolicyApi(OkHttpHelper.getClientForUploadFile(), hostname);

    final ServerPolicyApiValidationHelper validationHelper =
        new ServerPolicyApiValidationHelper(serverPolicyApi);

    clearSubscriptions();

    final Disposable subscription = ServerPolicyHelper.isApiVersionValid(validationHelper)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnTerminate(() -> view.hideLoader())
        .subscribe(
            serverValidation -> {
                onServerValid(hostname, serverValidation.usesSecureConnection());
            },
            throwable -> view.showConnectionError());

    addSubscription(subscription);
  }

  private void onServerValid(final String hostname, boolean usesSecureConnection) {
    rocketChatCache.setSelectedServerHostname(hostname);

    connectivityManager.addOrUpdateServer(hostname, hostname, !usesSecureConnection);
    connectivityManager.keepAliveServer();

    view.showHome();
  }
}
