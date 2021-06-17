package com.mbsetraining.sysmlhelper.taumigratorplugin;

import com.telelogic.rhapsody.core.IRPModelElement;

public class RhpElProject extends RhpElElement {
	
	public RhpElProject(
			String theElementName, 
			String theElementType,
			String theElementGuid,
			TauMigrator_Context context ) throws Exception{
		
		super(theElementName, theElementType, theElementGuid,context);
		
		dumpInfo();
	}

	private void dumpInfo() {
		String theMsg = "";
		theMsg += "===================================\n"; 
		theMsg += "Create " + this.getString() + "\n";
		theMsg += "===================================\n";		
		_context.info( theMsg );
	}

	public RhpElProject(
			String theElementName, 
			String theElementType,
			String theElementGuid,
			RhpEl theParent,
			String theText,
			String thePosition,
			String theSize,
			TauMigrator_Context context ) throws Exception {
		
		super(theElementName, theElementType, theElementGuid, theParent,context);
		
		dumpInfo();
	}

	@Override
	public IRPModelElement createRhpEl( 
			RhpEl treeRoot ) {
		
		_context.info("createRhpEl invoked for " + getString() );

		IRPModelElement theOwner = _context.get_rhpPrj();
		_rhpEl = theOwner.addNewAggr("Package", "u2Pkg" );			
		
		return _rhpEl;
	}
}
