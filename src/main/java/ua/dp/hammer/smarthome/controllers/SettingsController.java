package ua.dp.hammer.smarthome.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.dp.hammer.smarthome.models.FanSettingsInfo;
import ua.dp.hammer.smarthome.repositories.SettingsRepository;

@RestController
@RequestMapping(path = SettingsController.CONTROLLER_PATH)
public class SettingsController {
   public static final String CONTROLLER_PATH = "/server/settings";
   public static final String GET_FAN_SETTINGS_PATH = "/getFanSettings";
   public static final String SAVE_FAN_SETTINGS_PATH = "/saveFanSettings";

   private SettingsRepository settingsRepository;

   @GetMapping(path = GET_FAN_SETTINGS_PATH)
   public FanSettingsInfo getFanSettings(@RequestParam("name") String name) {
      return settingsRepository.getFanSettings(name);
   }

   @PostMapping(path = SAVE_FAN_SETTINGS_PATH, consumes="application/json")
   public void saveFanSettings(@RequestBody FanSettingsInfo fanSetup) {
      settingsRepository.saveFanSetting(fanSetup);
   }

   @Autowired
   public void setSettingsRepository(SettingsRepository settingsRepository) {
      this.settingsRepository = settingsRepository;
   }
}
