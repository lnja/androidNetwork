package len.android.network;

import android.content.Context;
import com.bumptech.glide.disklrucache.DiskLruCache;
import len.tools.android.AndroidUtils;
import len.tools.android.Log;
import len.tools.android.Md5Encrypt;
import len.tools.android.StorageUtils;

import java.io.IOException;

/**
 * 接口缓存工具类
 */

public class HttpCacheWrapper {

    public static final int DEFAULT_VERSION_CODE = 1;
    /**
     * 默认接口缓存大小
     */
    public static final long DEFAULT_CACHE_SIZE = 50 * 1024 * 1024;
    private static final Object SYNCOBJECT = new Object();
    private static final int DEFAULT_VALUE_COUNT = 1;
    public static DiskLruCache mHttpLruCache;
    private static volatile HttpCacheWrapper INSTANCE;

    public static HttpCacheWrapper instance() {
        if (INSTANCE == null) {
            synchronized (SYNCOBJECT) {
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
                Log.e("Http DiskLruCache 未成功初始化");
                return;
            }
            mHttpLruCache.delete();
            mHttpLruCache = null;
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("接口清除失败");
        }
    }

    public void initDiskCache(Context context) {
        try {
            mHttpLruCache = DiskLruCache.open(StorageUtils.getCacheCustomDir(context, "httpCache"), AndroidUtils.getVersionCode(context, 0), DEFAULT_VALUE_COUNT, DEFAULT_CACHE_SIZE);
        } catch (IOException e) {
            mHttpLruCache = null;
            e.printStackTrace();
            Log.e("DiskLruCache缓存初始化失败");
        }
    }

    public <T> void put(final String key, final BaseRsp result) {
        if (mHttpLruCache == null) {
            Log.e("Http DiskLruCache 未成功初始化");
            return;
        }
        final String cacheKey = Md5Encrypt.md5(key);
        /*AsyncTaskCompat.executeParallel(new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                DiskLruCache.Editor editor = null;
                try {
                    editor = mHttpLruCache.edit(cacheKey);
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
        }, key);*/
    }

    public void get(final String key, final HttpRequest httpRequest) {
        if (mHttpLruCache == null) {
            Log.e("Http DiskLruCache 未成功初始化");
            return;
        }
        final String cacheKey = Md5Encrypt.md5(key);
            /*AsyncTaskCompat.executeParallel(new AsyncTask<String, Void, HttpResult>() {
                @Override
                protected HttpResult doInBackground(String... params) {
                    String resultStr = null;
                    try {
                        DiskLruCache.Value value = mHttpLruCache.get(cacheKey);
                        if (value != null) {
                            resultStr = value.getString(0);
                            HttpResult httpResult = JsonUtils.fromJson(resultStr, HttpResult.class);
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
                protected void onPostExecute(HttpResult httpResult) {
                    if (httpRequest != null && !httpRequest.isSuccess() && httpResult != null) {
                        Log.d("命中缓存， md5前cacheKey : " + key);
                        httpRequest.onRestore(httpRequest.getResultData(httpResult));
                    }
                    super.onPostExecute(httpResult);
                }
            }, cacheKey);*/

    }

}
