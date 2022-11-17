package com.mbsetraining.sysmlhelper.businessvalueplugin;

import com.mbsetraining.sysmlhelper.common.BaseContext;
import com.telelogic.rhapsody.core.IRPClass;
import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPPackage;

public class BusinessValue_Context extends BaseContext {

	private static final String DEFAULT_CLASS_FULL_NAME = "- in BusinessValueProfile::_Templates";
	protected Boolean _isShowProfileVersionCheckDialogs;
	protected IRPClass _measuredByDefaultType;

	public final String BUSINESS_VALUE_NEW_TERM = "BusinessValue";
	public final static String NEEDS_NEW_TERM = "Needs";
	public final String TIER2_GOAL_NEW_TERM = "Tier2Goal";
	public final String TIER3_GOAL_NEW_TERM = "Tier3Goal";
	public final String METACLASS_FOR_MEASURED_BY = "Measured By";
	public final static String METACLASS_FOR_TIER_1_GOAL = "Tier 1 Goal";
	public final static String METACLASS_FOR_TIER_2_GOAL = "Tier 2 Goal";
	public final static String METACLASS_FOR_TIER_3_GOAL = "Tier 3 Goal";
	public final String VIEW_PREFIX = "view - ";
	public final String VIEWPOINT_PREFIX = "viewpoint - ";
	public final String CUSTOMERVIEW_PREFIX = "customv - ";
	public final String QUERY_PREFIX = "query - ";
	public final String VIEWPOINT_DIAGRAM_PREFIX = "vvd - ";
	public final String VIEW_STRUCTURE_STEREOTYPE = "ViewStructure";
	public final String CUSTOMV_TBD = "customv - TBD";
	public final String VIEW_TBD = "view - TBD";
	public final String CUSTOMV_TBD_EXPLICIT_ONLY = "customv - TBD explicit only";
	public final String VIEWPOINT_TBD = "viewpoint - TBD";
	public final String VIEW_AND_VIEWPOINT_DIAGRAM_TBD = "vvd - TBD";
	public final String QUERY_TBD = "query - TBD";
	public final String QUERYTBD_EXPLICIT_ONLY = "query - TBD explicit only";
	public final String STEREOTYPE_TBD = "TBD";

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