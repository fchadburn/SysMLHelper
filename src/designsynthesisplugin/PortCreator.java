package designsynthesisplugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JDialog;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class PortCreator {
	
	ExecutableMBSE_Context _context;
	
	public PortCreator(
			ExecutableMBSE_Context context) {
		
		_context = context;
	}
	
	public void createPublishFlowportsFor(
			List<IRPModelElement> theSelectedEls ){

		for (IRPModelElement selectedEl : theSelectedEls) {

			if (selectedEl instanceof IRPAttribute){

				IRPAttribute theAttribute = (IRPAttribute)selectedEl;
				_context.debug( _context.elInfo( theAttribute ) + " is being processed");

				createPublishFlowportFor(theAttribute);
			} else {
				_context.debug( "Doing nothing for " + _context.elInfo(selectedEl) 
						+ " as it is not an Atttribute" );
			}
		}
	}

	public IRPSysMLPort createPublishFlowportFor(
			IRPAttribute theAttribute ){

		IRPSysMLPort thePort = getExistingOrCreateNewFlowPortFor( theAttribute );

		if( thePort != null ){
			
			String theDesiredPortName = theAttribute.getName();
			
			// does port require renaming?
			if( !thePort.getName().equals( theDesiredPortName ) ){
				
				_context.debug( "Renaming " + _context.elInfo( thePort ) + " to " + theDesiredPortName );
				thePort.setName( theDesiredPortName );
			}
			
			thePort.setType( theAttribute.getType() );
			thePort.setPortDirection( "Out" );

			IRPStereotype existingSubscribeStereotype = 
					_context.getStereotypeCalled( "subscribe", theAttribute );

			if( existingSubscribeStereotype != null ){
				theAttribute.removeStereotype( existingSubscribeStereotype );
			}
			
			cleanUpAutoRippleDependencies( theAttribute );
			applyStereotypeAndChangeBackToValuePropertyIfNeeded( theAttribute, "publish" );
			_context.copyRequirementTraceabilityFrom( theAttribute, thePort );
			thePort.highLightElement();
			theAttribute.highLightElement();
			
			AutoConnectFlowPortsPanel.launchThePanel( theAttribute, _context );

		} else {
			_context.error("Error in createPublishFlowportFor, no port was created");
		}
		
		return thePort;
	}

	private void applyStereotypeAndChangeBackToValuePropertyIfNeeded(
			IRPAttribute theAttribute,
			String andStereotype ){
				
		_context.debug( "Applying «" + andStereotype + "» stereotype to " + _context.elInfo( theAttribute ) );

		_context.applyExistingStereotype( andStereotype, theAttribute );
		
		// Switch ValueProeprty back if 8.2+
		IRPModelElement theValuePropertyStereotype = 
				_context.findElementWithMetaClassAndName( 
						"Stereotype", "ValueProperty", theAttribute.getProject() );
		
		if( theValuePropertyStereotype != null ){
			theAttribute.changeTo( "ValueProperty" );
		}
	}
	
	public IRPSysMLPort createSubscribeFlowportFor(
			IRPAttribute theAttribute ){

		IRPSysMLPort thePort = getExistingOrCreateNewFlowPortFor( theAttribute );

		if( thePort != null ){

			thePort.setType( theAttribute.getType() );
			thePort.setPortDirection( "In" );

			IRPModelElement thePortOwner = thePort.getOwner();
			
			if( thePortOwner instanceof IRPClassifier ){
				
				IRPClassifier theClassifier = (IRPClassifier)thePortOwner;
				
				String theChangeEventName = "ch" + _context.capitalize(theAttribute.getName());
				
				_context.debug("Ensure there is a change event reception called " + 
						theChangeEventName + " on " + _context.elInfo( theClassifier ) );
				
				IRPModelElement theReception = _context.getExistingOrCreateNewElementWith(
						theChangeEventName,
						"Reception",
						theClassifier );
				
				_context.addAutoRippleDependencyIfOneDoesntExist( 
						theAttribute, theReception );
				
			} else {
				_context.error("Error in createSubscribeFlowportFor, element is not a classifier");
			}
			
			IRPStereotype existingStereotype = 
					_context.getStereotypeCalled( "publish", theAttribute );

			if( existingStereotype != null ){
				theAttribute.removeStereotype( existingStereotype );
			}

			cleanUpAutoRippleDependencies( theAttribute );
			applyStereotypeAndChangeBackToValuePropertyIfNeeded( theAttribute, "subscribe" );
			_context.copyRequirementTraceabilityFrom( theAttribute, thePort );
			thePort.highLightElement();
			theAttribute.highLightElement();

		} else {
			_context.error( "Error in createSubscribeFlowportFor, no port was created" );
		}

		return thePort;
	}
	
	private IRPSysMLPort getExistingOrCreateNewFlowPortFor(
			IRPAttribute theAttribute ){
		
		String theDesiredPortName = theAttribute.getName();
		
		IRPSysMLPort thePort = _context.getExistingFlowPort( theAttribute );

		if( thePort == null ){
			_context.debug( "Creating an flowport for " + _context.elInfo( theAttribute ) + " called " + theDesiredPortName );
			thePort = (IRPSysMLPort) theAttribute.getOwner().addNewAggr( "FlowPort", theDesiredPortName );
			
			_context.addAutoRippleDependencyIfOneDoesntExist( 
					theAttribute, thePort );
						
		} else if( !thePort.getName().equals( theDesiredPortName ) ){ // does port require renaming?
			
			_context.debug( "Renaming " + _context.elInfo( thePort ) + " to " + theDesiredPortName );
			thePort.setName(theDesiredPortName);
		}
		
		return thePort;
	}

	public void createSubscribeFlowportsFor(
			List<IRPModelElement> theSelectedEls ){

		for( IRPModelElement selectedEl : theSelectedEls ){

			if( selectedEl instanceof IRPAttribute ){

				IRPAttribute theAttribute = (IRPAttribute)selectedEl;
				_context.debug( _context.elInfo( theAttribute ) + "is being processed");

				createSubscribeFlowportFor(theAttribute);
			} else {
				_context.warning("Doing nothing for " + _context.elInfo(selectedEl) 
						+ " as it is not an Atttribute");
			}
		}
	}

	public void deleteFlowPortAndRelatedEls(
			IRPSysMLPort theFlowPort ){
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theReferences = theFlowPort.getReferences().toList();
		
		for( IRPModelElement theReference : theReferences ){
			
			if( theReference instanceof IRPDependency && 
				_context.hasStereotypeCalled( "AutoRipple", theReference ) ){
				
				IRPDependency theDependency = (IRPDependency)theReference;
				IRPModelElement theDependent = theDependency.getDependent();
				
				if( theDependent instanceof IRPAttribute ){
					deleteAttributeAndRelatedEls( (IRPAttribute) theDependent );
				} else {
					UserInterfaceHelper.showWarningDialog( 
							"Unable to delete as " + _context.elInfo( theFlowPort ) + " has no related attribute" );
				}
			}
		}
	}
	
	public void deleteAttributeAndRelatedEls(
			IRPAttribute theAttribute ){
	
		Set<IRPModelElement> theRelatedEls = 
				_context.getElementsThatHaveStereotypedDependenciesFrom(
						theAttribute, "AutoRipple" );
	
		JDialog.setDefaultLookAndFeelDecorated(true);

		String infoText = "Do you want to delete:\n" + "1. " + _context.elInfo( theAttribute );
				
		if( !theRelatedEls.isEmpty() ){
			
			int count = 1;
			
			infoText = infoText + "\n";
			
			for( IRPModelElement theRelatedEl : theRelatedEls ){
				
				count++;
				
				infoText = infoText + count + ". " + _context.elInfo( theRelatedEl ) + 
						"\n";
			}			
		}

		boolean answer = UserInterfaceHelper.askAQuestion( infoText );
		
		if( answer ){
		
			for( IRPModelElement theRelatedEl : theRelatedEls ){
				_context.info( "Deleting " + _context.elInfo( theRelatedEl ) + " from the project" );

				try {
					theRelatedEl.deleteFromProject();

				} catch (Exception e) {
					_context.error("Exception in deleteAttributeAndRelatedEls, trying to delete " + _context.elInfo( theRelatedEl ) + " failed");
				}
			}

			_context.info( "Deleting " + _context.elInfo( theAttribute ) + " from the project" );
			theAttribute.getOwner().highLightElement();

			try {
				theAttribute.deleteFromProject();

			} catch (Exception e) {
				_context.error("Exception in deleteAttributeAndRelatedEls, trying to delete attribute failed");
			}			
		}	
	}
	
	private void cleanUpAutoRippleDependencies(
			IRPAttribute theAttribute ){
		
		@SuppressWarnings("unchecked")
		List<IRPDependency> theExistingDeps = theAttribute.getDependencies().toList();

		Set<IRPDependency> dependenciesToDelete = new HashSet<IRPDependency>();

		for( IRPDependency theDependency : theExistingDeps ){

			IRPModelElement theDependsOn = theDependency.getDependsOn();

			if( theDependsOn instanceof IRPModelElement &&
				_context.hasStereotypeCalled( "AutoRipple", theDependency ) ){

				IRPModelElement theElementOwner = theDependsOn.getOwner();
				IRPModelElement theAttributeOwner = theAttribute.getOwner();

				boolean isCheckOperation = 
						theDependsOn instanceof IRPOperation && theDependsOn.getName().contains( "check" );

				boolean isReception =
						theDependsOn.getUserDefinedMetaClass().equals( "Reception" );

				boolean isFlowPort =
						theDependsOn instanceof IRPSysMLPort;

				boolean isTransition =
						theDependsOn instanceof IRPTransition;
				
				if( isCheckOperation || isReception || isFlowPort || isTransition ){

					if( !theElementOwner.equals( theAttributeOwner ) ){

						//_context.debug( "Detected a need to delete the «AutoRipple» dependency to " + _context.elInfo( theDependsOn ) + 
						//		" owned by " + _context.elInfo( theElementOwner ) + ", as it is not owned by " + 
						//		_context.elInfo( theAttributeOwner ) );

						dependenciesToDelete.add( theDependency );
						theDependency.highLightElement();
					} else {
						//_context.debug( _context.elInfo( theDependsOn ) + " was found based on «AutoRipple» dependency");
					}
				}	
			}
		}

		if( !dependenciesToDelete.isEmpty() ){
			
			/*
			JDialog.setDefaultLookAndFeelDecorated(true);

			String infoText = "To maintain consistency the following «AutoRipple» dependencies will be deleted: " +
					"\n";

			int count = 0;
			
			for (IRPDependency theDependency : dependenciesToDelete) {
				
				IRPModelElement theDependsOn = theDependency.getDependsOn();
				
				IRPModelElement theElementOwner = theDependsOn.getOwner();
				
				count++;
				
				infoText = infoText + count + ". " + _context.elInfo( theDependsOn ) + 
						" owned by " + _context.elInfo( theElementOwner ) + " \n";
			}

			_context.debug( infoText );
			*/
			
			theAttribute.getOwner().highLightElement();
			
			for( IRPDependency theDependency : dependenciesToDelete ){	
				theDependency.deleteFromProject();
			}
		}
	}
}

/**
 * Copyright (C) 2016-2021  MBSE Training and Consulting Limited (www.executablembse.com)

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
