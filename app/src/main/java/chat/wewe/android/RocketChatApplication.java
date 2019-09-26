package chat.wewe.android;

import android.os.StrictMode;
import android.support.multidex.MultiDexApplication;
import com.facebook.stetho.Stetho;
import com.portsip.PortSipSdk;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import java.util.List;
import chat.wewe.android.helper.OkHttpHelper;
import chat.wewe.persistence.realm.RealmStore;
import chat.wewe.android.service.ConnectivityManager;
import chat.wewe.core.models.ServerInfo;
import chat.wewe.android.widget.RocketChatWidgets;
import chat.wewe.persistence.realm.RocketChatPersistenceRealm;

/**
 * Customized Application-class for Rocket.Chat
 */
public class RocketChatApplication extends MultiDexApplication {

  public boolean mConference= false;
  public PortSipSdk mEngine;
  public boolean mUseFrontCamera= false;
  @Override
  public void onCreate() {
    mEngine = new PortSipSdk();
    if (BuildConfig.DEBUG) {
      enableStrictMode();

    }

    super.onCreate();

    RocketChatPersistenceRealm.init(this);

    List<ServerInfo> serverInfoList = ConnectivityManager.getInstance(this).getServerList();
    for (ServerInfo serverInfo : serverInfoList) {
      RealmStore.put(serverInfo.getHostname());
    }

    if (BuildConfig.DEBUG) {
      enableStetho();
    }

    RocketChatWidgets.initialize(this, OkHttpHelper.getClientForDownloadFile(this));
  }

  private void enableStrictMode() {
    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        .detectDiskReads()
        .detectDiskWrites()
        .detectNetwork()   // or .detectAll() for all detectable problems
        .penaltyLog()
        .build());
    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
        .detectLeakedSqlLiteObjects()
        .detectLeakedClosableObjects()
        .penaltyLog()
        .build());
  }

  private void enableStetho() {
    Stetho.initialize(Stetho.newInitializerBuilder(this)
        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
        .build());
  }
}
