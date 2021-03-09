package ua.dp.hammer.smarthome.controllers.exceptionhandlers;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.ContentCachingRequestWrapper;
import ua.dp.hammer.smarthome.exceptions.DeviceSetupException;
import ua.dp.hammer.smarthome.models.StatusCodes;
import ua.dp.hammer.smarthome.models.StatusResponse;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

@RestControllerAdvice
public class GlobalRestControllerExceptionHandler {
   private static final Logger LOGGER = LogManager.getLogger(GlobalRestControllerExceptionHandler.class);

   public static final String ERROR_OCCURRED_ATTRIBUTE = "ERROR_OCCURRED";

   @ExceptionHandler
   @ResponseStatus(HttpStatus.BAD_REQUEST)
   String handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest request) {
      LOGGER.error("Bad request", ex);
      ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
      try {
         LOGGER.error("Request body:\n" + new String(wrapper.getContentAsByteArray(), wrapper.getCharacterEncoding()));
      } catch (UnsupportedEncodingException e) {
         LOGGER.error("Couldn't decode the error request");
      }
      request.setAttribute(ERROR_OCCURRED_ATTRIBUTE, true);

      return null;
   }

   @ExceptionHandler
   @ResponseStatus(HttpStatus.BAD_REQUEST)
   String handleJsonMappingException(JsonMappingException ex, HttpServletRequest request) {
      request.setAttribute(ERROR_OCCURRED_ATTRIBUTE, true);
      LOGGER.error("Bad request", ex);
      return null;
   }

   @ExceptionHandler
   @ResponseStatus(HttpStatus.OK)
   StatusResponse handleDeviceSetupException(DeviceSetupException exception) {
      StatusResponse statusResponse = new StatusResponse(StatusCodes.ERROR);
      statusResponse.setErrorMessage(exception.getMessage());
      return statusResponse;
   }
}
