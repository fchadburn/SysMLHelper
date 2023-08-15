package com.mbsetraining.sysmlhelper.doorsng;

import java.util.HashMap;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.IRPRequirement;

public class RequirementToLinkInfosMap extends HashMap<IRPRequirement, LinkInfos> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6960458316324022559L;

	protected ExecutableMBSE_Context _context;
	
	public RequirementToLinkInfosMap(
			ExecutableMBSE_Context context ){
		
		_context = context;
	}
	
	public void dumpInfo(){
		
		for( Entry<IRPRequirement, LinkInfos>  entry : this.entrySet() ){
			
			LinkInfos theValue = entry.getValue();	
			theValue.dumpInfo();
		}
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