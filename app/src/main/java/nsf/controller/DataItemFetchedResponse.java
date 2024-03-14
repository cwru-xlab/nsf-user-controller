package nsf.controller;

public class DataItemFetchedResponse{
  String dataSourceId;
  String dataItemId;
  Object data;
  boolean isCached;
  boolean dontShare;

  public DataItemFetchedResponse(String dataSourceId, String dataItemId, Object data, boolean isCached) {
    this.dataSourceId = dataSourceId;
    this.dataItemId = dataItemId;
    this.data = data;
    this.isCached = isCached;
    this.dontShare = false;
  }

  public DataItemFetchedResponse() {

  }

  public static DataItemFetchedResponse dontShareData(){
    DataItemFetchedResponse r = new DataItemFetchedResponse();
    r.dontShare = true;
    return r;
  }
}