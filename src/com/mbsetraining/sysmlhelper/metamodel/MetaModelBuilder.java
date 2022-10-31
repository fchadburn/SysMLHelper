package com.mbsetraining.sysmlhelper.metamodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mbsetraining.sysmlhelper.common.BaseContext;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.telelogic.rhapsody.core.IRPClass;
import com.telelogic.rhapsody.core.IRPClassifier;
import com.telelogic.rhapsody.core.IRPDependency;
import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPPackage;
import com.telelogic.rhapsody.core.IRPProject;
import com.telelogic.rhapsody.core.IRPRelation;
import com.telelogic.rhapsody.core.IRPStereotype;
import com.telelogic.rhapsody.core.RhapsodyAppServer;

public class MetaModelBuilder {

	public static void main(String[] args) {

		// keep the application interface for later use
		Metamodel_Context context = new Metamodel_Context( 
				RhapsodyAppServer.getActiveRhapsodyApplication().getApplicationConnectionString() );

		MetaModelBuilder theBuilder = new MetaModelBuilder( context );

		IRPProject theRhpPrj = context.get_rhpPrj();

		theBuilder.setGeneralModelAddNewMenuStructurePropertyOn( theRhpPrj.getNewTermStereotype(), context.getSelectedElement(false) );

		context.info( "... Finished" );
	}

	private MenuItems getDefaultMenuItemsFromSysML(){

		_context.info( "getDefaultTokensFromSysML was invoked... ");

		MenuItems theSysMLMenuItems = new MenuItems( _context );

		try {
			IRPModelElement theSysMLStereotype = _context.get_rhpPrj().
					findElementsByFullName( "SysML::SysML", "Stereotype" );

			if( theSysMLStereotype instanceof IRPStereotype ){

				String theDefaultProperties = theSysMLStereotype.getPropertyValueExplicit( 
						"General.Model.AddNewMenuStructureContent" );

				Matcher m = Pattern.compile("(.*)$|(.*)/(.*),")
						.matcher( theDefaultProperties );

				while( m.find() ){
					String theFirstToken = m.group(2);

					String theSecondToken = m.group(3);

					if( theSecondToken != null && 
							!theSecondToken.isEmpty() && 
							!theSecondToken.equals("rpy_separator") ){

						//_context.info( "Found " + theGroup );
						theSysMLMenuItems.add( new MenuItem( theSecondToken, theFirstToken ) );
					} else {

						theSecondToken = m.group(3);

						if( theSecondToken != null && 
								!theSecondToken.isEmpty() && 
								!theSecondToken.equals("rpy_separator") ){

							//_context.info( "Found " + theGroup );
							theSysMLMenuItems.add( new MenuItem( theSecondToken, "" ) ); 
						}	 
					}

				}
			}

		} catch( Exception e ){
			_context.error( "Exception in getDefaultMenuItemsFromSysML, e=" + e.getMessage());
		}

		_context.info( "getDefaultMenuItemsFromSysML found " + theSysMLMenuItems.size() + 
				" existing element types in the SysML profile" );

		return theSysMLMenuItems;
	}

	private String[] _metaModelSingleRelationProperties = { 
	"PartMetaclassName" };

	private String[] _metaModelMultipleRelationProperties = { 
			"AllowedTypes", 
			"Sources", 
			"Targets", 
	"HideTabsInFeaturesDialog" };

	private List<String> _baseDiagramMetaClasses = Arrays.asList(
			"ObjectModelDiagram", 
			"StructureDiagram", 
			"UseCaseDiagram", 
			"ActivityDiagram", 
			"TimingDiagram", 
			"SequenceDiagram",
			"UseCaseDiagram",
			"DeploymentDiagram",
			"ComponentDiagram",
			"CommunicationDiagram",
			"PanelDiagram" );

	private BaseContext _context;

	public MetaModelBuilder( 
			BaseContext context ){

		_context = context;
	}

	public String aggregatesList(
			String guid ){

		String retval = "Unassigned";

		try {
			IRPModelElement theEl = _context.get_rhpPrj().findElementByGUID( guid );

			if( theEl != null ){

				retval = getStringFor( theEl ); 
			} else {
				retval = "Error!!!";
			}

		} catch( Exception e ){
			_context.debug("Exception in aggregatesList, e=" + e.getMessage());
		}

		return retval;
	}

	private String getStringFor(
			IRPModelElement theEl ) throws Exception{

		String retval;

		IRPStereotype theStereotype = getDependentStereotypeFor( theEl ); 

		if( theStereotype == null ){

			retval = _context.elInfo( theEl ) + " is missing a dependency to stereotype";

		} else {

			String theMetaClassesAppliedTo = theStereotype.getOfMetaClass();

			if( theMetaClassesAppliedTo.equals( "Project") ){

				retval = setGeneralModelAddNewMenuStructurePropertyOn(
						theStereotype, theEl);

				setModelStereotypeAggregatesPropertyOn(
						theStereotype, theEl );						

			} else {

				retval = setModelStereotypeAggregatesPropertyOn(
						theStereotype, theEl);						

				// All the base diagram types
				if( _baseDiagramMetaClasses.contains( theMetaClassesAppliedTo ) ){

					MenuItems theAggregates = new MenuItems( _context );

					appendComprisingOfElementFor( theEl, theAggregates, "Aggregation", false );

					String theDrawingToolbar = getCommaSeparatedString( theAggregates, false, false ); 

					_context.setStringPropertyValueInRhp( 
							theStereotype, "Model.Stereotype.DrawingToolbar", theDrawingToolbar );	

					retval = theDrawingToolbar;

				} else {

					for( String metaModelMultipleRelationProperty : _metaModelMultipleRelationProperties ){

						setAdditionalPropertiesFor(
								theEl, 
								theStereotype,
								metaModelMultipleRelationProperty,
								true );
					}

					for( String metaModelSingleRelationProperty : _metaModelSingleRelationProperties ){

						setAdditionalPropertiesFor(
								theEl, 
								theStereotype,
								metaModelSingleRelationProperty,
								false );
					}
				}
			}

			String theMetaClassName = theEl.getName();

			if( !theStereotype.getName().equals( theMetaClassName ) ){

				theStereotype.setName( theMetaClassName );
			}
		}

		return retval;
	}

	private void setAdditionalPropertiesFor(
			IRPModelElement theEl,
			IRPStereotype theStereotype, 
			String theDependencyStereotypeName,
			boolean isAllowMultiple ) throws Exception {

		List<IRPModelElement> theMetaClassEls = 
				getRelatedMetaclassElsFor( theEl, theDependencyStereotypeName );

		String thePropertyValue = "";

		for( Iterator<IRPModelElement> iterator = theMetaClassEls.iterator(); iterator.hasNext(); ) {

			IRPModelElement theMetaClassEl = (IRPModelElement) iterator.next();

			_context.debug( _context.elInfo( theEl ) + " has an " + theDependencyStereotypeName + " name in the metamodel!" );

			MenuItem theMenuItem = new MenuItem( theMetaClassEl );

			thePropertyValue += theMenuItem.getName();

			if( iterator.hasNext() ){
				thePropertyValue += ",";

				if( !isAllowMultiple ){
					throw new Exception( "There is more than more " + theDependencyStereotypeName + 
							" on " + _context.elInfo( theEl ) );
				}
			}
		}

		if( !thePropertyValue.isEmpty() ){

			_context.setStringPropertyValueInRhp( 
					theStereotype, 
					"Model.Stereotype." + theDependencyStereotypeName, 
					thePropertyValue );
		}
	}
	
	public void createMetamodelClassesFor(
			MenuItems theMenuItems,
			IRPClass withDirectedCompositionsFrom ) {
			
		IRPPackage theOwningPkg = _context.
				getOwningPackageFor( _context.getSelectedElement( false ) );
		
		if( theOwningPkg instanceof IRPPackage ) {
			
			String theUniquePackageName = _context.
					determineUniqueNameBasedOn( "SysMLMetaModelPkg", "Package", theOwningPkg );
			
			IRPPackage theNestedPkg = theOwningPkg.addNestedPackage( theUniquePackageName );
			
			for( MenuItem menuItem : theMenuItems ){
								
				String theName = _context.
						determineUniqueNameBasedOn( menuItem.getName(), "Class", theNestedPkg );
				
				IRPClass theClass = theNestedPkg.addClass( theName );
				
				/**
			 	 * Adds a new directed association to the classifier.
			 	 * @param otherClassifier the classifier that the current classifier should be associated with
			 	 * @param roleName the role name to use for the association end
			 	 * @param linkType used to determine the type of association to create. The strings that can be used for this parameter are Association, Aggregation and Composition (parameter is case-sensitive).
			 	 * @param multiplicity the multiplicity to use for the association end. You can use strings such as "1" or "14" to specify a specific number, or you can use one of the values listed in the Features dialog for attributes: "0,1", "*", or "1..*".
			 	 * @param linkName if you want to create an association class, use this parameter to specify the name of the class. If you do not want to create an association class, use an empty string as the value of this parameter.
			 	 * @return the association that was created
			 	 */
				IRPRelation theRelation = withDirectedCompositionsFrom.
						addUnidirectionalRelationTo( theClass, "", "Association", "1", "" );
		
				theRelation.setRelationType( "Composition" );
			}
		}
	}

	public String setGeneralModelAddNewMenuStructurePropertyOn(
			IRPStereotype theStereotype, 
			IRPModelElement theEl ){

		String retval = null;

		_context.debug( "aggregatesList is trying determine Add New Menu Structure for project...");

		MenuItems theExecutableMBSEMenus = new MenuItems( _context );
		
		appendComprisingOfElementFor( theEl, theExecutableMBSEMenus, "Composition", true );

		_context.debug( "theExecutableMBSEMenus ...");
		theExecutableMBSEMenus.dumpList();
		_context.debug( "... theExecutableMBSEMenus.");

		MenuItems theSysMLMenus = getDefaultMenuItemsFromSysML();

		_context.debug( "theSysMLMenus (unfiltered) ...");
		theSysMLMenus.dumpList();
		_context.debug( "... theSysMLMenus.");
		
		// Remove the menus in SysML that have been re-expressed in the profile
		removeMenuItemsFrom( theSysMLMenus, theExecutableMBSEMenus );

		_context.debug( "theSysMLMenus (filtered) ...");
		theSysMLMenus.dumpList();
		_context.debug( "... theSysMLMenus.");
		
		boolean answer = false;
		
		if( !theSysMLMenus.isEmpty() ) {
			
			answer = UserInterfaceHelper.askAQuestion( 
					"There are " + theSysMLMenus.size() + " residual menus not catered for, \n" + 
					"do you want to create classes for these and fix profile before trying again?" );
		}
		
		if( answer ) {
			createMetamodelClassesFor( theSysMLMenus, (IRPClass) theEl );	
		} else {	
			addMenuItemsTo( theExecutableMBSEMenus, theSysMLMenus );

			_context.debug( "theExecutableMBSEMenus (final) ...");
			theExecutableMBSEMenus.dumpList();
			_context.debug( "... theExecutableMBSEMenus (final).");

			retval = getCommaSeparatedString( theExecutableMBSEMenus, true, true ).replaceAll("Automotive Element/zHiddenFlowPort,", ""); 

			if( theStereotype != null ) {			
				_context.setStringPropertyValueInRhp( 
						theStereotype, "General.Model.AddNewMenuStructure", retval );						
			}
		}

		_context.debug( "... aggregatesList has completed determining Add New Menu Structure.");

		return retval;
	}

	private void addMenuItemsTo(
			MenuItems theMenuItems, 
			MenuItems theMenuItemsToAdd ){
		
		for( MenuItem menuItem : theMenuItemsToAdd ){
			theMenuItems.add( menuItem );
		}
	}

	private void removeMenuItemsFrom(
			MenuItems theMenuItems,
			MenuItems theMenuItemsToRemove ) {
		
		for( MenuItem menuItem : theMenuItemsToRemove ){

			String theName = menuItem.getName();

			if( theMenuItems.isMenuItemPresent( theName ) ){
				_context.info( "Filtering out " + theName );
				theMenuItems.removeElement( theName );

			} else {
				_context.debug( "Keeping " + theName );
			}
		}
	}

	private String setModelStereotypeAggregatesPropertyOn(
			IRPStereotype theStereotype, 
			IRPModelElement el) {

		String retval;

		MenuItems theCompositions = new MenuItems( _context );

		appendComprisingOfElementFor( el, theCompositions, "Composition", false );

		retval = getCommaSeparatedString( theCompositions, false, true ); 

		_context.setStringPropertyValueInRhp( 
				theStereotype, "Model.Stereotype.Aggregates", retval );

		return retval;
	}

	public class NameSorter implements Comparator<MenuItem> {
		public int compare(
				MenuItem o1, MenuItem o2 ){
			return o1.getName().compareTo( o2.getName() );
		}
	}

	public class SubheadingSorter implements Comparator<MenuItem> {
		public int compare(
				MenuItem o1, MenuItem o2 ){

			return o1.get_Subheading().compareTo( o2.get_Subheading() );
		}
	}

	private String getCommaSeparatedString(
			MenuItems theMenuItems,
			boolean isAddSubmenu,
			boolean isSort ){

		String theLine = "";

		if( isSort ){
			Collections.sort( theMenuItems, 
					new SubheadingSorter().thenComparing( 
							new NameSorter() ) );			
		}

		for( Iterator<MenuItem> iterator = theMenuItems.iterator(); iterator.hasNext();) {

			MenuItem theMenuItem = iterator.next();

			if( isAddSubmenu ){
				theLine += theMenuItem.get_Subheading();
			}

			theLine += theMenuItem.getName();

			if( iterator.hasNext() ){
				theLine += ",";
			}

			_context.debug( theLine );
		}

		return theLine;
	}

	private boolean isMenuItemAlreadyInList(
			MenuItem theMenuItem,
			List<MenuItem> theExistingMenuItems ){

		boolean result = false;

		for (MenuItem theExistingMenuItem : theExistingMenuItems) {

			if( theExistingMenuItem.getName().equals( theMenuItem.getName() ) &&
					theExistingMenuItem.get_Subheading().equals(theMenuItem.get_Subheading() ) ){

				result = true;
				break;
			}
		}

		return result;
	}

	private void appendComprisingOfElementFor( 
			IRPModelElement theEl, 
			MenuItems theComprisingMenuItems,
			String withRelationType,
			boolean recursive ){

		_context.debug( "appendComprisingOfElementFor invoked to find " + withRelationType + 
				" for " + _context.elInfo( theEl ) + "..." );

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theAssocEnds = theEl.getNestedElementsByMetaClass( 
				"AssociationEnd", 0 ).toList();

		for( IRPModelElement theAssocEnd : theAssocEnds ){

			IRPRelation theRelation = (IRPRelation)theAssocEnd;

			if( theRelation.getRelationType().equals( withRelationType ) ){

				IRPModelElement theOtherClass = theRelation.getOtherClass();

				MenuItem theOtherClassMenuItem = new MenuItem( theOtherClass );

				if( !isMenuItemAlreadyInList( 
						theOtherClassMenuItem, 
						theComprisingMenuItems ) ){

					theComprisingMenuItems.add( theOtherClassMenuItem );

					_context.debug( "added " + theOtherClassMenuItem.toString() );

					if( recursive ){

						appendComprisingOfElementFor( 
								theOtherClass, 
								theComprisingMenuItems, 
								withRelationType, 
								recursive );
					}
				} else {
					_context.debug( "Did not add or recurse for " + theOtherClassMenuItem.toString() + 
							" as it's aleady in list of " + theComprisingMenuItems.size() + " menu items" );
				}
			}
		}

		IRPClassifier theClassifier = (IRPClassifier)theEl;

		@SuppressWarnings("unchecked")
		List<IRPClassifier> theBaseClassifiers = theClassifier.getBaseClassifiers().toList();

		for( IRPModelElement theBaseClassifier : theBaseClassifiers ){	

			appendComprisingOfElementFor( 
					theBaseClassifier, 
					theComprisingMenuItems, 
					withRelationType, 
					recursive );
		}	

		_context.debug("... appendComprisingOfElementFor for " + _context.elInfo( theEl ) + " found " + theComprisingMenuItems.size() + " elements" );
	}

	private IRPStereotype getDependentStereotypeFor(
			IRPModelElement theEl ){

		List<IRPStereotype> theStereotypes = new ArrayList<>();
		IRPStereotype theStereotype = null;

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theDependencyEls = theEl.getDependencies().toList();

		for (IRPModelElement theDependencyEl : theDependencyEls) {

			@SuppressWarnings("unchecked")
			List<IRPModelElement> theStereotypesOnDependency = 
			theDependencyEl.getStereotypes().toList();

			if( theStereotypesOnDependency.isEmpty() ){

				IRPDependency theDependency = (IRPDependency)theDependencyEl;

				IRPModelElement theDependsOn = theDependency.getDependsOn();

				if( theDependsOn instanceof IRPStereotype ){
					theStereotypes.add( (IRPStereotype) theDependsOn );
				}
			}
		}

		if( theStereotypes.size() == 1 ){
			theStereotype = theStereotypes.get( 0 );
		}

		return theStereotype;
	}

	private List<IRPModelElement> getRelatedMetaclassElsFor(
			IRPModelElement theEl,
			String basedOnDependencyType ) throws Exception{

		List<IRPModelElement> theRelatedEls = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theDependencyEls = theEl.getDependencies().toList();

		for( IRPModelElement theDependencyEl : theDependencyEls ){

			try {
				@SuppressWarnings("unchecked")
				List<IRPModelElement> theStereotypesOnDependency = theDependencyEl.getStereotypes().toList();

				for( IRPModelElement theStereotypeOnDependency : theStereotypesOnDependency ){

					if( theStereotypeOnDependency.getName().equals( basedOnDependencyType ) ){

						IRPDependency theDependency = (IRPDependency)theDependencyEl;
						IRPModelElement theDependsOn = theDependency.getDependsOn();
						theRelatedEls.add( theDependsOn );						
					}
				}		
			} catch (Exception e) {
				throw new Exception( "get getRelatedMetaclassElFor threw e=" + e.getMessage() );
			}

		}

		return theRelatedEls;
	}
}

/**
 * Copyright (C) 2020-2022  MBSE Training and Consulting Limited (www.executablembse.com)

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
