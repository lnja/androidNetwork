package len.android.network;

import len.tools.android.model.JsonEntity;

/**
 * 请求参数基础类，与接口确定目前的请求格式统一为{"info":{auth对象}，"data":{其它数据对象，可以是javabean, map, list}}
 */

public class RequestParams extends JsonEntity {

    protected Req info;
    protected Object data;

    public RequestParams(Object tempData) {
        info = new Req();
        data = tempData;
    }

    public Req getInfo() {
        return info;
    }

    public Object getData() {
        return data;
    }
}
