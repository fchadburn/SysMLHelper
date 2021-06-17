package com.mbsetraining.sysmlhelper.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import sysmlhelperplugin.SysMLHelperPlugin;

import com.telelogic.rhapsody.core.*;

public class BaseContext extends RhpLog {

	private String _pluginVersionProperty;
	private String _userDefinedMetaClassesAsSeparateUnitProperty;
	private String _allowPluginToControlUnitGranularityProperty;
	
	protected RhpLog _rhpLog;
	protected String _namesRegEx;

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
		
		_pluginVersionProperty = pluginVersionProperty;
		_userDefinedMetaClassesAsSeparateUnitProperty = userDefinedMetaClassesAsSeparateUnitProperty;
		_allowPluginToControlUnitGranularityProperty = allowPluginToControlUnitGranularityProperty;
	}
	
	public IRPModelElement findElementByGUID( 
			String guid ){
		
		return _rhpPrj.findElementByGUID( guid );
	}
	
	public IRPCollection createNewCollection(){
		return _rhpApp.createNewCollection();
	}

	public IRPModelElement getSelectedElement(){
		return _rhpApp.getSelectedElement();
	}
	
	@SuppressWarnings("unchecked")
	public List<IRPModelElement> getSelectedElements(){
		return _rhpApp.getListOfSelectedElements().toList();
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
	
	public List<IRPGraphElement> getSelectedGraphElements(){

		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theSelectedGraphEls = _rhpApp.getSelectedGraphElements().toList();
		return theSelectedGraphEls;
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
				super.warning( "Unable to setStringPropertyValueInRhp for " + elInfo( theEl) + " because unit is read-only" );
			} else {

				try {
					String previousValue = theEl.getPropertyValue(withKey);

					if( previousValue.equals( toValue ) ){
						super.debug( "Skipping " + withKey + " property on " + elInfo( theEl ) + " as it's already set to " + toValue);

					} else {						
						super.debug( "Setting " + withKey + " property on " + elInfo( theEl ) + " to " + toValue);
						theEl.setPropertyValue(withKey, toValue);
					}

				} catch( Exception e ){
					super.warning( e.getMessage() + " has occurred which suggests a problem setting a property in the profile." );
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
	
	public String get_rhpAppID() {
		return _rhpApp.getApplicationConnectionString();
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
	
	public boolean getBooleanPropertyValue(
			IRPModelElement forContextEl,
			String thePropertyKey ){
		
		boolean result = false;
		
		String theValue = null;
		
		try {
			theValue = forContextEl.getPropertyValue(
					thePropertyKey );
			
		} catch( Exception e ){
			super.error( "Exception in getBooleanPropertyValue, e=" + e.getMessage() );
		}
				
		if( theValue != null ){
			result = theValue.equals( "True" );
		} else {
			super.error( "Error in getBooleanPropertyValue, " +
					"unable to get thePropertyKey property value from " + 
					super.elInfo( forContextEl ) );
		}
		
		return result;
	}
	
	public boolean isElementNameUnique(
			String theProposedName, 
			String ofMetaClass, 
			IRPModelElement underneathTheEl,
			int recursive ){
				
		int count = 0;
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theExistingEls = 
				underneathTheEl.getNestedElementsByMetaClass(
						ofMetaClass, 
						recursive ).toList();
		
		for (IRPModelElement theExistingEl : theExistingEls) {
			
			if (theExistingEl.getName().equals( theProposedName )){
				count++;
				break;
			}
		}
		
		if (count > 1){
			super.warning( "Warning in isElementNameUnique, there are " + count + " elements called " + 
					theProposedName + " of type " + ofMetaClass + " in the project. This may cause issues.");
		}
				
		boolean isUnique = (count == 0);

		return isUnique;
	}
	
	public String determineUniqueNameForPackageBasedOn(
			String theProposedNameWithoutPkg,
			IRPModelElement underElement ){
		
		int count = 0;
		
		String theUniqueName = theProposedNameWithoutPkg;
		
		while( !isElementNameUnique(
				theUniqueName + "Pkg", "Package", underElement, 1 ) ){
			
			count++;
			theUniqueName = theProposedNameWithoutPkg + count;
		}
		
		return theUniqueName;
	}
	
	public String determineUniqueNameBasedOn(
			String theProposedName,
			String ofMetaClass,
			IRPModelElement underElement){
		
		int count = 0;
		
		String theUniqueName = theProposedName;
		
		while( !isElementNameUnique(
				theUniqueName, ofMetaClass, underElement, 1 ) ){
			
			count++;
			theUniqueName = theProposedName + count;
		}
		
		return theUniqueName;
	}
	
	public void deleteIfPresent(
			String theElementWithName, 
			String andMetaClass, 
			IRPModelElement nestedUnderEl ){
		
		IRPModelElement theEl = nestedUnderEl.findNestedElementRecursive( theElementWithName, andMetaClass );
		
		if( theEl != null ){
			
			IRPCollection theNestedEls = theEl.getNestedElementsRecursive();
			
			int count = theNestedEls.getCount();
			
			if( count > 1 ){
				super.warning( "Decided against deleting " + super.elInfo( theEl ) + 
						" as there are " + count + " of them" );
			} else {
				super.info( super.elInfo( theEl ) + " was deleted from " + super.elInfo( nestedUnderEl ) );
				theEl.deleteFromProject();
			}
		}
	}
	
	public List<String> getListFromCommaSeparatedString(
			IRPModelElement theContextEl,
			String thePropertyKey ){
		
		List<String> theNewTerms = new ArrayList<>();
		
		String theValue = theContextEl.getPropertyValue( 
				thePropertyKey );
		
		if( theValue != null && !theValue.isEmpty() ){
			
			String[] split = theValue.split(",");
			
			for (int i = 0; i < split.length; i++) {
				String theNewTerm = split[ i ].trim();
				theNewTerms.add( theNewTerm );
			}
		}
		
		return theNewTerms;
	}
	
	protected IRPPackage getOwningPackageFor(
			IRPModelElement theElement ){

		IRPPackage theOwningPackage = null;

		if( theElement == null ){

			super.warning( "getOwningPackage for was invoked for a null element" );

		} else if( theElement instanceof IRPPackage ){
			theOwningPackage = (RPPackage)theElement;

		} else if( theElement instanceof IRPProject ){
			super.warning( "Unable to find an owning package for " + theElement.getFullPathNameIn() + " as I reached project" );

		} else {
			theOwningPackage = getOwningPackageFor( theElement.getOwner() );
		}

		return theOwningPackage;
	}
	
	public IRPPackage getPackageForSelectedEl(){
		return getOwningPackageFor( getSelectedElement() );
	}
	
	public boolean isLegalName(
			String theName, 
			IRPModelElement basedOnContext ){
		
		//String regEx = "^(([a-zA-Z_][a-zA-Z0-9_]*)|(operator.+))$";
		
		String theRegEx = basedOnContext.getPropertyValue( 
				"General.Model.NamesRegExp" );
		
		boolean isLegal = theName.matches( theRegEx );
		
		if( !isLegal ){
			super.warning( "Warning, detected that " + theName 
					+ " is not a legal name as it does not conform to the regex=" + theRegEx );
		}
		
		return isLegal;
	}
	
	public String toLegalClassName(String theInput) {
		
		StringBuilder nameBuilder = new StringBuilder(theInput.length());    

		boolean capitalizeNextChar = true;

		int n = 1;
		final int max = 40;
		
		for (char c:theInput.toCharArray()) {
			if (Character.isJavaIdentifierPart(c)){
				if (capitalizeNextChar) {
					nameBuilder.append(Character.toUpperCase(c));
				} else {
					if (n==1){
						nameBuilder.append(Character.toLowerCase(c));
					} else {
						nameBuilder.append(c);
					}

				}
				capitalizeNextChar = false;
			} else if (Character.isSpaceChar(c)){
				
				if (n<max){
					capitalizeNextChar = true;
					continue;
				} else {
					break;
				}
			}
			n++;
		}
		
		return nameBuilder.toString();
	}
	
	public String makeLegalName(
			String theInput ){
		
		StringBuilder nameBuilder = new StringBuilder(theInput.length());    
		
		for (char c:theInput.toCharArray()) {
			if (Character.isJavaIdentifierPart(c)){
				nameBuilder.append(c);

			} else { //if (Character.isSpaceChar(c)){
				
				nameBuilder.append("_");
			}
		}
		
		return nameBuilder.toString();
	}

	public String toMethodName(
			String theInput,
			int max ) {
		
		StringBuilder nameBuilder = new StringBuilder(theInput.length());    

		boolean capitalizeNextChar = false;

		int n = 1;
		
		for (char c:theInput.toCharArray()) {
			if (Character.isJavaIdentifierPart(c)){
				if (capitalizeNextChar) {
					nameBuilder.append(Character.toUpperCase(c));
				} else {
					if (n==1){
						nameBuilder.append(Character.toLowerCase(c));
					} else {
						nameBuilder.append(c);
					}

				}
				capitalizeNextChar = false;
			} else if (Character.isSpaceChar(c)){
				
				if (n<max){
					capitalizeNextChar = true;
					continue;
				} else {
					break;
				}
			}
			n++;
		}
		
		return nameBuilder.toString();
	}

	public String decapitalize(final String line){
		String theResult = null;
		
		if (line.length() > 1){
			theResult = Character.toLowerCase(line.charAt(0)) + line.substring(1);
		} else {
			theResult = line;
		}
		
		return theResult;	
	}
	
	public String capitalize(
			final String line ){
		
		String theResult = null;
		
		if( line.length() > 0 ){
			theResult = Character.toUpperCase(
					line.charAt(0) ) + line.substring( 1 );
		} else {
			theResult = line;
		}
		
		return theResult;
	}
	
	
	public IRPModelElement launchDialogToSelectElement(
			List<IRPModelElement> inList, 
			String messageToDisplay, 
			Boolean isFullPathRequested){
		
		IRPModelElement theEl = null;

		List<String> nameList = new ArrayList<String>();

		nameList.add("Nothing");

		for (int i = 0; i < inList.size(); i++) {
			if (isFullPathRequested){
				nameList.add(i, inList.get(i).getFullPathName());
			} else {
				nameList.add(i, inList.get(i).getName());
			}
		} 	

		Object[] options = nameList.toArray();

		JDialog.setDefaultLookAndFeelDecorated(true);

		String selectedElementName = (String) JOptionPane.showInputDialog(
				null,
				messageToDisplay,
				"Input",
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[0]);

		if( selectedElementName != null && 
			!selectedElementName.equals("Nothing") ){

			int index = nameList.indexOf(selectedElementName);
			theEl = inList.get(index);
			super.debug( super.elInfo( theEl ) + "was chosen");

		} else {
			super.debug("'Nothing' was chosen by user");
			theEl = null;
		}

		return theEl;
	}
	
	public IRPStereotype applyExistingStereotype(
			String withTheName, 
			IRPModelElement toTheEl ){
		
		IRPStereotype theChosenStereotype = 
				getExistingStereotype( withTheName, toTheEl.getProject() );
		
		if( theChosenStereotype != null ){
			toTheEl.setStereotype( theChosenStereotype );
		} else {
			super.error("Warning in applyExistingStereotype, unable to find stereotype <<" + 
					withTheName + ">> underneath " + super.elInfo( toTheEl.getProject() ) );
		}
		
		return theChosenStereotype;
	}
	
	public IRPStereotype getExistingStereotype(
			String withTheName,
			IRPModelElement underneathTheEl ){

		List<IRPModelElement> theStereotypeEls = 
				findElementsWithMetaClassAndName(
						"Stereotype", withTheName, underneathTheEl );
		
		IRPStereotype theChosenStereotype = null;
		boolean isNewTermFound = false;
		
		for( IRPModelElement theStereotypeEl : theStereotypeEls ){
			
			IRPStereotype theStereotype = (IRPStereotype)theStereotypeEl;
			
			// Favour new term stereotypes
			if( theStereotype.getIsNewTerm()==1 ){
				isNewTermFound = true;
				theChosenStereotype = theStereotype;
				
				if( theStereotypeEls.size() > 1 ){
					
					super.warning("getExistingStereotype has chosen " + super.elInfo( theStereotype ) + 
							" as it is a new term (there were x" + 
							theStereotypeEls.size() + " stereotypes with the same name)");
				}
			} else if( !isNewTermFound ){
				theChosenStereotype = theStereotype;
			}
		}
		
		return theChosenStereotype;
	}
	
	public IRPStereotype getStereotypeCalled(
			String theName, 
			IRPModelElement onTheEl){
		
		int count = 0;
		IRPStereotype theFoundStereotype = null;
		
		@SuppressWarnings("unchecked")
		List <IRPStereotype> theStereotypes = onTheEl.getStereotypes().toList();
		
		for (IRPStereotype theStereotype : theStereotypes) {
			if (theStereotype.getName().equals(theName)){
				
				theFoundStereotype = theStereotype;
				count++;
			}
		}
		
		if (count > 1){
			super.warning("Warning in getStereotypeCalled, found " + count 
					+ " elements that are called " + theName);
		}
		
		return theFoundStereotype;
	}
	
	public Boolean hasStereotypeCalled(
			String theName, 
			IRPModelElement onTheEl){
		
		Boolean isFound = false;
		
		@SuppressWarnings("unchecked")
		List <IRPStereotype> theStereotypes = onTheEl.getStereotypes().toList();
		
		for (IRPStereotype theStereotype : theStereotypes) {
			
			String theStereotypeName = theStereotype.getName();
			
			if( theStereotypeName.equals( theName ) ){
				isFound = true;
			} else if( theStereotype.getIsNewTerm()==1 ){
				
				try {
					// This copes with situation whereby the actual name of stereotype 
					// differs from it's name set via a property
					theStereotypeName = theStereotype.getPropertyValueExplicit( "Model.Stereotype.Name" );
					
					if( theStereotypeName.equals( theName ) ){
						isFound = true;
					}
				} catch ( Exception e ){
					// Silent exception will occur if property is not set
				}
			}
		}
		
		return isFound;
	}
	
	public IRPStereotype getStereotypeAppliedTo(
			IRPModelElement theElement, 
			String thatMatchesRegEx ){
				
		IRPStereotype foundStereotype = null;
		
		@SuppressWarnings("unchecked")
		List<IRPStereotype> theCandidateStereotypes = theElement.getStereotypes().toList();
		List<IRPStereotype> theMatchingStereotypes = new ArrayList<IRPStereotype>();
		
		for( IRPStereotype theCandidateStereotype : theCandidateStereotypes ){
			
			String theName = theCandidateStereotype.getName();
			
			if( theName.matches( thatMatchesRegEx ) ){
				theMatchingStereotypes.add( theCandidateStereotype );
			}		
		}
		
		int count = theMatchingStereotypes.size();
		
		if( count == 1 ){
			
			foundStereotype = theMatchingStereotypes.get( 0 );
			
		} else if( count > 1 ){
			
			super.warning(
					"Warning in getStereotypeAppliedTo, there are multiple stereotypes related to " + 
					super.elInfo(theElement) + " size=" + theMatchingStereotypes.size() + 
					"matching regex=" + thatMatchesRegEx );
			
			foundStereotype = theMatchingStereotypes.get( 0 );
		} 
		
		return foundStereotype;
	}
	
	public IRPModelElement findElementWithMetaClassAndName(
			String theMetaClass, 
			String andName, 
			IRPModelElement underneathTheEl){
		
		IRPModelElement theElement = null;
		
		List <IRPModelElement> theMatches = findElementsWithMetaClassAndName(
				theMetaClass, andName, underneathTheEl);

		if (theMatches.size()==1){
		
			theElement = theMatches.get(0);
		
		} else if (theMatches.size()>1){
			
			super.warning("Warning in findElementWithMetaClassAndName(" + theMetaClass + "," + 
					andName + ","+super.elInfo(underneathTheEl)+"), " + theMatches.size() + 
					" elements were found when I was expecting only one");
			
			theElement = theMatches.get(0);
		}
		
		return theElement;
	}

	public IRPModelElement findNestedElementUnder( 
			IRPClassifier theElement,
			String withName,
			String andMetaClass,
			boolean isIncludeBases ){
		
		IRPModelElement theNestedElement = 
				theElement.findNestedElement( withName, andMetaClass );
		
		if( theNestedElement == null && isIncludeBases ){
			
			@SuppressWarnings("unchecked")
			List<IRPModelElement> theBaseClassifiers = 
					theElement.getBaseClassifiers().toList();
		
			for( IRPModelElement theBaseClassifier : theBaseClassifiers ) {
				
				theNestedElement = findNestedElementUnder( 
						(IRPClassifier) theBaseClassifier, 
						withName, 
						andMetaClass, 
						isIncludeBases );
				
				if( theNestedElement != null ){
					break;
				}
			}
		}
		
		return theNestedElement;
	}
		
	public List<IRPModelElement> findElementsWithMetaClassAndName(
			String theMetaClass, 
			String andName, 
			IRPModelElement underneathTheEl ){
		
		List<IRPModelElement> theElements = new ArrayList<IRPModelElement>();
		
		@SuppressWarnings("unchecked")
		List <IRPModelElement> theCandidates = 
				underneathTheEl.getNestedElementsByMetaClass( 
						theMetaClass, 
						1 ).toList();
		
		for( IRPModelElement theCandidate : theCandidates ){
			if( theCandidate.getName().equals( andName ) ){
				theElements.add( theCandidate );
			}
		}
				
		return theElements;
	}
	
	public List<IRPModelElement> findElementsWithMetaClassAndStereotype(
			String theMetaClass, 
			String andStereotype, 
			IRPModelElement underneathTheEl,
			int recursive ){
		
		List<IRPModelElement> theFilteredList = new ArrayList<IRPModelElement>();
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theCandidates = 
				underneathTheEl.getNestedElementsByMetaClass(
						theMetaClass, recursive ).toList();

		for( IRPModelElement theCandidate : theCandidates ){
			
			if( hasStereotypeCalled( andStereotype, theCandidate ) ){
				theFilteredList.add( theCandidate );
			}
		}
		
		return theFilteredList;
	}
	
	public List<IRPModelElement> findElementsWithMetaClassStereotypeAndName(
			String theMetaClass, 
			String andStereotype, 
			String andName, 
			IRPModelElement underneathTheEl ){
		
		List <IRPModelElement> theFilteredList = new ArrayList<IRPModelElement>();
		
		List <IRPModelElement> theCandidates = 
				findElementsWithMetaClassAndName(
						theMetaClass, 
						andName, 
						underneathTheEl );

		for( IRPModelElement theCandidate : theCandidates) {
			
			if( hasStereotypeCalled( andStereotype, theCandidate ) ){
				theFilteredList.add( theCandidate );
			}
		}
		
		return theFilteredList;
	}
	
	public void copyRequirementTraceabilityFrom(
			IRPModelElement theElement,
			IRPModelElement toTheElement ){

		@SuppressWarnings("unchecked")
		List<IRPDependency> theDependenciesOnSource = theElement.getDependencies().toList();

		for( IRPDependency theDependencyOnSource : theDependenciesOnSource ){

			IRPModelElement theDependsOn = theDependencyOnSource.getDependsOn();

			if( theDependsOn instanceof IRPRequirement ){

				IRPStereotype theStereotype = 
						getStereotypeAppliedTo( 
								theDependencyOnSource, ".*" );

				if( theStereotype != null ){
					addStereotypedDependencyIfOneDoesntExist(
							toTheElement, theDependsOn, theStereotype );
				}

			}
		}
	}

	public int countStereotypedDependencies(
			IRPModelElement fromElement, 
			IRPModelElement toElement,
			String stereotypeName ){

		List<IRPModelElement> existingDeps = 
				findElementsWithMetaClassAndStereotype(
						"Dependency", stereotypeName, fromElement, 0 );

		int isExistingFoundCount = 0;

		for( IRPModelElement theExistingDep : existingDeps ){

			IRPDependency theDep = (IRPDependency)theExistingDep;
			IRPModelElement theDependsOn = theDep.getDependsOn();

			if( theDependsOn.equals( toElement )){
				isExistingFoundCount++;
			}
		}

		return isExistingFoundCount;
	}

	public IRPDependency getExistingStereotypedDependency(
			IRPModelElement fromElement, 
			IRPModelElement toElement,
			String stereotypeName ){

		IRPDependency theExistingDependency = null;

		if( fromElement instanceof IRPInstance ){
			IRPInstance theInstance = (IRPInstance)fromElement;
			IRPClassifier theClassifier = theInstance.getOtherClass();
			
			fromElement = theClassifier;
		}
		
		if( toElement instanceof IRPInstance ){
			IRPInstance theInstance = (IRPInstance)toElement;
			IRPClassifier theClassifier = theInstance.getOtherClass();
			
			toElement = theClassifier;
		}
		
		List<IRPDependency> theExistingDependencies = 
				getExistingStereotypedDependencies(
						fromElement, 
						toElement,
						stereotypeName );

		if( theExistingDependencies.size() > 0 ){

			if( theExistingDependencies.size() > 1 ){

				super.warning( "Duplicate «" + stereotypeName + 
						"» dependencies to " + elInfo( toElement ) + 
						" were found on " + elInfo( fromElement ) );
			}

			theExistingDependency = theExistingDependencies.get( 0 );
		} 

		return theExistingDependency;
	}

	@SuppressWarnings("unchecked")
	public List<IRPDependency> getExistingStereotypedDependencies(
			IRPModelElement fromElement, 
			IRPModelElement toElement,
			String stereotypeName ){

		List<IRPDependency> existingDeps = new ArrayList<>();

		super.debug( "getExistingStereotypedDependencies invoked to find " + stereotypeName + 
				" from " + super.elInfo( fromElement ) + " to " +
				super.elInfo( toElement ) );

		if( fromElement instanceof IRPInstance ){
			IRPInstance theInstance = (IRPInstance)fromElement;
			IRPClassifier theClassifier = theInstance.getOtherClass();
			
			fromElement = theClassifier;
		}
		
		if( toElement instanceof IRPInstance ){
			IRPInstance theInstance = (IRPInstance)toElement;
			IRPClassifier theClassifier = theInstance.getOtherClass();
			
			toElement = theClassifier;
		}
				
		List<IRPGeneralization> theGeneralizations = new ArrayList<>();

		if( fromElement instanceof IRPClassifier ){

			IRPClassifier theClassifier = (IRPClassifier)fromElement;

			theGeneralizations.addAll(
					theClassifier.getGeneralizations().toList() );
		}

		for( IRPGeneralization theGeneralization : theGeneralizations ){

			IRPClassifier theDerivedClass = theGeneralization.getDerivedClass();

			List<IRPDependency> existingDerivedClassDependencies =
					getExistingStereotypedDependencies(
							theDerivedClass, 
							toElement, 
							stereotypeName );

			existingDeps.addAll( existingDerivedClassDependencies );
		}

		List<IRPModelElement> candidates = 
				findElementsWithMetaClassAndStereotype(
						"Dependency", 
						stereotypeName, 
						fromElement, 
						0 ); // not recursive

		for( IRPModelElement candidate : candidates ){

			IRPDependency theDependency = (IRPDependency)candidate;
			IRPModelElement theDependsOn = theDependency.getDependsOn();

			if( theDependsOn.equals( toElement ) ){
				existingDeps.add( (IRPDependency) candidate );
			}
		}

		return existingDeps;
	}

	public IRPDependency addStereotypedDependencyIfOneDoesntExist(
			IRPModelElement fromElement, 
			IRPModelElement toElement,
			IRPStereotype theStereotype ){

		IRPDependency theDependency = null;

		int isExistingFoundCount = 
				countStereotypedDependencies(
						fromElement, 
						toElement, 
						theStereotype.getName() );

		if( isExistingFoundCount==0 ){
			theDependency = fromElement.addDependencyTo( toElement );
			theDependency.setStereotype( theStereotype );

			super.info( "Added a «" + theStereotype.getName() + "» dependency to " + 
					super.elInfo( fromElement ) + 
					" (to " + super.elInfo( toElement ) + ")" );				
		} else {
			super.info( "Skipped adding a «" + theStereotype.getName() + "» dependency to " + super.elInfo( fromElement ) + 
					" (to " + super.elInfo( toElement ) + 
					") as " + isExistingFoundCount + " already exists" );
		}

		return theDependency;
	}

	public Set<IRPRequirement> getRequirementsThatTraceFrom(
			IRPModelElement theElement, boolean withWarning){

		Set<IRPRequirement> theReqts = new HashSet<IRPRequirement>();

		@SuppressWarnings("unchecked")
		List<IRPDependency> theExistingDeps = theElement.getDependencies().toList();

		for (IRPDependency theDependency : theExistingDeps) {

			IRPModelElement theDependsOn = theDependency.getDependsOn();

			if (theDependsOn != null && theDependsOn instanceof IRPRequirement){

				IRPRequirement theReqt = (IRPRequirement)theDependsOn; 

				if (!theReqts.contains( theReqt )){

					theReqts.add( (IRPRequirement) theDependsOn );

				} else if (withWarning){

					super.warning( "Duplicate dependency to " + super.elInfo( theDependsOn ) + 
							" was found on " + super.elInfo( theElement ));
				} 			
			}
		}

		return theReqts;
	}

	public Set<IRPRequirement> getRequirementsThatTraceFromWithStereotype(
			IRPModelElement theElement, String withDependencyStereotype){

		Set<IRPRequirement> theReqts = new HashSet<IRPRequirement>();

		@SuppressWarnings("unchecked")
		List<IRPDependency> theExistingDeps = theElement.getDependencies().toList();

		for (IRPDependency theDependency : theExistingDeps) {

			IRPModelElement theDependsOn = theDependency.getDependsOn();

			if (theDependsOn != null && theDependsOn instanceof IRPRequirement){

				if (hasStereotypeCalled( withDependencyStereotype, theDependency) ){
					theReqts.add( (IRPRequirement) theDependsOn );
				}	
			}
		}

		return theReqts;
	}

	public Set<IRPModelElement> getStereotypedElementsThatHaveDependenciesFrom(
			IRPModelElement theElement, 
			String whereDependsOnHasStereotype ){

		Set<IRPModelElement> theEls = new HashSet<IRPModelElement>();

		@SuppressWarnings("unchecked")
		List<IRPDependency> theExistingDeps = theElement.getDependencies().toList();

		for( IRPDependency theDependency : theExistingDeps ){

			IRPModelElement theDependsOn = theDependency.getDependsOn();

			if( theDependsOn != null && 
					theDependsOn instanceof IRPModelElement ){
				
				if( hasStereotypeCalled( 
						whereDependsOnHasStereotype, theDependsOn ) ){

					theEls.add( theDependsOn );
				}	
			}
		}

		return theEls;
	}
	
	public Set<IRPModelElement> getElementsThatHaveStereotypedDependenciesFrom(
			IRPModelElement theElement, 
			String withDependencyStereotype ){

		Set<IRPModelElement> theEls = new HashSet<IRPModelElement>();

		@SuppressWarnings("unchecked")
		List<IRPDependency> theExistingDeps = theElement.getDependencies().toList();

		for( IRPDependency theDependency : theExistingDeps ){

			IRPModelElement theDependsOn = theDependency.getDependsOn();

			if( theDependsOn != null && theDependsOn instanceof IRPModelElement ){

				if( hasStereotypeCalled( 
						withDependencyStereotype, theDependency) ){

					theEls.add( theDependsOn );
				}	
			}
		}

		return theEls;
	}
	

	public List<File> getFilesMatching(
			String theRegEx, 
			String inThePath ){

		List<File> theFiles = new ArrayList<File>();

		File theDirectory = new File( inThePath );

		File[] theCandidateFiles = theDirectory.listFiles();

		if( theCandidateFiles != null ){
			for (File theCandidateFile : theCandidateFiles){

				if (theCandidateFile.isFile() && 
						theCandidateFile.getName().matches( theRegEx )){

					super.debug("getFilesMatching found: " + theCandidateFile.getAbsolutePath());
					theFiles.add( theCandidateFile );
				}		            
			}		    		        
		}

		return theFiles;
	}
	
	public List<IRPActivityDiagram> buildListOfActivityDiagramsFor(
			List<IRPModelElement> theSelectedEls) {
		
		List<IRPActivityDiagram> theADs = new ArrayList<IRPActivityDiagram>();
		
		for (IRPModelElement theSelectedEl : theSelectedEls) {
			
			@SuppressWarnings("unchecked")
			List<IRPActivityDiagram> theCandidates = theSelectedEl.getNestedElementsByMetaClass("ActivityDiagramGE", 1).toList();
			
			for (IRPActivityDiagram theCandidate : theCandidates) {
				if (!theADs.contains(theCandidate)){
					
					if (theCandidate.isReadOnly()==0){
						theADs.add(theCandidate);			
						super.debug("Adding " + super.elInfo(theCandidate.getOwner()) + " to the list");
					} else {
						super.debug("Skipping " + super.elInfo(theCandidate.getOwner()) + " as it is read-only");
					}
				}
			}
		}
		
		return theADs;
	}
	
	public IRPFlowchart getTemplateForActivityDiagram(
			IRPModelElement basedOnContext,
			String basedOnPropertyKey ){
		
		IRPFlowchart theTemplate = null;
		
		String theTemplateName = 
				basedOnContext.getPropertyValue( 
						basedOnPropertyKey );
		
		if( theTemplateName != null && 
			!theTemplateName.trim().isEmpty() ){
			
			theTemplate = (IRPFlowchart) basedOnContext.getProject().findNestedElementRecursive(
							theTemplateName, 
							"ActivityDiagram" );
			
			if( theTemplate == null ){
				super.warning( "Warning, unable to find template called " + 
						theTemplateName + " named in TemplateForActivityDiagram property" );
			}
		}
		
		super.debug( "getTemplateForActivityDiagram, found " + super.elInfo( theTemplate ) );
		
		return theTemplate;
	}
	
	public IRPGraphElement getCorrespondingGraphElement(
			IRPModelElement forElement,
			IRPActivityDiagram theAD ) throws Exception {

		IRPGraphElement ret = null;

		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theGraphEls = 
				theAD.getCorrespondingGraphicElements( forElement ).toList();
		
		if( theGraphEls.size() > 1 ){
			throw new Exception("There is more than one graph element for " + super.elInfo(forElement));
		} else if( theGraphEls.size() == 1 ){
			ret = theGraphEls.get( 0 );
		} else {
			super.warning("Warning, getCorrespondingGraphElement dif not find a graph element corresponding to " + 
					super.elInfo(forElement) + " on " + super.elInfo(theAD) );
		}
		
		return ret;
	}
	
	public IRPGraphElement getGraphElement(
			IRPModelElement element,
			IRPFlowchart fc) {

		IRPGraphElement ret = null;

		@SuppressWarnings("unchecked")
		List<IRPGraphElement> gList = fc.getGraphicalElements().toList();
		
		if (!gList.isEmpty()) {
			for (IRPGraphElement g : gList) {
				if (g.getModelObject() != null) {
					if(g.getModelObject().getGUID().equals(element.getGUID())) {

						ret = g;
						break;
					}
				}
			}
		}
		
		return ret;
	}
	
	public IRPPin getPin(
			String withName,
			IRPAcceptEventAction onAcceptEventAction ){
		
		IRPPin thePin = null;
		
		List<IRPPin> theCandidates = getPins( onAcceptEventAction );
		
		for( IRPPin theCandidate : theCandidates ){
			
			if( theCandidate.getName().equals( withName ) ){
				thePin = theCandidate;
			}
		}
		
		return thePin;
	}
	
	public List<IRPPin> getPins(
			IRPAcceptEventAction onAcceptEventAction ){
		
		List<IRPPin> thePins = new ArrayList<>();
		
		for( Object o: onAcceptEventAction.getSubStateVertices().toList()){
			if( o instanceof IRPPin ){
			
				thePins.add( (IRPPin) o );
			}
		}
		
		return thePins;
	}
	
	public IRPStateVertex getTargetOfOutTransitionIfSingleOneExisting(
			IRPStateVertex theStateVertex ){
		
		IRPStateVertex theTarget = null;
		
		@SuppressWarnings("unchecked")
		List<IRPTransition> theOutTransitions = theStateVertex.getOutTransitions().toList();
		
		if( theOutTransitions.size() == 1 ){
			
			IRPTransition theTransition = theOutTransitions.get( 0 );
			theTarget = theTransition.getItsTarget();
		}
		
		return theTarget;
	}
	
	public String determineUniqueStateBasedOn(
			String theProposedName,
			IRPState underElement ){
		
		int count = 0;
		
		String theUniqueName = theProposedName;
		
		while( !isStateUnique(
				theUniqueName, underElement ) ){
			
			count++;
			theUniqueName = theProposedName + count;
		}
		
		return theUniqueName;
	}
	
	public boolean isStateUnique(
			String theProposedName, 
			IRPState underneathTheEl ){
				
		int count = 0;
		
		@SuppressWarnings("unchecked")
		List<IRPState> theExistingEls = 
				underneathTheEl.getSubStates().toList();
		
		for (IRPModelElement theExistingEl : theExistingEls) {
			
			if (theExistingEl.getName().equals( theProposedName )){
				count++;
				break;
			}
		}
		
		if (count > 1){
			super.warning("Warning in isStateUnique, there are " + count + " elements called " + 
					theProposedName + ". This may cause issues.");
		}
				
		boolean isUnique = (count == 0);

		return isUnique;
	}
	
	public void dumpGraphicalPropertiesFor(
			IRPGraphElement theGraphEl){
	 
		@SuppressWarnings("unchecked")
		List<IRPGraphicalProperty> theGraphProperties = theGraphEl.getAllGraphicalProperties().toList();
		
		super.info("---------------------------");
		for (IRPGraphicalProperty theGraphicalProperty : theGraphProperties) {
			
			super.info(theGraphicalProperty.getKey() + "=" + theGraphicalProperty.getValue());
		}
		super.info("---------------------------"); 
	}
	
	public String buildStringFromModelEls(
			List<? extends IRPModelElement> theEls,
			int max ){
		
		String theString = "";

		int count = 0;
		
		for( Iterator<? extends IRPModelElement> iterator = theEls.iterator(); iterator.hasNext(); ) {
			
			count++;
			IRPModelElement theEl = (IRPModelElement) iterator.next();

			theString += count + ". " + super.elInfo( theEl ) + " \n";
			
			if( count >= max ){
				theString += "... (" + theEls.size() + " in list) \n";
				break;
			}
		}
		
		return theString;
	}
	
	public String buildStringFrom(
			List<String> theList, 
			int max ){
		
		String theString = "";
		
		int count = 0;
		
		for( Iterator<String> iterator = theList.iterator(); iterator.hasNext(); ) {
			
			count++;
			String string = (String) iterator.next();

			theString += string + " \n";
			
			if( count >= max ){
				theString += "... ( " + theList.size() + ") \n";
				break;
			}
		}
		
		return theString;
	}
	
	public void browseAndAddUnit(
			IRPProject inProject, 
			boolean relative ){

		JFileChooser theFileChooser = new JFileChooser( System.getProperty("user.dir") );
		theFileChooser.setFileFilter( new FileNameExtensionFilter( "Package", "sbs" ) );

		int choice = theFileChooser.showDialog( null, "Choose Unit (.sbs)" );

		if( choice==JFileChooser.CANCEL_OPTION ){
			super.info("Operation cancelled by user when trying to choose Unit (.sbs)");

		} else if( choice==JFileChooser.APPROVE_OPTION ){

			File theFile = theFileChooser.getSelectedFile();

			super.debug("theFile.getAbsolutePath = " + theFile.getAbsolutePath() );
			super.debug("theFile.getName = " + theFile.getName() );
			super.debug("theFile.getParent = " + theFile.getParent() );
			super.debug("theFile.getPath = " + theFile.getPath() );
			super.debug("theFile.getParentFile().getName() = " + theFile.getParentFile().getName() );
			
			String theTargetPath;

			try {
				theTargetPath = theFile.getCanonicalPath();

				super.debug( "theTargetPath=" + theTargetPath );
				
				SysMLHelperPlugin.getRhapsodyApp().addToModelByReference( theTargetPath );

				if( relative ){

					String theFileNameIncludingExtension = theFile.getName();
					
					String theName = theFileNameIncludingExtension.substring(0, theFileNameIncludingExtension.length()-3);

					int trimSize = theName.length()+5;
					
					Path targetPath = Paths.get( theTargetPath.substring(0, theTargetPath.length()-trimSize) );
					Path targetRoot = targetPath.getRoot();
					
					super.debug( "targetRoot.toString()=" + targetRoot.toString() );

					Path sourcePath = Paths.get( 
							inProject.getCurrentDirectory().replaceAll(
									inProject.getName()+"$", "") );

					Path sourceRoot = sourcePath.getRoot();

					super.debug( "sourceRoot.toString()=" + sourceRoot.toString() );
					
					if( !targetRoot.equals( sourceRoot ) ){
						super.debug("Unable to set Unit called " + theName + " to relative, as the drive letters are different");
						super.debug("theTargetDir root =" + targetPath.getRoot());
						super.debug("theTargetDir=" + targetPath);
						super.debug("theSourceDir root =" + sourcePath.getRoot());
						super.debug("theSourceDir=" + sourcePath);
					} else {
						Path theRelativePath = sourcePath.relativize(targetPath);

						IRPModelElement theCandidate = inProject.findAllByName( theName, "Package" );

						if( theCandidate != null && theCandidate instanceof IRPPackage ){

							IRPPackage theAddedPackage = (IRPPackage)theCandidate;

							theAddedPackage.setUnitPath( "..\\..\\" + theRelativePath.toString() );

							super.info( "Unit called " + theName + 
									".sbs was changed from absolute path='" + theTargetPath + 
									"' to relative path='" + theRelativePath + "'" );
						}
					}
				}
			} catch( IOException e ){
				super.error( "Error, unhandled IOException in RelativeUnitHandler.browseAndAddUnit");
			}
		}
	}
	
	public IRPState getStateCalled(
			String theName, 
			IRPStatechart inTheDiagram, 
			IRPModelElement ownedByEl ){
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theElsInDiagram = 
			inTheDiagram.getElementsInDiagram().toList();
		
		IRPState theState = null;
		
		int count = 0;
		
		for( IRPModelElement theEl : theElsInDiagram ){
			
			if( theEl instanceof IRPState 
					&& theEl.getName().equals( theName )
					&& getOwningClassifierFor( theEl ).equals( ownedByEl ) ){
				
				super.debug( "Found state called " + theEl.getName() + 
						" owned by " + theEl.getOwner().getFullPathName() );
				
				theState = (IRPState) theEl;
				count++;
			}
		}
		
		if (count != 1){
			super.warning( "Warning in getStateCalled (" + count + 
					") states called " + theName + " were found" );
		}
		
		return theState;
	}
	
	private IRPModelElement getOwningClassifierFor(
			IRPModelElement theState ){
		
		IRPModelElement theOwner = theState.getOwner();
		
		while( theOwner.getMetaClass().equals( "State" ) || 
			   theOwner.getMetaClass().equals( "Statechart" ) ){
			
			theOwner = theOwner.getOwner();
		}
		
		super.debug( "The owner for " + super.elInfo( theState ) + 
				" is " + super.elInfo( theOwner ) );
			
		return theOwner;
	}	

	public IRPGraphElement findGraphEl(
			IRPClassifier theClassifier, 
			String withTheName ){
		
		IRPGraphElement theFoundGraphEl = null;
		
		@SuppressWarnings("unchecked")
		List<IRPStatechartDiagram> theStatechartDiagrams = 
				theClassifier.getStatechart().getNestedElementsByMetaClass(
						"StatechartDiagram", 1 ).toList();
		
		for (IRPStatechartDiagram theStatechartDiagram : theStatechartDiagrams) {
			
			super.debug( super.elInfo(theStatechartDiagram) + " was found owned by " + 
					super.elInfo( theClassifier ) );
			
			@SuppressWarnings("unchecked")
			List<IRPGraphElement> theGraphEls = 
				theStatechartDiagram.getGraphicalElements().toList();
			
			for( IRPGraphElement theGraphEl : theGraphEls ){
				
				IRPModelElement theEl = theGraphEl.getModelObject();
				
				if( theEl != null ){
					super.debug( "Found " + theEl.getMetaClass() + 
							" called " + theEl.getName() );
					
					if( theEl.getName().equals( withTheName ) ){
						
						super.debug( "Success, found GraphEl called " + 
								withTheName + " in statechart for " + 
								super.elInfo( theClassifier ) );
						
						theFoundGraphEl = theGraphEl;
						break;
					}
				}
			}
		}
		
		return theFoundGraphEl;
	}
	
	public void cleanUpModelRemnants( 
			IRPProject inProject ){
		
		deleteIfPresent( "Structure1", "StructureDiagram", inProject );
		deleteIfPresent( "Model1", "ObjectModelDiagram", inProject );
		deleteIfPresent( "Default", "Package", inProject );
		
		IRPModelElement theDefaultComponent = 
				inProject.findElementsByFullName("DefaultComponent", "Component");
		
		if( theDefaultComponent != null ){
			theDefaultComponent.setName( "NotUsedComp" );
			
			IRPModelElement theDefaultConfig = 
					theDefaultComponent.findNestedElement("DefaultConfig", "Configuration");
			
			if( theDefaultConfig != null ){
				theDefaultConfig.setName( "NotUsedComp" );
			}
		}
	}
	
	public String getStringForTagCalled( 
			String theTagName,
			IRPModelElement onElement,
			String defaultIfNotSet ){
	
		String theValue = defaultIfNotSet;
		
		IRPTag theTag = onElement.getTag( theTagName );
		
		if( theTag != null ){
			theValue = theTag.getValue();
			
			if( theValue.isEmpty() ){
				theValue = defaultIfNotSet;
			}
		}
		
		return theValue;
	}
	
	public IRPPackage getExistingOrCreateNewPackageWith( 
			String theName, 
			IRPModelElement underneathTheEl ){

		IRPModelElement thePackage = findElementWithMetaClassAndName(
				"Package", theName, underneathTheEl );

		if( thePackage == null ){

			super.info( "Create a package called " + theName );
			thePackage = underneathTheEl.addNewAggr( "Package", theName );
		}

		return (IRPPackage) thePackage;
	}

	public IRPModelElement getExistingOrCreateNewElementWith( 
			String theName, 
			String andMetaClass,
			IRPModelElement underneathTheEl ){

		IRPModelElement theElement =
				findElementWithMetaClassAndName(
						andMetaClass, theName, underneathTheEl );

		try {
			if( theElement == null ){
				theElement = underneathTheEl.addNewAggr( andMetaClass, theName );
			}

		} catch (Exception e) {
			super.error("Exception in getExistingOrCreateNewElementWith( theName " + theName + 
					", andMetaClass=" + andMetaClass + ", underneath=" + super.elInfo(underneathTheEl));
		}

		return theElement;
	}
	
	public void setStringTagValueOn( 
			IRPModelElement theOwner, 
			String theTagName, 
			String theValue ){
		
		IRPTag theTag = theOwner.getTag( theTagName );
		
		if( theTag != null ){
			theOwner.setTagValue( theTag, theValue );
		} else {
			
			super.error( "Error in GeneralHelpers.setStringTagValueOn for " + 
					super.elInfo( theOwner) + ", unable to find tag called " + theTagName );
		}
	}
	
	public IRPProfile addProfileIfNotPresent(
			String theProfileName ){

		IRPProfile theProfile = (IRPProfile) _rhpPrj.
				findNestedElement( theProfileName, "Profile" );

		if( theProfile == null ){

			IRPUnit theUnit = _rhpApp.addProfileToModel( theProfileName );

			if( theUnit != null ){

				theProfile = (IRPProfile)theUnit;
				super.info( "Added profile called " + theProfile.getFullPathName() );

			} else {
				super.error( "Error in addProfileIfNotPresent. No profile found with name " + theProfileName );
			}

		} else {
			super.debug( super.elInfo( theProfile ) + " is already present in the project" );
		}

		return theProfile;		
	}
	
	public void addAComponentWith(
			String theName,
			IRPPackage theBlockTestPackage, 
			IRPClass theUsageDomainBlock,
			String theSimulationMode ){
		
		IRPComponent theComponent = 
				(IRPComponent) theBlockTestPackage.addNewAggr(
						"Component", theName + "_EXE");
		
		theComponent.setPropertyValue("Activity.General.SimulationMode", theSimulationMode);

		IRPConfiguration theConfiguration = (IRPConfiguration) theComponent.findConfiguration("DefaultConfig");
		
		String theEnvironment = theConfiguration.getPropertyValue("CPP_CG.Configuration.Environment");
		
		theConfiguration.setName( theEnvironment );			
		theConfiguration.setPropertyValue("WebComponents.WebFramework.GenerateInstrumentationCode", "True");		
		theConfiguration.addInitialInstance( theUsageDomainBlock );
		theConfiguration.setScopeType("implicit");

		IRPConfiguration theNoWebConfig = theComponent.addConfiguration( theEnvironment + "_NoWebify" );			
		theNoWebConfig.setPropertyValue("WebComponents.WebFramework.GenerateInstrumentationCode", "False");		
		theNoWebConfig.addInitialInstance( theUsageDomainBlock );
		theNoWebConfig.setScopeType("implicit");

		theConfiguration.getProject().setActiveConfiguration( theConfiguration );		
	}

	public Set<IRPClassifier> getBaseClassesOf( 
			Set<IRPClassifier> theClassifiers ){
		
		Set<IRPClassifier> theBaseClasses = new HashSet<IRPClassifier>();
		
		for (IRPModelElement theEl : theClassifiers ){
			
			@SuppressWarnings("unchecked")
			List<IRPModelElement> theGeneralizations = 
					theEl.getNestedElementsByMetaClass("Generalization", 0).toList();
			
			for (IRPModelElement theGenEl : theGeneralizations) {
				IRPGeneralization theGeneralization = (IRPGeneralization)theGenEl;
				
				IRPClassifier theBaseClass = theGeneralization.getBaseClass();
				theBaseClasses.add( theBaseClass );
			}
		}
		
		return theBaseClasses;
	}
}

/**
 * Copyright (C) 2021  MBSE Training and Consulting Limited (www.executablembse.com)

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