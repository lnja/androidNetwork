package len.android.network;

import com.google.gson.annotations.SerializedName;
import len.tools.android.extend.ListRspInterface;

import java.util.List;

public class ListRsp<D> extends BaseRsp implements ListRspInterface<D> {
    @SerializedName("pageNum")
    private int pageNum;
    @SerializedName("pageSize")
    private int pageSize;
    @SerializedName("totalNum")
    private int totalNum;
    @SerializedName("result")
    private List<D> list;

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(int totalNum) {
        this.totalNum = totalNum;
    }

    @Override
    public List<D> getList() {
        return list;
    }

    public void setList(List<D> list) {
        this.list = list;
    }
}
