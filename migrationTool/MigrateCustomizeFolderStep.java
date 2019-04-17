package migrationTool;

/**
 * Created by Mikhail_Asadchy (EPAM)
 */
public enum MigrateCustomizeFolderStep {
   PREPARE_MIGRATION_PROJECT,
   PLACE_SOURCE_VERSION_TO_MIGRATION,
   PLACE_TARGET_VERSION_TO_MIGRATION,
   PLACE_CUSTOMIZED_VERSION_TO_MIGRATION,
   APPLY_CHANGES_TO_CUSTOM_PROJECT
}