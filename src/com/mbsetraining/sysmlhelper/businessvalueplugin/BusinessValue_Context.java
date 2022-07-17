package com.mbsetraining.sysmlhelper.businessvalueplugin;

import com.mbsetraining.sysmlhelper.common.BaseContext;
import com.telelogic.rhapsody.core.IRPPackage;

public class BusinessValue_Context extends BaseContext {

	protected Boolean _isShowProfileVersionCheckDialogs;

	public final String BUSINESS_VALUE_NEW_TERM = "BusinessValue";
	
	public BusinessValue_Context(
			String theAppID ){

		super(  theAppID, 
				"BusinessValueProfile.General.EnableErrorLogging", 
				"BusinessValueProfile.General.EnableWarningLogging", 
				"BusinessValueProfile.General.EnableInfoLogging", 
				"BusinessValueProfile.General.EnableDebugLogging", 
				"BusinessValueProfile.General.PluginVersion", 
				"BusinessValueProfile.General.UserDefinedMetaClassesAsSeparateUnit", 
				"BusinessValueProfile.General.AllowPluginToControlUnitGranularity" );
	}

	@Override
	public IRPPackage addNewTermPackageAndSetUnitProperties(String theName,
			IRPPackage theOwner, String theNewTermName) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Boolean getIsShowProfileVersionCheckDialogs(){

		if( _isShowProfileVersionCheckDialogs == null ){	
			_isShowProfileVersionCheckDialogs = getBooleanPropertyValue(
				_rhpPrj,
				"BusinessValueProfile.General.IsShowProfileVersionCheckDialogs" );
		}
		
		return _isShowProfileVersionCheckDialogs;
	}
}

/**
 * Copyright (C) 2022  MBSE Training and Consulting Limited (www.executablembse.com)

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