package ua.dp.hammer.smarthome;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import ua.dp.hammer.smarthome.beans.DiscFilesHandlerBean;
import ua.dp.hammer.smarthome.beans.GoogleDriveUploaderBean;
import ua.dp.hammer.smarthome.beans.RouterPingerBean;
import ua.dp.hammer.smarthome.beans.VpsUploader;

@SpringBootApplication
@ComponentScan(excludeFilters =
 @ComponentScan.Filter(value = {GoogleDriveUploaderBean.class, RouterPingerBean.class, VpsUploader.class, DiscFilesHandlerBean.class},
                       type = FilterType.ASSIGNABLE_TYPE))
@EnableAsync
@EnableScheduling
public class Application {

   public static void main(String[] args) {
      new SpringApplicationBuilder(Application.class)
            .profiles("prod")
            .run(args);
   }
}