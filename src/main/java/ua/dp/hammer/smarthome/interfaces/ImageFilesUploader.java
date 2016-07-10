package ua.dp.hammer.smarthome.interfaces;

import java.nio.file.Path;
import java.util.SortedSet;

public interface ImageFilesUploader {
   void upload(String videoFileName, SortedSet<Path> imageFilesPath);
}
