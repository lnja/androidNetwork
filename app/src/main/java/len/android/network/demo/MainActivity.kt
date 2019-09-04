package len.android.network.demo

import android.os.Bundle
import len.android.basic.activity.BaseActivity
import len.android.network.BaseRsp
import len.android.network.HttpRequest
import len.android.network.RequestEntity
import len.tools.android.JsonUtils
import len.tools.android.Log

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        httpPostDemo360()
//        httpPostDemoTaobao()
        httpGetDemo360()
//        httpGetDemoTaobao()
    }

    private fun httpPostDemo360(){
        App.getInstance().initRetrofit360()
        var requestEntity:RequestEntity = RequestEntity.Builder("IPQuery/ipquery").addParams("ip","115.159.152.210").showCacheFirst(false).showCacheOnFail(true).extraCacheKey("?ip=115.159.152.210").build()

//        var requestEntity:RequestEntity = RequestEntity.Builder("IPQuery/ipquery").addParams("ip","115.159.152.210").showCacheFirst(false).showCacheOnFail(false).build()
        object:HttpRequest<Ip360Rsp>(this,requestEntity){

            override fun onRestore(result: Ip360Rsp?) {
                super.onRestore(result)
                Log.e("缓存: " + JsonUtils.gsonToJson(result))
                showToast("缓存: " + result!!.data)
            }

            override fun onSuccess(result: Ip360Rsp?) {
                super.onSuccess(result)
                Log.e("success: " + JsonUtils.gsonToJson(result))
                showToast(result!!.data)
            }

            override fun onFail(result: BaseRsp?) {
                super.onFail(result)
                Log.e("fail: " + JsonUtils.gsonToJson(result))
                showToast(result!!.msg)
            }

        }.post()
    }


    private fun httpPostDemoTaobao(){
        App.getInstance().initRetrofitTaobao()
        var requestEntity:RequestEntity = RequestEntity.Builder("service/getIpInfo.php").addParams("ip","115.159.152.210").build()
        object:HttpRequest<IpTaobaoRsp>(this,requestEntity){

            override fun onSuccess(result: IpTaobaoRsp?) {
                super.onSuccess(result)
                Log.d(JsonUtils.gsonToJson(result))
                showToast(result!!.data!!.city)
            }

            override fun onFail(result: BaseRsp?) {
                super.onFail(result)
                Log.d(JsonUtils.gsonToJson(result))
                showToast(result!!.msg)
            }

        }.post()
    }

    private fun httpGetDemo360(){
        App.getInstance().initRetrofit360()
        var requestEntity:RequestEntity = RequestEntity.Builder("IPQuery/ipquery").addParams("ip","115.159.152.210").showCacheFirst(true).showCacheOnFail(true).build()

//        var requestEntity:RequestEntity = RequestEntity.Builder("IPQuery/ipquery").addParams("ip","115.159.152.210").showCacheFirst(false).build()
        object:HttpRequest<Ip360Rsp>(this,requestEntity){

            override fun onRestore(result: Ip360Rsp?) {
                super.onRestore(result)
                Log.e("缓存: " + JsonUtils.gsonToJson(result))
                showToast("缓存: " + result!!.data)
            }

            override fun onSuccess(result: Ip360Rsp?) {
                super.onSuccess(result)
                Log.e("success: " + JsonUtils.gsonToJson(result))
                showToast(result!!.data)
            }

            override fun onFail(result: BaseRsp?) {
                super.onFail(result)
                Log.e("fail: " + JsonUtils.gsonToJson(result))
                showToast(result!!.msg)
            }

        }.get()
    }

    private fun httpGetDemoTaobao(){
        App.getInstance().initRetrofitTaobao()
        var requestEntity:RequestEntity = RequestEntity.Builder("service/getIpInfo.php").addParams("ip","115.159.152.210").build()
        object:HttpRequest<IpTaobaoRsp>(this,requestEntity){

            override fun onSuccess(result: IpTaobaoRsp?) {
                super.onSuccess(result)
                Log.d(JsonUtils.gsonToJson(result))
                showToast(result!!.data!!.city)
            }

            override fun onFail(result: BaseRsp?) {
                super.onFail(result)
                Log.d(JsonUtils.gsonToJson(result))
                showToast(result!!.msg)
            }

        }.get()
    }
}
