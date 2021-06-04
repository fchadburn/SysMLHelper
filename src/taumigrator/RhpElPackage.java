package taumigrator;

import com.telelogic.rhapsody.core.IRPModelElement;

public class RhpElPackage extends RhpElElement {

	
	public RhpElPackage(
			String theElementName, 
			String theElementType,
			String theElementGuid,
			TauMigrator_Context context ) throws Exception{
		
		super(theElementName, theElementType, theElementGuid, context);
		
		dumpInfo();
	}

	private void dumpInfo() {
		String theMsg = "";
		theMsg += "===================================\n"; 
		theMsg += "Create " + this.getString() + "\n";
		theMsg += "===================================\n";		
		_context.info( theMsg );
	}

	public RhpElPackage(
			String theElementName, 
			String theElementType,
			String theElementGuid,
			RhpEl theParent,
			TauMigrator_Context context ) throws Exception {
		
		super(theElementName, theElementType, theElementGuid, theParent, context);
		
		dumpInfo();
	}
	
	@Override
	public IRPModelElement createRhpEl(
			RhpEl treeRoot ) throws Exception {

		_context.info("createRhpEl invoked for " + getString() + " owned by " + parent.getString());

		String theLegalName = _context.makeLegalName( _elementName );
		
		if( _elementName != theLegalName ){
			_context.info("Changed name from " + _elementName + " to " + theLegalName);
		}
		
		IRPModelElement theOwner = parent.get_rhpEl();
		
		if( theOwner == null ){
			throw new Exception("Parent element was null");
		}
		
		_rhpEl = theOwner.addNewAggr("Package", theLegalName );			
		
		return _rhpEl;
	}
}
