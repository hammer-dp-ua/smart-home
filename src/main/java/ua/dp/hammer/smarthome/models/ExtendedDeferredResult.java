package ua.dp.hammer.smarthome.models;

import org.springframework.web.context.request.async.DeferredResult;

public class ExtendedDeferredResult<T> extends DeferredResult<T> {
   private String clientIp;

   public String getClientIp() {
      return clientIp;
   }

   public void setClientIp(String clientIp) {
      this.clientIp = clientIp;
   }
}
