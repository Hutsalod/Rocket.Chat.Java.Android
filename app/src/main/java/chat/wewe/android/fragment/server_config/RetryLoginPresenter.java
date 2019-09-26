package chat.wewe.android.fragment.server_config;

import android.support.annotation.NonNull;

import com.fernandocejas.arrow.optional.Optional;
import io.reactivex.android.schedulers.AndroidSchedulers;

import chat.wewe.android.BackgroundLooper;
import chat.wewe.android.api.MethodCallHelper;
import chat.wewe.android.helper.Logger;
import chat.wewe.android.helper.TextUtils;
import chat.wewe.android.shared.BasePresenter;
import chat.wewe.core.interactors.SessionInteractor;
import chat.wewe.core.models.Session;

import static chat.wewe.android.activity.Intro.TOKEN_RC;

public class RetryLoginPresenter extends BasePresenter<RetryLoginContract.View>
    implements RetryLoginContract.Presenter {

  private final SessionInteractor sessionInteractor;
  private final MethodCallHelper methodCallHelper;

  public RetryLoginPresenter(SessionInteractor sessionInteractor,
                             MethodCallHelper methodCallHelper) {
    this.sessionInteractor = sessionInteractor;
    this.methodCallHelper = methodCallHelper;
  }

  @Override
  public void bindView(@NonNull RetryLoginContract.View view) {
    super.bindView(view);

    subscribeToDefaultSession();
  }

  @Override
  public void onLogin(String token) {
    view.showLoader();

    methodCallHelper.loginWithToken(token)
        .continueWith(task -> {
          if (task.isFaulted()) {
            view.hideLoader();
          }
          return null;
        });
  }

  private void subscribeToDefaultSession() {
    addSubscription(
        sessionInteractor.getDefault()
            .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                this::onSession,
                Logger::report
            )
    );
  }

  private void onSession(Optional<Session> sessionOptional) {
    if (!sessionOptional.isPresent()) {
      return;
    }

    final Session session = sessionOptional.get();

    final String token = session.getToken();
    if (!TextUtils.isEmpty(token)) {
      view.showRetry(token);
    }
//TOKEN_RC = session.getToken();
    final String errorMessage = session.getError();
    if (!TextUtils.isEmpty(errorMessage)) {
      view.showError(errorMessage);
    }
  }
}
