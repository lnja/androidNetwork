package len.android.network.demo;

import com.google.gson.annotations.SerializedName;
import len.android.network.BaseRsp;
import len.android.network.ErrorCode;

public class Ip360Rsp extends BaseRsp {

    @SerializedName("errno")
    private int code;
    @SerializedName("errmsg")
    private String msg;

    private String data;


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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
