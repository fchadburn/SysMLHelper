package com.mbsetraining.sysmlhelper.activitydiagram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mbsetraining.sysmlhelper.common.BaseContext;
import com.telelogic.rhapsody.core.*;
 
public class ActionList extends ArrayList<ActionInfo> {
 
	/**
	 * 
	 */
	private static final long serialVersionUID = -2124165730546915443L;
	
	ActionList(
			IRPActivityDiagram theAD,
			BaseContext context ){
		
		super();
						
		@SuppressWarnings("unchecked")
		List<IRPModelElement> candidateEls = theAD.getElementsInDiagram().toList();
		
		for (IRPModelElement theEl : candidateEls) {
			
			if( ActionInfo.isTraceabilityNeededFor( theEl, context )){
				
				ActionInfo theInfo = new ActionInfo( theEl, context ); 
				this.add( theInfo );
			}
		}
		
		ensureUniqueReferenceActivityNames();
	}
	
	public void ensureUniqueReferenceActivityNames(){
		
		Map<String, List<ActionInfo>> theNameMap = new HashMap<>();
		
		for (ActionInfo theCandidate : this) {
		
			if( theCandidate.getType().equals( "ReferenceActivity" ) ){
				
				String theName = theCandidate.getDesiredName();
				
				List<ActionInfo> existingInfos = 
						theNameMap.getOrDefault( theName, new ArrayList<>() );
				
				existingInfos.add( theCandidate );
				
				theNameMap.put( theName, existingInfos );
			}
		}
		
		for( Entry<String, List<ActionInfo>>  entryForId : theNameMap.entrySet() ){

			List<ActionInfo> value = entryForId.getValue();
			
			if( value.size() > 1 ){
				
				int count = 1;
				
				Iterator<ActionInfo> iterator = value.iterator();
				
				// skip first one
				iterator.next();
				
				while (iterator.hasNext() ) {
					
					ActionInfo actionInfo = iterator.next();
											
					String newName;
						
					do {
						newName =  actionInfo.getDesiredName() + "_" + count;
						count++;

					} while ( theNameMap.containsKey( newName ) );
						
					actionInfo.setDesiredName( newName );
					actionInfo.setChosenNameToDesiredName();							
				}
			}				
		}
	}
	
	public boolean isRenamingNeeded(){
		boolean isNeeded = false;
		
		for (ActionInfo theInfo : this) {
			if (theInfo.isRenameNeeded()){
				isNeeded = true;
				break;
			}
		}
		
		return isNeeded;
	}
	
	public int getNumberOfRenamesNeeded(){
		int count = 0;
		
		for (ActionInfo theInfo : this) {
			if (theInfo.isRenameNeeded()){
				count++;
			}
		}
		
		return count;
	}
	
	public int getNumberOfTraceabilityFailures(){
		int count = 0;
		
		for (ActionInfo theInfo : this) {
			if (theInfo.isTraceabilityFailure()){
				count++;
			}
		}
		
		return count;
	}
	
	public void chooseNames(){
	
		chooseNamesFirstPass();
		chooseNamesSecondPass();
		chooseNamesThirdPass();
	}
	
	private void chooseNamesFirstPass(){
		
		for (ActionInfo theInfo : this) {
			if (!theInfo.isRenameNeeded()){
				theInfo.setChosenNameToOldName();
			}
		}
	}
	
	private void chooseNamesSecondPass(){

		for (ActionInfo theInfo : this) {
			if (theInfo.isRenameNeeded()){
				if (isNameFree(theInfo.getDesiredName())){
					theInfo.setChosenNameToDesiredName();
				}
			}
		}
	}
	
	private void chooseNamesThirdPass(){

		for (ActionInfo theInfo : this) {
			if (!theInfo.isNameChosen()){
				
				int count = 0;
				String theNameToTry = theInfo.getDesiredName();
				
				while (!isNameFree( theNameToTry )){
					count++;
					theNameToTry = theInfo.getDesiredName()+ " (" + count + ")";
				}
				
				theInfo.setChosenName(theNameToTry);
			}
		}
	}
	
	public boolean isNameFree(String theName){
		
		boolean result = true;
		
		for (ActionInfo theInfo : this) {
			if (theInfo.isNameChosen() && 
				theInfo.getChosenName().equals(theName)){
				result = false;
			}
		}
		return result;
	}
	
	public void performRenames(){
		
		chooseNames();
		for (ActionInfo theInfo : this) {
			theInfo.performRename();
		}
			
	}
	
	public List<ActionInfo> getListOfActionsCheckedForTraceability(){
		
		List<ActionInfo> theActions = new ArrayList<ActionInfo>();
		
		for (ActionInfo theInfo : this) {
			if (theInfo.isTraceabilityChecked()){
				theActions.add(theInfo);
			}
		}
		return theActions;
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
