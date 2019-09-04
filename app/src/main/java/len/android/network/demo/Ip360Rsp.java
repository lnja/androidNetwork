package len.android.network.demo;

import com.google.gson.annotations.SerializedName;
import len.android.network.BaseRsp;
import len.android.network.ErrorCode;

public class Ip360Rsp extends BaseRsp {

    @SerializedName("errno")
    private Integer code;
    @SerializedName("errmsg")
    private String msg;

    private String data;


    public boolean isSuccess() {
        return code == ErrorCode.ERR_CODE_OK;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
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
