package len.android.network;

import com.google.gson.annotations.SerializedName;
import len.tools.android.model.JsonEntity;

public class Rsp extends JsonEntity {

    @SerializedName("code")
    private Integer code;
    @SerializedName("msg")
    private String msg;

    public boolean isSuccess() {
        return code == ErrorCode.ERR_CODE_OK;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
