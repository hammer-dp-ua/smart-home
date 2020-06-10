package ua.dp.hammer.smarthome.boot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
      args = "--log4j2ConfigFile=log4j2.xml")
@ActiveProfiles("integration")
public class SpringBootTests {

   @Test
   public void testTest() {

   }
}
