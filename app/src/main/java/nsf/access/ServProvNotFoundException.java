package nsf.access;

public class ServProvNotFoundException extends Exception {
  public ServProvNotFoundException(){
    super("Service Provider was not found in the metadata database.");
  }
}
