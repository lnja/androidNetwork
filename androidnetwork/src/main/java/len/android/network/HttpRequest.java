package len.android.network;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import len.android.network.HttpCacheWrapper.HttpCacheListener;
import len.tools.android.JsonUtils;
import len.tools.android.Log;
import len.tools.android.extend.ListRspInterface;
import len.tools.android.extend.ListViewUiHandler;
import len.tools.android.extend.RequestUiHandler;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 接口请求发起类
 */

public abstract class HttpRequest<T extends BaseRsp> {

    private static final Map<String, List<HttpRequest>> ACTIVITY_REQUEST_MAP =
            new ConcurrentHashMap<>();
    private Context mContext;
    private RequestEntity mRequestEntity;
    private RequestUiHandler mRequestUiHandler;
    private boolean isSuccess = false;
    private Call<String> call;
    private HttpCacheListener httpCacheListener = null;

    public HttpRequest(Context context, @NonNull RequestEntity requestEntity, RequestUiHandler requestUiHandler) {
        this.mContext = context;
        this.mRequestEntity = requestEntity;
        this.mRequestUiHandler = requestUiHandler;
    }

    public HttpRequest(Context context, @NonNull RequestEntity requestEntity) {
        this(context, requestEntity, null);
    }

    /**
     * common GET method request
     */
    public void get() {
        if(!beforeDefineCall()){
            return;
        }
        if (mRequestEntity.getParams() == null) {
            call = RetrofitWrapper.getInstance().getService().commonGet(mRequestEntity.getApiPath());
        } else if (mRequestEntity.getParams() instanceof Map) {
            call = RetrofitWrapper.getInstance().getService().commonGet(mRequestEntity.getApiPath(), (Map<String, Object>) mRequestEntity.getParams());
        } else {
            call = RetrofitWrapper.getInstance().getService().commonGet(mRequestEntity.getApiPath(), JsonUtils.gsonToJson(mRequestEntity.getParams()));
        }
        if(!afterDefineCall()){
            return;
        }
        executeInternal(call);
    }

    /**
     * common POST method request
     */
    public void post() {
        if(!beforeDefineCall()){
            return;
        }
        Log.e(JsonUtils.gsonToJson(mRequestEntity.getParams()));
        if (mRequestEntity.getParams() == null) {
            call = RetrofitWrapper.getInstance().getService().commonPost(mRequestEntity.getApiPath());
        } else {
            call = RetrofitWrapper.getInstance().getService().commonPost(mRequestEntity.getApiPath(), mRequestEntity.getParams());
        }
        if(!afterDefineCall()){
            return;
        }
        executeInternal(call);
    }

    private boolean beforeDefineCall(){
        isSuccess = false;
        if (mRequestEntity == null) {
            Log.e("RequestEntity cannot be null");
            return false;
        }
        if (mRequestUiHandler != null) {
            mRequestUiHandler.onStart(mRequestEntity.getHintMsg().getMsg());
        }
        return true;
    }

    private boolean afterDefineCall(){
        if (!mRequestEntity.isPersistent()) {
            addRequest(mContext,this);
        }
        if (mRequestEntity.isShowCacheFirst() || mRequestEntity.isShowCacheOnFail()) {
            httpCacheListener = new HttpCacheListener<T>() {

                @Override
                public void onRestore(T result) {
                    HttpRequest.this.onRestore(result);
                }
            };
            if (mRequestEntity.isShowCacheFirst()) {
                fetchResultFromCache(call.request().url().toString() + mRequestEntity.getExtraCacheKey(), httpCacheListener,getClazzOfT(this));
            }
        }
        return true;
    }

    private void executeInternal(Call<String> call) {
        RetrofitWrapper.getInstance().execute(call, new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                handRetrofitOnResponse(call,response);
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                handRetrofitOnFailure(call, t);
            }
        });
    }

    /**
     * retrofit execute
     * @param requestCall
     * @param requestEntity
     * @param requestUiHandler
     */
    public void execute(Call<T> requestCall,RequestEntity requestEntity,RequestUiHandler requestUiHandler) {
        isSuccess = false;
        mRequestEntity = requestEntity;
        mRequestUiHandler = requestUiHandler;
        if(!beforeDefineCall()){
            return;
        }
        if(!afterDefineCall()){
            return;
        }
        RetrofitWrapper.getInstance().execute(requestCall, new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (call.isCanceled()) {
                    return;
                }
                if(response.isSuccessful()){
                    T result = response.body();
                    onSuccess(result);
                    if (mRequestEntity.isShouldCache()) {
                        putResultToCache(call.request().url().toString() + mRequestEntity.getExtraCacheKey(), result);
                    }
                }else {
                    handRetrofitOnResponseIsNotSuccessful(response);
                }
                removeFinishedRequest();
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                if (call.isCanceled()) {
                    return;
                }
                handRetrofitOnFailure(call, t);
            }
        });
    }

    /**
     * handle response when retrofit onResponse() called
     * @param call
     * @param response
     */
    private void handRetrofitOnResponse(Call<String> call, Response<String> response){
        if (call.isCanceled()) {
            return;
        }
        if (response.isSuccessful()) {
            Log.d(response.body());
            T objectTresult = JsonUtils.gsonToEntity(response.body(), getTypeOfTfromSupperclass(this));
            if (objectTresult != null && objectTresult.isSuccess()) {
                onSuccess(objectTresult);
                if (mRequestEntity.isShouldCache()) {
                    putResultToCache(call.request().url().toString() + mRequestEntity.getExtraCacheKey(), objectTresult);
                }
            } else {
                if (objectTresult == null) {
                    BaseRsp result = new BaseRsp();
                    result.setCode(ErrorCode.ERR_CODE_PARSE_FAILURE);
                    result.setMsg(mContext.getString(R.string.parse_failure_try_later));
                    onFail(result);
                } else {
                    onFail(objectTresult);
                }
            }
        }else{
            handRetrofitOnResponseIsNotSuccessful(response);
        }
        removeFinishedRequest();
    }

    /**
     * handle the not successful case when retrofit onResponse() called
     * @param response
     */
    private void handRetrofitOnResponseIsNotSuccessful(Response response){
        if (response.code() == 401 || response.code() == 403) { //授权异常
            BaseRsp result = new BaseRsp();
            result.setCode(ErrorCode.ERR_CODE_AUTH_FAILURE);
            result.setMsg(mContext.getString(R.string.auth_failure));
            onFail(result);
        } else {
            BaseRsp result = new BaseRsp();
            result.setCode(ErrorCode.ERR_CODE_SERVER_ANOMALY);
            result.setMsg(mContext.getString(R.string.server_anomaly_try_later));
            onFail(result);
        }
    }


    /**
     * handle the failure when retrofit onFailure() called
     * @param call
     * @param t
     */
    private void handRetrofitOnFailure(Call call, Throwable t) {
        if (call.isCanceled()) {
            return;
        }
        String errMsg = null;
        int errCode = 0;
        BaseRsp result = new BaseRsp();
        if (t instanceof SocketTimeoutException) {
            errMsg = mContext.getString(R.string.network_unstable_pls_check);
            errCode = ErrorCode.ERR_CODE_NETWORK_UNSTABLE;
        } else if (t instanceof ConnectException) {
            errMsg = mContext.getString(R.string.network_unavailable_pls_check);
            errCode = ErrorCode.ERR_CODE_NETWORK_UNAVAILABLE;
        } else {
            errMsg = mContext.getString(R.string.request_anomaly_try_later);
            errCode = ErrorCode.ERR_CODE_REQUEST_ANOMALY;
        }
        result.setMsg(errMsg);
        result.setCode(errCode);
        onFail(result);
        removeFinishedRequest();
        if (BuildConfig.DEBUG) {
            Log.e(t.getMessage());
            t.printStackTrace();
        }
    }

    /**
     * the default request error process logic
     * @param result
     */
    protected void performRequestErrorByDefault(BaseRsp result) {
        if (mContext instanceof RequestUiHandler) {
            ((RequestUiHandler) mContext).onError(result.getCode(), result.getMsg());
        } else if (mContext instanceof Activity) {
            Toast.makeText(mContext, result.getMsg(), Toast.LENGTH_LONG).show();
        } else {
            Log.w(result.getMsg());
        }
    }

    /**
     * 请求成功回调，与后台API确定errorCode为成功值时回调
     * @param result
     */
    protected void onSuccess(T result) {
        isSuccess = true;
        fillResultToUiHandler(result);
    }

    /**
     * 请求失败回调，除{@link #onSuccess(BaseRsp)}情况外都回调
     * @param result
     */
    protected void onFail(BaseRsp result) {
        if (mRequestUiHandler != null && result != null) {
            mRequestUiHandler.onError(result.getCode(), result.getMsg());
        } else {
            performRequestErrorByDefault(result);
        }
        //如果已经优先显示了缓存数据，则不在重新调用缓存
        if (call != null&& !mRequestEntity.isShowCacheFirst() && mRequestEntity.isShowCacheOnFail()) {
               fetchResultFromCache(call.request().url().toString() + mRequestEntity.getExtraCacheKey(), httpCacheListener,getClazzOfT(this));
        }
    }

    private void fillResultToUiHandler(T result){
        if (mRequestUiHandler != null) {
            mRequestUiHandler.onSuccess();
            if (mRequestUiHandler instanceof ListViewUiHandler && result instanceof ListRspInterface) {
                ((ListViewUiHandler) mRequestUiHandler).onListRspSuccess((ListRspInterface<?>) result, mRequestEntity.getPageNum(), mRequestEntity.getPageSize());
            }
        }
    }

    /**
     * called when result is restored from the cache data of last request
     * @param result
     */
    protected void onRestore(T result) {
    }

    /**
     * 将请求返回结果保存到本地缓存中
     * @param key
     * @param result
     */
    private void putResultToCache(String key,T result){
        HttpCacheWrapper.instance().put(key, result);
    }

    /**
     * 获取请求的保存在本地缓存的数据
     * @param key
     * @param listener
     */
    private void fetchResultFromCache(String key, HttpCacheListener<T> listener,Class<T> clazz){
        HttpCacheWrapper.instance().get(key, listener,clazz);
    }

    /**
     * 取消该请求，并从请求维护表中删除
     */
    public void cancel() {
        if (call != null && !call.isCanceled()) {
            call.cancel();
            removeRequest(mContext,this);
            onCanceled();
        }
    }

    /**
     * 取消该请求，未从请求维护表中删除，供内部调用
     */
    private void cancelInternal() {
        if (call != null && !call.isCanceled()) {
            call.cancel();
            onCanceled();
        }
    }

    /**
     * 请求被取消时回调
     */
    protected void onCanceled() {
        Log.d("接口请求已取消(并非绝对) - " + mRequestEntity.getApiPath() + mRequestEntity.getExtraCacheKey());
    }

    /**
     * 查询请求是否成功，当该方法返回true后，该方法只有再次调用{@link #get()} / {@link #post()} / {@link #execute(Call, RequestEntity, RequestUiHandler)}时才会被返回false
     * @return
     */
    public boolean isSuccess() {
        return isSuccess;
    }

    public RequestEntity getRequestEntity() {
        return mRequestEntity;
    }

    /**
     * 请求完成后一定要回调，无论成功或者失败返回，处理完毕即时回调
     */
    private void removeFinishedRequest() {
        removeRequest(mContext,this);
    }

    private Type getTypeOfTfromSupperclass(HttpRequest<T> original) {
        Type superClazz = original.getClass().getGenericSuperclass();
        Type type = null;
        if (superClazz instanceof ParameterizedType) {
            type = ((ParameterizedType) superClazz).getActualTypeArguments()[0];
        } else {
            throw new RuntimeException("the original should be a child class of some class which has parameterized type");
        }
        return type;
    }

    private Class<T> getClazzOfT(HttpRequest<T> original){
        return (Class<T>)JsonUtils.getRawType(getTypeOfTfromSupperclass(original));
    }

    /**
     * 取消所有关联该Context的请求，一般在该Context销毁时调用该方法
     * @param context
     */
    public static void cancelRequests(Context context) {
        List<HttpRequest> requestList = ACTIVITY_REQUEST_MAP.get(context.toString());
        if (requestList != null) {
            for (int i = 0; i < requestList.size(); i++) {
                HttpRequest httpRequest = requestList.get(i);
                if (httpRequest != null) {
                    httpRequest.cancelInternal();
                }
            }
            requestList.clear();
        }
    }

    /**
     * 从请求维护表中，删除该Context关联的该HttpRequest请求
     * @param context
     * @param httpRequest
     */
    private static void removeRequest(Context context,HttpRequest httpRequest) {
        List<HttpRequest> requestList = ACTIVITY_REQUEST_MAP.get(context.toString());
        if (requestList != null) {
            requestList.remove(httpRequest);
        }
    }

    /**
     * 从请求维护表中，删除所有关联该Context的请求
     */
    private static void removeRequests(Context context) {
        ACTIVITY_REQUEST_MAP.remove(context.toString());
    }

    /**
     * 添加一个非持续类型的HttpRequest对象到请求维护表中，当该请求处理完毕或者{@link #cancel()} / {@link #cancelInternal()}被调用时，务必从请求维护表中清除
     */
    private static void addRequest(Context context,HttpRequest httpRequest) {
        List<HttpRequest> requestList = ACTIVITY_REQUEST_MAP.get(context.toString());
        if (requestList == null) {
            requestList = new CopyOnWriteArrayList<>();
            ACTIVITY_REQUEST_MAP.put(context.toString(), requestList);
        }
        requestList.add(httpRequest);
    }
}
