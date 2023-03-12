package nsf.controller;

import java.util.Objects;

public class DataResourceIdentifier {
  private String uri;

  private String datastoreId;

  private String namespace;

  public DataResourceIdentifier(String dataUri) throws Exception {
    this.uri = Objects.requireNonNull(dataUri);

    String[] splitUriComponents = dataUri.split(":");

    if (splitUriComponents.length <= 2){
      throw new Exception();
    }

    this.datastoreId = splitUriComponents[1];
    this.namespace = splitUriComponents[2];
  }

  public String getDatastoreId() {
    return datastoreId;
  }

  public String getNamespace() {
    return namespace;
  }

  @Override
  public String toString(){
    return uri;
  }
}
