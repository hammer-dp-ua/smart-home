package ua.dp.hammer.smarthome.models;

public class ServerStatus {
   private StatusCodes statusCode;

   public ServerStatus(StatusCodes statusCode) {
      this.statusCode = statusCode;
   }

   public StatusCodes getStatusCode() {
      return statusCode;
   }

   public void setStatusCode(StatusCodes statusCode) {
      this.statusCode = statusCode;
   }
}
