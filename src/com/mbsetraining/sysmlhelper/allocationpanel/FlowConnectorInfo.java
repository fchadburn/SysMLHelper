package com.mbsetraining.sysmlhelper.allocationpanel;

import java.util.ArrayList;
import java.util.List;

import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class FlowConnectorInfo {
	
	protected ExecutableMBSE_Context _context;
	protected FunctionAllocationMap _functionAllocationMap;
	protected IRPLink _srcLink;
	protected IRPInstance _srcFromInstance;
	protected IRPInstance _srcToInstance;
	protected IRPSysMLPort _srcFromPort;
	protected IRPSysMLPort _srcToPort;

	protected IRPLink _tgtLink;
	protected List<IRPInstance> _tgtFromInstances;
	protected List<IRPInstance> _tgtToInstances;
	protected IRPClassifier _tgtCommonOwner;
	protected List<IRPLink> _tgtLinks;
	protected IRPSysMLPort _tgtFromPort;
	protected IRPSysMLPort _tgtToPort;
	
	public FlowConnectorInfo(
			IRPLink theLink,
			FunctionAllocationMap functionAllocationMap,
			ExecutableMBSE_Context context ){
		
		_context = context;
		_functionAllocationMap = functionAllocationMap;
		_srcLink = theLink;
		_srcFromInstance = _srcLink.getFrom();
		_srcToInstance = _srcLink.getTo();
		_srcFromPort = _srcLink.getFromSysMLPort();
		_srcToPort = _srcLink.getToSysMLPort();
	}
	
	public void dumpInfo() {
		
		_context.info( "_srcLink         = " + _context.elInfo( _srcLink ) );
		_context.info( "_srcFromInstance = " + _context.elInfo( _srcFromInstance ) );
		_context.info( "_srcToInstance   = " + _context.elInfo( _srcToInstance ) );
		_context.info( "_srcFromPort     = " + _context.elInfo( _srcFromPort ) );
		_context.info( "_srcToPort       = " + _context.elInfo( _srcToPort ) );
	}
	
	public boolean isMappable() {
		
		boolean isMappable = true;
		
		if( !_functionAllocationMap.containsKey( _srcFromInstance ) ) {
			_context.info( "Unable to map " + _context.elInfo( _srcLink ) + " as " + _context.elInfo( _srcFromInstance ) + " is not in mapping" );
			isMappable = false;
		}
		
		if( !_functionAllocationMap.containsKey( _srcToInstance ) ) {
			_context.info( "Unable to map " + _context.elInfo( _srcLink ) + " as " + _context.elInfo( _srcToInstance ) + " is not in mapping" );
			isMappable = false;
		}
		
		return isMappable;
	}
	
	public boolean performMapping() {
		
		_context.info( "performMapping... " ); 

		boolean isMappable = true;
		
		if( isMappable ) {
			
			_tgtFromInstances = _functionAllocationMap.get( _srcFromInstance )._validAllocatedUsages;
			_tgtToInstances = _functionAllocationMap.get( _srcToInstance )._validAllocatedUsages;

			if (_tgtFromInstances.size() == 1 && 
				_tgtToInstances.size() == 1 ) {
				
				IRPInstance fromInstance = _tgtFromInstances.get( 0 );
				_context.info( "fromInstance  = " + _context.elInfo( fromInstance ) + " owned by " + _context.elInfo( fromInstance.getOwner() ) );

				IRPInstance toInstance = _tgtToInstances.get( 0 );
				_context.info( "toInstance    = " + _context.elInfo( toInstance ) + " owned by " + _context.elInfo( toInstance.getOwner() ) );
				
				_tgtCommonOwner = findCommonOwnerFor( fromInstance, toInstance );
				_context.info( "_tgtCommonOwner    = " + _context.elInfo( _tgtCommonOwner ) );
				
				if( fromInstance.isTypelessObject() == 0 ) {
					_tgtFromPort = _srcFromPort;
				} else {
					List<IRPModelElement> thePorts = _context.
							findElementsWithMetaClassAndName( 
									"SysMLPort", _srcFromPort.getName(), fromInstance.getOtherClass() );
					
					if( thePorts.size() == 1 ) {
						_tgtFromPort = (IRPSysMLPort) thePorts.get( 0 );
					}
				}
				
				if( toInstance.isTypelessObject() == 0 ) {
					_tgtToPort = _srcToPort;
				} else {
					List<IRPModelElement> thePorts = _context.
							findElementsWithMetaClassAndName( 
									"SysMLPort", _srcToPort.getName(), toInstance.getOtherClass() );
					
					if( thePorts.size() == 1 ) {
						_tgtToPort = (IRPSysMLPort) thePorts.get( 0 );
					}
				}
				
				if( _tgtCommonOwner.equals( fromInstance.getOwner() ) &&
						_tgtCommonOwner.equals( toInstance.getOwner() )) {
					
					_context.info( "Create flow connector between: " );
					
					_context.info( "_tgtFromPort  = " + _context.elInfo( _tgtFromPort ) + " owned by " + _context.elInfo( _tgtFromPort.getOwner() ) );
					_context.info( "_tgtToPort    = " + _context.elInfo( _tgtToPort ) + " owned by " + _context.elInfo( _tgtToPort.getOwner() ) );
					
					_tgtLinks = getExistingLinks(fromInstance, _tgtFromPort, toInstance, _tgtToPort, _tgtCommonOwner);
					
					if( _tgtLinks.isEmpty() ) {
						
						try {
							IRPLink theLink = fromInstance.addLinkToElement( toInstance, null, _tgtFromPort, _tgtToPort );						

							_context.info( "Created " + _context.elInfo( theLink ) + "  owned by " + _context.elInfo( theLink.getOwner() ) );
							
							theLink.changeTo( _context.FLOW_CONNECTOR );
							
							_tgtLinks.add( theLink );
						} catch (Exception e) {
							_context.error( "Exception creating link, e=" + e.getMessage() );
						}

					}
				}
				
				_context.info( "...end performMapping" ); 

			}
			
			dumpInfo();
			
		}
		
		return isMappable;
	}
	
	private List<IRPLink> getExistingLinks(
			IRPInstance fromInstance, 
			IRPSysMLPort fromPort, 
			IRPInstance toInstance, 
			IRPSysMLPort toPort,
			IRPClassifier underClassifier ) {
		
		List<IRPLink> theExistingLinks = new ArrayList<>();
		
		@SuppressWarnings("unchecked")
		List<IRPLink> theCandidateLinks = underClassifier.getLinks().toList();
		
		for( IRPLink theCandidateLink : theCandidateLinks ){
			
			IRPSysMLPort fromCandidatePort = theCandidateLink.getFromSysMLPort();
			_context.info( "fromCandidatePort  = " + _context.elInfo( fromCandidatePort ) + " owned by " + _context.elInfo( fromCandidatePort.getOwner() ) );

			IRPSysMLPort toCandidatePort = theCandidateLink.getToSysMLPort();
			_context.info( "toCandidatePort  = " + _context.elInfo( toCandidatePort ) + " owned by " + _context.elInfo( toCandidatePort.getOwner() ) );

			IRPInstance fromCandidateInstance = theCandidateLink.getFrom();
			_context.info( "fromCandidateInstance  = " + _context.elInfo( fromCandidateInstance ) + " owned by " + _context.elInfo( fromCandidateInstance.getOwner() ) );

			IRPInstance toCandidateInstance = theCandidateLink.getTo();
			_context.info( "toCandidateInstance  = " + _context.elInfo( toCandidateInstance ) + " owned by " + _context.elInfo( toCandidateInstance.getOwner() ) );
			
			if( fromCandidatePort != null && fromCandidatePort.equals( fromPort ) &&
					toCandidatePort != null && toCandidatePort.equals( toPort ) &&
					fromCandidateInstance != null && fromCandidateInstance.equals( fromInstance ) &&
							toCandidateInstance != null && toCandidateInstance.equals( toInstance ) ) {
				
				theExistingLinks.add( theCandidateLink );
			}
		}
		
		return theExistingLinks;
	}
	
	private IRPClassifier findCommonOwnerFor(
			IRPInstance fromInstance,
			IRPInstance toInstance ){

		IRPClassifier theCommonOwner = null;
		
		List<IRPInstance> fromParts = getPartsList( fromInstance );
		List<IRPInstance> toParts = getPartsList( toInstance );

		if( fromParts != null && !fromParts.isEmpty() && toParts != null ){
			for( IRPInstance thePart : fromParts ){

				if( thePart != null ){

					IRPModelElement thePartOwner = thePart.getOwner();

					for( IRPInstance andThePart : toParts ){

						IRPModelElement andThePartOwner = andThePart.getOwner();

						if( andThePartOwner.equals( thePartOwner ) ){
							theCommonOwner = (IRPClassifier) thePartOwner;
							break;
						}
					}

					if( theCommonOwner != null ){
						break;
					}
				}
			}
		}

		return theCommonOwner;	
	}
	
	private List<IRPInstance> getPartsList( 
			IRPInstance forPart ){

		List<IRPInstance> theParts = new ArrayList<>();
		appendOwners( (IRPInstance) forPart, theParts );
		return theParts;
	}
	
	private void appendOwners(
			IRPInstance thePart, 
			List<IRPInstance> theParts ){

		if( thePart != null ){
			
			theParts.add( thePart );

			IRPModelElement theOwner = thePart.getOwner();

			if( theOwner instanceof IRPClassifier ){

				@SuppressWarnings("unchecked")
				List<IRPInstance> theCandidates = filterListToJustParts( 
						theOwner.getReferences().toList() );

				if( theCandidates.size() == 1 ){

					IRPInstance theOwningPart = theCandidates.get(0);				
					_context.debug( "a part for " + _context.elInfo( theOwner ) + " is " + _context.elInfo( theOwningPart ) );
					
					IRPClassifier theClassifier = theOwningPart.getOtherClass();
					
					if( theClassifier != null && 
							theClassifier.equals( theOwner ) ) {

						_context.debug( "Detected that " + _context.elInfo( thePart ) + 
								" is typed by its owner, hence not adding to prevent infinite recursion" );
					} else {						
						appendOwners( theOwningPart, theParts );
					}
				}				
			}
		}
	}
	
	private List<IRPInstance> filterListToJustParts(
			List<IRPModelElement> fromEls ){

		List<IRPInstance> theParts = new ArrayList<>();

		for (IRPModelElement fromEl : fromEls) {

			if( fromEl.getMetaClass().equals( "Object" ) ){
				theParts.add( (IRPInstance) fromEl );
			}
		}

		return theParts;
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
