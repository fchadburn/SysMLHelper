package functionalanalysisplugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.mbsetraining.sysmlhelper.common.ConfigurationSettings;
import com.telelogic.rhapsody.core.*;

public class ModelElementList extends ArrayList<IRPModelElement>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected ConfigurationSettings _context;
	
	public ModelElementList(
			ConfigurationSettings context ) {
		super();
		_context = context;
	}

	public ModelElementList(
			Collection<? extends IRPModelElement> c,
			ConfigurationSettings context ) {
		super(c);
		_context = context;
	}

	public ModelElementList(
			int initialCapacity,
			ConfigurationSettings context ) {
		super(initialCapacity);
		_context = context;
	}

	public ModelElementList getListFilteredBy(String theMetaClass){
		
		ModelElementList theList = new ModelElementList( _context );
		
		for (IRPModelElement theEl : this) {
			if (theEl.getMetaClass().equals(theMetaClass)){
				theList.add(theEl);
			}
		}
		
		return theList;
		
	}
	
	public ModelElementList getListFilteredBy(IRPModelElement theElement){
		
		ModelElementList theList = new ModelElementList( _context );
		
		for (IRPModelElement theEl : this) {
			if (theEl.equals(theElement)){
				theList.add(theEl);
			}
		}
		
		return theList;
		
	}
	
	public boolean hasDuplicates(){
		
		boolean isDuplicatesFound = false;
		
		Set<IRPModelElement> theSet = new HashSet<IRPModelElement>(this);

		if (theSet.size() < this.size()){
		    isDuplicatesFound = true;
		}
		
		return isDuplicatesFound;
	}
	
	public void deleteFromProject(){
		this.removeDuplicates();
		
		for (IRPModelElement theEl : this) {
			_context.info("Deleting " + _context.elInfo(theEl) + " from the project");
			theEl.deleteFromProject();
		}
	}
	
	public void removeDuplicates(){
		
		Set<IRPModelElement> theSet = new HashSet<IRPModelElement>(this);
		this.clear();
		this.addAll(theSet);
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
