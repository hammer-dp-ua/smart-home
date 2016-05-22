package ua.dp.hammer.smarthome.beans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class RouterPingerBean {

   private String routerIp;
   private String routerLogin;
   private String routerPassword;

   @Autowired
   private Environment environment;

   @PostConstruct
   public void init() {
      routerIp = environment.getRequiredProperty("routerIp");
      routerLogin = environment.getRequiredProperty("routerLogin");
      routerPassword = environment.getRequiredProperty("routerPassword");
   }


}
