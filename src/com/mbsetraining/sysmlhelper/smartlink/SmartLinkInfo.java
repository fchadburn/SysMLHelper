package com.mbsetraining.sysmlhelper.smartlink;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mbsetraining.sysmlhelper.common.ConfigurationSettings;
import com.mbsetraining.sysmlhelper.common.LayoutHelper;
import com.telelogic.rhapsody.core.*;

public class SmartLinkInfo {

	private DiagramElementList _startLinkElements;
	private DiagramElementList _endLinkElements;
	private IRPStereotype _relationType;
	private boolean _isPopulatePossible;	
	private int _countRelationsNeeded;
	private Set<RelationInfo> _relationInfos;
	private ConfigurationSettings _context;
	
	public SmartLinkInfo(
			List<IRPModelElement> theStartLinkEls,
			List<IRPGraphElement> theStartLinkGraphEls,
			List<IRPModelElement> theEndLinkEls,
			List<IRPGraphElement> theEndLinkGraphEls,
			ConfigurationSettings context ){
		
		_context = context;
		
		_relationInfos = new HashSet<RelationInfo>();
		
		if( _startLinkElements != null ){
			_startLinkElements.clear();			
		}
		
		_startLinkElements = new DiagramElementList( 
				theStartLinkEls, theStartLinkGraphEls, _context );
		
		if( _endLinkElements != null ){
			_endLinkElements.clear();			
		}
		
		_endLinkElements = new DiagramElementList( 
				theEndLinkEls, theEndLinkGraphEls, _context );
		
		_isPopulatePossible = false;
		
		//IRPModelElement contextEl = theEndLinkEls.get(0);
		
		if( _startLinkElements.areElementsAllReqts() ){

			_relationType = _context.getExistingStereotype(
					"deriveReqt", _context.get_rhpPrj() );

		} else if( _startLinkElements.areElementsAllDeriveDependencySources() ){

			_relationType = _context.getStereotypeToUseForActions();

		} else if( _startLinkElements.areElementsAllRefinementDependencySources() ){

			_relationType = _context.getStereotypeToUseForUseCases();

		} else if( _startLinkElements.areElementsAllVerificationDependencySources() ){

			_relationType = _context.getExistingStereotype( "verify", _context.get_rhpPrj() );
			
		} else if( _startLinkElements.areElementsAllSatisfyDependencySources() ){

			_relationType = _context.getStereotypeToUseForFunctions();

		} else {

			_relationType = null;
			_context.error( "Unable to find relation type" );
		}
		
		_context.debug( "SmartLinkInfo: Determined that relation type needed is " + 
				_context.elInfo( _relationType ) );
		
		if( _relationType != null ){
			
			for( DiagramElementInfo theStartLinkEl : _startLinkElements ){
				
				for( DiagramElementInfo theEndLinkEl : _endLinkElements ){
					
					RelationInfo theRelationInfo = new RelationInfo(
							theStartLinkEl, theEndLinkEl, _relationType, _context );

					_relationInfos.add( theRelationInfo );		
					
					boolean isPopulatePossibleForRelation = 
							performPopulateOnDiagram(
									theRelationInfo,
									true );
					
					if( isPopulatePossibleForRelation ){
						_isPopulatePossible = true;
					}
				}
			}
		}
		
		_countRelationsNeeded = 0;
		
		for( RelationInfo relationInfo : _relationInfos ){
			
			if( relationInfo.getExistingStereotypedDependency() == null ){
				_countRelationsNeeded++;
			}
		}		
	}

	private boolean performPopulateOnDiagram(
			RelationInfo theRelationInfo,
			boolean isJustCheckWithoutDoing ){
		
		IRPDependency existingDependency = 
				theRelationInfo.getExistingStereotypedDependency();

		boolean isPopulatePossible = false;
		
		for( IRPGraphElement theStartGraphEl : theRelationInfo.getStartElement().getGraphEls() ){

			for( IRPGraphElement theEndGraphEl : theRelationInfo.getEndElement().getGraphEls() ){

				if( theStartGraphEl.getDiagram().equals( theEndGraphEl.getDiagram() )){

					IRPDiagram theDiagram = theStartGraphEl.getDiagram();

					if( existingDependency == null ){
						
						isPopulatePossible = true;
						
					} else { // check if relation is already shown on diagram

						@SuppressWarnings("unchecked")
						List<IRPGraphElement> theExistingGraphEls =
								theDiagram.getCorrespondingGraphicElements( 
										existingDependency ).toList();

						if( theExistingGraphEls.isEmpty() ){

							_context.debug( "Determined graphEdge needed for " + 
									_context.elInfo( _relationType ) + " from " + 
									_context.elInfo( theStartGraphEl.getModelObject() ) + " to " + 
									_context.elInfo( theEndGraphEl.getModelObject() ) + " on " +
									_context.elInfo( theDiagram ) );

							isPopulatePossible = true;
							
							if( !isJustCheckWithoutDoing ){

								LayoutHelper theHelper = new LayoutHelper( _context );
								
								theHelper.drawDependencyToMidPointsFor(
										existingDependency, 
										theStartGraphEl, 
										theEndGraphEl,
										theDiagram );
							}

						} else {
							
							_context.info( "Determined graphEdge for " + 
									_context.elInfo( _relationType ) + " already exists from " + 
									_context.elInfo( theStartGraphEl.getModelObject() ) + " to " + 
									_context.elInfo( theEndGraphEl.getModelObject() ) + " on " +
									_context.elInfo( theDiagram ) );
						}
					}
				}
			}
		}
		return isPopulatePossible;
	}
	
	public String getDescriptionHTML(){
		
		String theMsg = "<html><div style=\"width:300px;\">";
		
		theMsg+= "<p style=\"text-align:center;font-weight:normal\">";
		
		if( _startLinkElements.size() == 1 && _endLinkElements.size()==1 ){
			theMsg+="Create a ";
		} else {
			theMsg+="Create ";
		}
		
		theMsg+= "<span style=\"font-weight:bold\">«" +  _relationType.getName() + "»</span>";
		
		if( _startLinkElements.size() == 1 && _endLinkElements.size()==1 ){
			theMsg+=" dependency from:</p>";
		} else {
			theMsg+=" dependencies from:</p>";
		}
		
		theMsg+="<p></p>";
		theMsg+="<p style=\"text-align:center;font-weight:normal\">";
		
		if( _startLinkElements.size() == 1 ){
			theMsg+= _startLinkElements.size() + " element (a ";
		} else {
			theMsg+= _startLinkElements.size() + " elements (a "; 
		}

		theMsg+= _startLinkElements.getCommaSeparatedListOfElementsHTML( 3 );
		theMsg+=")</p>";
		theMsg+="<p></p>";
		theMsg+="<p style=\"text-align:center;font-weight:normal\">to:</p>";
		theMsg+="<p></p>";
		theMsg+="<p style=\"text-align:center;font-weight:normal\">";
		
		if( _endLinkElements.size() == 1 ){
			theMsg+= _endLinkElements.size() + " element (a  ";
		} else {
			theMsg+= _endLinkElements.size() + " elements (a  ";
		}
		
		theMsg+= _endLinkElements.getCommaSeparatedListOfElementsHTML( 3 );						
		theMsg+= ")</p>";
		theMsg+="<p></p>";
		
		if( _countRelationsNeeded > 0 ){
			
			theMsg+= "<p style=\"text-align:center;font-weight:normal\">" + _countRelationsNeeded + " new dependencies will be created" + "</p>";
			
		} else if ( getIsPopulatePossible()==false ){
			
			theMsg+= "<p style=\"text-align:center;font-weight:normal\">" + "There is nothing to do, i.e. relations already exist and/or are shown" + "</p>";
			
		} else {
			
			theMsg+= "<p style=\"text-align:center;font-weight:normal\">" + "These relations already exist (but are not shown)" + "</p>";

		}
		
		theMsg+="<p></p>";
		theMsg+="<p></p>";
		theMsg+="</div></html>";
		
		return theMsg;
	}
	
	public boolean getIsPopulatePossible(){
		
		return _isPopulatePossible;
	}
	
	public boolean getAreNewRelationsNeeded(){
		
		return ( _countRelationsNeeded > 0 );
	}
	
	protected boolean isDeriveDependencyNeeded(){
		
		boolean isNeeded = 
				_startLinkElements.areElementsAllDeriveDependencySources() && 
				_endLinkElements.areElementsAllReqts();
		
		_context.debug( "isDeriveDependencyNeeded is returning " + isNeeded );
		
		return isNeeded;
	}
	
	public void createDependencies( 
			boolean withPopulateOnDiagram ){
		
		for( RelationInfo theRelationInfo : _relationInfos ){
			
			IRPDependency theDependency = 
					theRelationInfo.getExistingStereotypedDependency();
			
			if( theDependency == null ){
				
				theDependency = _context.addStereotypedDependencyIfOneDoesntExist(
						theRelationInfo.getStartElement().getElement(), 
						theRelationInfo.getEndElement().getElement(), 
						_relationType );
			}
			
			performPopulateOnDiagram(
					theRelationInfo,
					false );
		}
	}
}

/**
 * Copyright (C) 2017-2021  MBSE Training and Consulting Limited (www.executablembse.com)

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