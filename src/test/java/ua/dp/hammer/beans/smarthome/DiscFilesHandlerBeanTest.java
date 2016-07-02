package ua.dp.hammer.beans.smarthome;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import ua.dp.hammer.smarthome.config.AppConfig;
import ua.dp.hammer.smarthome.beans.DiscFilesHandlerBean;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Iterator;
import java.util.SortedSet;

import static junit.framework.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class, loader = AnnotationConfigContextLoader.class)
@TestPropertySource(properties = { "ramVideosDir: D:/UserTemp/JavaUnitTests", "discVideosDir: C:/Videos", "criticalFreeSpaceMb: 200" })
public class DiscFilesHandlerBeanTest {

   private static String ramVideosDir;

   @Autowired
   private DiscFilesHandlerBean discFilesHandlerBean;

   @Autowired
   private Environment environment;

   @Before
   public void beforeTest() {
      ramVideosDir = environment.getProperty("ramVideosDir");
   }

   @AfterClass
   public static void deInit() {
      for (File file : new File(ramVideosDir).listFiles()) {
         file.delete();
      }
   }

   @Test
   public void testAscendingFileSorting() throws IOException {
      try {
         Files.createDirectory(FileSystems.getDefault().getPath(ramVideosDir));
      }
      catch (FileAlreadyExistsException e) {
      }

      Path filePath1 = Files.createFile(FileSystems.getDefault().getPath(ramVideosDir + "/File1.tmp"));
      Files.setLastModifiedTime(filePath1, FileTime.fromMillis(System.currentTimeMillis()));
      Path filePath2 = Files.createFile(FileSystems.getDefault().getPath(ramVideosDir + "/File2.tmp"));
      Files.setLastModifiedTime(filePath2, FileTime.fromMillis(System.currentTimeMillis() + 1));
      Path filePath3 = Files.createFile(FileSystems.getDefault().getPath(ramVideosDir + "/File3.tmp"));
      Files.setLastModifiedTime(filePath3, FileTime.fromMillis(System.currentTimeMillis() + 2));

      SortedSet<Path> newFiles = discFilesHandlerBean.getNewVideoFiles();
      newFiles.add(filePath2);
      Iterator<Path> newFilesIterator = newFiles.iterator();

      assertEquals(3, newFiles.size());
      assertEquals(filePath1, newFilesIterator.next());
      assertEquals(filePath2, newFilesIterator.next());
      assertEquals(filePath3, newFilesIterator.next());
   }
}
