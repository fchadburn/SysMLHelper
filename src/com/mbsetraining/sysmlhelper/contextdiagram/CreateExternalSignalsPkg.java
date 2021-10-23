package com.mbsetraining.sysmlhelper.contextdiagram;

import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class CreateExternalSignalsPkg {
	
	protected IRPPackage _externalSignalPkg;
	protected ExecutableMBSE_Context _context;
	
	public enum CreateExternalSignalsPkgOption {
		DoNothing,
	    CreateNewButEmpty,
	    UseExisting
	}
	
	CreateExternalSignalsPkg(
			CreateExternalSignalsPkgOption theCreatePkgOption,
			IRPPackage thePkgOwner,
			String thePkgName,
			IRPPackage theFlowFromPkg,
			IRPPackage theOptionalExistingPkg,
			ExecutableMBSE_Context theContext ){
		
		_context = theContext;
		
		if( theCreatePkgOption == CreateExternalSignalsPkgOption.CreateNewButEmpty ){

			_externalSignalPkg = createExternalSignalsPackage( 
					thePkgOwner, 
					thePkgName );

		} else if( theCreatePkgOption == CreateExternalSignalsPkgOption.UseExisting ){
			
			_externalSignalPkg = theOptionalExistingPkg;
		}
		
		theFlowFromPkg.addDependencyTo( _externalSignalPkg );
	}

	private IRPPackage createExternalSignalsPackage(
			IRPPackage underThePackage,
			String withTheName ){
		
		IRPPackage thePkg = underThePackage.addNestedPackage( withTheName );
		thePkg.changeTo( _context.REQTS_ANALYSIS_EXTERNAL_SIGNALS_PACKAGE );
		_context.setSavedInSeparateDirectoryIfAppropriateFor( thePkg );
		
		return thePkg;
	}
}

/**
 * Copyright (C) 2021  MBSE Training and Consulting Limited (www.executablembse.com)

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