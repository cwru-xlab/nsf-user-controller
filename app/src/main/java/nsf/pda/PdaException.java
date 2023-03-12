package nsf.pda;

import nsf.controller.DataAccessException;

public class PdaException extends DataAccessException {
  private int statusCode;

  public PdaException(int statusCode){
    this.statusCode = statusCode;
  }
}
