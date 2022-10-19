package com.mbsetraining.sysmlhelper.metamodel;

import com.mbsetraining.sysmlhelper.common.BaseContext;
import com.telelogic.rhapsody.core.IRPPackage;

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

	@Override
	public IRPPackage addNewTermPackageAndSetUnitProperties(String theName,
			IRPPackage theOwner, String theNewTermName) {
		// TODO Auto-generated method stub
		return null;
	}
}

/**
 * Copyright (C) 2020-2022  MBSE Training and Consulting Limited (www.executablembse.com)

    This file is part of SysMLHelperPlugin.

    SysMLHelperPlugin is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SysMLHelperPlugin is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SysMLHelperPlugin.  If not, see <http://www.gnu.org/licenses/>.
*/
