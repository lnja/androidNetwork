package len.android.network;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.gson.JsonObject;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
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
    private Call<JsonObject> call;
    private HttpCacheListener httpCacheListener = new HttpCacheListener<T>() {
        @Override
        public void onRestore(T result) {
            onRestore(result);
        }
    };

    public HttpRequest(Context context, @NonNull RequestEntity requestEntity, RequestUiHandler requestUiHandler) {
        this.mContext = context;
        this.mRequestEntity = requestEntity;
        this.mRequestUiHandler = requestUiHandler;
    }

    public HttpRequest(Context context, @NonNull RequestEntity requestEntity) {
        this(context, requestEntity, null);
    }

    public void get() {
        isSuccess = false;
        if (mRequestEntity == null) {
            Log.e("RequestEntity cannot be null");
            return;
        }
        if (mRequestUiHandler != null) {
            mRequestUiHandler.onStart(mRequestEntity.getHintMsg().getMsg());
        }
        if (mRequestEntity.getParams() == null) {
            call = RetrofitWrapper.getInstance().getService().commonGet(mRequestEntity.getmApiPath());
        } else if (mRequestEntity.getParams() instanceof Map) {
            call = RetrofitWrapper.getInstance().getService().commonGet(mRequestEntity.getmApiPath(), (Map<String, Object>) mRequestEntity.getParams());
        } else {
            call = RetrofitWrapper.getInstance().getService().commonGet(mRequestEntity.getmApiPath(), JsonUtils.toJson(mRequestEntity.getParams()));
        }
        if (!mRequestEntity.isPersistent()) {
            addRequest(mContext,this);
        }
        if (mRequestEntity.isShowCacheFirst()) {
            fetchResultFromCache(call.request().url().toString() + mRequestEntity.getExtraCacheKey(), httpCacheListener);

        }
        executeInternal(call);
    }

    public void post() {
        isSuccess = false;
        if (mRequestEntity == null) {
            Log.e("RequestEntity cannot be null");
            return;
        }
        if (mRequestUiHandler != null) {
            mRequestUiHandler.onStart(mRequestEntity.getHintMsg().getMsg());
        }
        if (mRequestEntity.getParams() == null) {
            call = RetrofitWrapper.getInstance().getService().commonPost(mRequestEntity.getmApiPath());
        } else {
            call = RetrofitWrapper.getInstance().getService().commonPost(mRequestEntity.getmApiPath(), mRequestEntity.getParams());
        }
        if (!mRequestEntity.isPersistent()) {
            addRequest(mContext,this);
        }
        if (mRequestEntity.isShowCacheFirst()) {
            fetchResultFromCache(call.request().url().toString() + mRequestEntity.getExtraCacheKey(), httpCacheListener);

        }
        executeInternal(call);
    }

    private void executeInternal(Call<JsonObject> call) {
        RetrofitWrapper.getInstance().execute(call, new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                handRetrofitOnResponse(call,response);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                handRetrofitOnFailure(call, t);
            }
        });
    }

    public void execute(Call<T> requestCall,RequestEntity requestEntity,RequestUiHandler requestUiHandler) {
        isSuccess = false;
        mRequestEntity = requestEntity;
        mRequestUiHandler = requestUiHandler;
        if (mRequestEntity == null) {
            Log.e("RequestEntity cannot be null");
            return;
        }
        if (mRequestUiHandler != null) {
            mRequestUiHandler.onStart(mRequestEntity.getHintMsg().getMsg());
        }
        if (!mRequestEntity.isPersistent()) {
            addRequest(mContext,this);
        }
        if (mRequestEntity.isShowCacheFirst()) {
            fetchResultFromCache(requestCall.request().url().toString() + mRequestEntity.getExtraCacheKey(), httpCacheListener);
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

    private void handRetrofitOnResponse(Call<JsonObject> call, Response<JsonObject> response){
        if (call.isCanceled()) {
            return;
        }
        if (response.isSuccessful()) {
            Log.d(JsonUtils.gsonToJson(response.body()));
            T objectTresult = JsonUtils.gsonToEntity(response.body(), getClassOfTFromSupperclass(this));
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
               fetchResultFromCache(call.request().url().toString() + mRequestEntity.getExtraCacheKey(), httpCacheListener);
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
    private void fetchResultFromCache(String key, HttpCacheListener<T> listener){
        HttpCacheWrapper.instance().get(key, listener);
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
        Log.d(mRequestEntity.getmApiPath() + mRequestEntity.getExtraCacheKey() + "接口请求已取消（并非绝对）");
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

    private Class<T> getClassOfTFromSupperclass(HttpRequest<T> original) {
        Type superClass = original.getClass().getGenericSuperclass();
        Type type = null;
        if (superClass instanceof ParameterizedType) {
            type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
        } else {
            throw new RuntimeException("the original should be a child class of some class which has parameterized type");
        }
        Class<T> classOfT = (Class<T>) getRawType(type);
        return classOfT;
    }

    public static Class<?> getRawType(Type type) {
        if (type instanceof Class<?>) {
            // type is a normal class.
            return (Class<?>) type;

        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            // I'm not exactly sure why getRawType() returns Type instead of Class.
            // Neal isn't either but suspects some pathological case related
            // to nested classes exists.
            Type rawType = parameterizedType.getRawType();
            if (!(rawType instanceof Class)) {
                throw new IllegalArgumentException();
            }
            return (Class<?>) rawType;

        } else if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            return Array.newInstance(getRawType(componentType), 0).getClass();

        } else if (type instanceof TypeVariable) {
            // we could use the variable's bounds, but that won't work if there are multiple.
            // having a raw type that's more general than necessary is okay
            return Object.class;

        } else if (type instanceof WildcardType) {
            return getRawType(((WildcardType) type).getUpperBounds()[0]);

        } else {
            String className = type == null ? "null" : type.getClass().getName();
            throw new IllegalArgumentException("Expected a Class, ParameterizedType, or "
                    + "GenericArrayType, but <" + type + "> is of type " + className);
        }
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
