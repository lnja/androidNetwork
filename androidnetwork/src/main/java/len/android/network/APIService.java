package len.android.network;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

/**
 * 接口请求的方法类，所有的接口请求方法都写在这里
 */

public interface APIService {

    @POST("{apiURL}")
    Call<JsonObject> commonPost(@Path(value = "apiURL", encoded = true) String apiURL);

    @POST("{apiURL}")
    Call<JsonObject> commonPost(@Path(value = "apiURL", encoded = true) String apiURL, @Body Object dataBody);

    @GET("{apiURL}")
    Call<JsonObject> commonGet(@Path(value = "apiURL", encoded = true) String apiURL);

    @GET("{apiURL}")
    Call<JsonObject> commonGet(@Path(value = "apiURL", encoded = true) String apiURL, @Query("data") String data);

    @GET("{apiURL}")
    Call<JsonObject> commonGet(@Path(value = "apiURL", encoded = true) String apiURL, @QueryMap Map<String, Object> params);
}
