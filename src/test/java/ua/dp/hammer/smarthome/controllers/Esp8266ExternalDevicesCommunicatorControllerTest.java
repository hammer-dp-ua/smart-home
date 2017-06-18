package ua.dp.hammer.smarthome.controllers;

import org.junit.Assert;
import org.junit.Test;
import ua.dp.hammer.smarthome.models.Esp8266ResetReasons;

public class Esp8266ExternalDevicesCommunicatorControllerTest {

   @Test
   public void testDescribeResetReason() {
      Esp8266ExternalDevicesCommunicatorController controller = new Esp8266ExternalDevicesCommunicatorController();

      String result = controller.describeResetReason(" 1 bla");

      Assert.assertFalse(result.contains("1"));
      Assert.assertTrue(result.contains(Esp8266ResetReasons.getReason(1)));

      result = controller.describeResetReason("adsads 2 134 bla\r\n 567");
      Assert.assertFalse(result.contains("2"));
      Assert.assertTrue(result.contains(Esp8266ResetReasons.getReason(2)));

      result = controller.describeResetReason("1 : asd 2 dsa");
      Assert.assertFalse(result.contains("1"));
      Assert.assertTrue(result.contains(Esp8266ResetReasons.getReason(1)));
   }
}
