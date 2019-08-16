package len.android.network.demo

import android.app.Activity
import android.os.Bundle
import len.android.network.BaseRsp
import len.android.network.HttpRequest
import len.android.network.RequestEntity
import len.tools.android.JsonUtils
import len.tools.android.Log

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        httpDemo()
    }

    private fun httpDemo(){
//        var requestEntity:RequestEntity = RequestEntity.Builder("service/getIpInfo.php?ip=115.159.152.210").build()
        var requestEntity:RequestEntity = RequestEntity.Builder("IPQuery/ipquery").addParams("ip","115.159.152.210").build()
        object:HttpRequest<IpRsp>(this,requestEntity){

            override fun onSuccess(result: IpRsp?) {
                super.onSuccess(result)
                Log.e(JsonUtils.toJson(result))
            }

            override fun onFail(result: BaseRsp?) {
                super.onFail(result)
                Log.e(JsonUtils.toJson(result))
            }

        }.post()
    }
}
