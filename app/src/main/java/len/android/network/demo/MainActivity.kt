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
        var requestEntity:RequestEntity = RequestEntity.Builder("IPQuery/ipquery").addParams("ip","115.159.152.210").build()
        object:HttpRequest<Ip360Rsp>(this,requestEntity){

            override fun onSuccess(result: Ip360Rsp?) {
                super.onSuccess(result)
                Log.d(JsonUtils.toJson(result))
                showToast(result!!.data)
            }

            override fun onFail(result: BaseRsp?) {
                super.onFail(result)
                Log.d(JsonUtils.toJson(result))
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
                Log.d(JsonUtils.toJson(result))
                showToast(result!!.data!!.city)
            }

            override fun onFail(result: BaseRsp?) {
                super.onFail(result)
                Log.d(JsonUtils.toJson(result))
                showToast(result!!.msg)
            }

        }.post()
    }

    private fun httpGetDemo360(){
        App.getInstance().initRetrofit360()
        var requestEntity:RequestEntity = RequestEntity.Builder("IPQuery/ipquery").addParams("ip","115.159.152.210").build()
        object:HttpRequest<Ip360Rsp>(this,requestEntity){

            override fun onSuccess(result: Ip360Rsp?) {
                super.onSuccess(result)
                Log.d(JsonUtils.toJson(result))
                showToast(result!!.data)
            }

            override fun onFail(result: BaseRsp?) {
                super.onFail(result)
                Log.d(JsonUtils.toJson(result))
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
                Log.d(JsonUtils.toJson(result))
                showToast(result!!.data!!.city)
            }

            override fun onFail(result: BaseRsp?) {
                super.onFail(result)
                Log.d(JsonUtils.toJson(result))
                showToast(result!!.msg)
            }

        }.get()
    }

}
