package com.mbsetraining.sysmlhelper.taumigratorplugin;

import com.mbsetraining.sysmlhelper.common.ConfigurationSettings;

public class TauMigrator_Context extends ConfigurationSettings {

	public TauMigrator_Context(
			String theAppID ){
		
		super( theAppID, 
				"TauMigratorProfile.General.EnableErrorLogging", 
				"TauMigratorProfile.General.EnableWarningLogging",
				"TauMigratorProfile.General.EnableInfoLogging", 
				"TauMigratorProfile.General.EnableDebugLogging",
				"TauMigratorProfile.General.PluginVersion",
				"TauMigratorProfile.General.UserDefinedMetaClassesAsSeparateUnit",
				"TauMigratorProfile.General.AllowPluginToControlUnitGranularity",
				"TauMigrator.properties", 
				"TauMigrator_MessagesBundle",
				"TauMigrator" 
				);
	}
	
	public boolean getIsShowProfileVersionCheckDialogs(){
		
		boolean result = getBooleanPropertyValue(
				_rhpPrj,
				"SysMLHelper.General.IsShowProfileVersionCheckDialogs" );
				
		return result;
	}
}
