package chat.wewe.android.api;

import com.google.gson.JsonObject;

import java.util.Map;

import chat.wewe.android.api.model.sub;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BaseApiService {


    @POST("rest_api/auth/")
    public Call<ResponseBody> getStatusUsers();

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



    @POST("rest/user/subscription/")
    Call<JsonObject>subscription(@Header("Authorization-Token") String Token,@Header("Content-Type") String Type,@Body Map<String, Object> params);

    @GET("rest/user/remove_transactionid/")
    Call<ResponseBody>removeTransactionid(@Header("Authorization-Token") String Token);



    @Headers({"X-Auth-Token: hDUgZ30KAl4KG7_rofwCEBk0ewAl1CcrQGLZSx0i65x","X-User-Id: gdP4WgEFQ3mKhZXyJ"})
    @GET("api/v1/users.info?")
    public Call<ResponseBody>getStatus(@Query("username") String user);

    @Headers({"X-Auth-Token: hDUgZ30KAl4KG7_rofwCEBk0ewAl1CcrQGLZSx0i65x","X-User-Id: gdP4WgEFQ3mKhZXyJ","Content-type: application/json"})
    @GET("api/v1/spotlight?")
    public Call<ResponseBody>getList(@Query("query") String user);


    @GET("rest/blacklist/get/")
    Call<ResponseBody>getBlacklist(@Header("Authorization-Token") String Token);

    @Headers({"Content-Type: application/json"})
    @POST("rest/blacklist/add/")
    Call<ResponseBody>getBlacklistAdd(@Header("Authorization-Token") String Token,@Body Map<String, Object> params);

    @Headers({"Content-Type: application/json"})
    @POST("rest/blacklist/delete/")
    Call<JsonObject>getBlacklistDell(@Header("Authorization-Token") String Token,@Body Map<String, Object> params);


    @Headers({"X-Auth-Token: hDUgZ30KAl4KG7_rofwCEBk0ewAl1CcrQGLZSx0i65x","X-User-Id: gdP4WgEFQ3mKhZXyJ"})
    @GET("api/v1/users.list?count=0")
    public Call<ResponseBody>getListStatus();
}
