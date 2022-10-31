package com.mbsetraining.sysmlhelper.metamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.mbsetraining.sysmlhelper.common.BaseContext;

public class MenuItems extends ArrayList<MenuItem> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1064762770590310599L;

	public MenuItems(
			BaseContext context ){
		super();
		_context = context;
	}

	public MenuItems(
			Collection<? extends MenuItem> c,
			BaseContext context ){
		super(c);
		_context = context;
	}

	public MenuItems(
			int initialCapacity,
			BaseContext context ){
		super(initialCapacity);
		_context = context;
	}

	BaseContext _context;
	
	public boolean isMenuItemPresent( 
			String withName ) {
		
		boolean isElementPresent = false;
		
		for( MenuItem theMenuItem : this ){
			
			if( theMenuItem.getName().equals( withName ) ) {
				isElementPresent = true;
				break;
			}
		}
		
		return isElementPresent;
	}
	
	public MenuItem getMenuItem( 
			String withName ) {
		
		MenuItem theMenuItem = null;
		
		for( MenuItem theCandidate : this ){
			
			if( theCandidate.getName().equals( withName ) ) {
				theMenuItem = theCandidate;
				break;
			}
		}
		
		return theMenuItem;
	}
	
	public void removeElement( String withName ) {
		
		Iterator<MenuItem> iterator = this.iterator();
		
		while( iterator.hasNext() ){
			
			MenuItem current = iterator.next();   

			if( current.getName().equals( withName ) ){
				iterator.remove();
			}
		}
	}
	
	public void dumpList() {
		
		for( MenuItem menuItem : this ){
			_context.info( menuItem.toString() );
		}
	}
}

/**
 * Copyright (C) 2022  MBSE Training and Consulting Limited (www.executablembse.com)

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
