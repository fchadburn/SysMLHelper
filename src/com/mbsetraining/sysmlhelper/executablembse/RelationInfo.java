package com.mbsetraining.sysmlhelper.executablembse;

import com.mbsetraining.sysmlhelper.common.BaseContext;
import com.telelogic.rhapsody.core.*;

public class RelationInfo {
	
	private DiagramElementInfo _startElInfo;
	private DiagramElementInfo _endElInfo;
	private IRPStereotype _relationStereotype;
	private BaseContext _context;
	
	public RelationInfo(
			DiagramElementInfo fromStartElement,
			DiagramElementInfo toEndElement, 
			IRPStereotype withRelationType,
			BaseContext context ){
				
		_context = context;
		
		this._startElInfo = fromStartElement;
		this._endElInfo = toEndElement;
		this._relationStereotype = withRelationType;
	}
	
	public DiagramElementInfo getStartElement() {
		return _startElInfo;
	}
	
	public DiagramElementInfo getEndElement() {
		return _endElInfo;
	}
	
	public IRPStereotype getRelationType() {
		return _relationStereotype;
	}
	
	public int getExistingCount(){
		
		return _context.countStereotypedDependencies(
				_startElInfo.getElement(),
				_endElInfo.getElement(),
				_relationStereotype.getName() );
	}
	
	public IRPDependency getExistingStereotypedDependency(){
		
		return _context.getExistingStereotypedDependency(
				_startElInfo.getElement(),
				_endElInfo.getElement(),
				_relationStereotype.getName() );
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
