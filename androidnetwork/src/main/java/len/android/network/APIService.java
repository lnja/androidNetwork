package len.android.network;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * 接口请求的方法类，所有的接口请求方法都写在这里
 */

public interface APIService {
    @POST("{apiURL}")
    Call<JsonObject> commonPost(@Path(value = "apiURL", encoded = true) String apiURL, @Body Object dataBody);

    @POST("{apiURL}")
    Call<JsonObject> commonPost(@Path(value = "apiURL", encoded = true) String apiURL);

    @GET("{apiURL}")
    Call<JsonObject> commonGet(@Path(value = "apiURL", encoded = true) String apiURL, @Query("data") String data);
}
