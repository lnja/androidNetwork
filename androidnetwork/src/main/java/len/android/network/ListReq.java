package len.android.network;


import com.google.gson.annotations.SerializedName;

public class ListReq extends Req {
    @SerializedName("pageSize")
    private Integer pageSize;
    @SerializedName("pageNo")
    private Integer pageNum;

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer tempPageSize) {
        this.pageSize = tempPageSize;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer tempPageNum) {
        this.pageNum = tempPageNum;
    }
}
