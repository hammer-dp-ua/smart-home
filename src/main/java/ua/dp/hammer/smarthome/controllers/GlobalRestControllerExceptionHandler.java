package ua.dp.hammer.smarthome.controllers;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalRestControllerExceptionHandler {
   private static final Logger LOGGER = LogManager.getLogger(GlobalRestControllerExceptionHandler.class);

   public static final String ERROR_OCCURRED_ATTRIBUTE = "ERROR_OCCURRED";

   @ExceptionHandler
   @ResponseStatus(HttpStatus.BAD_REQUEST)
   String handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest request) {
      request.setAttribute(ERROR_OCCURRED_ATTRIBUTE, true);
      LOGGER.error("Bad request", ex);
      return null;
   }

   @ExceptionHandler
   @ResponseStatus(HttpStatus.BAD_REQUEST)
   String handleJsonMappingException(JsonMappingException ex, HttpServletRequest request) {
      request.setAttribute(ERROR_OCCURRED_ATTRIBUTE, true);
      LOGGER.error("Bad request", ex);
      return null;
   }
}
