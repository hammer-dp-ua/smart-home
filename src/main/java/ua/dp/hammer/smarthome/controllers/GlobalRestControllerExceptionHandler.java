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

@ControllerAdvice
public class GlobalRestControllerExceptionHandler {
   private static final Logger LOGGER = LogManager.getLogger(GlobalRestControllerExceptionHandler.class);

   @ExceptionHandler
   @ResponseStatus(HttpStatus.BAD_REQUEST)
   @ResponseBody
   String handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
      LOGGER.error("Bad request", ex);
      return null;
   }

   @ExceptionHandler
   @ResponseStatus(HttpStatus.BAD_REQUEST)
   @ResponseBody
   String handleJsonMappingException(JsonMappingException ex) {
      LOGGER.error("Bad request", ex);
      return null;
   }
}
