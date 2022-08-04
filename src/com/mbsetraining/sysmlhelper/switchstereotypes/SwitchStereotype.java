package com.mbsetraining.sysmlhelper.switchstereotypes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mbsetraining.sysmlhelper.common.BaseContext;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class SwitchStereotype {

	protected BaseContext _context;

	protected IRPStereotype _oldStereotype;
	protected IRPStereotype _newStereotype;
	protected List<IRPModelElement> _validTargetEls;

	public static void main(String[] args) {
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();

		ExecutableMBSE_Context theContext = 
				new ExecutableMBSE_Context( theRhpApp.getApplicationConnectionString() );

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theCandidates = theContext.get_rhpPrj().
		getNestedElementsByMetaClass( "Stereotype", 1 ).toList();

		IRPModelElement theOldStereotype = UserInterfaceHelper.launchDialogToSelectElement(theCandidates, "Pick the stereotype to switch from?", true );

		SwitchStereotype theSwitcher = new SwitchStereotype( theContext );

		if( theOldStereotype instanceof IRPStereotype ){			
			theSwitcher.setOldStereotype( (IRPStereotype) theOldStereotype );

			IRPModelElement theNewStereotype = UserInterfaceHelper.launchDialogToSelectElement(theCandidates, "Pick the stereotype to switch to?", true );

			if( theNewStereotype instanceof IRPStereotype ){
				theSwitcher.setNewStereotype( (IRPStereotype) theNewStereotype );

				theSwitcher.performAction();
			}
		}
	}

	public SwitchStereotype(
			BaseContext context ) {
		_context = context;
	}

	public void setOldStereotype( 
			IRPStereotype theStereotype ){

		_oldStereotype = theStereotype;
	}

	public void setNewStereotype( 
			IRPStereotype theStereotype ){

		_newStereotype = theStereotype;
	}

	public boolean checkValidity(
			boolean isMessageEnabled) {

		boolean isValid = true;
		String errorMsg = "";

		if ( _oldStereotype == null ){

			errorMsg += "No old stereotype was selected\n";				
			isValid = false;
		}

		if ( _newStereotype == null ){

			errorMsg += "No new stereotype was selected\n";				
			isValid = false;
		}

		if( _oldStereotype != null &&
				_newStereotype != null ){

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theCandidateEls = _oldStereotype.getReferences().toList();

			if( theCandidateEls.isEmpty() ){
				
				errorMsg += "No elements were found with " + _context.elInfo( _oldStereotype ) + "\n";				
				isValid = false;
			} else {
				
				_validTargetEls = new ArrayList<IRPModelElement>();
				
				List<IRPModelElement> invalidTargets = new ArrayList<IRPModelElement>();

				List<String> theNewMetaclasses = _context.getListFromString( _newStereotype.getOfMetaClass() );

				Set<String> theMissingMetaclasses = new HashSet<String>();

				//_context.info( "theNewMetaclasses for " + _context.elInfo( _newStereotype ) + " are: ");

				//for (String theNewMetaclass : theNewMetaclasses) {
				//	_context.info( theNewMetaclass );
				//}

				//_context.info( "theOldEls for " + _context.elInfo( _oldStereotype ) + " are: ");

				for( IRPModelElement theCandidateEl : theCandidateEls ){
					//_context.info( _context.elInfo( theOldEl ) );

					if( theNewMetaclasses.contains( theCandidateEl.getMetaClass() ) ){
						_validTargetEls.add( theCandidateEl );
					} else {
						invalidTargets.add( theCandidateEl );
						theMissingMetaclasses.add( theCandidateEl.getMetaClass() );
						isValid = false;
					}
				}

				if( !invalidTargets.isEmpty() ){

					errorMsg += "Unable to do conversion as " + invalidTargets.size() + " elements cannot be switched\n";			

					errorMsg += "Missing metaclasses on " + _context.elInfo( _newStereotype ) + " are: \n";

					for( String theMissingMetaclass : theMissingMetaclasses ){
						errorMsg += theMissingMetaclass + "\n";
					}
				}
			}

		}

		if( !isValid && 
				errorMsg != null ){

			if( isMessageEnabled ){				
				UserInterfaceHelper.showWarningDialog( errorMsg );
			} else {
				_context.info( errorMsg );
			}
		}

		return isValid;
	}

	public void performAction() {

		if( checkValidity( false ) ){

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theOldEls = _oldStereotype.getReferences().toList();
			
			String msg = "There were " + theOldEls.size() + " elements found to switch from " + 
					_oldStereotype.getFullPathNameIn() + " to " + _newStereotype.getFullPathNameIn() + 
					"\nDo you want to proceed?";
			
			if( UserInterfaceHelper.askAQuestion( msg ) ){
				
				String postFix = " from " + _oldStereotype.getFullPathNameIn() + " to " + _newStereotype.getFullPathNameIn();

				for (IRPModelElement theEl : theOldEls) {

					_context.info( "Switching " + _context.elInfo( theEl ) + postFix );

					try {
						theEl.removeStereotype( _oldStereotype );
						theEl.addSpecificStereotype( _newStereotype );
					} catch( Exception e ){
						_context.error( "Exception in performAction, e=" + e.getMessage() );
					}
				}
			} else {
				_context.info( "User chose to cancel" );
			}

		} else {
			_context.error( "Error in SwitchStereotype.performAction, checkValidity returned false" );
		}		
	}
	
	public void switchStereotypeFrom(
			IRPStereotype theSelectedEl ){
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theRefs = theSelectedEl.getReferences().toList();

		if( theRefs.isEmpty() ){
			UserInterfaceHelper.showInformationDialog( "Nothing to do. No elements were found with " + _context.elInfo( theSelectedEl ) + " applied.\n" );
		} else {							
			SwitchStereotype theSwitcher = new SwitchStereotype( _context );
			theSwitcher.setOldStereotype( (IRPStereotype) theSelectedEl );

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theCandidates = _context.get_rhpPrj().
				getNestedElementsByMetaClass( "Stereotype", 1 ).toList();

			String msg = "You have selected to switch uses of " + theSelectedEl.getFullPathName() + 
					" to a different stereotype. \n\n" + 
					"Which stereotype do you want to use instead?";
			
			IRPModelElement theNewStereotype = 
					UserInterfaceHelper.launchDialogToSelectElement(
							theCandidates, 
							msg , true );

			if( theNewStereotype instanceof IRPStereotype ){
				
				theSwitcher.setNewStereotype( (IRPStereotype) theNewStereotype );

				if( theSwitcher.checkValidity( true ) ){
					theSwitcher.performAction();					
				}
			}
		}
	}
}
