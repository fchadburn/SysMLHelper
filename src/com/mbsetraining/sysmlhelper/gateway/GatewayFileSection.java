package com.mbsetraining.sysmlhelper.gateway;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import com.mbsetraining.sysmlhelper.common.BaseContext;

public class GatewayFileSection {

	private String m_SectionName = null;
	private boolean m_isImmutable = false;
	protected BaseContext _context;
	
	LinkedHashMap<String, String> m_SettingsMap = new LinkedHashMap<String, String>();
	
	public GatewayFileSection(
			String theDocumentName,
			BaseContext context ) {
		
		_context = context;
		this.m_SectionName = theDocumentName;
	}
	
	private String getVariableXKey(
			String forVariableXName){
		
		String theVariableXKey = null;
		
        Iterator<Entry<String, String>> theIterator =  m_SettingsMap.entrySet().iterator();
        
        while( theIterator.hasNext() ) {
        	
           Entry<String, String> theEntry = theIterator.next();
           
           String theFullKey = theEntry.getKey();
           String theFullValue = theEntry.getValue();
           
           if (theFullValue.equals( forVariableXName )){
        	   
        	   // take Name off string and replace with Value
        	   theVariableXKey = theFullKey.substring(0, theFullKey.length()-4) + "Value";
        	   break;
           }   
        }
        
        return theVariableXKey;
	}
	
	public String getVariableXValue(
			String forVariableXName){
		
		String theValue = null;
		String theKey = getVariableXKey( forVariableXName );
	
        Iterator<Entry<String, String>> theIterator =  m_SettingsMap.entrySet().iterator();
        
        while( theIterator.hasNext() ) {
        	
           Entry<String, String> theEntry = theIterator.next();
           
           String theFullKey = theEntry.getKey();
           String theFullValue = theEntry.getValue();
           
           if (theFullKey.equals( theKey )){
        	   theValue=theFullValue;
           }
        }
        
        return theValue;
	}
	
	public void renameStringInAllValues(
			String replaceThisString, 
			String withThisString ){

        Iterator<Entry<String, String>> theIterator =  m_SettingsMap.entrySet().iterator();
        
        while( theIterator.hasNext() ) {
        	
           Entry<String, String> theEntry = theIterator.next();
           
           String theFullKey = theEntry.getKey();
           String theFullValue = theEntry.getValue();
           
           String newValue = theFullValue.replaceAll( replaceThisString, withThisString );
           
           if( !theFullValue.equals( newValue ) ){
        	   _context.debug("Updating value for " + theFullKey + " from '" + theFullValue + "' to '" + newValue + "'");
        	   setValueFor( theFullKey, newValue );
           }
        }
	}
	
	public void setVariableXValue(
			String forVariableXName, 
			String toNewValue){
		
		String theKey = getVariableXKey( forVariableXName );
		
		if (theKey != null){
			setValueFor( theKey, toNewValue);
		} else {
			_context.error("Error in setVariableXValue, " + forVariableXName + " was not found");
		}
	}
	
	public String getValueFor(
			String theSettingName){
		
		String theValue = null;
		
		if (m_SettingsMap.containsKey(theSettingName)){
			theValue = m_SettingsMap.get( theSettingName );
		} 
		
		return theValue;
	}
	
	public void setValueFor(
			String theSettingName, 
			String toValue){	
		
		m_SettingsMap.put( theSettingName, toValue );
	}
	
	public String getSectionName(){
		return m_SectionName;
	}
	
	public void setSectionName(
			String theNewName){
		m_SectionName = theNewName;
	}
	
	public List<String> getRqtfLinesForDocument(){
		
		List<String> theFullDoc = new ArrayList<String>();

		theFullDoc.add( "[" + getSectionName() + "]" );
		
        Iterator<Entry<String, String>> theIterator =  m_SettingsMap.entrySet().iterator();
        
        while(theIterator.hasNext()) {
        	
           Entry<String, String> theEntry = theIterator.next();
           
           theFullDoc.add( theEntry.getKey() + "=" + theEntry.getValue() );
        }
        
        theFullDoc.add("");
		
		return theFullDoc;
	}

	public boolean isImmutable() {
		return m_isImmutable;
	}

	public void setIsImmutable(
			boolean isImmutable) {
		this.m_isImmutable = isImmutable;
	}
	
	public void MergeInChangesIn( 
			GatewayFileSection theGatewayDoc ){
		
		Iterator<Entry<String, String>> theIterator =  theGatewayDoc.m_SettingsMap.entrySet().iterator();

		while( theIterator.hasNext() ) {

			Entry<String, String> theEntry = theIterator.next();

			String theFullKey = theEntry.getKey();
			String theFullValue = theEntry.getValue();

			String theCurrentValue = m_SettingsMap.get( theFullKey );

			if( theCurrentValue == null ){
				_context.debug("Value of " + theFullKey + " needs to be added as not present, the new value is " + theFullValue);

			} else if( !theCurrentValue.equals( theFullValue ) ){
				_context.debug("Value of " + theFullKey + " needs to change from " + theCurrentValue + " to " + theFullValue );
				setValueFor( theFullKey, theFullValue );
			}
		}
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