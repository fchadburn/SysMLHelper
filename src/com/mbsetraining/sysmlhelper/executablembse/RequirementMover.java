package com.mbsetraining.sysmlhelper.executablembse;

import java.util.List;

import com.telelogic.rhapsody.core.*;

public class RequirementMover extends ElementMover {

	private IRPStereotype _moveToStereotype = null;
	
	public RequirementMover(
			IRPModelElement theElement,
			ExecutableMBSE_Context context ){
		
		super( theElement, context.getRequirementPackageStereotype( theElement ), context );
		
		_moveToStereotype = getMoveToStereotype( _moveToPkg );
	}
	
	private IRPStereotype getMoveToStereotype( 
			IRPPackage basedOnPackage ){
		
		IRPStereotype theMoveToStereotype = null;
		
		if( basedOnPackage != null ){

			@SuppressWarnings("unchecked")
			List<IRPStereotype> theStereotypes = basedOnPackage.getStereotypes().toList();
			
			for( IRPStereotype theStereotype : theStereotypes ){
				
				if( theStereotype.getName().startsWith( "from" ) ){
					theMoveToStereotype = theStereotype;
					_context.debug( "Found move to " + _context.elInfo( theStereotype ) );
					break;
				}
			}
		}
		
		return theMoveToStereotype;
	}

	public boolean performMove(){
		
		boolean isSuccess = super.performMove();
		
		if( isSuccess ){
			
			if( _moveToStereotype != null ){
				try {
					_element.setStereotype( _moveToStereotype );
					
				} catch( Exception e ){
					_context.error( "Error in RequirementsMover.performMove, " +
							"unable exception trying to apply " + 
							_context.elInfo( _moveToStereotype ) + 
							" to " + _context.elInfo( _element ) );
				}
			}
		}
		
		return isSuccess;
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