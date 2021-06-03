package com.mbsetraining.sysmlhelper.executablembse;

import java.util.ArrayList;
import java.util.List;

import com.telelogic.rhapsody.core.*;

public class HierarchyHelper {

	private List<IRPInstance> _instances = new ArrayList<>();
	private ExecutableMBSE_Context _context;
	
	public HierarchyHelper(
			IRPModelElement basedOnRoot,
			ExecutableMBSE_Context context ){
		
		_context = context;
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theCandidateParts = 
				basedOnRoot.getNestedElementsByMetaClass( 
						"Instance", 1 ).toList();
		
		for( IRPModelElement theCandidatePart : theCandidateParts ){
			
			if( !( theCandidatePart instanceof IRPPort ) &&
				!( theCandidatePart instanceof IRPModule ) &&
				!( theCandidatePart instanceof IRPSysMLPort ) ){
				
				_instances.add( (IRPInstance) theCandidatePart );
			}
		}
	}
	
	public IRPClassifier getRootClassifierForPort(
			IRPPort thePort ){
		
		IRPClassifier theRootClassifier = null;
		
		IRPModelElement thePortsOwner = thePort.getOwner();
		
		if( thePortsOwner instanceof IRPClass ){
			
			List<IRPInstance> theInstances = getAllPartsInProject(
					(IRPClassifier) thePortsOwner );
			
			int size = theInstances.size();
			
			if( size == 1 ){
				
				IRPInstance theInstance = theInstances.get( 0 );
				
				List<IRPInstance> theOwningParts = 
						getOwningPartsFor( theInstance );
				
				for (IRPInstance theOwningPart : theOwningParts) {
					_context.debug( _context.elInfo( theOwningPart ) + " is a part" );
				}
				
				IRPInstance theRootInstance = 
						theOwningParts.get( theOwningParts.size()-1 );
				
				theRootClassifier = theRootInstance.getOtherClass();
			}
		}
		
		return theRootClassifier;
	}
	
	public List<IRPInstance> getOwningPartsFor(
			IRPInstance theInstance ){
		
		_context.debug("getOwningPartsFor invoked for " + _context.elInfo( theInstance ) );
		
		List<IRPInstance> theOwningParts = new ArrayList<>();
	
		try {
			
			theOwningParts.add( theInstance );
			
			IRPModelElement theOwner = theInstance.getOwner();
			
			if( theOwner instanceof IRPClass ){
				
				IRPClass theClass = (IRPClass) theOwner;
				
				List<IRPInstance> thePartsOfOtherClass = 
						getAllPartsInProject( theClass );
				
				if( thePartsOfOtherClass.size() == 1 ){
					
					IRPInstance thePart = thePartsOfOtherClass.get( 0 );	
				
					_context.debug( _context.elInfo( thePart) + " is the part");
					
						List<IRPInstance> theOwningPartsFor = 
								getOwningPartsFor( thePart );
					
					if( theOwningPartsFor != null ){
						theOwningParts.addAll( theOwningPartsFor );

					}
					
				} else if( thePartsOfOtherClass.size() > 1 ){
					_context.error("Error in getOwningPartsFor, found " + thePartsOfOtherClass.size() + 
							" parts typed by " + _context.elInfo( theClass ) + " when expecting one" );
				}
			}		
			
		} catch (Exception e) {
			_context.error("Execption e=" + e.getMessage());
		}
		
		return theOwningParts;
	}
	
	public List<IRPInstance> getAllPartsInProject(
			IRPClassifier typedByClassifier ){
		
		List<IRPInstance> theParts = new ArrayList<>();

		for( IRPInstance theCandidatePart : _instances ){

			_context.debug( "Checking theCandidatePart = " + _context.elInfo( theCandidatePart ) );
			
			if( theCandidatePart.getOtherClass().equals( 
					typedByClassifier) ){
				
				_context.debug("getAllParts typed by " + _context.elInfo( typedByClassifier ) + 
						" found " + _context.elInfo( theCandidatePart ) );
				
				theParts.add( theCandidatePart );
			}
		}

		_context.debug( "getAllPartsInProject is returning " + theParts.size() + 
				" parts for " + _context.elInfo( typedByClassifier ) );
		
		return theParts;
	}
	
	public List<IRPInstance> getAllPartsOwnedByPackages(){
		
		List<IRPInstance> theParts = new ArrayList<>();

		for( IRPInstance theCandidatePart : _instances ){
			
			IRPModelElement theOwner = theCandidatePart.getOwner();
			
			_context.debug( _context.elInfo( theCandidatePart ) + " has owner " + _context.elInfo( theOwner) );
			
			if( theOwner instanceof IRPClass && 
				theOwner.getName().equals( "TopLevel" ) &&
				!theParts.contains( theCandidatePart )){
				
				_context.debug("getAllPartsOwnedByPackages found " + _context.elInfo( theCandidatePart ) );
				theParts.add( theCandidatePart );
			}
		}

		_context.debug( "getAllPartsOwnedByPackages is returning " + theParts.size() + " parts" );
		
		return theParts;
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