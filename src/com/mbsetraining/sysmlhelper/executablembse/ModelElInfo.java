package com.mbsetraining.sysmlhelper.executablembse;

import java.util.ArrayList;
import java.util.List;

import com.telelogic.rhapsody.core.*;

public class ModelElInfo {
	
	public IRPClassifier _classifier;
	public IRPInstance _part;
	public boolean _isSilent;
	
	public ModelElInfo(
			IRPClassifier theClassifier,
			IRPInstance thePart,
			boolean isSilent ){
	
		_classifier = theClassifier;
		_part = thePart;
		_isSilent = isSilent;
	}
	
	public String toString(){
		
		String theString = "";
		
		if( _part != null ){
			theString += _part.getName();
		}
		
		theString += ":";
		
		theString += _classifier.getName();
				
		if( _part != null ){
    		theString += " (" + _part.getUserDefinedMetaClass();
		} else {
    		theString += " (" + _classifier.getUserDefinedMetaClass();
		}
		
		if( _isSilent ){
			theString += " - BASE TYPE";
		}
		
		theString += ")";

		return theString;
	}

	public List<ModelElInfo> getChildren(){

		List<ModelElInfo> theChildren = getChildrenWhichAreParts();

		@SuppressWarnings("unchecked")
		List<IRPGeneralization> theGeneralizations = _classifier.getGeneralizations().toList();
		
		for (IRPGeneralization theGeneralization : theGeneralizations) {
			IRPClassifier theClassifier = theGeneralization.getBaseClass();
			theChildren.add( new ModelElInfo( theClassifier, null, true ) );
		}
		
		return theChildren;
	}
	
	private List<ModelElInfo> getChildrenWhichAreParts(){

		List<ModelElInfo> theChildren = new ArrayList<>();
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theObjectEls = _classifier.getNestedElementsByMetaClass( 
				"Object", 0 ).toList();

		for( IRPModelElement theObjectEl : theObjectEls ){
			
			IRPInstance theObject = (IRPInstance)theObjectEl;

				IRPClassifier theOtherClass = theObject.getOtherClass();
				ModelElInfo theChild = new ModelElInfo( theOtherClass, theObject, false );
				theChildren.add( theChild );
		}
		
		return theChildren;
	}
}
