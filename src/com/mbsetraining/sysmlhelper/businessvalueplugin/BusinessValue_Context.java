package com.mbsetraining.sysmlhelper.businessvalueplugin;

import com.mbsetraining.sysmlhelper.common.BaseContext;
import com.telelogic.rhapsody.core.IRPClass;
import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPPackage;

public class BusinessValue_Context extends BaseContext {

	private static final String DEFAULT_CLASS_FULL_NAME = "- in BusinessValueProfile::_Templates";
	protected Boolean _isShowProfileVersionCheckDialogs;
	protected IRPClass _measuredByDefaultType;

	public final static String BUSINESS_VALUE_NEW_TERM = "BusinessValue";
	public final static String NEEDS_NEW_TERM = "Needs";
	public final static String TIER1_GOAL_USER_DEFINIED_TYPE = "Tier 1 Goal";
	public final static String TIER2_GOAL_USER_DEFINIED_TYPE = "Tier 2 Goal";
	public final static String TIER3_GOAL_USER_DEFINIED_TYPE = "Tier 3 Goal";
	public final static String TIER2_GOAL_NEW_TERM = "Tier2Goal";
	public final static String TIER3_GOAL_NEW_TERM = "Tier3Goal";
	
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
	
	public IRPClass getMeasuredByDefaultClass(){
		
		if( _measuredByDefaultType == null ){
			
			IRPModelElement theClassEl = _rhpPrj.
					findElementsByFullName( DEFAULT_CLASS_FULL_NAME, "Class" );
			
			if( theClassEl instanceof IRPClass ){
				_measuredByDefaultType = (IRPClass) theClassEl;
			} else {
				warning( "getMeasuredByDefaultClass was unable to find " + DEFAULT_CLASS_FULL_NAME );
			}
		}
		
		return _measuredByDefaultType;
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