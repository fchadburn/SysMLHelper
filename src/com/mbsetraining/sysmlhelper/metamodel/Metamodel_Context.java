package com.mbsetraining.sysmlhelper.metamodel;

import com.mbsetraining.sysmlhelper.common.BaseContext;

public class Metamodel_Context extends BaseContext {

	private static String _enableErrorLoggingProperty = "MetamodelProfile.General.EnableErrorLogging";
	private static String _enableWarningLoggingProperty = "MetamodelProfile.General.EnableWarningLogging";
	private static String _enableInfoLoggingProperty = "MetamodelProfile.General.EnableInfoLogging";
	private static String _enableDebugLoggingProperty = "MetamodelProfile.General.EnableDebugLogging";
	private static String _pluginVersionProperty = "MetamodelProfile.General.PluginVersion";
	private static String _userDefinedMetaClassesAsSeparateUnitProperty = "MetamodelProfile.General.UserDefinedMetaClassesAsSeparateUnit";
	private static String _allowPluginToControlUnitGranularityProperty = "MetamodelProfile.General.AllowPluginToControlUnitGranularity";

	public Metamodel_Context(
			String theAppID ){
		
		super( theAppID, 
				_enableErrorLoggingProperty, 
				_enableWarningLoggingProperty,
				_enableInfoLoggingProperty, 
				_enableDebugLoggingProperty,
				_pluginVersionProperty,
				_userDefinedMetaClassesAsSeparateUnitProperty,
				_allowPluginToControlUnitGranularityProperty );
	}
}
