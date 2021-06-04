package com.mbsetraining.sysmlhelper.common;

import java.util.Set;

import com.telelogic.rhapsody.core.*;

public class ElementMover {

	protected IRPModelElement _element;
	protected IRPPackage _moveToPkg;
	protected String _whereMoveToHasStereotype;
	protected ConfigurationSettings _context;

	public ElementMover(
			IRPModelElement theElement,
			String whereMoveToHasStereotype,
			ConfigurationSettings context ) {
		
		_context = context;
		_element = theElement;
		_whereMoveToHasStereotype = whereMoveToHasStereotype;
		_moveToPkg = getMoveToPackage( theElement );
	}
	
	protected IRPPackage getMoveToPackage( 
			IRPModelElement basedOnEl ){
		
		IRPPackage theMoveToPkg = null;
		
		Set<IRPModelElement> theCandidateEls =
				_context.getStereotypedElementsThatHaveDependenciesFrom( 
						basedOnEl, 
						_whereMoveToHasStereotype );
		
		if( theCandidateEls.size()==1 ){
			
			IRPModelElement theCandidate = null;
			
			for (IRPModelElement theCandidateEl : theCandidateEls) {
				theCandidate = theCandidateEl;
			}
			
			if( theCandidate instanceof IRPPackage ){
				theMoveToPkg = (IRPPackage) theCandidate;
			}
			
		} else if( theCandidateEls.size()==0 ){
			
			IRPModelElement theOwner = basedOnEl.getOwner();
			
			if( !(theOwner instanceof IRPProject) ){
				
				theMoveToPkg = getMoveToPackage( 
						basedOnEl.getOwner() );

			} else {
				// Unable to find a matching package in corresponding ownership tree
			}
		}
		
		return theMoveToPkg;
	}
	
	public boolean performMove(){

		boolean isSuccess = false;

		if( _moveToPkg != null && 
			!_moveToPkg.equals( _element.getOwner() ) ){

			// check if already element of same name
			IRPModelElement alreadyExistingEl = 
					_moveToPkg.findNestedElement( 
							_element.getName(),
							_element.getMetaClass() );

			if( alreadyExistingEl != null ){

				String uniqueName = _context.determineUniqueNameBasedOn( 
						_element.getName(), 
						_element.getMetaClass(), 
						_moveToPkg );

				_context.info( "Same name as " + _context.elInfo( _element ) 
						+ " already exists under " + _context.elInfo( _moveToPkg ) + 
						", hence element was renamed to " + uniqueName );

				_element.setName( uniqueName );
			}

			_context.info( "Moving " + _context.elInfo( _element ) + 
					" to " + _context.elInfo( _moveToPkg ) );

			_element.getProject().save();
			_element.setOwner( _moveToPkg );

			isSuccess = true;
			
			_element.highLightElement();
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