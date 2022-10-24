package com.mbsetraining.sysmlhelper.executablembse;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.telelogic.rhapsody.core.*;

public class DiagramElementInfo {

	private IRPModelElement _element = null;
	private Set<IRPGraphElement> _graphEls = new HashSet<IRPGraphElement>();

	public IRPModelElement getElement() {
		return _element;
	}
	
	public DiagramElementInfo(
			IRPModelElement theModelEl,
			List<IRPGraphElement> theGraphEls ){
		
		_element = theModelEl;
		
		for( IRPGraphElement theCandidateGraphEl : theGraphEls ){
			
			IRPModelElement theModelObject = theCandidateGraphEl.getModelObject();
			
			if( theModelObject != null &&
					theCandidateGraphEl.getModelObject().equals( theModelEl ) ){
				
				_graphEls.add( theCandidateGraphEl );
			}
		}
	}
	
	public boolean isThereAGraphElement(){
		
		return !_graphEls.isEmpty();
		
	}
	
	public Set<IRPGraphElement> getGraphEls(){
		
		return _graphEls;
	}
	
}

/**
 * Copyright (C) 2017-2022  MBSE Training and Consulting Limited (www.executablembse.com)

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
