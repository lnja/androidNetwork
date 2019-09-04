package len.android.network;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * 接口请求的方法类，所有的接口请求方法都写在这里
 */

public interface APIService {

    @POST("{apiURL}")
    Call<String> commonPost(@Path(value = "apiURL", encoded = true) String apiURL);

    @POST("{apiURL}")
    Call<String> commonPost(@Path(value = "apiURL", encoded = true) String apiURL, @Body Object dataBody);

    @GET("{apiURL}")
    Call<String> commonGet(@Path(value = "apiURL", encoded = true) String apiURL);

    @GET("{apiURL}")
    Call<String> commonGet(@Path(value = "apiURL", encoded = true) String apiURL, @Query("data") String data);

    @GET("{apiURL}")
    Call<String> commonGet(@Path(value = "apiURL", encoded = true) String apiURL, @QueryMap Map<String, Object> params);
}
