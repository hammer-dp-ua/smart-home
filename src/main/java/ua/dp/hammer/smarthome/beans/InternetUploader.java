package ua.dp.hammer.smarthome.beans;

import java.nio.file.Path;

public interface InternetUploader {
   boolean transferVideoFile(Path path);
}
