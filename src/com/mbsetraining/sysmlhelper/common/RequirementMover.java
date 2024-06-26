package com.mbsetraining.sysmlhelper.common;

import java.util.List;

import com.telelogic.rhapsody.core.*;

public class RequirementMover extends ElementMover {

	private List<IRPStereotype> _moveToStereotypes = null;
	
	public RequirementMover(
			IRPModelElement theElement,
			String whereMoveToHasStereotype,
			BaseContext context ){
		
		super( theElement, whereMoveToHasStereotype, context );
		
		_moveToStereotypes = _context.getMoveToStereotypes( _newOwner );
	}

	public boolean performMove(
			IRPModelElement theElement ){
		
		boolean isSuccess = super.performMove( theElement );
		
		if( isSuccess ){
			
			if( !_moveToStereotypes.isEmpty() ){
				
				for( IRPStereotype _moveToStereotype : _moveToStereotypes ){
					
					try {
						theElement.setStereotype( _moveToStereotype );
						
					} catch( Exception e ){
						_context.error( "Error in RequirementsMover.performMove, " +
								"unable exception trying to apply " + 
								_context.elInfo( _moveToStereotype ) + 
								" to " + _context.elInfo( theElement ) );
					}
				}
			}
		}
		
		return isSuccess;
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