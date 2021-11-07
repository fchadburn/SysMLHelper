package com.mbsetraining.sysmlhelper.common;

import java.util.List;
import java.util.Set;

import com.telelogic.rhapsody.core.*;

public class ElementMover {

	final protected IRPPackage _moveToPkg;
	final protected String _whereMoveToHasStereotype;
	final protected BaseContext _context;
	
	public ElementMover(
			IRPModelElement basedOnEl,
			String whereMoveToHasStereotype,
			BaseContext context ) {
		
		_context = context;
		_whereMoveToHasStereotype = whereMoveToHasStereotype;
		_moveToPkg = determineMoveToPackage( basedOnEl );
		
		_context.debug( "_moveToPkg for " + whereMoveToHasStereotype + " is " + _context.elInfo( _moveToPkg ) );
	}
	
	protected IRPPackage determineMoveToPackage( 
			IRPModelElement basedOnEl ){
		
		IRPPackage theMoveToPkg = null;
		
		Set<IRPModelElement> theCandidateEls =
				_context.getStereotypedElementsThatHaveDependenciesFrom( 
						basedOnEl, 
						_whereMoveToHasStereotype );
		
		_context.debug( theCandidateEls.size() + " theCandidateEls were found for " + _context.elInfo( basedOnEl ) );

		if( theCandidateEls.size()==1 ){
			
			IRPModelElement theCandidate = null;
			
			for( IRPModelElement theCandidateEl : theCandidateEls ){
				theCandidate = theCandidateEl;
			}
			
			if( theCandidate instanceof IRPPackage ){
				theMoveToPkg = (IRPPackage) theCandidate;
			}
			
		} else if( theCandidateEls.size()==0 ){
			
			IRPModelElement theOwner = basedOnEl.getOwner();
				
			if( theOwner instanceof IRPClassifier &&
					theOwner.getName().equals( "TopLevel" ) ){
					
				_context.debug( "TopLevel is " + _context.elInfo( theOwner ) );
				
				@SuppressWarnings("unchecked")
				List<IRPPackage> thePkgs = _context.get_rhpPrj().getNestedElementsByMetaClass( "Package", 1 ).toList();
				
				for( IRPPackage thePkg : thePkgs ){
					
					@SuppressWarnings("unchecked")
					List<IRPInstance> theGlobalObjects = thePkg.getGlobalObjects().toList();
					
					if( theGlobalObjects.contains( basedOnEl ) ){
						
						_context.debug( "The global object is in " + _context.elInfo( thePkg ) );
						theOwner = thePkg;
					}
				}
			}
						
			if( !( theOwner instanceof IRPProject ) ){
				
				theMoveToPkg = determineMoveToPackage( theOwner );

			} else {
				// Unable to find a matching package in corresponding ownership tree
			}
		}
		
		// Don't move to itself
		if( theMoveToPkg != null &&
				theMoveToPkg.equals( basedOnEl.getOwner() ) ){
			
			theMoveToPkg = null;
		}
		
		return theMoveToPkg;
	}
	
	public boolean isMovePossible(){
	
		return _moveToPkg != null;
	}
	
	public IRPModelElement get_moveToPkg(){
		
		return _moveToPkg;
	}
	
	public boolean performMove(
			IRPModelElement ofElement ){

		boolean isSuccess = false;

		if( _moveToPkg != null ){

			// check if already element of same name
			IRPModelElement alreadyExistingEl = 
					_moveToPkg.findNestedElement( 
							ofElement.getName(),
							ofElement.getMetaClass() );

			if( alreadyExistingEl != null ){

				String uniqueName = _context.determineUniqueNameBasedOn( 
						ofElement.getName(), 
						ofElement.getMetaClass(), 
						_moveToPkg );

				_context.info( "Same name as " + _context.elInfo( ofElement ) 
						+ " already exists under " + _context.elInfo( _moveToPkg ) + 
						", hence element was renamed to " + uniqueName );

				ofElement.setName( uniqueName );
			}

			_context.info( "Moving " + _context.elInfo( ofElement ) + 
					" to " + _context.elInfo( _moveToPkg ) );

			ofElement.getProject().save();
			ofElement.setOwner( _moveToPkg );

			isSuccess = true;
			
			ofElement.highLightElement();
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