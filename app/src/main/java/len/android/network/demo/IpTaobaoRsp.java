package len.android.network.demo;

import com.google.gson.annotations.SerializedName;
import len.android.network.BaseRsp;
import len.android.network.ErrorCode;

public class IpTaobaoRsp extends BaseRsp {

    @SerializedName("code")
    private int code;

    @SerializedName("msg")
    private String msg;

    private IpDetail data;


    public boolean isSuccess() {
        return code == ErrorCode.ERR_CODE_OK;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public IpDetail getData() {
        return data;
    }

    public void setData(IpDetail data) {
        this.data = data;
    }
}
