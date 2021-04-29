package com.mbsetraining.sysmlhelper.metamodel;

import java.util.ArrayList;
import java.util.List;

import com.telelogic.rhapsody.core.*;

class MenuItem {

	private String _name;
	private String _subheading;
	private IRPModelElement _theEl;

	MenuItem( IRPModelElement theMetaClass ){
		
		_theEl = theMetaClass;

		IRPStereotype theStereotype = getDependentStereotypeFor( _theEl ); 

		if( theStereotype == null ){
			_name = theMetaClass.getName();
		} else {
			_name = getNewTermName( theStereotype );
		}

		_subheading = getSubmenuString( theMetaClass );
	}

	public String getName() {
		return _name;
	}

	public String get_Subheading() {
		return _subheading;
	}
	
	@Override
	public String toString(){
		return get_Subheading() + getName();
	}

	private IRPStereotype getDependentStereotypeFor(
			IRPModelElement theEl ){

		List<IRPStereotype> theStereotypes = new ArrayList<>();
		IRPStereotype theStereotype = null;

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theDependencyEls = theEl.getDependencies().toList();

		for (IRPModelElement theDependencyEl : theDependencyEls) {

			IRPDependency theDependency = (IRPDependency)theDependencyEl;

			IRPModelElement theDependsOn = theDependency.getDependsOn();

			if( theDependsOn instanceof IRPStereotype ){
				theStereotypes.add( (IRPStereotype) theDependsOn );
			}
		}

		if( theStereotypes.size() == 1 ){
			theStereotype = theStereotypes.get( 0 );
		}

		return theStereotype;
	}

	private String getNewTermName(
			IRPStereotype forStereotype ){

		String theName = forStereotype.getName();

		String thePropertyName = forStereotype.getPropertyValue( "Model.Stereotype.Name" );

		if( thePropertyName != null && !thePropertyName.isEmpty() ){
			theName = thePropertyName;
		}

		return theName;
	}

	private String getSubmenuString(
			IRPModelElement forEl ){

		String theSubmenuString = "";

		@SuppressWarnings("unchecked")
		List<IRPModelElement> theStereotypeEls = forEl.getStereotypes().toList();
		List<String> theFilteredList = new ArrayList<>();

		for( IRPModelElement theStereotypedEl : theStereotypeEls ){

			String theName = theStereotypedEl.getName();

			if( !theName.equals( "NewTerm" ) ){
				theFilteredList.add( theName );
			}
		}

		if( theFilteredList.size() == 1 ){
			theSubmenuString = theFilteredList.get(0) + "/";
		}

		return theSubmenuString;
	}
}