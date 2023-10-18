package com.mbsetraining.sysmlhelper.common;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;

import com.telelogic.rhapsody.core.IRPModelElement;

public class RhapsodyComboBox extends JComboBox<Object>{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 871276583137927861L;
	protected List<IRPModelElement> _elementChoices;
	protected List<String> _nameChoices;
	
	public RhapsodyComboBox(
			List<IRPModelElement> inList, 
			Boolean isFullPathRequested ){
		
		super();
		
		_elementChoices = inList;
		
		_nameChoices = new ArrayList<String>();
		
		for( int i = 0; i < _elementChoices.size(); i++ ){
			
			if( isFullPathRequested ){
				_nameChoices.add( i, _elementChoices.get(i).getFullPathName() );
			} else {
				_nameChoices.add( i, _elementChoices.get(i).getName() );
			}
		} 	
		
		insertItemAt( "Nothing", 0 );
		
		for( String theName : _nameChoices ){
			this.addItem( theName );
		}
		
		this.setSelectedItem( "Nothing" );
	}
	
	public IRPModelElement getSelectedRhapsodyItem(){
		
		IRPModelElement theModelEl = null;
		
		Object theSelectedItem = this.getSelectedItem();	
		
		if (!theSelectedItem.equals("Nothing")){
			int index = _nameChoices.indexOf(theSelectedItem);		
			theModelEl = _elementChoices.get(index);
		}

		return theModelEl;
	}
	
	public void setSelectedRhapsodyItem(
			IRPModelElement toTheElement ){
				
		for( int i = 0; i < _elementChoices.size(); i++ ){

			IRPModelElement theElement = _elementChoices.get( i );
			
			if( theElement != null && 
				theElement.equals( toTheElement ) ){
				
				setSelectedIndex( i+1 );
				break;
			}
		}
	}
	
	public void setSelectedToNothing(){
		setSelectedIndex( 0 );
	}
}

/**
 * Copyright (C) 2016-2023  MBSE Training and Consulting Limited (www.executablembse.com)

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
