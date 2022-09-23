package com.mbsetraining.sysmlhelper.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.telelogic.rhapsody.core.*;

public abstract class BaseContext {

	private static final String NOT_USED_COMP = "NotUsedComp";
	private static final String NOT_USED_CONFIG = "NotUsedConfig";

	private String _pluginVersionProperty;
	private String _userDefinedMetaClassesAsSeparateUnitProperty;
	//private String _allowPluginToControlUnitGranularityProperty;
	private String _enableWarningLoggingProperty;
	private String _enableErrorLoggingProperty;
	private String _enableDebugLoggingProperty;
	private String _enableInfoLoggingProperty;

	protected List<String> _userDefinedMetaClassesAsSeparateUnit;
	protected IRPStereotype _stereotypeToUseForFunctions;
	protected IRPStereotype _stereotypeToUseForActions;
	protected IRPStereotype _stereotypeToUseForUseCases;

	protected String _namesRegEx;
	protected RhpLog _rhpLog;
	protected IRPApplication _rhpApp;
	protected IRPProject _rhpPrj;

	public BaseContext(
			String theAppID,
			String enableErrorLoggingProperty,
			String enableWarningLoggingProperty,
			String enableInfoLoggingProperty,
			String enableDebugLoggingProperty,
			String pluginVersionProperty,
			String userDefinedMetaClassesAsSeparateUnitProperty,
			String allowPluginToControlUnitGranularityProperty ) {

		_rhpApp = RhapsodyAppServer.getActiveRhapsodyApplicationByID( theAppID );
		_rhpPrj = _rhpApp.activeProject();

		_enableWarningLoggingProperty = enableWarningLoggingProperty;
		_enableErrorLoggingProperty = enableErrorLoggingProperty;
		_enableDebugLoggingProperty = enableDebugLoggingProperty;
		_enableInfoLoggingProperty = enableInfoLoggingProperty;

		_rhpLog = new RhpLog( 
				_rhpApp, 
				_rhpPrj, 
				isEnableWarningLogging(), 
				isEnableErrorLogging(), 
				isEnableDebugLogging(), 
				isEnableInfoLogging() );

		_pluginVersionProperty = pluginVersionProperty;
		_userDefinedMetaClassesAsSeparateUnitProperty = userDefinedMetaClassesAsSeparateUnitProperty;
		//_allowPluginToControlUnitGranularityProperty = allowPluginToControlUnitGranularityProperty;
	}

	private boolean isEnableWarningLogging(){

		return getBoolPropertyValueFromRhp(
				_rhpPrj, 
				_enableWarningLoggingProperty, 
				true );
	}

	private boolean isEnableErrorLogging(){

		return getBoolPropertyValueFromRhp(
				_rhpPrj, 
				_enableErrorLoggingProperty, 
				true );
	}

	private boolean isEnableDebugLogging(){

		return getBoolPropertyValueFromRhp(
				_rhpPrj, 
				_enableDebugLoggingProperty, 
				true );
	}

	private boolean isEnableInfoLogging(){

		return getBoolPropertyValueFromRhp(
				_rhpPrj, 
				_enableInfoLoggingProperty, 
				true );
	}

	public IRPModelElement findElementByGUID( 
			String guid ){

		return _rhpPrj.findElementByGUID( guid );
	}

	public IRPCollection createNewCollection(){
		return _rhpApp.createNewCollection();
	}

	public IRPModelElement getSelectedElement(
			boolean isFavourDiagramElement ){

		IRPModelElement theSelectedEl = _rhpApp.getSelectedElement();

		if( isFavourDiagramElement ){

			@SuppressWarnings("unchecked")
			List<IRPGraphElement> theSelectedGraphEls = _rhpApp.getSelectedGraphElements().toList();

			if( !theSelectedGraphEls.isEmpty() ){

				IRPGraphElement selectedGraphEl = theSelectedGraphEls.get( 0 );

				IRPModelElement theModelObject = selectedGraphEl.getModelObject();

				if( theModelObject != null ){				
					theSelectedEl = theModelObject;
				}
			}
		}

		//debug( "getSelectedElement is returning " + elInfo( theSelectedEl ) );

		return theSelectedEl;
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

		//_rhpLog.debug( "getGraphNodeIfOnlyOneIsSelected found " + theGraphNodes.size() + " elements" );

		return theGraphNode;
	}

	public IRPGraphElement getSelectedGraphEl(){

		IRPGraphElement theGraphEl = null;

		@SuppressWarnings("unchecked")
		List<IRPGraphElement> theSelectedGraphEls = _rhpApp.getSelectedGraphElements().toList();

		if( theSelectedGraphEls.size() == 1 ){
			theGraphEl = theSelectedGraphEls.get( 0 );
		} else {
			_rhpLog.debug( "getSelectedGraphEl was invoked, " + theSelectedGraphEls.size() + " were found when expecting one" );
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

		if( _userDefinedMetaClassesAsSeparateUnit == null ){
			_userDefinedMetaClassesAsSeparateUnit = new ArrayList<>();

			String theUserDefinedMetaClassPropertyValue =
					getStringPropertyValueFromRhp(
							_rhpPrj,
							_userDefinedMetaClassesAsSeparateUnitProperty,
							"" );

			String[] theCandidates = theUserDefinedMetaClassPropertyValue.split( "," );

			for( String theCandidate : theCandidates ){
				_userDefinedMetaClassesAsSeparateUnit.add( theCandidate.trim() );
			}
		}

		return _userDefinedMetaClassesAsSeparateUnit;

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
			_rhpLog.warning( "getElementInProjectThatMatches found " + matches.size() + " elements with name " + 
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
				_rhpLog.warning( "Unable to setStringPropertyValueInRhp for " + _rhpLog.elInfo( theEl) + " because unit is read-only" );
			} else {

				try {
					String previousValue = theEl.getPropertyValue(withKey);

					if( previousValue.equals( toValue ) ){
						_rhpLog.debug( "Skipping " + withKey + " property on " + _rhpLog.elInfo( theEl ) + " as it's already set to " + toValue);

					} else {						
						_rhpLog.debug( "Setting " + withKey + " property on " + _rhpLog.elInfo( theEl ) + " to " + toValue);
						theEl.setPropertyValue(withKey, toValue);
					}

				} catch( Exception e ){
					_rhpLog.warning( e.getMessage() + " has occurred which suggests a problem setting a property in the profile." );
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
				// just give a warning as this may occur when using stereotypes in the SysML profile
				_rhpLog.warning( "Unable to setBoolPropertyValueInRhp for " + _rhpLog.elInfo( theEl) + " because unit is read-only" );

			} else {

				try {
					String theValue = "False";

					if( toValue ){
						theValue = "True";
					}

					//_rhpLog.debug( "Setting " + withKey + " property on " + _rhpLog.elInfo( theEl ) + " to " + theValue);
					theEl.setPropertyValue(withKey, theValue);

				} catch( Exception e ){
					_rhpLog.warning( e.getMessage() + " has occurred which suggests a problem setting a property in the profile.");
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

			} catch( Exception e ){
				_rhpLog.warning( e.getMessage() + " has occurred which suggests a problem finding property in the profile." );
				e.printStackTrace();}
		}

		return isSet;
	}

	protected String getStringPropertyValueFromRhp(
			IRPModelElement theEl,
			String propertyKey,
			String defaultIfNotSet ) {

		String theValue = defaultIfNotSet;

		if( theEl != null ){
			try {
				theValue = theEl.getPropertyValue(
						propertyKey );

			} catch( Exception e ){
				_rhpLog.warning( "'" + e.getMessage() + "' has occurred which suggests a problem finding property in the profile." );
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
			_rhpLog.error( "Unable to find stereotype with name " + theName + " in project");
		}

		return theStereotype;
	}

	public IRPTag getTag( 
			IRPModelElement appliedToModelEl,
			String withTagName ){

		IRPTag theTag = appliedToModelEl.getTag( withTagName );

		if( theTag == null ){

			_rhpLog.error( "setTagValue for " + _rhpLog.elInfo( appliedToModelEl ) + 
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
			_rhpLog.error( "getTagValueOnNewTermStereotype for " + _rhpLog.elInfo( appliedToModelEl ) + 
					" was unable to find tag called " + withTagName + 
					" related to stereotype called " + _rhpLog.elInfo( theStereotype )  );
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

				//_rhpLog.debug( "setTagValue is setting tag " + _rhpLog.elInfo( theTag ) + " on " + _rhpLog.elInfo( onStereotype ) + " to " + theValue );

				appliedToModelEl.setTagValue( theTag, theValue );
				wasValueChanged = true;
			}
		} else {
			_rhpLog.error( "setTagValue for " + _rhpLog.elInfo( appliedToModelEl ) + 
					" was unable to find tag called " + withTagName + 
					" related to stereotype called " + _rhpLog.elInfo( onStereotype )  );
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

	/*
	public boolean isAllowPluginToGetUnitGranularity(){

		return getBoolPropertyValueFromRhp(
				_rhpPrj, 
				_allowPluginToControlUnitGranularityProperty, 
				false );
	}*/

	public boolean getBooleanPropertyValue(
			IRPModelElement forContextEl,
			String thePropertyKey ){

		boolean result = false;

		String theValue = null;

		try {
			theValue = forContextEl.getPropertyValue(
					thePropertyKey );

		} catch( Exception e ){
			_rhpLog.error( "Exception in getBooleanPropertyValue, e=" + e.getMessage() );
		}

		if( theValue != null ){
			result = theValue.equals( "True" );
		} else {
			_rhpLog.error( "Error in getBooleanPropertyValue, " +
					"unable to get thePropertyKey property value from " + 
					_rhpLog.elInfo( forContextEl ) );
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
			_rhpLog.warning( "Warning in isElementNameUnique, there are " + count + " elements called " + 
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
				_rhpLog.warning( "Decided against deleting " + _rhpLog.elInfo( theEl ) + 
						" as there are " + count + " of them" );
			} else {
				_rhpLog.info( _rhpLog.elInfo( theEl ) + " was deleted from " + _rhpLog.elInfo( nestedUnderEl ) );
				theEl.deleteFromProject();
			}
		}
	}

	public List<String> getListFromCommaSeparatedString(
			IRPModelElement theContextEl,
			String thePropertyKey ){

		List<String> theNewTerms = new ArrayList<>();

		try {
			String theValue = theContextEl.getPropertyValue( 
					thePropertyKey );
			
			theNewTerms = getListFromString( theValue );

		} catch( Exception e ){
			error( "Exception in getListFromCommaSeparatedString, e=" + e.getMessage() );
		}

		return theNewTerms;
	}

	public List<String> getListFromString(
			String theValue ){

		List<String> theList = new ArrayList<>();

		if( theValue != null && !theValue.isEmpty() ){

			String[] split = theValue.split( "," );

			for( int i = 0; i < split.length; i++ ){
				String theNewTerm = split[ i ].trim();
				theList.add( theNewTerm );
			}
		}

		return theList;
	}

	public IRPPackage getOwningPackageFor(
			IRPModelElement theElement ){

		IRPPackage theOwningPackage = null;

		if( theElement == null ){

			_rhpLog.warning( "getOwningPackage for was invoked for a null element" );

		} else if( theElement instanceof IRPPackage ){
			theOwningPackage = (RPPackage)theElement;

		} else if( theElement instanceof IRPProject ){
			_rhpLog.warning( "Unable to find an owning package for " + theElement.getFullPathNameIn() + " as I reached project" );

		} else {
			theOwningPackage = getOwningPackageFor( theElement.getOwner() );
		}

		return theOwningPackage;
	}

	public boolean isLegalName(
			String theName, 
			IRPModelElement basedOnContext ){

		//String regEx = "^(([a-zA-Z_][a-zA-Z0-9_]*)|(operator.+))$";

		String theRegEx = basedOnContext.getPropertyValue( 
				"General.Model.NamesRegExp" );

		boolean isLegal = theName.matches( theRegEx );

		if( !isLegal ){
			_rhpLog.warning( "Warning, detected that " + theName 
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
			Boolean isFullPathRequested ){

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
			//_rhpLog.debug( _rhpLog.elInfo( theEl ) + "was chosen");

		} else {
			_rhpLog.debug("'Nothing' was chosen by user");
			theEl = null;
		}

		return theEl;
	}

	public IRPStereotype applyExistingStereotype(
			String withTheName, 
			IRPModelElement toTheEl ){

		IRPStereotype theChosenStereotype = getStereotypeWith( withTheName );

		if( theChosenStereotype != null ){
			toTheEl.setStereotype( theChosenStereotype );
		} else {
			_rhpLog.error("Warning in applyExistingStereotype, unable to find stereotype <<" + 
					withTheName + ">> underneath " + _rhpLog.elInfo( toTheEl.getProject() ) );
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

					_rhpLog.debug("getExistingStereotype has chosen " + _rhpLog.elInfo( theStereotype ) + 
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
			IRPModelElement onTheEl ){

		int count = 0;
		IRPStereotype theFoundStereotype = null;

		@SuppressWarnings("unchecked")
		List <IRPStereotype> theStereotypes = onTheEl.getStereotypes().toList();

		for( IRPStereotype theStereotype : theStereotypes ){
			if( theStereotype.getName().equals( theName ) ){

				theFoundStereotype = theStereotype;
				count++;
			}
		}

		if( count > 1 ){
			_rhpLog.warning( "getStereotypeCalled found " + count 
					+ " elements that are called " + theName + " when expecting 1" );
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

			_rhpLog.warning(
					"Warning in getStereotypeAppliedTo, there are multiple stereotypes related to " + 
							_rhpLog.elInfo(theElement) + " size=" + theMatchingStereotypes.size() + 
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

			_rhpLog.debug( "Warning in findElementWithMetaClassAndName(" + theMetaClass + "," + 
					andName + ","+_rhpLog.elInfo(underneathTheEl)+"), " + theMatches.size() + 
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

				_rhpLog.warning( "Duplicate «" + stereotypeName + 
						"» dependencies to " + _rhpLog.elInfo( toElement ) + 
						" were found on " + _rhpLog.elInfo( fromElement ) );
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

		//_rhpLog.debug( "getExistingStereotypedDependencies invoked to find " + stereotypeName + 
		//		" from " + _rhpLog.elInfo( fromElement ) + " to " +
		//		_rhpLog.elInfo( toElement ) );

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

			//_rhpLog.debug( "Added a «" + theStereotype.getName() + "» dependency to " + 
			//		_rhpLog.elInfo( fromElement ) + 
			//		" (to " + _rhpLog.elInfo( toElement ) + ")" );				
		} else {
			//_rhpLog.debug( "Skipped adding a «" + theStereotype.getName() + "» dependency to " + _rhpLog.elInfo( fromElement ) + 
			//		" (to " + _rhpLog.elInfo( toElement ) + 
			//		") as " + isExistingFoundCount + " already exists" );
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

					_rhpLog.warning( "Duplicate dependency to " + _rhpLog.elInfo( theDependsOn ) + 
							" was found on " + _rhpLog.elInfo( theElement ));
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

					_rhpLog.debug("getFilesMatching found: " + theCandidateFile.getAbsolutePath());
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
						_rhpLog.debug("Adding " + _rhpLog.elInfo(theCandidate.getOwner()) + " to the list");
					} else {
						_rhpLog.debug("Skipping " + _rhpLog.elInfo(theCandidate.getOwner()) + " as it is read-only");
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
				_rhpLog.warning( "Warning, unable to find template called " + 
						theTemplateName + " named in TemplateForActivityDiagram property" );
			}
		}

		_rhpLog.debug( "getTemplateForActivityDiagram, found " + _rhpLog.elInfo( theTemplate ) );

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
			throw new Exception("There is more than one graph element for " + _rhpLog.elInfo(forElement));
		} else if( theGraphEls.size() == 1 ){
			ret = theGraphEls.get( 0 );
		} else {
			_rhpLog.warning("Warning, getCorrespondingGraphElement dif not find a graph element corresponding to " + 
					_rhpLog.elInfo(forElement) + " on " + _rhpLog.elInfo(theAD) );
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
			_rhpLog.warning("Warning in isStateUnique, there are " + count + " elements called " + 
					theProposedName + ". This may cause issues.");
		}

		boolean isUnique = (count == 0);

		return isUnique;
	}

	public void dumpGraphicalPropertiesFor(
			IRPGraphElement theGraphEl){

		@SuppressWarnings("unchecked")
		List<IRPGraphicalProperty> theGraphProperties = theGraphEl.getAllGraphicalProperties().toList();

		_rhpLog.info("---------------------------");
		for (IRPGraphicalProperty theGraphicalProperty : theGraphProperties) {

			_rhpLog.info(theGraphicalProperty.getKey() + "=" + theGraphicalProperty.getValue());
		}
		_rhpLog.info("---------------------------"); 
	}

	public String buildStringFromModelEls(
			List<? extends IRPModelElement> theEls,
			int max ){

		String theString = "";

		int count = 0;

		for( Iterator<? extends IRPModelElement> iterator = theEls.iterator(); iterator.hasNext(); ) {

			count++;
			IRPModelElement theEl = (IRPModelElement) iterator.next();

			theString += count + ". " + _rhpLog.elInfo( theEl ) + " \n";

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
			_rhpLog.info("Operation cancelled by user when trying to choose Unit (.sbs)");

		} else if( choice==JFileChooser.APPROVE_OPTION ){

			File theFile = theFileChooser.getSelectedFile();

			_rhpLog.debug("theFile.getAbsolutePath = " + theFile.getAbsolutePath() );
			_rhpLog.debug("theFile.getName = " + theFile.getName() );
			_rhpLog.debug("theFile.getParent = " + theFile.getParent() );
			_rhpLog.debug("theFile.getPath = " + theFile.getPath() );
			_rhpLog.debug("theFile.getParentFile().getName() = " + theFile.getParentFile().getName() );

			String theTargetPath;

			try {
				theTargetPath = theFile.getCanonicalPath();

				_rhpLog.debug( "theTargetPath=" + theTargetPath );

				_rhpApp.addToModelByReference( theTargetPath );

				if( relative ){

					String theFileNameIncludingExtension = theFile.getName();

					String theName = theFileNameIncludingExtension.substring(0, theFileNameIncludingExtension.length()-3);

					int trimSize = theName.length()+5;

					Path targetPath = Paths.get( theTargetPath.substring(0, theTargetPath.length()-trimSize) );
					Path targetRoot = targetPath.getRoot();

					_rhpLog.debug( "targetRoot.toString()=" + targetRoot.toString() );

					Path sourcePath = Paths.get( 
							inProject.getCurrentDirectory().replaceAll(
									inProject.getName()+"$", "") );

					Path sourceRoot = sourcePath.getRoot();

					_rhpLog.debug( "sourceRoot.toString()=" + sourceRoot.toString() );

					if( !targetRoot.equals( sourceRoot ) ){
						_rhpLog.debug("Unable to set Unit called " + theName + " to relative, as the drive letters are different");
						_rhpLog.debug("theTargetDir root =" + targetPath.getRoot());
						_rhpLog.debug("theTargetDir=" + targetPath);
						_rhpLog.debug("theSourceDir root =" + sourcePath.getRoot());
						_rhpLog.debug("theSourceDir=" + sourcePath);
					} else {
						Path theRelativePath = sourcePath.relativize(targetPath);

						IRPModelElement theCandidate = inProject.findAllByName( theName, "Package" );

						if( theCandidate instanceof IRPPackage ){

							IRPPackage theAddedPackage = (IRPPackage)theCandidate;

							theAddedPackage.setUnitPath( "..\\..\\" + theRelativePath.toString() );

							_rhpLog.info( "Unit called " + theName + 
									".sbs was changed from absolute path='" + theTargetPath + 
									"' to relative path='" + theRelativePath + "'" );
						}
					}
				}
			} catch( IOException e ){
				_rhpLog.error( "Error, unhandled IOException in RelativeUnitHandler.browseAndAddUnit");
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

				//_rhpLog.debug( "Found state called " + theEl.getName() + 
				//		" owned by " + theEl.getOwner().getFullPathName() );

				theState = (IRPState) theEl;
				count++;
			}
		}

		if( count != 1 ){
			_rhpLog.warning( "Warning in getStateCalled (" + count + 
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

		//_rhpLog.debug( "The owner for " + _rhpLog.elInfo( theState ) + 
		//		" is " + _rhpLog.elInfo( theOwner ) );

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

			//_rhpLog.debug( _rhpLog.elInfo(theStatechartDiagram) + " was found owned by " + 
			//		_rhpLog.elInfo( theClassifier ) );

			@SuppressWarnings("unchecked")
			List<IRPGraphElement> theGraphEls = 
			theStatechartDiagram.getGraphicalElements().toList();

			for( IRPGraphElement theGraphEl : theGraphEls ){

				IRPModelElement theEl = theGraphEl.getModelObject();

				if( theEl != null ){
					//_rhpLog.debug( "Found " + theEl.getMetaClass() + 
					//		" called " + theEl.getName() );

					if( theEl.getName().equals( withTheName ) ){

						//_rhpLog.debug( "Success, found GraphEl called " + 
						//		withTheName + " in statechart for " + 
						//		_rhpLog.elInfo( theClassifier ) );

						theFoundGraphEl = theGraphEl;
						break;
					}
				}
			}
		}

		return theFoundGraphEl;
	}

	public void cleanUpModelRemnants(){

		deleteIfPresent( "Structure1", "StructureDiagram", _rhpPrj );
		deleteIfPresent( "Model1", "ObjectModelDiagram", _rhpPrj );
		deleteIfPresent( "Default", "Package", _rhpPrj );

		IRPModelElement theDefaultComponent = 
				_rhpPrj.findElementsByFullName( "DefaultComponent", "Component" );

		if( theDefaultComponent != null ){

			try {
				theDefaultComponent.setName( NOT_USED_COMP );

			} catch( Exception e ){
				error( "Unable to change name of " + elInfo( theDefaultComponent ) + 
						" to " + NOT_USED_COMP + ", e=" + e.getMessage()  );
			}

			IRPModelElement theDefaultConfig = 
					theDefaultComponent.findNestedElement( "DefaultConfig", "Configuration" );

			if( theDefaultConfig != null ){

				try {
					theDefaultConfig.setName( NOT_USED_CONFIG );

				} catch( Exception e ){
					error( "Unable to change name of " + elInfo( theDefaultConfig ) + 
							" to " + NOT_USED_CONFIG + ", e=" + e.getMessage()  );
				}			
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

			_rhpLog.info( "Create a package called " + theName );
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
			_rhpLog.error("Exception in getExistingOrCreateNewElementWith( theName " + theName + 
					", andMetaClass=" + andMetaClass + ", underneath=" + _rhpLog.elInfo(underneathTheEl));
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
			_rhpLog.error( "Error in GeneralHelpers.setStringTagValueOn for " + 
					_rhpLog.elInfo( theOwner) + ", unable to find tag called " + theTagName );
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
				_rhpLog.info( "Added profile called " + theProfile.getFullPathName() );

			} else {
				_rhpLog.error( "Error in addProfileIfNotPresent. No profile found with name " + theProfileName );
			}

		} else {
			//_rhpLog.debug( _rhpLog.elInfo( theProfile ) + " is already present in the project" );
		}

		return theProfile;		
	}

	public IRPComponent addAComponentWith(
			String theName,
			IRPPackage theBlockTestPackage, 
			IRPClass theUsageDomainBlock,
			String theSimulationMode ){

		IRPComponent theComponent = 
				(IRPComponent) theBlockTestPackage.addNewAggr(
						"Component", theName + "_EXE" );

		theComponent.setPropertyValue( "Activity.General.SimulationMode", theSimulationMode );

		IRPConfiguration theConfiguration = (IRPConfiguration) theComponent.findConfiguration( "DefaultConfig" );

		String theEnvironment = theConfiguration.getPropertyValue( "CPP_CG.Configuration.Environment" );

		theConfiguration.setName( theEnvironment );			
		theConfiguration.setPropertyValue( "WebComponents.WebFramework.GenerateInstrumentationCode", "True" );		
		theConfiguration.addInitialInstance( theUsageDomainBlock );
		theConfiguration.setScopeType( "implicit" );
		theConfiguration.setInstrumentationType( "Animation" );

		IRPConfiguration theNoWebConfig = theComponent.addConfiguration( theEnvironment + "_NoWebify" );			
		theNoWebConfig.setPropertyValue( "WebComponents.WebFramework.GenerateInstrumentationCode", "False" );		
		theNoWebConfig.addInitialInstance( theUsageDomainBlock );
		theNoWebConfig.setScopeType( "implicit" );
		theNoWebConfig.setInstrumentationType( "Animation" );

		theConfiguration.getProject().setActiveConfiguration( theConfiguration );		

		return theComponent;
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

	public String getActionTextFrom(
			IRPModelElement theEl ){

		String theSourceInfo = null;

		if( theEl instanceof IRPState ){

			IRPState theState = (IRPState)theEl;
			String theStateType = theState.getStateType();

			if (theStateType.equals("Action")){
				theSourceInfo = theState.getEntryAction();

			} else if (theStateType.equals("AcceptEventAction")){ // receive event

				IRPAcceptEventAction theAcceptEventAction = (IRPAcceptEventAction)theEl;
				IRPEvent theEvent = theAcceptEventAction.getEvent();

				if (theEvent==null){
					//_rhpLog.debug( "Event has no name so using Name" );
					theSourceInfo = theState.getName();
				} else {
					theSourceInfo = theEvent.getName();
				}

			} else if (theStateType.equals("EventState")){ // send event

				IRPSendAction theSendAction = theState.getSendAction();

				if (theSendAction != null){
					IRPEvent theEvent = theSendAction.getEvent();

					if (theEvent != null){
						theSourceInfo = theEvent.getName();
					} else {
						//_rhpLog.debug("SendAction has no Event so using Name of action");
						theSourceInfo = theState.getName();
					}
				} else {
					_rhpLog.warning( "Warning in deriveDownstreamRequirement, theSendAction is null" );
				}	

			} else if (theStateType.equals("TimeEvent")){

				IRPAcceptTimeEvent theAcceptTimeEvent = (IRPAcceptTimeEvent)theEl;
				String theDuration = theAcceptTimeEvent.getDurationTime();

				if (theDuration.isEmpty()){
					theSourceInfo = theAcceptTimeEvent.getName();
				} else {
					theSourceInfo = theDuration;
				}

			} else {
				_rhpLog.warning( "Warning in getActionTextFrom, " + theStateType + " was not handled" );
			}

		} else if (theEl instanceof IRPTransition){

			IRPTransition theTrans = (IRPTransition)theEl;
			IRPGuard theGuard = theTrans.getItsGuard();

			// check that transition has a guard before trying to use it
			if( theGuard != null ){
				theSourceInfo = ((IRPTransition) theEl).getItsGuard().getBody();
			} else {
				theSourceInfo = "TBD"; // no source info available
			}

		} else if (theEl instanceof IRPComment){

			theSourceInfo = theEl.getDescription();

		} else if (theEl instanceof IRPRequirement){

			theSourceInfo =  theEl.getName();

			// use specification text if requirement name is just a number or is default
			if( theSourceInfo.matches( "\\d+" ) || 
					theSourceInfo.matches( "requirement_\\d+") ){

				IRPRequirement theReqt = (IRPRequirement)theEl;
				theSourceInfo = theReqt.getSpecification();
			}

		} else if (theEl instanceof IRPConstraint){

			IRPConstraint theConstraint = (IRPConstraint)theEl;
			theSourceInfo = theConstraint.getSpecification();		

		} else {
			_rhpLog.error("Error in getActionTextFrom, " + _rhpLog.elInfo(theEl) + " was not handled as of an unexpected type");
			theSourceInfo = ""; // default
		}

		if( theSourceInfo != null ){

			if( theSourceInfo.isEmpty() ){

				_rhpLog.debug( "getActionTextFrom determined that " + _rhpLog.elInfo( theEl ) + " has no text" );
			} else {
				theSourceInfo = decapitalize( theSourceInfo ).trim();
			}
		}

		return theSourceInfo;
	}

	public List<IRPModelElement> findModelElementsWithoutStereotypeNestedUnder(
			IRPModelElement rootEl, 
			String ofMetaClass, 
			String withStereotypeMatchingRegEx){

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theCandidateEls = rootEl.getNestedElementsByMetaClass(ofMetaClass, 1).toList();
		List<IRPModelElement> theFound = new ArrayList<IRPModelElement>();

		for (IRPModelElement theEl : theCandidateEls) {

			IRPStereotype theStereotype = getStereotypeAppliedTo(theEl, withStereotypeMatchingRegEx);

			if (theStereotype==null){
				theFound.add(theEl);
			}			
		}

		return theFound;
	}

	public Set<IRPModelElement> findModelElementsNestedUnder(
			IRPModelElement rootEl, 
			String ofMetaClass, 
			String withStereotypeMatchingRegEx){

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theCandidateEls = rootEl.getNestedElementsByMetaClass(ofMetaClass, 1).toList();

		Set<IRPModelElement> theFound = new LinkedHashSet<IRPModelElement>();

		for (IRPModelElement theEl : theCandidateEls) {

			IRPStereotype theStereotype = getStereotypeAppliedTo( theEl, withStereotypeMatchingRegEx );

			if( theStereotype != null ){
				// don't add if element is under the profile.
				if (!checkIsNestedUnderAProfile( theEl )){
					theFound.add( theEl );
				}
			}			
		}

		return theFound;
	}

	public boolean checkIsNestedUnderAProfile(
			IRPModelElement theElementToCheck){

		boolean isUnderAProfile = false;

		IRPModelElement theOwner = theElementToCheck.getOwner();

		if (theOwner!=null){

			if (theOwner instanceof IRPProfile){
				isUnderAProfile = true;
			} else {
				isUnderAProfile = checkIsNestedUnderAProfile( theOwner );
			}
		}

		return isUnderAProfile;
	}

	public Set<IRPModelElement> findModelElementsIn(
			List<IRPGraphElement> theGraphElementList, 
			String withMetaClass){

		Set<IRPModelElement> theFilteredSet = new HashSet<IRPModelElement>();

		for (IRPGraphElement theGraphEl : theGraphElementList) {

			IRPModelElement theEl = theGraphEl.getModelObject();

			if (theEl != null && theEl.getMetaClass().equals( withMetaClass )){
				theFilteredSet.add( theEl );
			}
		}

		return theFilteredSet;
	}

	public List<IRPModelElement> findElementsIn(
			List<IRPModelElement> theModelElementList, 
			String withMetaClass){

		List<IRPModelElement> theFilteredList = new ArrayList<IRPModelElement>();

		for (IRPModelElement theEl : theModelElementList) {

			if (theEl.getMetaClass().equals( withMetaClass )){
				theFilteredList.add( theEl );
			}
		}

		return theFilteredList;
	}

	public boolean doUnderlyingModelElementsIn(
			List<IRPGraphElement> theGraphElementList, 
			String haveTheMetaClass){

		boolean result = true;

		for (IRPGraphElement theGraphEl : theGraphElementList) {
			IRPModelElement theEl = theGraphEl.getModelObject();

			if (theEl != null && !theEl.getMetaClass().equals( haveTheMetaClass )){
				result = false;
			}
		}

		return result;
	}

	public void applyStereotypeToDeriveReqtDependenciesOriginatingFrom( 
			IRPModelElement theReqt, 
			IRPStereotype theStereotypeToApply ) {

		@SuppressWarnings("unchecked")
		List<IRPDependency> theDependencies = theReqt.getDependencies().toList();

		for (IRPDependency theDependency : theDependencies) {

			IRPStereotype theExistingGatewayStereotype = 
					getStereotypeAppliedTo( theDependency, "from.*" );

			if (theExistingGatewayStereotype == null && 
					hasStereotypeCalled("deriveReqt", theDependency)){

				//_rhpLog.debug("Applying " + _rhpLog.elInfo(theStereotypeToApply) + " to " + _rhpLog.elInfo(theDependency));

				if( theStereotypeToApply != null ){
					theDependency.setStereotype(theStereotypeToApply);					
				}

				theDependency.changeTo("Derive Requirement");
			}
		}
	}

	public IRPClass findOwningClassIfOneExistsFor( 
			IRPModelElement theModelEl ){

		IRPModelElement theOwner = theModelEl.getOwner();
		IRPClass theResult = null;

		if( ( theOwner != null ) &&
				!( theOwner instanceof IRPProject ) ){

			if( theOwner.getMetaClass().equals( "Class" ) ){

				theResult = (IRPClass) theOwner;
			} else {
				theResult = findOwningClassIfOneExistsFor( theOwner );
			}
		}

		return theResult;
	}

	public List<IRPLink> getLinksBetween(
			IRPSysMLPort thePort,
			IRPInstance ownedByPart,
			IRPSysMLPort andThePort,
			IRPInstance whichIsOwnedByPart,
			IRPClassifier inBuildingBlock ){

		List<IRPLink> theLinksBetween = 
				new ArrayList<IRPLink>();

		@SuppressWarnings("unchecked")
		List<IRPLink> theExistingLinks = 
		inBuildingBlock.getLinks().toList();

		for( IRPLink theExistingLink : theExistingLinks ){

			IRPSysMLPort fromSysMLPort = theExistingLink.getFromSysMLPort();
			IRPModelElement fromSysMLElement = theExistingLink.getFromElement();

			IRPSysMLPort toSysMLPort = theExistingLink.getToSysMLPort();
			IRPModelElement toSysMLElement = theExistingLink.getToElement();

			if( fromSysMLPort != null && 
					fromSysMLElement != null && fromSysMLElement instanceof IRPInstance &&
					toSysMLPort != null &&
					toSysMLElement != null && toSysMLElement instanceof IRPInstance ){

				if( thePort.equals( fromSysMLPort ) && 
						ownedByPart.equals( fromSysMLElement ) &&
						andThePort.equals( toSysMLPort ) &&
						whichIsOwnedByPart.equals( toSysMLElement ) ){

					//_rhpLog.debug("Check for links between " + _rhpLog.elInfo(fromSysMLPort) + " and " + 
					//		_rhpLog.elInfo( toSysMLPort ) + " successfully found " + 
					//		_rhpLog.elInfo( theExistingLink ) );

					theLinksBetween.add( theExistingLink );

				} else if( thePort.equals( toSysMLPort ) && 
						ownedByPart.equals( fromSysMLElement ) &&
						andThePort.equals( fromSysMLPort ) &&
						whichIsOwnedByPart.equals( toSysMLElement ) ){

					//_rhpLog.debug("Check for links between " + _rhpLog.elInfo(toSysMLPort) + " and " + 
					//		_rhpLog.elInfo( fromSysMLPort ) + " successfully found " + 
					//		_rhpLog.elInfo( theExistingLink ) );

					theLinksBetween.add( theExistingLink );

				} else {
					//					Logger.writeLine("Check for links between " + Logger.elementInfo(toSysMLPort) + " and " + 
					//							Logger.elementInfo( fromSysMLPort ) + " found no match to " + 
					//							Logger.elementInfo( theExistingLink ) );
				}

			} else {
				// we're only interested in flow ports
			}
		}

		//_rhpLog.debug("getLinksBetween " + _rhpLog.elInfo( thePort ) + " and " +
		//		_rhpLog.elInfo( andThePort ) + " has found " + 
		//		theLinksBetween.size() + " matches");

		return theLinksBetween;
	}

	public String getCreateRequirementTextForPrefixing(
			IRPModelElement basedOnContextEl,
			String andPropertyName ){

		String thePropertyValue = 
				basedOnContextEl.getPropertyValue( 
						"ExecutableMBSEProfile.RequirementsAnalysis.CreateRequirementTextForPrefixing" );

		if( thePropertyValue == null ){
			thePropertyValue = "Error in getCreateRequirementTextForPrefixing";
		}

		//#005 10-APR-2016: Support ProductName substitution in reqt text tag (F.J.Chadburn)
		thePropertyValue = thePropertyValue.replaceAll(
				"ProjectName", basedOnContextEl.getProject().getName() );

		return thePropertyValue;
	}

	abstract public IRPPackage addNewTermPackageAndSetUnitProperties( 
			String theName,
			IRPPackage theOwner,
			String theNewTermName );


	public IRPStereotype getStereotypeToUseForFunctions(){

		if( _stereotypeToUseForFunctions == null ){

			_stereotypeToUseForFunctions = getStereotypeBasedOn(
					_rhpPrj, 
					"ExecutableMBSEProfile.FunctionalAnalysis.TraceabilityTypeToUseForFunctions" );
		}

		return _stereotypeToUseForFunctions;
	}

	public IRPStereotype getStereotypeToUseForActions(){

		if( _stereotypeToUseForActions == null ){

			_stereotypeToUseForActions = getStereotypeBasedOn(
					_rhpPrj, 
					"ExecutableMBSEProfile.RequirementsAnalysis.TraceabilityTypeToUseForActions" );
		}

		return _stereotypeToUseForActions;
	}

	public IRPStereotype getStereotypeToUseForUseCases(){

		if( _stereotypeToUseForUseCases == null ){

			_stereotypeToUseForUseCases = getStereotypeBasedOn(
					_rhpPrj, 
					"ExecutableMBSEProfile.RequirementsAnalysis.TraceabilityTypeToUseForUseCases" );
		}

		return _stereotypeToUseForUseCases;
	}

	protected IRPStereotype getStereotypeBasedOn(
			IRPModelElement theContextEl,
			String andPropertyValue ){

		IRPStereotype theStereotype = null;

		String theStereotypeName = 
				theContextEl.getPropertyValue( 
						andPropertyValue );

		if( theStereotypeName != null && 
				!theStereotypeName.trim().isEmpty() ){

			theStereotype = getStereotypeWith( 
					theStereotypeName );

			if( theStereotype == null ){
				_rhpLog.error( "Error in getStereotypeBasedOn, no Stereotyped called " + theStereotypeName + " was found" );

				//theStereotype = selectAndPersistStereotype( basedOnContext.getProject(), thePkg, theTag );

			} else {				
				//_rhpLog.debug( "Using " + _rhpLog.elInfo( theStereotype ) + " for " + andPropertyValue );
			}		
		}

		return theStereotype;
	}

	public IRPLink addConnectorBetweenSysMLPortsIfOneDoesntExist(
			IRPSysMLPort theSrcPort,
			IRPInstance theSrcPart, 
			IRPSysMLPort theTgtPort,
			IRPInstance theTgtPart) {

		IRPLink theLink = null;

		IRPClass theAssemblyBlock = (IRPClass) theSrcPart.getOwner();

		//_rhpLog.debug( "addConnectorBetweenSysMLPortsIfOneDoesntExist has determined that " + 
		//		_rhpLog.elInfo( theAssemblyBlock ) + " is the assembly block" );

		List<IRPLink> theLinks = getLinksBetween(
				theSrcPort, 
				theSrcPart,
				theTgtPort, 
				theTgtPart,
				theAssemblyBlock );

		// only add if one does not already exist
		if( theLinks.size() == 0 ){

			//_rhpLog.debug( "Adding a new connector between " + _rhpLog.elInfo( theSrcPort ) + 
			//		" and " + _rhpLog.elInfo( theTgtPort ) + " as one does not exist" ); 

			IRPPackage thePkg = (IRPPackage) theAssemblyBlock.getOwner();

			theLink = thePkg.addLinkBetweenSYSMLPorts(
					theSrcPart, 
					theTgtPart, 
					null, 
					theSrcPort, 
					theTgtPort );

			theLink.changeTo( "connector" );

			String theUniqueName = determineUniqueNameBasedOn(
					theSrcPart.getName() + "_" + theSrcPort.getName() + "__" + 
							theTgtPart.getName() + "_" + theTgtPort.getName(), 
							"Link", 
							theAssemblyBlock );

			theLink.setName( theUniqueName );		
			theLink.setOwner( theAssemblyBlock );

			//_rhpLog.debug("Added " + _rhpLog.elInfo( theLink ) + 
			//		" to " + _rhpLog.elInfo( theAssemblyBlock ));
		}

		return theLink;
	}

	public Set<IRPModelElement> getSetOfElementsFromCombiningThe(
			List<IRPModelElement> theSelectedEls,
			List<IRPGraphElement> theSelectedGraphEls ){

		Set<IRPModelElement> theSetOfElements = 
				new HashSet<IRPModelElement>( theSelectedEls );

		for( IRPGraphElement theGraphEl : theSelectedGraphEls ){

			IRPModelElement theEl = theGraphEl.getModelObject();

			if( theEl != null ){
				theSetOfElements.add( theEl );
			}
		}

		return theSetOfElements;
	}

	public void addGeneralization(
			IRPClassifier fromElement, 
			String toBlockWithName, 
			IRPPackage underneathTheRootPackage ){

		IRPModelElement theBlock = 
				underneathTheRootPackage.findNestedElementRecursive( 
						toBlockWithName, "Block" );

		if( theBlock != null ){
			fromElement.addGeneralization( (IRPClassifier) theBlock );
		} else {
			_rhpLog.error( "Error: Unable to find element with name " + toBlockWithName );
		}
	}

	public List<IRPModelElement> getExistingElementsBasedOn(
			IRPPackage theOwningPackage,
			String theRelatedPackageStereotype,
			String withMetaClass ){

		List<IRPModelElement> existingEls = new ArrayList<>();

		if( theOwningPackage != null ){

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theElsInOwningPackage =
			theOwningPackage.getNestedElementsByMetaClass( withMetaClass, 0 ).toList();

			existingEls.addAll( theElsInOwningPackage );

			@SuppressWarnings("unchecked")
			List<IRPDependency> theDependencies = theOwningPackage.getDependencies().toList();

			for (IRPDependency theDependency : theDependencies) {

				IRPModelElement theDependsOn = theDependency.getDependsOn();

				if( theDependsOn instanceof IRPPackage &&
						hasStereotypeCalled( theRelatedPackageStereotype, theDependsOn ) ){

					@SuppressWarnings("unchecked")
					List<IRPModelElement> theDependsOnPackage =
					theDependsOn.getNestedElementsByMetaClass( withMetaClass, 0 ).toList();

					existingEls.addAll( theDependsOnPackage );
				}
			}
		}

		return existingEls;
	}
	
	public IRPDependency getExistingDependency(
			IRPModelElement fromEl, 
			IRPModelElement toEl ){
		
		IRPDependency theExistingDependency = null;
		
		List<IRPDependency> theExistingDependencies = new ArrayList<>();
		
		@SuppressWarnings("unchecked")
		List<IRPDependency> theCandidates = fromEl.getDependencies().toList();
		
		for( IRPDependency theCandidate : theCandidates ){
			
			IRPModelElement theDependsOn = theCandidate.getDependsOn();
			
			if( theDependsOn.equals( toEl ) ){
				theExistingDependencies.add( theCandidate );
			}
		}
		
		int size = theExistingDependencies.size();
		
		if( size == 1 ){
			theExistingDependency = theExistingDependencies.get( 0 );
		
		} else if( size > 1 ) {
			
			warning( "getExistingDependency found " + size + " dependencies from " + 
					elInfo( fromEl ) + " to " + elInfo( toEl ) + 
					", hence is returning the first one");
			
			theExistingDependency = theExistingDependencies.get( 0 );
		}
		
		return theExistingDependency;
	}

	public List<IRPModelElement> getExistingElementsBasedOn(
			IRPPackage theOwningPackage,
			String theRelatedPackageStereotype,
			String withMetaClass,
			String andNewTerm ){

		List<IRPModelElement> matchingEls = new ArrayList<>();

		List<IRPModelElement> existingEls = getExistingElementsBasedOn(
				theOwningPackage, 
				theRelatedPackageStereotype, 
				withMetaClass );

		for( IRPModelElement existingEl : existingEls ){

			if( existingEl.getUserDefinedMetaClass().equals( andNewTerm ) ){

				matchingEls.add( existingEl );
			}
		}

		return matchingEls;
	}

	public void moveRequirementIfNeeded(
			IRPRequirement theReqt) {
	}

	public void autoPopulateProjectPackageDiagramIfNeeded() {
	}

	public void info( 
			String theStr ){

		_rhpLog.info( theStr );
	}

	public void debug( 
			String theStr ){

		_rhpLog.debug( theStr );
	}

	public void error( 
			String theStr ){

		_rhpLog.error( theStr );
	}

	public void warning( 
			String theStr ){

		_rhpLog.warning( theStr );
	}

	public String elInfo( 
			IRPModelElement theEl ){

		return _rhpLog.elInfo( theEl );		
	}

	public Date getDate( 
			String fromString ){

		SimpleDateFormat parser = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		Date date = null;

		try {
			date = parser.parse( fromString );

		} catch( ParseException e ){
			error( "Exception in getDate trying to parse date" );
		}

		return date;
	}

	public IRPGraphEdge getCorrespondingGraphEdgeFor( 
			IRPLink theLink ){

		IRPGraphEdge theGraphEdge = null;

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theReferences = theLink.getReferences().toList();

		if( theReferences.size() == 1 ){
			IRPModelElement theReference = theReferences.get( 0 );	

			debug( "theReference is " + elInfo( theReference ) );

			if( theReference instanceof IRPStructureDiagram ){

				IRPStructureDiagram theDiagram = (IRPStructureDiagram)theReference;

				@SuppressWarnings("unchecked")
				List<IRPGraphElement> theGraphEls = theDiagram.getCorrespondingGraphicElements( theLink ).toList();

				if( theGraphEls.size() == 1 ){
					IRPGraphElement theGraphEl = theGraphEls.get( 0 );

					if( theGraphEl instanceof IRPGraphEdge ){
						theGraphEdge = (IRPGraphEdge) theGraphEl;
					}
				}
			}
		}

		return theGraphEdge;
	}

	public IRPGraphNode addGraphNodeFor(
			IRPModelElement thePortEl,
			IRPDiagram toDiagram,
			int x,
			int y ){

		IRPGraphNode thePortNode = null;

		@SuppressWarnings("unchecked")
		List<IRPGraphNode> theGraphNodes = 
		toDiagram.getCorrespondingGraphicElements( thePortEl ).toList();

		if( theGraphNodes.size() == 1 ){

			thePortNode = theGraphNodes.get( 0 );

			String thePosition = x + "," + y;

			debug( "Setting Position of existing " + elInfo( thePortEl ) + " to " + thePosition );
			thePortNode.setGraphicalProperty( "Position", thePosition );

		} else {

			thePortNode = toDiagram.addNewNodeForElement( thePortEl, x, y, 12, 12 );
		}

		return thePortNode;
	}

	public void setPortDirectionFor(
			IRPSysMLPort theSysMLPort,
			String theDirection,
			String theTypeName ){

		IRPType theType = get_rhpPrj().findType( theTypeName );

		if( theType == null ){

			error( "pluginMethodForInputPort was unable to find the '" + 
					theTypeName + "' type to use for " + elInfo( theSysMLPort ) );

		} else {

			theSysMLPort.setType( theType );
			theSysMLPort.setPortDirection( theDirection );
		}
	}
	
	public void addHyperLink( 
			IRPModelElement fromElement, 
			IRPModelElement toElement ){
	
		IRPHyperLink theHyperLink = (IRPHyperLink) fromElement.addNewAggr("HyperLink", "");
		theHyperLink.setDisplayOption( HYPNameType.RP_HYP_NAMETEXT, "" );
		theHyperLink.setTarget( toElement );
	}
	
	public IRPDependency getExistingOrAddNewDependency(
			IRPModelElement fromEl,
			IRPModelElement toEl,
			IRPStereotype withTheStereotype ){
		
		info( "Adding dependency to " + toEl.getName() + 
				" (" + toEl.getUserDefinedMetaClass() + ") from " + fromEl.getName() + 
				" with " + elInfo( withTheStereotype ) );
		
		IRPDependency theDependency = getExistingDependency( fromEl, toEl );
		
		if( theDependency == null ){
			theDependency = fromEl.addDependencyTo( toEl ); 
		}
		
		addStereotypeIfNeeded( withTheStereotype, theDependency );
		
		return theDependency;
	}

	public void addStereotypeIfNeeded(
			IRPStereotype withStereotype,
			IRPModelElement toTheEl ){
		
		if( withStereotype != null ){
			
			boolean isFound = false;
			
			@SuppressWarnings("unchecked")
			List<IRPStereotype> theExistingStereotypes = toTheEl.getStereotypes().toList();
			
			for( IRPStereotype theExistingStereotype : theExistingStereotypes ){
				if( theExistingStereotype.equals( withStereotype ) ){
					isFound = false;
				}
			}
			
			if( !isFound ){						
				toTheEl.addSpecificStereotype( withStereotype );
			}
		}
	}
}

/**
 * Copyright (C) 2021-2022  MBSE Training and Consulting Limited (www.executablembse.com)

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