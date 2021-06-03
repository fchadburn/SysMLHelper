package com.mbsetraining.sysmlhelper.common;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

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
}