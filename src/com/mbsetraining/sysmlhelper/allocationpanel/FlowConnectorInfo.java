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
	protected IRPClassifier _tgtSystemRootEl;
	protected List<IRPLink> _tgtLinks;
	protected IRPSysMLPort _tgtFromPort;
	protected IRPSysMLPort _tgtToPort;
	protected IRPPort _fromSubsystemPort;
	protected IRPPort _toSubsystemPort;

	public FlowConnectorInfo(
			IRPLink theLink,
			FunctionAllocationMap functionAllocationMap,
			ExecutableMBSE_Context context ){

		_context = context;
		_functionAllocationMap = functionAllocationMap;
		_tgtSystemRootEl = _functionAllocationMap._systemRootEl;
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

				//_tgtCommonOwner = findCommonOwnerFor( fromInstance, toInstance );
				//_context.info( "_tgtCommonOwner    = " + _context.elInfo( _tgtCommonOwner ) );

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

				if( fromInstance.getOwner().equals( toInstance.getOwner() )) {

					_context.info( "Checking for flow connector between: " );

					_context.info( "_tgtFromPort  = " + _context.elInfo( _tgtFromPort ) + " owned by " + _context.elInfo( _tgtFromPort.getOwner() ) );
					_context.info( "_tgtToPort    = " + _context.elInfo( _tgtToPort ) + " owned by " + _context.elInfo( _tgtToPort.getOwner() ) );

					_tgtLinks = getExistingLinks( fromInstance, _tgtFromPort, toInstance, _tgtToPort, (IRPClassifier) fromInstance.getOwner() );

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
					
				} else {

					buildConnectorsViaProxyPortBridge( fromInstance, toInstance );
				}

				_context.info( "...end performMapping" ); 
			}

			dumpInfo();
		}

		return isMappable;
	}

	private List<IRPInstance> getOwningSubsystemUsagesFor(
			IRPInstance theFunctionUsage,
			IRPClassifier underSystemClassifier ){
		
		List<IRPInstance> theOwningSubsystemUsages = new ArrayList<>();
		//_context.info( "theFunctionUsage = " + _context.elInfo( theFunctionUsage ) );

		IRPModelElement theFunctionUsageOwner = theFunctionUsage.getOwner();
		//_context.info( "theFunctionUsageOwner = " + _context.elInfo( theFunctionUsageOwner ) );
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theCandidateEls = underSystemClassifier.getNestedElementsByMetaClass( "Object", 0 ).toList();
		
		for( IRPModelElement theCandidateEl : theCandidateEls) {
			
			IRPInstance theInstance = (IRPInstance)theCandidateEl;
			//_context.info( "theInstance = " + _context.elInfo( theInstance ) );

			if( theInstance.isTypelessObject() == 0 ) {
				IRPClassifier theClassifier = theInstance.getOtherClass();
				
				if( theClassifier.equals( theFunctionUsageOwner ) ) {
					theOwningSubsystemUsages.add( theInstance );
				}
			}
		}
		
		if( theOwningSubsystemUsages.isEmpty() ) {
			
			_context.error("getOwningSubsystemUsagesFor was unable to find any subsystems owning " + 
					_context.elInfo( theFunctionUsage ) + " under " + _context.elInfo ( underSystemClassifier ));
			
		} else if( theOwningSubsystemUsages.size() > 1 ) {
			
			_context.warning("getOwningSubsystemUsagesFor found " + theOwningSubsystemUsages.size() + " usages " + 
					_context.elInfo( theFunctionUsage ) + " under " + _context.elInfo ( underSystemClassifier ) + 
					" when expecting 1" );
		} else {
			_context.info("getOwningSubsystemUsagesFor found that " + 
					_context.elInfo( theFunctionUsage ) + " is used by " + _context.elInfo( theOwningSubsystemUsages.get(0) ) + 
					" under " + _context.elInfo ( underSystemClassifier ) );
		}
		
		return theOwningSubsystemUsages;
	}
	
	private void buildConnectorsViaProxyPortBridge(
			IRPInstance fromInstance, 
			IRPInstance toInstance) {
		
		IRPModelElement fromOwner = fromInstance.getOwner();
		_context.info( "fromOwner = " + _context.elInfo( fromOwner ) );
		
		IRPModelElement toOwner = toInstance.getOwner();
		_context.info( "toOwner = " + _context.elInfo( toOwner ) );

		if( fromOwner instanceof IRPClassifier && 
				toOwner instanceof IRPClassifier ) {

			List<IRPLink> theLinks = getExistingLinks( 
					(IRPClassifier)fromOwner, 
					(IRPClassifier)toOwner, 
					_tgtSystemRootEl );

			if( theLinks.isEmpty() ) {

				_context.info( "New link needed between");

				String toName = _context.getPortAppropriateNameFor( toOwner );
				String fromName = _context.getPortAppropriateNameFor( fromOwner );

				IRPPackage theOwningPkg = _context.getOwningPackageFor( _tgtSystemRootEl );

				String theInterfaceBlockName = "IB_" + fromName + "_To_" + toName;

				IRPClass theInterfaceBlock = getExistingOrCreateNewInterfaceBlock( theInterfaceBlockName, theOwningPkg );
				IRPPort fromPort = getExistingOrCreateNewProxyPort( "p" + toName, fromOwner, theInterfaceBlock, false  );
				IRPPort toPort = getExistingOrCreateNewProxyPort( "p" + fromName, toOwner, theInterfaceBlock, true );

				List<IRPInstance> fromSubsystemInstances = getOwningSubsystemUsagesFor( fromInstance, _tgtSystemRootEl );
				List<IRPInstance> toSubsystemInstances = getOwningSubsystemUsagesFor( toInstance, _tgtSystemRootEl );

				if( fromSubsystemInstances.size()==1 &&
						fromSubsystemInstances.size()==1 ){
					
					IRPLink theLink = createConnector( fromSubsystemInstances.get( 0 ), toSubsystemInstances.get( 0 ), fromPort, toPort );
					
					theLinks.add( theLink );
				}
			}

			if( theLinks.size()==1 ) {

				IRPLink theLink = theLinks.get( 0 );

				_fromSubsystemPort = theLink.getFromPort();
				_toSubsystemPort = theLink.getToPort();

				if( _fromSubsystemPort instanceof IRPPort &&
						_tgtFromPort instanceof IRPSysMLPort ) {

					getOrCreateSubsystemFunctionConnectorFor( _fromSubsystemPort, _tgtFromPort, fromInstance );
				}

				_toSubsystemPort = theLink.getToPort();

				if( _toSubsystemPort instanceof IRPPort &&
						_tgtToPort instanceof IRPSysMLPort ) {

					getOrCreateSubsystemFunctionConnectorFor( _toSubsystemPort, _tgtToPort, toInstance );
				}

			} else {

				_context.info( theLinks.size() + " links were found between  " + _context.elInfo( fromOwner ) + " and " + _context.elInfo( toOwner ) );


			}
		}
	}

	private IRPClass getExistingOrCreateNewInterfaceBlock(
			String withName,
			IRPPackage theOwningPkg ) {
		
		IRPClass theInterfaceBlock = (IRPClass) theOwningPkg.findNestedElementRecursive( withName, "Class" );

		if( theInterfaceBlock == null ) {
			theInterfaceBlock = theOwningPkg.addClass( withName );
			theInterfaceBlock.changeTo( "InterfaceBlock" );
		}
		
		return theInterfaceBlock;
	}

	private IRPPort getExistingOrCreateNewProxyPort(
			String withName,
			IRPModelElement fromOwner, 
			IRPClass theInterfaceBlock,
			boolean isConjugated ){
		
		IRPPort thePort = (IRPPort) fromOwner.findNestedElement( withName, "Port" );
		
		if( thePort == null ) {
			
			thePort = (IRPPort) fromOwner.addNewAggr( "Port", withName );
			
			//_context.debug( "Created fromPort as " + _context.elInfo( fromPort ) );
			thePort.changeTo( "ProxyPort" );
			thePort.setOtherClass( theInterfaceBlock );
			
			if( isConjugated ) {
				thePort.setIsReversed( 1 );
			}
		}
		
		return thePort;
	}

	private IRPLink createConnector(
			IRPInstance fromInstance, 
			IRPInstance toInstance, 
			IRPPort fromPort, 
			IRPPort toPort ){
		
		IRPLink theLink = null;
		
		IRPClass theOwningClass = (IRPClass) fromInstance.getOwner();
		
		_context.info( "createConnector invoked for: ");
		_context.info( "theOwningClass = " + _context.elInfo( theOwningClass ) );
		_context.info( "fromInstance = " + _context.elInfo( fromInstance ) + " owned by " + _context.elInfo( fromInstance.getOwner() ) );
		_context.info( "toInstance = " + _context.elInfo( toInstance ) + " owned by " + _context.elInfo( toInstance.getOwner() ) );
		_context.info( "fromPort = " + _context.elInfo( fromPort ) + " owned by " + _context.elInfo( fromPort.getOwner() ) );
		_context.info( "toPort = " + _context.elInfo( toPort ) + " owned by " + _context.elInfo( toPort.getOwner() ) );
		
		try {
			theLink = theOwningClass.addLink( fromInstance, toInstance, null, fromPort, toPort );
			theLink.changeTo( _context.CONNECTOR );

		} catch( Exception e ){
			_context.error( "Exception in createConnector trying to create link, e=" + e.getMessage() );
		}
		
		return theLink;
	}

	private IRPLink getOrCreateSubsystemFunctionConnectorFor(
			IRPPort fromSubsystemPort,
			IRPSysMLPort toFlowPort,
			IRPInstance ownedByInstance ){

		IRPLink theConnector = null;

		List<IRPLink> theExistingLinks = getExistingLinks( 
				fromSubsystemPort, 
				toFlowPort );

		if( theExistingLinks.isEmpty() ) {

			IRPModelElement theOwnerEl = fromSubsystemPort.getOwner();

			if( theOwnerEl instanceof IRPClass ) {

				/**
				 * This method is used to create a delegation connector between a class and one of its parts. 
				 * In addition to specifying the part to use, you must specify the association that the link 
				 * should represent, or, alternatively, the two ports that should be used for the link. If you 
				 * provide the two ports as arguments, you should use Null for the association argument. 
				 * Similarly, if you specify an association, you should use Null for the two port arguments. 
				 * Note that if you are not specifying the two ports, you must provide an association as an 
				 * argument even if there is only one relevant association.
				 * @param toPart the part that should be linked to
				 * @param partPort the port to use on the part
				 * @param classPort the port to use on the class
				 * @param assoc the association that the link should represent
				 * @return the link created
				 */
				//public IRPLink addLinkToPartViaPort(IRPInstance toPart, IRPInstance partPort, IRPInstance classPort, IRPRelation assoc);

				IRPClass theOwnerClass = (IRPClass)theOwnerEl;
				theConnector = theOwnerClass.addLinkToPartViaPort( ownedByInstance, toFlowPort, fromSubsystemPort, null );
				theConnector.changeTo( _context.SUBSYSTEM_FUNCTION_CONNECTOR );

				_context.info( "Created new " + _context.elInfo( theConnector ) );
				_context.info( _context.elInfo( fromSubsystemPort ) + " owned by " + _context.elInfo( fromSubsystemPort.getOwner() ) );
				_context.info( _context.elInfo( toFlowPort ) + " owned by " + _context.elInfo( toFlowPort.getOwner() ) );

			}
		} else if( theExistingLinks.size()== 1 ){

			theConnector = theExistingLinks.get( 0 );

			_context.info( "Found existing " + _context.elInfo( theConnector ) );
			_context.info( _context.elInfo( fromSubsystemPort ) + " owned by " + _context.elInfo( fromSubsystemPort.getOwner() ) );
			_context.info( _context.elInfo( toFlowPort ) + " owned by " + _context.elInfo( toFlowPort.getOwner() ) );

		} else {

			_context.warning( "Unable to create flow connector as " + theExistingLinks.size() + " links were found between:" );
			_context.warning( _context.elInfo( fromSubsystemPort ) + " owned by " + _context.elInfo( fromSubsystemPort.getOwner() ) );
			_context.warning( _context.elInfo( toFlowPort ) + " owned by " + _context.elInfo( toFlowPort.getOwner() ) );
		}

		return theConnector;
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
			//_context.info( "fromCandidatePort  = " + _context.elInfo( fromCandidatePort ) + " owned by " + _context.elInfo( fromCandidatePort.getOwner() ) );

			IRPSysMLPort toCandidatePort = theCandidateLink.getToSysMLPort();
			//_context.info( "toCandidatePort  = " + _context.elInfo( toCandidatePort ) + " owned by " + _context.elInfo( toCandidatePort.getOwner() ) );

			IRPInstance fromCandidateInstance = theCandidateLink.getFrom();
			//_context.info( "fromCandidateInstance  = " + _context.elInfo( fromCandidateInstance ) + " owned by " + _context.elInfo( fromCandidateInstance.getOwner() ) );

			IRPInstance toCandidateInstance = theCandidateLink.getTo();
			//_context.info( "toCandidateInstance  = " + _context.elInfo( toCandidateInstance ) + " owned by " + _context.elInfo( toCandidateInstance.getOwner() ) );

			if( fromCandidatePort != null && 
					toCandidatePort != null ) {

				// Check for both directions as link direction is not important
				if( ( fromCandidatePort.equals( fromPort ) && toCandidatePort.equals( toPort ) && 
						fromCandidateInstance.equals( fromInstance ) && toCandidateInstance.equals( toInstance ) ) ||
						( toCandidatePort.equals( fromPort ) && fromCandidatePort.equals( toPort ) &&
								toCandidateInstance.equals( fromInstance ) && fromCandidateInstance.equals( toInstance ) ) ){

					theExistingLinks.add( theCandidateLink );
				}
			}
		}

		return theExistingLinks;
	}

	private List<IRPLink> getExistingLinks(
			IRPClassifier fromClassifier, 
			IRPClassifier toClassifier, 
			IRPClassifier underClassifier ) {

		List<IRPLink> theExistingLinks = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<IRPLink> theCandidateLinks = underClassifier.getLinks().toList();

		for( IRPLink theCandidateLink : theCandidateLinks ){

			IRPInstance fromCandidateInstance = theCandidateLink.getFrom();
			_context.info( "fromCandidateInstance  = " + _context.elInfo( fromCandidateInstance ) + " owned by " + _context.elInfo( fromCandidateInstance.getOwner() ) );

			IRPClassifier fromCandidateClassifier = fromCandidateInstance.getOtherClass();
			_context.info( "fromCandidateClassifier  = " + _context.elInfo( fromCandidateClassifier ) );

			IRPInstance toCandidateInstance = theCandidateLink.getTo();
			_context.info( "toCandidateInstance  = " + _context.elInfo( toCandidateInstance ) + " owned by " + _context.elInfo( toCandidateInstance.getOwner() ) );

			IRPClassifier toCandidateClassifier = toCandidateInstance.getOtherClass();
			_context.info( "toCandidateClassifier  = " + _context.elInfo( toCandidateClassifier ) );

			//			if( fromCandidateClassifier != null && 
			//					toCandidateClassifier != null ) {

			// Check for both directions as link direction is not important
			if( ( fromCandidateClassifier.equals( fromClassifier ) &&
					toCandidateClassifier.equals( toClassifier ) ) || 
					( toCandidateClassifier.equals( fromClassifier ) &&
							fromCandidateClassifier.equals( toClassifier ) ) ) {

				theExistingLinks.add( theCandidateLink );
			}
			//			}
		}

		return theExistingLinks;
	}


	private List<IRPLink> getExistingLinks(
			IRPPort fromPort, 
			IRPSysMLPort toSysMLPort ) {

		List<IRPLink> theExistingLinks = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theCandidateEls = fromPort.getReferences().toList();

		for( IRPModelElement theCandidateEl : theCandidateEls ){

			if( theCandidateEl instanceof IRPLink ) {

				IRPLink theCandidateLink = (IRPLink) theCandidateEl;

				_context.info( "Checking " + _context.elInfo(( theCandidateLink ) ) );
				theCandidateLink.highLightElement();

				IRPPort fromCandidatePort = theCandidateLink.getFromPort();
				_context.info( "fromCandidatePort = " + _context.elInfo( fromCandidatePort ) );

				IRPSysMLPort fromCandidateSysMLPort = theCandidateLink.getFromSysMLPort();
				_context.info( "fromCandidateSysMLPort = " + _context.elInfo( fromCandidateSysMLPort ) );

				IRPPort toCandidatePort = theCandidateLink.getToPort();
				_context.info( "toCandidatePort = " + _context.elInfo( toCandidatePort ) );

				IRPSysMLPort toCandidateSysMLPort = theCandidateLink.getToSysMLPort();
				_context.info( "toCandidateSysMLPort = " + _context.elInfo( toCandidateSysMLPort ) );

				if( toCandidateSysMLPort instanceof IRPSysMLPort &&
						toSysMLPort.equals( toCandidateSysMLPort ) ) {

					_context.info( _context.elInfo( fromPort ) + " is is connected to " + _context.elInfo( toSysMLPort ) + " via " + _context.elInfo(theCandidateLink));
					theExistingLinks.add( theCandidateLink );
				}
			}
		}

		return theExistingLinks;
	}

	private IRPClassifier findCommonOwnerFor(
			IRPInstance fromInstance,
			IRPInstance toInstance ){

		_context.info( "findCommonOwnerFor invoked for:");
		_context.info( _context.elInfo( fromInstance ) + " owned by " + _context.elInfo( fromInstance.getOwner() ) );
		_context.info( _context.elInfo( toInstance ) + " owned by " + _context.elInfo( toInstance.getOwner() ) );

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
