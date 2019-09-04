package len.android.network.demo;

import com.google.gson.annotations.SerializedName;
import len.android.network.BaseRsp;
import len.android.network.ErrorCode;

public class IpTaobaoRsp extends BaseRsp {

    @SerializedName("code")
    private Integer code;

    @SerializedName("msg")
    private String msg;

    private IpDetail data;


    public boolean isSuccess() {
        return code == ErrorCode.ERR_CODE_OK;
    }

    public IpDetail getData() {
        return data;
    }

    public void setData(IpDetail data) {
        this.data = data;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String getMsg() {
        return msg;
    }

    @Override
    public void setMsg(String msg) {
        this.msg = msg;
    }
}
