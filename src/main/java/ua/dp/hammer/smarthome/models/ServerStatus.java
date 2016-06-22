package ua.dp.hammer.smarthome.models;

public class ServerStatus {
   private StatusCodes statusCode;
   private boolean includeDebugInfo;

   public ServerStatus(StatusCodes statusCode) {
      this.statusCode = statusCode;
   }

   public StatusCodes getStatusCode() {
      return statusCode;
   }

   public void setStatusCode(StatusCodes statusCode) {
      this.statusCode = statusCode;
   }

   public boolean isIncludeDebugInfo() {
      return includeDebugInfo;
   }

   public void setIncludeDebugInfo(boolean includeDebugInfo) {
      this.includeDebugInfo = includeDebugInfo;
   }
}
