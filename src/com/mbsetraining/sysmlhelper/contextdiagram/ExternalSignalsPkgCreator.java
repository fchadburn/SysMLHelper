package com.mbsetraining.sysmlhelper.contextdiagram;

import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class ExternalSignalsPkgCreator {
	
	protected IRPPackage _externalSignalPkg;
	protected ExecutableMBSE_Context _context;
	
	public ExternalSignalsPkgCreator(
			ExecutableMBSE_Context theContext ){
		
		_context = theContext;	
	}

	public IRPPackage createExternalSignalsPackage(
			IRPPackage underThePackage,
			String withTheName ){
		
		IRPPackage thePkg = underThePackage.addNestedPackage( withTheName );
		thePkg.changeTo( _context.REQTS_ANALYSIS_EXTERNAL_SIGNALS_PACKAGE );
		_context.setSavedInSeparateDirectoryIfAppropriateFor( thePkg );
		
		return thePkg;
	}
}

/**
 * Copyright (C) 2021-2023  MBSE Training and Consulting Limited (www.executablembse.com)

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