package ua.dp.hammer.smarthome.utils;

import org.springframework.web.context.request.NativeWebRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Utils {
   private static final List<String> IP_HEADERS =
         Arrays.asList("X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR");

   public static String getClientIpAddr(HttpServletRequest request) {
      return IP_HEADERS.stream()
            .map(request::getHeader)
            .filter(Objects::nonNull)
            .filter(ip -> !ip.isEmpty() && !ip.equalsIgnoreCase("unknown"))
            .findFirst()
            .orElseGet(request::getRemoteAddr);
   }

   public static String getClientIpAddr(NativeWebRequest request) {
      return IP_HEADERS.stream()
            .map(request::getHeader)
            .filter(Objects::nonNull)
            .filter(ip -> !ip.isEmpty() && !ip.equalsIgnoreCase("unknown"))
            .findFirst()
            .orElse(null);
   }
}
