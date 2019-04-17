package migrationTool.findDuplicationsInPath;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Created by Mikhail_Asadchy (EPAM)
 */
public class Main {

   public static void main(String[] args) throws IOException {
      String path1 = "/mnt/b72c5a51-4de0-4cd2-800b-a9e788068220/m3/HYBRISCOMM6700P_0-80003492/hybris/bin/custom/hybrisaccelerator/hybrisacceleratorcore";
      String path2 = "/mnt/b72c5a51-4de0-4cd2-800b-a9e788068220/m3/HYBRISCOMM6700P_0-80003492/hybris/bin/custom/velcom/velcomcore";

      final List<File> acceleratorCollectedFiles = Files.walk(Paths.get(path1)).filter(Files::isRegularFile).map(e->e.toFile()).collect(Collectors.toList());
      final List<File> velcomCoreCollectedFiles = Files.walk(Paths.get(path2)).filter(Files::isDirectory).map(e->e.toFile()).collect(Collectors.toList());

      Set<File> duplicatedFiles = new HashSet<>();
      for (File file : acceleratorCollectedFiles) {
         final String fileName = file.getName();


         final FilenameFilter filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
               return name.equals(fileName);
            }
         };

         for (File velcomCoreCollectedFile : velcomCoreCollectedFiles) {

         File [] files = velcomCoreCollectedFile.listFiles(filenameFilter);
            duplicatedFiles.addAll(Arrays.asList(files));
         }

      }
         for (File foundFile : duplicatedFiles) {
         foundFile.delete();
//            System.out.println(foundFile.getAbsolutePath());
         }
   }



}
