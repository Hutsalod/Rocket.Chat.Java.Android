package chat.wewe.core;

/*https://comeliest-adaptions.000webhostapp.com/*/
public class UtilsApi {
    // 10.0.2.2 ini adalah localhost.
    public static final String BASE_URL_API = "https://chat.weltwelle.com/";

    // Mendeklarasikan Interface BaseApiService
    public static BaseApiService getAPIService(){
        return RetrofitClient.getClient(BASE_URL_API).create(BaseApiService.class);
    }
}
