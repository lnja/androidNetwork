package len.android.network;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;
import com.google.gson.JsonObject;
import len.tools.android.JsonUtils;
import len.tools.android.Log;
import len.tools.android.extend.ListRspInterface;
import len.tools.android.extend.ListViewUiHandler;
import len.tools.android.extend.RequestUiHandler;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.lang.reflect.*;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 接口请求发起类
 */

public abstract class HttpRequest<T extends BaseRsp> {

    private static final Map<String, List<HttpRequest>> ACTIVITY_REQUEST_SET =
            new HashMap<>();
    private Context mContext;
    private RequestEntity mRequestEntity;
    private RequestUiHandler mRequestUiHandler;
    private boolean isSuccess = false;
    private Call<JsonObject> call;

    public HttpRequest(Context context, @NonNull RequestEntity requestEntity, RequestUiHandler requestUiHandler) {
        this.mContext = context;
        this.mRequestEntity = requestEntity;
        this.mRequestUiHandler = requestUiHandler;
    }

    public HttpRequest(Context context, @NonNull RequestEntity requestEntity) {
        this(context, requestEntity, null);

    }

    public static void clearRequest(Activity activity) {
        List<HttpRequest> requestList = ACTIVITY_REQUEST_SET.get(activity.toString());
        if (requestList != null) {
            for (int i = 0; i < requestList.size(); i++) {
                HttpRequest httpRequest = requestList.get(i);
                if (httpRequest != null) {
                    httpRequest.cancel();
                }
            }
            requestList.clear();
            ACTIVITY_REQUEST_SET.remove(requestList);
        }
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

    private void addRequest() {
        List<HttpRequest> requestList = ACTIVITY_REQUEST_SET.get(mContext.toString());
        if (requestList == null) {
            requestList = new ArrayList<>();
            ACTIVITY_REQUEST_SET.put(mContext.toString(), requestList);
        }
        requestList.add(this);
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    private void removeFinishedRequest() {
        List<HttpRequest> requestList = ACTIVITY_REQUEST_SET.get(mContext.toString());
        if (requestList != null) {
            requestList.remove(this);
        }
    }

    public void get() {
        if (mRequestEntity == null) {
            Log.e("RequestEntity cannot be null");
            return;
        }
        if (mRequestUiHandler != null) {
            mRequestUiHandler.onStart(mRequestEntity.getHintMsg().getMsg());
        }
        if (mRequestEntity.getParams() == null) {
            call = RetrofitWrapper.getInstance().getService().commonGet(mRequestEntity.getmApiPath(), null);
        } else {
            call = RetrofitWrapper.getInstance().getService().commonGet(mRequestEntity.getmApiPath(), JsonUtils.toJson(mRequestEntity.getParams()));
        }
        if (!mRequestEntity.isPersistent()) {
            addRequest();
        }
        if (mRequestEntity.isShowCacheFirst()) {
//            HttpCacheUtil.instance().get(call.request().url().toString() + mRequestEntity.getExtraCacheKey(), this);

        }
        execute(call);
    }

    public void post() {
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
            addRequest();
        }
        if (mRequestEntity.isShowCacheFirst()) {
//            HttpCacheUtil.instance().get(call.request().url().toString() + mRequestEntity.getExtraCacheKey(), this);

        }
        execute(call);
    }

    private void execute(Call call) {
        RetrofitWrapper.getInstance().execute(call, new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (call.isCanceled()) {
                    return;
                }
                if (response.isSuccessful()) {
                    Log.e(JsonUtils.gsonToJson(response.body()));
                    T objectT = JsonUtils.gsonToEntity(response.body(), getClassOfT());
                    if (objectT.isSuccess()) {
                        onSuccess(objectT);
                        if (mRequestEntity.isShouldCache()) {
//                            HttpCacheUtil.instance().put(call.request().url().toString() + mRequestEntity.getExtraCacheKey(), response.body());
                        }
                    } else {
                        /*if (!TextUtils.isEmpty(response.body().getErrMsg())) {
                            mContext.getToastDialog().showToJast(response.body().getErrMsg());
                        }*/
                        onFail(objectT);
                    }
                } else if (response.code() == 401 || response.code() == 403) { //授权异常
                    BaseRsp result = new BaseRsp();
                    result.setCode(ErrorCode.ERR_CODE_AUTH_FAILURE);
                    onFail(result);
                } else {
                    BaseRsp result = new BaseRsp();
                    result.setCode(ErrorCode.ERR_CODE_SERVER_ANOMALY);
                    result.setMsg(mContext.getString(R.string.server_anomaly_try_later));
                    onFail(result);
                }
                removeFinishedRequest();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                handRetrofitError(call, t);
            }
        });
    }

    private void handRetrofitError(Call call, Throwable t) {
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

    public void get(Call<T> requestCall) {
//        call = requestCall;
        if (mRequestEntity == null) {
            Log.e("RequestEntity cannot be null");
            return;
        }
        if (mRequestUiHandler != null) {
            mRequestUiHandler.onStart(mRequestEntity.getHintMsg().getMsg());
        }
        if (!mRequestEntity.isPersistent()) {
            addRequest();
        }
        if (mRequestEntity.isShowCacheFirst()) {
//            HttpCacheUtil.instance().get(call.request().url().toString() + mRequestEntity.getExtraCacheKey(), this);
        }
        RetrofitWrapper.getInstance().execute(requestCall, new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                Log.d(response.body().toString());
                if (call.isCanceled()) {
                    return;
                }
                onSuccess(response.body());
                removeFinishedRequest();
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                handRetrofitError(call, t);
            }
        });

    }

    protected void performRequestError(BaseRsp result) {
        if (mContext instanceof RequestUiHandler) {
            ((RequestUiHandler) mContext).onError(result.getCode(), result.getMsg());
        } else if (mContext instanceof Activity) {
            Toast.makeText(mContext, result.getMsg(), Toast.LENGTH_LONG).show();
        } else {
            Log.w(result.getMsg());
        }
    }

    protected void onSuccess(T result) {
        isSuccess = true;
        if (mRequestUiHandler != null) {
            mRequestUiHandler.onSuccess();
            if (mRequestUiHandler instanceof ListViewUiHandler && result instanceof ListRspInterface) {
                ((ListViewUiHandler) mRequestUiHandler).onListRspSuccess((ListRspInterface<?>) result, mRequestEntity.getPageNum(), mRequestEntity.getPageSize());
            }
        }

    }

    protected void onCanceled() {
        Log.d(mRequestEntity.getmApiPath() + "接口请求取消");
    }

    private Class<T> getClassOfT() {
        Type superClass = getClass().getGenericSuperclass();
        Type type = null;
        if (superClass instanceof ParameterizedType) {
            type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
        } else {
            throw new RuntimeException("should be initialize the child of HttpRequest class");
        }
        Class<T> classOfT = (Class<T>) getRawType(type);
        return classOfT;
    }

    protected void onFail(BaseRsp result) {
        if (mRequestUiHandler != null && result != null) {
            mRequestUiHandler.onError(result.getCode(), result.getMsg());
        } else {
            performRequestError(result);
        }
        if (call != null) {
//                HttpCacheUtil.instance().get(call.request().url().toString() + mRequestEntity.getExtraCacheKey(), this);
        }
    }

    protected void onRestore(String result) {

    }

    public void cancel() {
        if (call != null && !call.isCanceled()) {
            call.cancel();
            onCanceled();
            Log.d("call is be canceled");
        }

        Log.d("HttpRequest is be canceled");
    }


}
