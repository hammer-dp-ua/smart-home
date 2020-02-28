package ua.dp.hammer.smarthome.controllers.interceptors;

import org.springframework.web.context.request.async.WebAsyncManager;
import org.springframework.web.context.request.async.WebAsyncUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AsyncHandlerInterceptor extends HandlerInterceptorAdapter {
   private static final Object DEFERRED_INTERCEPTOR_KEY = new Object();

   @Override
   public boolean preHandle(HttpServletRequest request,
                            HttpServletResponse response,
                            Object handler) {
      WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);

      asyncManager.registerDeferredResultInterceptor(DEFERRED_INTERCEPTOR_KEY, new DeferredResultInterceptor());
      return true;

   }
}
