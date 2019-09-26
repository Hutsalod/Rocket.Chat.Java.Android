package chat.wewe.android.fragment.server_config;

import java.util.List;
import chat.wewe.android.shared.BaseContract;
import chat.wewe.core.models.LoginServiceConfiguration;

public interface LoginContract {

  interface View extends BaseContract.View {

    void showLoader();

    void hideLoader();

    void showError(String message);

    void showLoginServices(List<LoginServiceConfiguration> loginServiceList);

    void showTwoStepAuth();
  }

  interface Presenter extends BaseContract.Presenter<View> {

    void login(String username, String password);
  }
}
