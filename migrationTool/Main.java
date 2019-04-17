package migrationTool;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static migrationTool.MigrateCustomizeFolderStep.APPLY_CHANGES_TO_CUSTOM_PROJECT;
import static migrationTool.MigrateCustomizeFolderStep.PLACE_CUSTOMIZED_VERSION_TO_MIGRATION;
import static migrationTool.MigrateCustomizeFolderStep.PLACE_SOURCE_VERSION_TO_MIGRATION;
import static migrationTool.MigrateCustomizeFolderStep.PLACE_TARGET_VERSION_TO_MIGRATION;
import static migrationTool.MigrateCustomizeFolderStep.PREPARE_MIGRATION_PROJECT;



/**
 * Created by Mikhail_Asadchy (EPAM)
 */
public class Main {

   private static final Logger LOG = LoggerFactory.getLogger(Main.class);

   public static void main(String[] args) throws IOException, InterruptedException {

//      final List<MigrateCustomizeFolderStep> steps = Arrays.asList(PREPARE_MIGRATION_PROJECT);
//            final List<MigrateCustomizeFolderStep> steps = Arrays.asList(PREPARE_MIGRATION_PROJECT, PLACE_SOURCE_VERSION_TO_MIGRATION, PLACE_TARGET_VERSION_TO_MIGRATION, PLACE_CUSTOMIZED_VERSION_TO_MIGRATION);
            final List<MigrateCustomizeFolderStep> steps = Arrays.asList(APPLY_CHANGES_TO_CUSTOM_PROJECT);

      final String sourceRoot = "/mnt/b72c5a51-4de0-4cd2-800b-a9e788068220/e0";
      final String targetRoot = "/mnt/b72c5a51-4de0-4cd2-800b-a9e788068220/e22";
      final String customizedRoot = "/mnt/b72c5a51-4de0-4cd2-800b-a9e788068220/m5";
      final String migrationConflictsProjectRoot = "/mnt/b72c5a51-4de0-4cd2-800b-a9e788068220/migrationConflicts";

      final String customizedFolderSubPath = "/hybris/bin/custom/hybrisacceleratorws/hybrisacceleratorws";
      final String targetCustomizePath = customizedFolderSubPath;
      final FilesComparisonStrategy strategy = FilesComparisonStrategy.SINGLE_FILE;

      for (MigrateCustomizeFolderStep step : steps) {
         migrateCustomizeDirUnderConfigtemplates(sourceRoot, targetRoot, customizedRoot, migrationConflictsProjectRoot,
               customizedFolderSubPath, targetCustomizePath, step, strategy);
      }
   }

   private static void migrateCustomizeDirUnderConfigtemplates(final String sourceRoot, final String targetRoot,
         final String customizedRoot, final String migrationConflictsProjectRoot, final String subPathToMigrate,
         final String targetCustomizePath, final MigrateCustomizeFolderStep step, final FilesComparisonStrategy strategy)
         throws IOException {
      final File file = new File(migrationConflictsProjectRoot);
      if (step == PREPARE_MIGRATION_PROJECT) {
         FileUtils.deleteQuietly(file);
         file.mkdirs();
         final File readMeFile = new File(file.getAbsolutePath() + "/README");
         final boolean readMeCreated = readMeFile.createNewFile();
         if (!readMeCreated) {
            LOG.error("Cannot create a new file under migration directory. Please, check access rights to that folder (" + file
                  .getAbsolutePath() + ")");
         }
         executeShellCommand(file, "git init");
         executeShellCommand(file, "git add *");
         executeShellCommand(file, "git commit -m \"initial commit\"");
         executeShellCommand(file, "git status");
         return;
      }

      final String customizeFolderPath = customizedRoot + targetCustomizePath;

      if (step == PLACE_SOURCE_VERSION_TO_MIGRATION) {
         if (strategy == FilesComparisonStrategy.CUSTOMIZED_FILES_ONLY) {
            final List<File> customizedFiles = getChangedFilesOnly(customizeFolderPath);
            final List<File> sourceFiles = buildSourceFiles(sourceRoot, customizeFolderPath, customizedFiles,
                  subPathToMigrate);
            placeFilesToMigrationDirectory(migrationConflictsProjectRoot, subPathToMigrate, sourceFiles, sourceRoot,
                  targetCustomizePath);
         }
         else if (strategy == FilesComparisonStrategy.WHOLE_DIRECTORY) {
            placeWholeDirectoryToMigrationDirectory(migrationConflictsProjectRoot, sourceRoot, subPathToMigrate,
                  subPathToMigrate);
         }
         else if (strategy == FilesComparisonStrategy.SINGLE_FILE) {
            placeASingleFileToMigrationDirectory(migrationConflictsProjectRoot, sourceRoot, subPathToMigrate);
         }
         executeShellCommand(file, "git add *");
         executeShellCommand(file, "git commit -m \"OOTBSourceVersion\"");
      }
      else if (step == PLACE_TARGET_VERSION_TO_MIGRATION) {
         executeShellCommand(file, "git checkout -b targetOOTBVersion");
         if (strategy == FilesComparisonStrategy.CUSTOMIZED_FILES_ONLY) {
            final List<File> customizedFiles = getChangedFilesOnly(customizeFolderPath);
            final List<File> targetFiles = buildSourceFiles(targetRoot, customizeFolderPath, customizedFiles,
                  subPathToMigrate);
            placeFilesToMigrationDirectory(migrationConflictsProjectRoot, subPathToMigrate, targetFiles, targetRoot,
                  targetCustomizePath);
         }
         else if (strategy == FilesComparisonStrategy.WHOLE_DIRECTORY) {
            placeWholeDirectoryToMigrationDirectory(migrationConflictsProjectRoot, targetRoot, subPathToMigrate, subPathToMigrate);
         }
         else if (strategy == FilesComparisonStrategy.SINGLE_FILE) {
            placeASingleFileToMigrationDirectory(migrationConflictsProjectRoot, targetRoot, subPathToMigrate);
         }
         executeShellCommand(file, "git add *");
         executeShellCommand(file, "git commit -m \"OOTBTargetVersion\"");
      }
      else if (step == PLACE_CUSTOMIZED_VERSION_TO_MIGRATION) {
         final List<File> customizedFiles = getChangedFilesOnly(customizeFolderPath);
         executeShellCommand(file, "git checkout master");
         executeShellCommand(file, "git reset --hard");
         executeShellCommand(file, "git clean -df");
         executeShellCommand(file, "git checkout -b customizedSourceVersion");
         if (strategy == FilesComparisonStrategy.CUSTOMIZED_FILES_ONLY) {
            placeFilesToMigrationDirectory(migrationConflictsProjectRoot, subPathToMigrate, customizedFiles,
                  customizedRoot, targetCustomizePath);
         }
         else if (strategy == FilesComparisonStrategy.WHOLE_DIRECTORY) {
            placeWholeDirectoryToMigrationDirectory(migrationConflictsProjectRoot, customizedRoot, subPathToMigrate,
                  targetCustomizePath);
         }
         else if (strategy == FilesComparisonStrategy.SINGLE_FILE) {
            placeASingleFileToMigrationDirectory(migrationConflictsProjectRoot, customizedRoot, subPathToMigrate);
         }
         executeShellCommand(file, "git add *");
         executeShellCommand(file, "git commit -m \"CustomizedSourceVersion\"");
         executeShellCommand(file, "git checkout -b customizedTargetVersion");
         LOG.info("Open the directory " + file.getAbsolutePath() + " in your IDE and merge the branches");
      }
      else if (step == APPLY_CHANGES_TO_CUSTOM_PROJECT) {
         applyResolvedChangesToCustomizedProject(customizedRoot, migrationConflictsProjectRoot, subPathToMigrate, targetCustomizePath);
         executeShellCommand(new File(customizedRoot), "git add *");
      }

      LOG.info("done");
   }

   private static void placeASingleFileToMigrationDirectory(final String migrationConflictsProjectRoot,
         final String rootFolderToCopy,
         final String subPathToMigrate) throws IOException {
      final String pathToCopy = rootFolderToCopy + subPathToMigrate;
      final String migrationConflictsPath = migrationConflictsProjectRoot + subPathToMigrate;

      final File fileToCopy = new File(pathToCopy);

      final File migrationRootFolder = new File(migrationConflictsPath);
      cleanMigrationConflictsDirectory(migrationRootFolder);

      // file could be removed
      if (fileToCopy.exists() && fileToCopy.isFile()) {
         System.out.println("coping a single file " + fileToCopy.getAbsolutePath());
         FileUtils.copyFile(fileToCopy, migrationRootFolder);
         System.out.println("finished coping a single file " + fileToCopy.getAbsolutePath());
      }
   }

   private static void placeWholeDirectoryToMigrationDirectory(final String migrationConflictsProjectRoot,
         final String customizedRoot, final String customizedFolderSubPath, final String targetCustomizePath) throws IOException {
      final String customizeFolderPath = customizedRoot + targetCustomizePath;
      final String migrationConflictsPath = migrationConflictsProjectRoot + customizedFolderSubPath;
      final File dir = new File(customizeFolderPath);
      final File migrationConflictsDir = new File(migrationConflictsPath);
      Assert.assertTrue(dir.isDirectory());
      cleanMigrationConflictsDirectory(migrationConflictsDir);
      LOG.info("Coping " + dir.getAbsolutePath() + " to " + migrationConflictsDir.getAbsolutePath());
      FileUtils.copyDirectory(dir, migrationConflictsDir);
      LOG.info("Coping finished " + dir.getAbsolutePath() + " to " + migrationConflictsDir.getAbsolutePath());
   }

   private static void cleanMigrationConflictsDirectory(final File migrationConflictsDir) {
      if (migrationConflictsDir.exists()) {
         LOG.info("deleting target directory " + migrationConflictsDir.getAbsolutePath());
         FileUtils.deleteQuietly(migrationConflictsDir);
         LOG.info("Deleting of the target directory " + migrationConflictsDir.getAbsolutePath() + " finished");
      }
   }

   private static List<File> listFilesForFolder(final File folder) {
      List<File> files = new ArrayList<>();
      for (final File fileEntry : folder.listFiles()) {
         if (fileEntry.isDirectory()) {
            files.addAll(listFilesForFolder(fileEntry));
         } else {
            files.add(fileEntry);
         }
      }
      return files;
   }

   private static List<File> getChangedFilesOnly(final String customizeFolderPath) throws IOException {
      final File rootDirToFindFilesIn = new File(customizeFolderPath);
      return listFilesForFolder(rootDirToFindFilesIn);
   }

   private static void executeShellCommand(final File file, final String command) {
      LOG.info("executing shell command " + command);
      //      sleep();
      String output = executeCommand(command, file);
      LOG.info(output);
   }

   private static void sleep() {
      try {
         Thread.sleep(10000);
      }
      catch (InterruptedException e) {
         e.printStackTrace();
      }
   }

   private static String executeCommand(String command, final File dir) {

      new ExecCommand(command, null, dir);

      return "";

      //      StringBuffer output = new StringBuffer();
      //
      //      Process p;
      //      BufferedReader reader = null;
      //      InputStream inputStream = null;
      //      InputStreamReader inReader = null;
      //      try {
      //         p = Runtime.getRuntime().exec(command, null, dir);
      //         p.waitFor();
      //         LOG.info(p.isAlive() + "");
      //         inputStream = p.getInputStream();
      //         inReader = new InputStreamReader(inputStream);
      //         reader = new BufferedReader(inReader);
      //         String line = "";
      //         while ((line = reader.readLine()) != null) {
      //            output.append(line + "\n");
      //         }
      //
      //      }
      //      catch (Exception e) {
      //         e.printStackTrace();
      //      }finally {
      //         try {
      //            reader.close();
      //            inputStream.close();
      //            inReader.close();
      //         }
      //         catch (IOException e) {
      //            e.printStackTrace();
      //         }
      //      }
      //
      //      return output.toString();

   }

   private static void applyResolvedChangesToCustomizedProject(final String customizedRoot,
         final String migrationConflictsProjectRoot, final String customizedFolderSubPath, final String targetCustomizePath) throws IOException {
      final String customizedFolderPath = customizedRoot + targetCustomizePath;
      final File customizedDirectory = new File(customizedFolderPath);

      LOG.info("removing old customized directory to place a new one here...");
      LOG.info(customizedDirectory.getAbsolutePath());
      FileUtils.deleteQuietly(customizedDirectory);
      final String migratedFolderPathString = migrationConflictsProjectRoot + customizedFolderSubPath;
      final File migratedDirectory = new File(migratedFolderPathString);
      Assert.assertTrue(migratedDirectory.exists());
      LOG.info("coping migrated directory...");
      LOG.info(migratedDirectory.getAbsolutePath());
      LOG.info(customizedDirectory.getAbsolutePath());
      if (migratedDirectory.isDirectory()) {
         FileUtils.copyDirectory(migratedDirectory, customizedDirectory);
      }
      else if (migratedDirectory.isFile()) {
         FileUtils.copyFile(migratedDirectory, customizedDirectory);
      }
   }

   private static void placeFilesToMigrationDirectory(final String migrationConflictsProjectRoot,
         final String customizedFolderSubPath, final List<File> files, final String filesRoot, final String targetCustomizePath)
         throws IOException {
      for (File file : files) {
         if (file.exists()) {
            File targetFile = new File(file.getAbsolutePath().replace(filesRoot, migrationConflictsProjectRoot).replace(
                  targetCustomizePath, customizedFolderSubPath));
            LOG.info("coping source file " + file.getAbsolutePath() + " to target " + targetFile.getAbsolutePath());
            FileUtils.copyFile(file, targetFile);
         }
      }
   }

   private static List<File> buildSourceFiles(final String sourceRoot, final String customizeFolderPath,
         final List<File> customizedFiles, final String targetCustomizePath) {
      final List<File> sourceFiles = new ArrayList<>();

      for (File customizedFile : customizedFiles) {
         final String sourceFilePathString = customizedFile.getAbsolutePath().replace(customizeFolderPath,
               sourceRoot + targetCustomizePath);
         File sourceFile = new File(sourceFilePathString);
         sourceFiles.add(sourceFile);
      }

      return sourceFiles;
   }
}
