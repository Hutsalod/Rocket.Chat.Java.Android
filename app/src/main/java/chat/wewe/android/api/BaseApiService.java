package chat.wewe.android.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BaseApiService {

    // Fungsi ini untuk memanggil API http://10.0.2.2/mahasiswa/login.php
    @FormUrlEncoded
    @POST("rest_api/auth/")
    public Call<ResponseBody> loginRequest(@Field("USER_LOGIN") String email,
                                           @Field("USER_PASSWORD") String password,
                                           @Field("DEVID") String devid);

    // Fungsi ini untuk memanggil API http://10.0.2.2/mahasiswa/register.php
    @FormUrlEncoded
    @POST("rest_api/register/")
    public Call<ResponseBody> registerRequest(@Field("DEVID") String nama,
                                              @Field("USER_LOGIN") String email,
                                              @Field("USER_PASSWORD") String password);


    @GET("rest/user/settings/")
    Call<ResponseBody> getSettings(@Header("Authorization-Token") String authKeys);

    @FormUrlEncoded
    @POST("rest/user/access_device/")
    Call<ResponseBody>postDevice(@Header("Authorization-Token") String authKeys,@Field("UF_ACCESS_OTH_DEVICE") String DEVICE);

    @Headers({"Authorization-Token: KEY:9a055dbe-cbe5086c-f8bad6ee-aed30fca","Content-Type: application/json"})
    @FormUrlEncoded
    @POST("rest/user/subscription/")
    Call<ResponseBody>subscription(@Field("UF_ORIGINAL_TRID") String UF_ORIGINAL_TRID);



    @Headers({"X-Auth-Token: hDUgZ30KAl4KG7_rofwCEBk0ewAl1CcrQGLZSx0i65x","X-User-Id: gdP4WgEFQ3mKhZXyJ"})
    @GET("api/v1/users.info?")
    public Call<ResponseBody>getStatus(@Query("username") String user);

    @Headers({"X-Auth-Token: hDUgZ30KAl4KG7_rofwCEBk0ewAl1CcrQGLZSx0i65x","X-User-Id: gdP4WgEFQ3mKhZXyJ","Content-type: application/json"})
    @GET("api/v1/spotlight?")
    public Call<ResponseBody>getList(@Query("query") String user);


    @GET("rest/blacklist/get/")
    Call<ResponseBody>getBlacklist(@Header("Authorization-Token") String Token);

    @POST("rest/blacklist/add/")
    @FormUrlEncoded
    Call<ResponseBody>getBlacklistAdd(@Header("Authorization-Token") String Token,@Field("UF_ROCKET_LOGIN") String UF_ROCKET_LOGIN);
}
