package chat.wewe.android.api;

import chat.wewe.android.api.BaseApiService;
import chat.wewe.android.api.RetrofitClient;

/*https://comeliest-adaptions.000webhostapp.com/*/
public class UtilsApiChat {
    // 10.0.2.2 ini adalah localhost.
    public static final String BASE_URL_API_CHAT = "https://chat.weltwelle.com/";

    // Mendeklarasikan Interface BaseApiService
    public static BaseApiService getAPIService(){
        return RetrofitClientChat.getClient(BASE_URL_API_CHAT).create(BaseApiService.class);
    }
}
