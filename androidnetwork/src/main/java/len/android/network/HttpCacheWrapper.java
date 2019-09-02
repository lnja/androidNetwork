package len.android.network;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import com.bumptech.glide.disklrucache.DiskLruCache;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import len.tools.android.AndroidUtils;
import len.tools.android.JsonUtils;
import len.tools.android.Log;
import len.tools.android.Md5Encrypt;
import len.tools.android.StorageUtils;

/**
 * 接口缓存工具类
 */

public class HttpCacheWrapper {

    public static final int DEFAULT_VERSION_CODE = 1;
    /**
     * 默认接口缓存大小
     */
    public static final long DEFAULT_CACHE_SIZE = 50 * 1024 * 1024;
    private static final Object SYNC_OBJECT = new Object();
    private static final int DEFAULT_VALUE_COUNT = 1;
    public static DiskLruCache mHttpLruCache;
    private static volatile HttpCacheWrapper INSTANCE;

    public static HttpCacheWrapper instance() {
        if (INSTANCE == null) {
            synchronized (SYNC_OBJECT) {
                if (INSTANCE == null) {
                    INSTANCE = new HttpCacheWrapper();
                }
            }
        }
        return INSTANCE;
    }

    public static void clear() {
        try {
            if (mHttpLruCache == null) {
                Log.e("Http DiskLruCache 未初始化");
                return;
            }
            mHttpLruCache.delete();
            mHttpLruCache = null;
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("接口缓存清除失败");
        }
    }

    public void initDiskCache(Context context) {
        try {
            mHttpLruCache = DiskLruCache.open(StorageUtils.getCacheCustomDir(context, "httpCache"), AndroidUtils.getVersionCode(context, 0), DEFAULT_VALUE_COUNT, DEFAULT_CACHE_SIZE);
        } catch (IOException e) {
            mHttpLruCache = null;
            e.printStackTrace();
            Log.e("DiskLruCache接口缓存初始化失败");
        }
    }

    @SuppressLint("StaticFieldLeak")
    public <T extends BaseRsp> void put(String key, final T result) {
        if (mHttpLruCache == null) {
            Log.e("Http DiskLruCache 未初始化");
            return;
        }
        String cacheKey = Md5Encrypt.md5(key);
        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                String cacheKeyInternal = params[0];
                DiskLruCache.Editor editor = null;
                try {
                    editor = mHttpLruCache.edit(cacheKeyInternal);
                    if (editor == null) {
                        Log.d("不能同时操作一个缓存editor");
                        return null;
                    }
                    editor.set(0, JsonUtils.toJson(result));
                    editor.commit();
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        editor.abort();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    Log.e("接口缓存添加失败");
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cacheKey);
    }

    @SuppressLint("StaticFieldLeak")
    public <T extends BaseRsp> void get(String key, final HttpCacheListener<T> httpCacheListener) {
        if (mHttpLruCache == null) {
            Log.e("Http DiskLruCache 未初始化");
            return;
        }
        final String cacheKey = Md5Encrypt.md5(key);
            new AsyncTask<String, Void, T>() {
                @Override
                protected T doInBackground(String... params) {
                    String cacheKeyInternal = params[0];
                    String resultStr = null;
                    try {
                        DiskLruCache.Value value = mHttpLruCache.get(cacheKeyInternal);
                        if (value != null) {
                            resultStr = value.getString(0);
                            T httpResult = JsonUtils.toEntity(resultStr, getClassOfTFromInterface(httpCacheListener));
                            return httpResult;
                        } else {
                            return null;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(T httpResult) {
                    if (httpResult != null && !httpResult.isSuccess()) {
                        Log.d("命中缓存， md5前cacheKey: " + cacheKey);
                        httpCacheListener.onRestore(httpResult);
                    }
                    super.onPostExecute(httpResult);
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cacheKey);

    }

    private <T extends BaseRsp> Class<T> getClassOfTFromInterface(HttpCacheListener<T> httpCacheListener) {
        Type[] interfaces = httpCacheListener.getClass().getGenericInterfaces();
        Type type = null;
        if (interfaces[0] instanceof ParameterizedType) {
            type = ((ParameterizedType) interfaces[0]).getActualTypeArguments()[0];
        } else {
            throw new RuntimeException("the original should be initialize the child of some class which has ParameterizedType");
        }
        Class<T> classOfT = (Class<T>) HttpRequest.getRawType(type);
        return classOfT;
    }

    public interface HttpCacheListener<T extends BaseRsp>{
        void onRestore(T result);
    }

}
