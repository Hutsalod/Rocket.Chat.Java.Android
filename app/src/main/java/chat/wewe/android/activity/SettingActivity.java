package chat.wewe.android.activity;

import android.app.Activity;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.google.gson.JsonObject;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import chat.wewe.android.api.BaseApiService;
import chat.wewe.android.api.UtilsApi;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static chat.wewe.android.activity.Intro.TOKENWE;
import static chat.wewe.android.fragment.sidebar.SidebarMainFragment.getName;
import static chat.wewe.android.layouthelper.chatroom.MessageFormManager.mnameBlack;

public class SettingActivity extends Activity {

    String name = "SIP";

    BaseApiService       mApiService = UtilsApi.getAPIService();



    public void UF_ROCKET_LOGIN_BLOC(String UF_ROCKET_LOGIN_BLOC){
    // SharedPreferences SipData = getApplicationContext().getSharedPreferences("SIP", MODE_PRIVATE);
        Map<String, Object> jsonParams = new ArrayMap<>();
        jsonParams.put("UF_ROCKET_LOGIN_BLOC", UF_ROCKET_LOGIN_BLOC);
       mApiService.getBlacklistDell("KEY:"+TOKENWE,jsonParams)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject > response) {
                        try{
                            Log.e("POSTTEST", ""+response.body().toString());
                            if (response.body().getAsJsonObject("result").get("SUCCESS").equals("false")){

                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        Log.e("debug", "onFailure: ERROR > " + t.toString());
                    }
                });
    }

    public void getBlacklist(){
        mApiService.getBlacklist("KEY:"+TOKENWE)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()){
                            try {

                                JSONObject json = new JSONObject(response.body().string());
                                JSONArray values = json.getJSONObject("result").getJSONArray("USERS");
                                mnameBlack = new String[values .length()];
                                for (int i = 0; i < values .length(); i++) {
                                    JSONObject jsonobject = values .getJSONObject(i);
                                    mnameBlack[i] = jsonobject.getString("UF_ROCKET_LOGIN_BLOC");

                                }

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
                        //   Log.e("debug", "onFailure: ERROR > " + t.toString());

                    }
                });
    }
}
