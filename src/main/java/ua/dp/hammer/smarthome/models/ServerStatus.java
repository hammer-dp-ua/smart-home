package ua.dp.hammer.smarthome.models;

public class ServerStatus {
   private StatusCodes statusCode;
   private boolean includeDebugInfo;
   private boolean updateFirmware;

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

   public boolean isUpdateFirmware() {
      return updateFirmware;
   }

   public void setUpdateFirmware(boolean updateFirmware) {
      this.updateFirmware = updateFirmware;
   }
}
