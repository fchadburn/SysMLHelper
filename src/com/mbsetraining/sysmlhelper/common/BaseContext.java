package com.mbsetraining.sysmlhelper.common;

import java.util.ArrayList;
import java.util.List;

import com.telelogic.rhapsody.core.*;

public class BaseContext extends RhpLog {

	private String _pluginVersionProperty;
	private String _userDefinedMetaClassesAsSeparateUnitProperty;
	private String _allowPluginToControlUnitGranularityProperty;
	
	protected IRPApplication _rhpApp = null;
	protected IRPProject _rhpPrj = null;
	protected RhpLog _rhpLog = null;
	protected String _namesRegEx = null;

	public BaseContext(
			String theAppID,
			String enableErrorLoggingProperty,
			String enableWarningLoggingProperty,
			String enableInfoLoggingProperty,
			String enableDebugLoggingProperty,
			String pluginVersionProperty,
			String userDefinedMetaClassesAsSeparateUnitProperty,
			String allowPluginToControlUnitGranularityProperty ) {

		super( theAppID,
				enableErrorLoggingProperty,
				enableWarningLoggingProperty,
				enableInfoLoggingProperty,
				enableDebugLoggingProperty );
		
		_rhpApp = RhapsodyAppServer.getActiveRhapsodyApplicationByID( theAppID );
		_rhpPrj = _rhpApp.activeProject();
		_pluginVersionProperty = pluginVersionProperty;
		_userDefinedMetaClassesAsSeparateUnitProperty = userDefinedMetaClassesAsSeparateUnitProperty;
		_allowPluginToControlUnitGranularityProperty = allowPluginToControlUnitGranularityProperty;
	}
	
	public IRPModelElement findElementByGUID( 
			String guid ){
		
		return _rhpPrj.findElementByGUID( guid );
	}

	public IRPModelElement getSelectedElement(){
		return _rhpApp.getSelectedElement();
	}

	public List<IRPGraphNode> getSelectedGraphNodes(){

		List<IRPGraphNode> theGraphNodes = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theSelectedGraphEls = _rhpApp.getSelectedGraphElements().toList();

		for (IRPGraphElement theSelectedGraphEl : theSelectedGraphEls) {

			if( theSelectedGraphEl instanceof IRPGraphNode ){
				theGraphNodes.add( (IRPGraphNode) theSelectedGraphEl );
			}
		}

		return theGraphNodes;
	}
	
	public List<IRPGraphEdge> getSelectedGraphEdges(){

		List<IRPGraphEdge> theGraphEdges = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theSelectedGraphEls = _rhpApp.getSelectedGraphElements().toList();

		for( IRPGraphElement theSelectedGraphEl : theSelectedGraphEls ){

			if( theSelectedGraphEl instanceof IRPGraphEdge ){
				theGraphEdges.add( (IRPGraphEdge) theSelectedGraphEl );
			}
		}

		return theGraphEdges;
	}
			
	public IRPGraphNode getGraphNodeIfOnlyOneIsSelected(){

		IRPGraphNode theGraphNode = null;

		List<IRPGraphNode> theGraphNodes = getSelectedGraphNodes();

		if( theGraphNodes.size() == 1 ){
			theGraphNode = theGraphNodes.get( 0 );
		}

		debug( "getGraphNodeIfOnlyOneIsSelected found " + theGraphNodes.size() + " elements" );

		return theGraphNode;
	}

	public IRPGraphElement getSelectedGraphEl(){

		IRPGraphElement theGraphEl = null;

		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theSelectedGraphEls = _rhpApp.getSelectedGraphElements().toList();

		if( theSelectedGraphEls.size() == 1 ){
			theGraphEl = theSelectedGraphEls.get( 0 );
		} else {
			this.debug( "getSelectedGraphEl was invoked, " + theSelectedGraphEls.size() + " were found when expecting one" );
		}

		return theGraphEl;
	}

	public List<IRPModelElement> getElementsInProjectThatMatch(
			String theBaseMetaClass, 
			String theUserDefinedMetaClass ){

		List<IRPModelElement> matches = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theCandidates = this.get_rhpPrj().getNestedElementsByMetaClass( 
				theBaseMetaClass, 1 ).toList();

		for (IRPModelElement theCandidate : theCandidates) {

			if( theCandidate.getUserDefinedMetaClass().equals( theUserDefinedMetaClass ) ){
				matches.add( theCandidate );
			}
		}

		return matches;
	}
	
	public List<String> getUserDefinedMetaClassesAsSeparateUnit(){

		List<String> theMetaClasses = new ArrayList<>();

		String theUserDefinedMetaClassPropertyValue =
				getStringPropertyValueFromRhp(
						_rhpPrj,
						_userDefinedMetaClassesAsSeparateUnitProperty,
						"" );
		
		String[] theCandidates = theUserDefinedMetaClassPropertyValue.split( "," );
		
		for( String theCandidate : theCandidates ){
			theMetaClasses.add( theCandidate.trim() );
		}

		return theMetaClasses;
	}

	public List<IRPModelElement> getElementsInProjectThatMatch(
			String theBaseMetaClass, 
			String theUserDefinedMetaClass,
			String andName ){

		List<IRPModelElement> matches = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theCandidates = this.get_rhpPrj().getNestedElementsByMetaClass( 
				theBaseMetaClass, 1 ).toList();

		for (IRPModelElement theCandidate : theCandidates) {

			if( theCandidate.getUserDefinedMetaClass().equals( theUserDefinedMetaClass ) &&
					theCandidate.getName().equals( andName ) ){

				matches.add( theCandidate );
			}
		}

		return matches;
	}

	public IRPModelElement getElementInProjectThatMatches(
			String theBaseMetaClass, 
			String theUserDefinedMetaClass,
			String andName ){

		IRPModelElement matchingEl = null;

		List<IRPModelElement> matches = getElementsInProjectThatMatch(
				theBaseMetaClass, 
				theUserDefinedMetaClass, 
				andName );

		if( matches.size()==1 ){
			matchingEl = matches.get( 0 ); 
		} else if( matches.size() > 1 ){
			warning( "getElementInProjectThatMatches found " + matches.size() + " elements with name " + 
					andName + " and type " + theUserDefinedMetaClass + ", when expecting one " +
					"(hence is returning the first one found)" );
			matchingEl = matches.get( 0 ); 
		}

		return matchingEl;
	}

	protected String getUniqueNameBasedOn(
			IRPModelElement theOwningEl,
			String theName,
			String withMetatype ) {

		int count = 0;

		//		String theOrigLegalName = getLegalName( theName );
		String theLegalName = theName;

		// increment number until name is unique in context
		while( theOwningEl.findNestedElement( theLegalName, withMetatype ) != null ){
			count++;
			theLegalName = theName + count;
		}

		return theLegalName;
	}

	public IRPProject get_rhpPrj(){
		return _rhpPrj;
	}

	public void setStringPropertyValueInRhp(
			IRPModelElement theEl,
			String withKey, 
			String toValue){

		if( theEl != null ){

			IRPUnit theUnit = theEl.getSaveUnit();

			if( theUnit.isReadOnly()==1 ){
//				UserInterfaceHelper.showWarningDialog(
//						"Unable to setBoolPropertyValueInRhp because project is read-only");
				
				// just give a warning as this may occur when using stereotypes in the SysML profile
				warning( "Unable to setStringPropertyValueInRhp for " + elInfo( theEl) + " because unit is read-only" );
			} else {

				try {
					String previousValue = theEl.getPropertyValue(withKey);

					if( previousValue.equals( toValue ) ){
						debug( "Skipping " + withKey + " property on " + elInfo( theEl ) + " as it's already set to " + toValue);

					} else {						
						debug( "Setting " + withKey + " property on " + elInfo( theEl ) + " to " + toValue);
						theEl.setPropertyValue(withKey, toValue);
					}

				} catch( Exception e ){
					warning( e.getMessage() + " has occurred which suggests a problem setting a property in the profile." );
					e.printStackTrace();
				}
			}
		}
	}
	public void setBoolPropertyValueInRhp(
			IRPModelElement theEl,
			String withKey, 
			boolean toValue){

		if( theEl != null ){

			IRPUnit theUnit = theEl.getSaveUnit();

			if( theUnit.isReadOnly()==1 ){
//				UserInterfaceHelper.showWarningDialog(
//						"Unable to setBoolPropertyValueInRhp because project is read-only" );
				
				// just give a warning as this may occur when using stereotypes in the SysML profile
				warning( "Unable to setBoolPropertyValueInRhp for " + elInfo( theEl) + " because unit is read-only" );
			} else {

				try {
					String theValue = "False";

					if( toValue ){
						theValue = "True";
					}

					debug( "Setting " + withKey + " property on " + elInfo( theEl ) + " to " + theValue);
					theEl.setPropertyValue(withKey, theValue);

				} catch( Exception e ){
					warning( e.getMessage() + " has occurred which suggests a problem setting a property in the profile.");
					e.printStackTrace();
				}
			}
		}
	}

	protected boolean getBoolPropertyValueFromRhp(
			IRPModelElement theEl,
			String propertyKey,
			boolean defaultIfNotSet ) {

		boolean isSet = defaultIfNotSet;

		if( theEl != null ){
			try {
				String theValue = theEl.getPropertyValue(
						propertyKey );

				if( theValue != null ){
					isSet = theValue.equals( "True" );
				}

			} catch (Exception e) {
				warning( e.getMessage() + " has occurred which suggests a problem finding property in the profile." );
				e.printStackTrace();}
		}

		return isSet;
	}

	private String getStringPropertyValueFromRhp(
			IRPModelElement theEl,
			String propertyKey,
			String defaultIfNotSet ) {

		String theValue = defaultIfNotSet;

		if( theEl != null ){
			try {
				theValue = theEl.getPropertyValue(
						propertyKey );

			} catch (Exception e) {
				warning( "'" + e.getMessage() + "' has occurred which suggests a problem finding property in the profile." );
				e.printStackTrace();
			}
		}

		return theValue;
	}

	public IRPStereotype getStereotypeWith( 
			String theName ) {

		IRPStereotype theStereotype = null;
		IRPModelElement theModelEl = _rhpPrj.findAllByName( theName, "Stereotype" );

		if( theModelEl != null && theModelEl instanceof IRPStereotype ){
			theStereotype = (IRPStereotype)theModelEl;
		} else {
			error( "Unable to find stereotype with name " + theName + " in project");

		}

		return theStereotype;
	}

	public IRPTag getTag( 
			IRPModelElement appliedToModelEl,
			String withTagName ){
		
		IRPTag theTag = appliedToModelEl.getTag( withTagName );

		if( theTag == null ){
			
			error( "setTagValue for " + elInfo( appliedToModelEl ) + 
					" was unable to find tag called " + withTagName );
		}

		return theTag;
	}
	
	public String getTagValueOnNewTermStereotype( 
			IRPModelElement appliedToModelEl,
			String withTagName ){
		
		String theTagValue = null;
		
		IRPStereotype theStereotype = appliedToModelEl.getNewTermStereotype();

		IRPTag theTag = theStereotype.getTag( withTagName );

		if( theTag != null ){
			
			theTagValue = theTag.getValue();
			
		} else {
			error( "getTagValueOnNewTermStereotype for " + elInfo( appliedToModelEl ) + 
					" was unable to find tag called " + withTagName + 
					" related to stereotype called " + elInfo( theStereotype )  );
		}

		return theTagValue;
	}
	
	public boolean setTagValue( 
			IRPStereotype onStereotype, 
			IRPModelElement appliedToModelEl,
			String withTagName,
			String theValue ){

		boolean wasValueChanged = false;
		
		IRPTag theTag = onStereotype.getTag( withTagName );

		if( theTag != null ){

			if( !theTag.getValue().equals( theValue ) ){

				debug( "setTagValue is setting tag " + elInfo( theTag ) + " on " + elInfo( onStereotype ) + " to " + theValue );
				
				appliedToModelEl.setTagValue( theTag, theValue );
				wasValueChanged = true;
			}
		} else {
			error( "setTagValue for " + elInfo( appliedToModelEl ) + 
					" was unable to find tag called " + withTagName + 
					" related to stereotype called " + elInfo( onStereotype )  );
		}

		return wasValueChanged;
	}

	public boolean setTagValue( 
			String onStereotypeName, 
			IRPModelElement appliedToModelEl,
			String withTagName,
			String theValue ){

		boolean wasValueChanged = false;

		IRPStereotype theStereotype = getStereotypeWith( onStereotypeName );

		if( theStereotype != null ){
			
			wasValueChanged = setTagValue(
					theStereotype, appliedToModelEl, withTagName, theValue );
		}

		return wasValueChanged;
	}

	public IRPApplication get_rhpApp() {
		return _rhpApp;
	}
		
	public String getPluginVersion(){

		String isEnabled = getStringPropertyValueFromRhp(
				_rhpPrj,
				_pluginVersionProperty,
				"ERROR: MISSING " + _pluginVersionProperty + " PROPERTY" );

		return isEnabled;
	}

	public boolean isAllowPluginToGetUnitGranularity(){

		return getBoolPropertyValueFromRhp(
				_rhpPrj, 
				_allowPluginToControlUnitGranularityProperty, 
				false );
	}
}