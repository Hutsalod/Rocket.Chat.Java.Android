package chat.wewe.core;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface BaseApiService {


    @Headers({"X-Auth-Token: hDUgZ30KAl4KG7_rofwCEBk0ewAl1CcrQGLZSx0i65x","X-User-Id: gdP4WgEFQ3mKhZXyJ"})
    @GET("api/v1/users.info?")
    public Call<ResponseBody>getList(@Query("username") String user);


    @POST("api/v1/users.delete")
    @FormUrlEncoded
    Call<ResponseBody> deleteUsers(@Header("Authorization-Token") String token,@Header("X-User-Id") String id,@Header("Content-type") String type,
                                                   @Field("username") String docId

    );
}
