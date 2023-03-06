package com.mbsetraining.sysmlhelper.modelchecks;

import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.IRPCollection;
import com.telelogic.rhapsody.core.IRPExternalCheckRegistry;
import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPRequirement;
import com.telelogic.rhapsody.core.RPExternalCheck;

public class CheckForRequirementCFLRChars extends RPExternalCheck {

	private ExecutableMBSE_Context _context;

	public CheckForRequirementCFLRChars(
			IRPExternalCheckRegistry externalCheckerRegistry,
			ExecutableMBSE_Context context ){
		
		_context = context;
		connect( externalCheckerRegistry );
	}
	
	public boolean check(
			IRPModelElement elementToCheck,
			IRPCollection failedElements ){
		
		boolean isOk = true;
		
		// Check if requirement has Line Feed or Carriage Return chars in its specification
		if( elementToCheck instanceof IRPRequirement ){
			
			IRPRequirement req = (IRPRequirement) elementToCheck;
			
			String theSpecification = req.getSpecification();
			
			if( theSpecification.contains( "\r" ) ||
					theSpecification.contains( "\n" ) ||
					theSpecification.contains( ";" ) ) {
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
		return "Requirement has line feed, carriage return or ; chars in its specification, hence won't export to csv well";
	}

	public String getSeverity() {
		return "Warning";
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