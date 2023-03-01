package com.mbsetraining.sysmlhelper.modelchecks;

import java.util.List;

import com.telelogic.rhapsody.core.IRPCollection;
import com.telelogic.rhapsody.core.IRPExternalCheckRegistry;
import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPRequirement;
import com.telelogic.rhapsody.core.RPExternalCheck;

public class CheckForRequirementChildren extends RPExternalCheck {

	public CheckForRequirementChildren(
			IRPExternalCheckRegistry externalCheckerRegistry ){
		
		connect( externalCheckerRegistry );
	}
	
	public boolean check(
			IRPModelElement ElementToCheck,
			IRPCollection FailedElements ){
		
		boolean isOk = true;
		
		// Check if requirement is using anchors as we don't want this
		if( ElementToCheck instanceof IRPRequirement ){
			
			IRPRequirement req = (IRPRequirement) ElementToCheck;
			
			@SuppressWarnings("unchecked")
			List<IRPModelElement> theChildren = req.getNestedElements().toList();
			
			if( !theChildren.isEmpty() ) {
				isOk = false;
			}
		}
		
		return isOk;
	}	

	public String getDomain() {
		return "ExecutableMBSE Profile";
	}

	public String getMetaclasses() {
		return "Requirement";
	}

	public String getName() {
		return "Requirement has child elements";
	}

	public String getSeverity() {
		return "Info";
	}

	public boolean getShouldCallFromCG() {
		return true;
	}

	public void doExit() {
		disconnect();
	}

	public boolean getCompleteness() {
		return true;
	}
}

/**
 * Copyright (C) 2023  MBSE Training and Consulting Limited (www.executablembse.com)

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