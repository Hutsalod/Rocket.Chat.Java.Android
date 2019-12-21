package chat.wewe.android.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class JoinWeWe {

    public static void postCallvoip(Context Context, String NAME){
        SharedPreferences SipData;
        BaseApiService mApiService = UtilsApi.getAPIService();
        SipData = Context.getSharedPreferences("SIP", MODE_PRIVATE);
        Log.d("TREWQ",""+ UUID.randomUUID().toString()+" "+NAME);
        Map<String, Object> jsonParams = new ArrayMap<>();
        jsonParams.put("GUID", UUID.randomUUID().toString());
        jsonParams.put("LOGIN_TO", NAME);
        mApiService.postCallvoip("KEY:"+SipData.getString("TOKENWE",""),jsonParams)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()){
                            try {
                                JSONObject jsonRESULTS = new JSONObject(response.body().string());
                                Log.d("TREWQ",""+jsonRESULTS);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                    }
                });
    }
}
