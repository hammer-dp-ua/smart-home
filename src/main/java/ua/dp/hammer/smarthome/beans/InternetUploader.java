package ua.dp.hammer.smarthome.beans;

import java.nio.file.Path;

public interface InternetUploader {
   void transferFile(Path path);
}
