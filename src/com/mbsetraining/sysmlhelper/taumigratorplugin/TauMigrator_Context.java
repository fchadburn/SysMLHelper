package com.mbsetraining.sysmlhelper.taumigratorplugin;

import com.mbsetraining.sysmlhelper.common.BaseContext;
import com.telelogic.rhapsody.core.IRPPackage;

public class TauMigrator_Context extends BaseContext {

	public TauMigrator_Context(
			String theAppID ){
		
		super( theAppID, 
				"TauMigratorProfile.General.EnableErrorLogging", 
				"TauMigratorProfile.General.EnableWarningLogging",
				"TauMigratorProfile.General.EnableInfoLogging", 
				"TauMigratorProfile.General.EnableDebugLogging",
				"TauMigratorProfile.General.PluginVersion",
				"TauMigratorProfile.General.UserDefinedMetaClassesAsSeparateUnit",
				"TauMigratorProfile.General.AllowPluginToControlUnitGranularity"
				);
	}
	
	public boolean getIsShowProfileVersionCheckDialogs(){
		
		boolean result = getBooleanPropertyValue(
				_rhpPrj,
				"SysMLHelper.General.IsShowProfileVersionCheckDialogs" );
				
		return result;
	}

	@Override
	public IRPPackage addNewTermPackageAndSetUnitProperties(String theName,
			IRPPackage theOwner, String theNewTermName) {
		return null;
	}
}
