package com.mbsetraining.sysmlhelper.executablembse;

import java.util.ArrayList;
import java.util.List;

import functionalanalysisplugin.FunctionalAnalysisSettings;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.telelogic.rhapsody.core.*;

public class PortBasedConnector {

	private IRPClassifier _sourceClassifier = null;
	private IRPPort _sourcePort = null;
	private IRPClassifier _targetClassifier = null;
	private IRPPort _targetPort = null;
	private IRPLink _link = null;
	private ExecutableMBSE_Context _context;
	
	public PortBasedConnector(
			IRPClassifier theSourceClassifier ,
			IRPClassifier theTargetClassifier,
			ExecutableMBSE_Context context ){
	
		_context = context;
		
		HierarchyHelper theHierarchyHelper = 
				new HierarchyHelper( theSourceClassifier.getProject(), _context );
		
		List<IRPInstance> theSourceParts = theHierarchyHelper.getAllPartsInProject( theSourceClassifier );
		List<IRPInstance> theTargetParts = theHierarchyHelper.getAllPartsInProject( theTargetClassifier );

		if( theSourceParts.size() == 1 && theTargetParts.size() == 1 ){
			
			IRPInstance theSourcePart = theSourceParts.get( 0 );
			IRPInstance theTargetPart = theTargetParts.get( 0 );

			determineWiringBetween( theSourcePart, theTargetPart );
		}
	}
	
	public PortBasedConnector(
			IRPInstance theSourcePart,
			IRPInstance theTargetPart,
			ExecutableMBSE_Context context ){

		_context = context;

		determineWiringBetween( theSourcePart, theTargetPart );
	}

	private void determineWiringBetween(
			IRPInstance theSourcePart,
			IRPInstance theTargetPart ){
		
		IRPModelElement theSourceOwner = theSourcePart.getOwner();
		IRPModelElement theTargetOwner = theTargetPart.getOwner();

		if( !theSourceOwner.equals( theTargetOwner ) ){
			
			UserInterfaceHelper.showWarningDialog(
					"Error in PortBasedConnector, the owner for " +
					_context.elInfo( theSourcePart ) + "\n" + 
					" is not the same as the owner" + " for " + 
					_context.elInfo( theTargetOwner ) );
			
		} else if( theSourceOwner instanceof IRPClassifier ){
			
			IRPClassifier theOwningClassifier = (IRPClassifier) theSourceOwner;
			
			_sourceClassifier = theSourcePart.getOtherClass();
			_targetClassifier = theTargetPart.getOtherClass();

			List<IRPLink> theMatchingLinks = 
					getExistingLinksBetween(
							theSourcePart, 
							theTargetPart,
							theOwningClassifier );
						
			for (IRPLink theLink : theMatchingLinks) {
				_context.debug( _context.elInfo( theLink ) + 
						" is a link between " + _context.elInfo( theSourcePart ) + " and " + 
						_context.elInfo( theTargetPart ) + " in the context of " + 
						_context.elInfo( theOwningClassifier ) );
			}
			
			if( theMatchingLinks.size() == 1 ){
				_link = theMatchingLinks.get( 0 );
			}
			
			if( _link != null ){
				
				IRPPort fromPort = _link.getFromPort();
				IRPPort toPort = _link.getToPort();
				
				if( fromPort.getOwner().equals( _sourceClassifier ) &&
						toPort.getOwner().equals( _targetClassifier ) ){
					
					_sourcePort = fromPort;
					_targetPort = toPort;
					
				} else if( fromPort.getOwner().equals( _targetClassifier ) &&
						toPort.getOwner().equals( _sourceClassifier ) ){
					
					_sourcePort = toPort;
					_targetPort = fromPort;	
					
				} else {
					UserInterfaceHelper.showWarningDialog("Error");
				}
				
				_context.debug( _context.elInfo( _sourcePort ) + " on " + _context.elInfo( _sourceClassifier) + " is required interface port" );				
				_context.debug( _context.elInfo( _targetPort ) + " on " + _context.elInfo( _targetClassifier) + " is the provided interface port" );
			}
		}
	}

	public boolean doesLinkExist(){
		return ( _link != null );
	}
	
	public IRPLink getLink(){
		return _link;
	}
	
	public void addEvent( IRPEvent theEvent ){
		
		if( !doesLinkExist() ){
			_context.error( "Error, unable to add " + _context.elInfo( theEvent ) + 
					" as no link exists" );
		} else {
			FunctionalAnalysisSettings theSettings = new FunctionalAnalysisSettings(_context);
			
			IRPPackage theInterfacesPkg = 
					theSettings.getPkgThatOwnsEventsAndInterfaces( 
							theEvent );
			
			IRPClass theProvidedInterface = getExistingOrCreateNewProvidedInterfaceOnTargetPort( theInterfacesPkg );
			theProvidedInterface.addReception( theEvent.getName() );
		}
	}
	
	public IRPClass getExistingOrCreateNewProvidedInterfaceOnTargetPort(
			IRPPackage theInterfacesPkg ){
	
		return getExistingOrCreateNewProvidedInterface( 
				_targetPort, _sourcePort, theInterfacesPkg );
	}
	
	public IRPClass getExistingOrCreateNewProvidedInterfaceOnSourcePort(
			IRPPackage theInterfacesPkg ){
		
		return getExistingOrCreateNewProvidedInterface( 
				_sourcePort, _targetPort, theInterfacesPkg );
	}
	
	private IRPClass getExistingOrCreateNewProvidedInterface(
			IRPPort onTargetPort,
			IRPPort withRequiredPort,
			IRPPackage theInterfacesPkg ){
		
		IRPClass theProvidedInterface = null;
		
		@SuppressWarnings("unchecked")
		List<IRPClass> theProvidedInterfaces = 
			onTargetPort.getProvidedInterfaces().toList();
		
		if( theProvidedInterfaces.size() > 1 ){
			_context.error("Error, there are " + 
					theProvidedInterfaces.size() + " provided interfaces when expecting 1" );
			
		} else if( theProvidedInterfaces.size()==1 ){
			theProvidedInterface = theProvidedInterfaces.get( 0 );
			
		} else {
			
			theProvidedInterface = 
					theInterfacesPkg.addClass( "i" + 
							withRequiredPort.getOwner().getName() + "_To_" + 
							onTargetPort.getOwner().getName() );
			
			theProvidedInterface.changeTo( "Interface" );
			onTargetPort.addProvidedInterface( theProvidedInterface );
			onTargetPort.setIsBehavioral( 1 );
			
			addRealizationTo( 
					(IRPClassifier) onTargetPort.getOwner(), 
					theProvidedInterface );
			
			withRequiredPort.addRequiredInterface( theProvidedInterface );
		}
		
		return theProvidedInterface;
	}
	
	private boolean isThereAnExistingGeneralizationOwnedBy(
			IRPClassifier theClassifier,
			IRPClass toTheClass ){
	
		boolean isThereAnExistingGeneralization = false;
		
		@SuppressWarnings("unchecked")
		List<IRPClassifier> theExistingBaseClasses = 
				theClassifier.getBaseClassifiers().toList();
		
		if( theExistingBaseClasses.contains( toTheClass ) ){
			isThereAnExistingGeneralization = true;
		}
		
		return isThereAnExistingGeneralization;
	}
	
	public void addRealizationTo( 
			IRPClassifier ownedByElement,
			IRPClass theInterface ){
		
		_context.debug( _context.elInfo( ownedByElement ) + 
				" is the provider of " + _context.elInfo( theInterface ) +
				" so added a Realization to it to enable inherited operations to be seen" );

		try {
			boolean isThereAnExistingGeneralization = 
					isThereAnExistingGeneralizationOwnedBy(
							ownedByElement, theInterface );
			
			if( !isThereAnExistingGeneralization ){
				
				ownedByElement.addGeneralization( theInterface );

				@SuppressWarnings("unchecked")
				List<IRPModelElement> theGeneralizations = 
						ownedByElement.getNestedElementsByMetaClass( 
								"Generalization", 0 ).toList();
				
				for( IRPModelElement theGeneralizationEl : theGeneralizations ){
					
					IRPGeneralization theGen = (IRPGeneralization)theGeneralizationEl;
					
					if( theGen.getBaseClass().equals( theInterface ) ){
						_context.debug( "Changing " + _context.elInfo( theGen ) + " to be a Realization" );
						theGen.changeTo( "Realization" );
					}
				}
			} else {
				_context.debug("Skipped adding a realization to " + _context.elInfo( theInterface ) + 
						" from " + _context.elInfo( ownedByElement ) + " as one already exists" );
			}

		} catch (Exception e) {
			_context.error("Exception in addRealizationTo, trying to add generalisation, e=" + e.getMessage() );
		}

	}
	
	private List<IRPLink> getExistingLinksBetween(
			IRPInstance theSourcePart,
			IRPInstance theTargetPart, 
			IRPClassifier theOwningClassifier ){

		List<IRPLink> theMatchingLinks = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<IRPLink> theLinkCandidates = theOwningClassifier.getLinks().toList();

		for( IRPLink theLink : theLinkCandidates ){

			IRPInstance toPart = theLink.getTo();
			IRPPort toPort = theLink.getToPort();

			IRPInstance fromPart = theLink.getFrom();
			IRPPort fromPort = theLink.getFromPort();

			Boolean isBetweenNormalPorts = 
					( toPort != null ) && ( fromPort != null );

			if( isBetweenNormalPorts ){
				if( ( theSourcePart.equals( toPart ) && theTargetPart.equals( fromPart ) ) || 
						( theSourcePart.equals( fromPart ) && theTargetPart.equals( toPart ) ) ){

					theMatchingLinks.add( theLink );
				}
			}
		}

		return theMatchingLinks;
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