package ua.dp.hammer.smarthome.controllers;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;

@ControllerAdvice
public class GlobalRestControllerExceptionHandler {
   private static final Logger LOGGER = LogManager.getLogger(GlobalRestControllerExceptionHandler.class);

   @ExceptionHandler
   @ResponseStatus(HttpStatus.BAD_REQUEST)
   @ResponseBody
   String handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest httpServletRequest) {
      LOGGER.error("Bad request: \r\n" + getRequestBody(httpServletRequest), ex);
      return null;
   }

   @ExceptionHandler
   @ResponseStatus(HttpStatus.BAD_REQUEST)
   @ResponseBody
   String handleJsonMappingException(JsonMappingException ex, HttpServletRequest httpServletRequest) {
      LOGGER.error("Bad request: \r\n" + getRequestBody(httpServletRequest), ex);
      return null;
   }

   private String getRequestBody(HttpServletRequest httpServletRequest) {
      String requestBody = null;

      try {
         StringBuilder stringBuilder = new StringBuilder();
         BufferedReader inputStream = httpServletRequest.getReader();
         String line;

         while ((line = inputStream.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append("\r\n");
         }
         requestBody = stringBuilder.toString();
      } catch (IOException e) {
         LOGGER.error(e);
      }
      return requestBody;
   }
}
