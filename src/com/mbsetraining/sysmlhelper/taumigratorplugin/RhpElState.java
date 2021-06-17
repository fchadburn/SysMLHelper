package com.mbsetraining.sysmlhelper.taumigratorplugin;

import com.telelogic.rhapsody.core.*;

public class RhpElState extends RhpElGraphNode {

	String _stateType = null;
	String _text = null;

	public RhpElState(
			String theElementName, 
			String theElementType,
			String theElementGuid,
			String theStateType,
			String theText,
			String thePosition,
			String theSize,
			TauMigrator_Context context ) throws Exception{

		super( theElementName, theElementType, theElementGuid, thePosition, theSize, context );

		_stateType = theStateType;
		_text = theText;

		dumpInfo();
	}

	public RhpElState(
			String theElementName, 
			String theElementType,
			String theElementGuid,
			RhpEl theParent,
			String theStateType,
			String theText,
			String thePosition,
			String theSize,
			TauMigrator_Context context ) throws Exception {

		super( theElementName, theElementType, theElementGuid, theParent, thePosition, theSize, context );

		_stateType = theStateType;
		_text = theText;

		dumpInfo();
	}

	private void dumpInfo() {
		String theMsg = "";
		theMsg += "===================================\n"; 
		theMsg += "Constructor called for " + this.getString() + "\n";
		theMsg += "_stateType       	= " + _stateType + "\n";
		theMsg += "_text                = " + _text + "\n";
		theMsg += "_xPosition     = " + _xPosition + "\n";
		theMsg += "_yPosition     = " + _yPosition + "\n";
		theMsg += "_nWidth        = " + _nWidth + "\n";
		theMsg += "_nHeight       = " + _nHeight + "\n";
		theMsg += "===================================\n";		
		_context.info( theMsg );
	}

	@Override
	public IRPModelElement createRhpEl( 
			RhpEl treeRoot ) {

		_rhpEl = null;

		_context.info("createRhpEl invoked for " + getString() + " owned by " + parent.getString());

		_context.info( "The parent is " + _context.elInfo( parent.get_rhpEl() ) );			
		IRPFlowchart theActivityDiagram = (IRPFlowchart) parent.get_rhpEl();
		IRPActivityDiagram theActivityDiagramGE = theActivityDiagram.getFlowchartDiagram();

		IRPState theRootState = theActivityDiagram.getRootState();

		String theLegalName = 
				_context.determineUniqueStateBasedOn(
						_context.makeLegalName( _text ), 
						theActivityDiagram.getRootState() );

		if( !_text.equals( theLegalName ) ){
			_context.info("Changed name from " + _text + " to " + theLegalName);
		}

		// condition connector; see also Fork, History, Join, and Termination
		_rhpEl = theRootState.addState( theLegalName );

		IRPState theState = (IRPState)_rhpEl;

		theState.setStateType( _stateType );
		theState.setEntryAction( _text );

		_graphNode = theActivityDiagramGE.addNewNodeForElement(
				_rhpEl, 
				_xPosition, 
				_yPosition, 
				_nWidth, 
				_nHeight );

		return _rhpEl;
	}
}