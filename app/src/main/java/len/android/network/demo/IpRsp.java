package len.android.network.demo;

import len.android.network.BaseRsp;

public class IpRsp extends BaseRsp {

    private IpDetail data;

    public IpDetail getData() {
        return data;
    }

    public void setData(IpDetail data) {
        this.data = data;
    }
    /*private String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }*/
}
