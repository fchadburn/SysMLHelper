package com.mbsetraining.sysmlhelper.taumigratorplugin;

public abstract class RhpElElement extends RhpEl {

	public RhpElElement(
			String theElementName, 
			String theElementType,
			String theElementGuid,
			TauMigrator_Context context ) throws Exception{
		
		super( theElementName, theElementType, theElementGuid, context );
	}

	public RhpElElement(
			String theElementName, 
			String theElementType,
			String theElementGuid,
			RhpEl theParent,
			TauMigrator_Context context ) throws Exception{
		
		super( theElementName, theElementType, theElementGuid, theParent, context );
	}
}

/**
 * Copyright (C) 2018-2021  MBSE Training and Consulting Limited (www.executablembse.com)

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