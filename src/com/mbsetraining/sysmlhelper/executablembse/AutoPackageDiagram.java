package com.mbsetraining.sysmlhelper.executablembse;

import java.util.ArrayList;
import java.util.List;

import com.mbsetraining.sysmlhelper.common.ConfigurationSettings;
import com.telelogic.rhapsody.core.*;

public class AutoPackageDiagram {

	ConfigurationSettings _context;
	IRPObjectModelDiagram _diagram = null; 
	
	private boolean isRootOwnerInProjectAProfile(
			IRPModelElement theEl ){
		
		boolean isAProfile;
		
		IRPModelElement theOwner = theEl.getOwner();
		
		if( theOwner instanceof IRPProject ){
			
			if( theEl instanceof IRPProfile ){
				isAProfile = true;
			} else {
				isAProfile = false;
			}
		} else {
			isAProfile = isRootOwnerInProjectAProfile( theOwner );
		}
		
		return isAProfile;
	}
	
	public AutoPackageDiagram( 
			ConfigurationSettings theContext ) {
		
		_context = theContext;
	}
	
	private String getDiagramName(){
		return "PKG - " + _context.get_rhpPrj().getName();
	}
	
	@SuppressWarnings("unchecked")
	public void drawDiagram() {

		String theName = getDiagramName();
		
		IRPModelElement theExistingDiagramEl =
				_context.get_rhpPrj().findNestedElement( 
						theName, 
						"ObjectModelDiagram" );
		
		List<IRPModelElement> elementsAlreadyPresent = 
				new ArrayList<>();
				
		if( theExistingDiagramEl != null ){
			
			_diagram = (IRPObjectModelDiagram) theExistingDiagramEl;
			elementsAlreadyPresent = _diagram.getElementsInDiagram().toList();
		}
		
		List<IRPModelElement> theCandidates = 
				_context.get_rhpPrj().getNestedElementsByMetaClass( 
						"Package", 1 ).toList();
		
		IRPCollection theCollection = _context.get_rhpApp().createNewCollection();

		for( IRPModelElement thePkg : theCandidates ){
			
			if( !elementsAlreadyPresent.contains(thePkg) && 
				!isRootOwnerInProjectAProfile( thePkg ) ){
				theCollection.addItem( thePkg );
			}
		}
		
		IRPCollection theRelationsCollection = _context.get_rhpApp().createNewCollection();
		theRelationsCollection.setSize( 1 );
		theRelationsCollection.setString( 1, "AllRelations" );
		
		if( _diagram == null ){
			_diagram = _context.get_rhpPrj().addObjectModelDiagram( theName );
			_diagram.changeTo( "Package Diagram" );
		}

		_diagram.populateDiagram( theCollection, theRelationsCollection, "among" );
	}
}

/**
 * Copyright (C) 2018-2021  MBSE Training and Consulting Limited (www.executablembse.com)

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