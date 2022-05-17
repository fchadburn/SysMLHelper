package com.mbsetraining.sysmlhelper.populateparts;

import java.util.ArrayList;
import java.util.List;

import com.mbsetraining.sysmlhelper.common.BaseContext;
import com.telelogic.rhapsody.core.*;

public class ModelElInfo {

	public IRPClassifier _classifier;
	public IRPInstance _part;
	public boolean _isSilent;
	private BaseContext _context;

	public ModelElInfo(
			IRPClassifier theClassifier,
			IRPInstance thePart,
			boolean isSilent,
			BaseContext context ){

		_context = context;
		_classifier = theClassifier;
		_part = thePart;
		_isSilent = isSilent;
	}

	public String toString(){

		String theString = "";

		if( _part != null ){
			theString += _part.getName();
		}

		theString += ":";

		theString += _classifier.getName();

		if( _part != null ){
			theString += " (" + _part.getUserDefinedMetaClass();
		} else {
			theString += " (" + _classifier.getUserDefinedMetaClass();
		}

		if( _isSilent ){
			theString += " - BASE TYPE";
		}

		theString += ")";

		return theString;
	}

	public List<ModelElInfo> getChildren(
			boolean isIncludeTypeless ){

		List<ModelElInfo> theChildren = getChildrenWhichAreParts( isIncludeTypeless );

		@SuppressWarnings("unchecked")
		List<IRPGeneralization> theGeneralizations = _classifier.getGeneralizations().toList();

		for (IRPGeneralization theGeneralization : theGeneralizations) {
			IRPClassifier theClassifier = theGeneralization.getBaseClass();
			theChildren.add( new ModelElInfo( theClassifier, null, true, _context ) );
		}

		return theChildren;
	}

	private List<ModelElInfo> getChildrenWhichAreParts(
			boolean isIncludeTypeless ){

		List<ModelElInfo> theChildren = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theObjectEls = _classifier.getNestedElementsByMetaClass( 
				"Object", 0 ).toList();

		for( IRPModelElement theObjectEl : theObjectEls ){

			// Don't add ports or implicit classes, i.e. objects with no type
			if( !( theObjectEl instanceof IRPPort ) &&
					!( theObjectEl instanceof IRPSysMLPort ) ){
				
				if( isIncludeTypeless ||
						((IRPRelation) theObjectEl).isTypelessObject()==0 ){

					IRPInstance theObject = (IRPInstance)theObjectEl;
					IRPClassifier theOtherClass = theObject.getOtherClass();
					ModelElInfo theChild = new ModelElInfo( theOtherClass, theObject, false, _context );
					theChildren.add( theChild );					
				}
			}
		}

		return theChildren;
	}
}

/**
 * Copyright (C) 2018-2022  MBSE Training and Consulting Limited (www.executablembse.com)

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