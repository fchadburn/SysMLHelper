package com.mbsetraining.sysmlhelper.taumigratorplugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.telelogic.rhapsody.core.*;

public class RhpElSendAction extends RhpElGraphNode {

	final Pattern _pattern = Pattern.compile( "(\\w*)\\((.*)\\)" );
	String _text = null;
	String _signalGuid = null;

	public RhpElSendAction(
			String theElementName, 
			String theElementType,
			String theElementGuid,
			String theSignalGuid,
			String theText,
			String thePosition,
			String theSize,
			TauMigrator_Context context ) throws Exception{

		super( theElementName, theElementType, theElementGuid, thePosition, theSize,context );

		_text = theText;
		_signalGuid = theSignalGuid;

		dumpInfo();
	}

	public RhpElSendAction(
			String theElementName, 
			String theElementType,
			String theElementGuid,
			RhpEl theParent,
			String theStateType,
			String theSignalGuid,
			String theText,
			String thePosition,
			String theSize,
			TauMigrator_Context context ) throws Exception {

		super( theElementName, theElementType, theElementGuid, theParent, thePosition, theSize,context );

		_text = theText;
		_signalGuid = theSignalGuid;

		dumpInfo();
	}

	private void dumpInfo() {
		String theMsg = "";
		theMsg += "===================================\n"; 
		theMsg += "Constructor called for " + this.getString() + "\n";
		theMsg += "_text          = " + _text + "\n";
		theMsg += "_xPosition     = " + _xPosition + "\n";
		theMsg += "_yPosition     = " + _yPosition + "\n";
		theMsg += "_nWidth        = " + _nWidth + "\n";
		theMsg += "_nHeight       = " + _nHeight + "\n";
		theMsg += "_signalGuid    = " + _signalGuid + "\n";
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
						_context.makeLegalName( _text.replaceAll("[\\(\\)]", "") ), 
						theRootState );

		if( !_text.equals( theLegalName ) ){
			_context.info("Changed name from " + _text + " to " + theLegalName);
		}

		// condition connector; see also Fork, History, Join, and Termination
		_rhpEl = theRootState.addState( theLegalName );
		_rhpEl.setDisplayName( _text );

		IRPState theState = (IRPState)_rhpEl;
		theState.setStateType("EventState");	
		
		RhpEl theModelEl = 
				treeRoot.findNestedElementWith( _signalGuid );
		
		if( theModelEl != null ){
			_context.info("Successfully found signal for accept event action");
		
			IRPSendAction theSendAction = (IRPSendAction)theState.getSendAction();
			
			theSendAction.setEvent( (IRPEvent) theModelEl.get_rhpEl() );
			
			IRPModelElement theOwner = (IRPClass) theActivityDiagram.getOwner();
			
			_context.info( "The owner is " + theOwner);
			
			IRPModelElement thePort = theOwner.findNestedElement("p", "Port");
			
			if( thePort != null ){
				theSendAction.setTarget( thePort );				
			}
			
			Matcher theMatcher = _pattern.matcher( _text );
			
			if( theMatcher.find() ){
				String theArg = theMatcher.group( 2 );
				_context.info( theArg );
			}
			
			theSendAction.setDisplayName( _text );

			
//			theState.setReferenceToActivity( (IRPEvent) theModelEl.get_rhpEl() );
		} else {
			_context.info("Odd. I could not find signal with guid " + 
					_signalGuid + " for accept event action" );
		}
		
		_graphNode = theActivityDiagramGE.addNewNodeForElement(
				_rhpEl, 
				_xPosition, 
				_yPosition, 
				_nWidth, 
				40 ); // default Send Action height

		return _rhpEl;
	}
}