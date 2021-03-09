package ua.dp.hammer.smarthome.models;

public class StatusResponse {
   private StatusCodes statusCode;
   private String errorMessage;

   public StatusResponse(){}

   public StatusResponse(StatusCodes statusCode) {
      this.statusCode = statusCode;
   }

   public StatusCodes getStatusCode() {
      return statusCode;
   }

   public void setStatusCode(StatusCodes statusCode) {
      this.statusCode = statusCode;
   }

   public String getErrorMessage() {
      return errorMessage;
   }

   public void setErrorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
   }
}
