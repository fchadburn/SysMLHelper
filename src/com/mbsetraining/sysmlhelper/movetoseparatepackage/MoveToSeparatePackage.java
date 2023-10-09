package com.mbsetraining.sysmlhelper.movetoseparatepackage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class MoveToSeparatePackage {

	protected ExecutableMBSE_Context _context;
	protected String _postFix;
	protected String _userDefinedMetaClassToMove;
	protected String _newTermPackageType;
	
	public MoveToSeparatePackage(
			ExecutableMBSE_Context context,
			String theUserDefinedMetaClassToMove,
			String theNewTermPackageType,
			String thePostFix ) {
		
		_context = context;
		_userDefinedMetaClassToMove = theUserDefinedMetaClassToMove;
		_postFix = thePostFix;
		_newTermPackageType = theNewTermPackageType;
	}
	
	public void performMoveIfConfirmed() {
		
		List<IRPModelElement> theElsToMove = new ArrayList<>();

		List<IRPModelElement> theSelectedEls = _context.getSelectedElements();
		List<IRPGraphElement> theSelectedGraphEls = _context.getSelectedGraphElements();
		
		Set<IRPModelElement> theCandidateEls = 
				_context.getSetOfElementsFromCombiningThe( 
						theSelectedEls, theSelectedGraphEls );

		for( IRPModelElement theCandidateEl : theCandidateEls ){

			if( theCandidateEl.getUserDefinedMetaClass().equals( _userDefinedMetaClassToMove ) ){
				theElsToMove.add( theCandidateEl );
			}
		}

		if( theElsToMove.isEmpty() ){

			_context.warning( "There were no selected " + _userDefinedMetaClassToMove + "s. \n" + 
					"Right-click a " + _userDefinedMetaClassToMove + " and try again");

		} else {

			String theMsg = "Do you want to move the " + theElsToMove.size() + " selected " + 
					_userDefinedMetaClassToMove + "s \ninto their own packages? \n";

			boolean answer = UserInterfaceHelper.askAQuestion( theMsg );

			if( answer ){
				moveToNewPackages( theElsToMove, true );
			}
		}
	}
	
	private void moveToNewPackages(
			List<IRPModelElement> theEls,
			boolean isNestedBelow ){
	
		for( IRPModelElement theEl : theEls ){
			moveToNewPackage( theEl, isNestedBelow );
		}
	}
	
	private void moveToNewPackage(
			IRPModelElement theEl,
			boolean isNestedBelow ){
		
		String theOriginalName = theEl.getName();
		
		String theCamelCaseName = _context.toLegalClassName( theOriginalName );
		
		IRPPackage theOwningPkg;
		
		if( isNestedBelow ){
			theOwningPkg = (IRPPackage) _context.getOwningPackageFor( theEl );
		} else {
			theOwningPkg = (IRPPackage) _context.getOwningPackageFor( theEl ).getOwner();
		}

		String theUniqueName = _context.determineUniqueNameForPackageBasedOn( 
				theCamelCaseName, theOwningPkg) + _postFix;
		
		_context.debug( "Moving " + _context.elInfo( theEl ) + " to " + theUniqueName );

		IRPPackage theNewPkg = theOwningPkg.addNestedPackage( theUniqueName );
		theNewPkg.changeTo( _newTermPackageType );
		
		theNewPkg.highLightElement();
		
		theEl.setOwner( theNewPkg );	
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