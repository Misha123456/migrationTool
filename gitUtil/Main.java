package gitUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;


/**
 * Created by Mikhail_Asadchy (EPAM)
 */
public class Main {

   private static final List<String> GIT_FILES_TO_EXCLUDE = Arrays.asList("/bin/npm");

   public static void main(String[] args) throws Exception {
      String gitRepoRoot = "/mnt/b72c5a51-4de0-4cd2-800b-a9e788068220/mig/";
      String gitRepoIndexFile = "/mnt/b72c5a51-4de0-4cd2-800b-a9e788068220/mig/gitIndex.txt";
      String outputFolder = "/mnt/b72c5a51-4de0-4cd2-800b-a9e788068220/tempFolder/diffs/baseDevelopCommit/";
      cleanOutputDirectory(outputFolder);
      copyFilesFromGitIndex(gitRepoRoot, gitRepoIndexFile, outputFolder);
   }

   private static void cleanOutputDirectory(final String outputFolder) {
      final File outputDir = new File(outputFolder);
      FileUtils.deleteQuietly(outputDir);
      outputDir.mkdirs();
   }

   private static void copyFilesFromGitIndex(final String gitRepoRoot, final String gitRepoIndexFile, final String outputFolder)
         throws Exception {
      Set<String> gitRepoFileSubPaths = readGitRepoFiles(gitRepoIndexFile);
      for (String fileUnderGitSubPath : gitRepoFileSubPaths) {
         copyFile(fileUnderGitSubPath, gitRepoRoot, outputFolder);
      }
   }

   private static void copyFile(final String fileUnderGitSubPath, final String gitRepoRoot, final String outputFolder)
         throws IOException {
      if (!shouldBeExcluded(fileUnderGitSubPath)) {
         final File fileToCopy = new File(gitRepoRoot + fileUnderGitSubPath);
         final File targetFile = new File(outputFolder + fileUnderGitSubPath);
         Assert.assertTrue(buildErrorMessage(fileToCopy, targetFile), fileToCopy.exists());
         Assert.assertTrue(buildErrorMessage(fileToCopy, targetFile), fileToCopy.isFile());
         Assert.assertFalse(buildErrorMessage(fileToCopy, targetFile), targetFile.exists());
         FileUtils.copyFile(fileToCopy, targetFile);
      }
   }

   private static boolean shouldBeExcluded(final String fileUnderGitSubPath) {
      return GIT_FILES_TO_EXCLUDE.stream().anyMatch(fileUnderGitSubPath::contains);
   }

   private static String buildErrorMessage(final File fileToCopy, final File targetFile) {
      StringBuilder sb = new StringBuilder();
      sb.append("fileToCopy:").append(fileToCopy.getAbsolutePath()).append("\r\n");
      sb.append("targetFile:").append(targetFile.getAbsolutePath()).append("\r\n");
      return sb.toString();
   }

   private static Set<String> readGitRepoFiles(final String gitRepoIndexFile) throws Exception {

      final FileInputStream in = new FileInputStream(gitRepoIndexFile);
      final InputStreamReader inReader = new InputStreamReader(in);
      BufferedReader br = new BufferedReader(inReader);
      String line = null;

      Set<String> result = new HashSet<>();
      while ((line = br.readLine()) != null) {
         if (StringUtils.isNoneBlank(line)) {
            result.add(line);
         }
      }
      in.close();
      inReader.close();
      br.close();

      return result;
   }

}
