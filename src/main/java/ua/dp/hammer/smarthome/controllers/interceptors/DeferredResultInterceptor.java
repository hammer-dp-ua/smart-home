package ua.dp.hammer.smarthome.controllers.interceptors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.DeferredResultProcessingInterceptor;
import ua.dp.hammer.smarthome.utils.Utils;

public class DeferredResultInterceptor implements DeferredResultProcessingInterceptor {
   private static final Logger LOGGER = LogManager.getLogger(DeferredResultInterceptor.class);

   /*@Override
   public <T> void afterCompletion(NativeWebRequest request, DeferredResult<T> deferredResult) {
      LOGGER.info("Deferred OK. Client: " + Utils.getClientIpAddr(request));
   }*/

   @Override
   public <T> boolean handleTimeout(NativeWebRequest request, DeferredResult<T> deferredResult) {
      LOGGER.debug("Deferred request timeout. Client: " + Utils.getClientIpAddr(request));
      return true;
   }
}
