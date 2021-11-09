package com.mbsetraining.sysmlhelper.createtestcase;

import java.util.List;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class TestCaseCreator {
	
	ExecutableMBSE_Context _context;
	
	public static void main(String[] args) {
		
		IRPApplication theRhpEl = RhapsodyAppServer.getActiveRhapsodyApplication();
		
		ExecutableMBSE_Context _context = 
				new ExecutableMBSE_Context( theRhpEl.getApplicationConnectionString() );

		IRPModelElement theSelectedEl = theRhpEl.getSelectedElement();
		
		TestCaseCreator theCreator = new TestCaseCreator( _context );
		
		if (theSelectedEl instanceof IRPClass){

			theCreator.createTestCaseFor( (IRPClass) theSelectedEl );

		} else if (theSelectedEl instanceof IRPSequenceDiagram){

			theCreator.createTestCaseFor( (IRPSequenceDiagram) theSelectedEl );
		}
	}
	
	public TestCaseCreator(
			ExecutableMBSE_Context context ) {
		
		_context = context;
	}
	
	public void createTestCaseFor( 
			IRPSequenceDiagram theSD ){

		_context.debug( "createTestCaseFor invoked for " + _context.elInfo( theSD ) );

		IRPCollaboration theLogicalCollab = theSD.getLogicalCollaboration();

		@SuppressWarnings("unchecked")
		List<IRPMessage> theMessages = theLogicalCollab.getMessages().toList();

		_context.get_selectedContext().setContextTo( theSD );
		
		IRPClass theBuildingBlock = 
				_context.get_selectedContext().getBuildingBlock();

		if( theBuildingBlock != null ){

			IRPClass theTestBlock = 
					_context.get_selectedContext().getTestBlock( theBuildingBlock );
			
			IRPOperation theTC = createTestCaseFor( theTestBlock );

			String theCode = 
					"comment(\"\");\n" +
							"start_of_test();\n";

			List<IRPActor> theActors =
					_context.get_selectedContext().getActors( theBuildingBlock );

			for (IRPMessage theMessage : theMessages) {

				IRPModelElement theSource = theMessage.getSource();
				IRPInterfaceItem theInterfaceItem = theMessage.getFormalInterfaceItem();

				if (theInterfaceItem instanceof IRPEvent) {
					
					_context.debug( _context.elInfo( theMessage ) + " was found with source = " + 
							_context.elInfo( theSource ) + ", and theInterfaceItem = " + 
							_context.elInfo( theInterfaceItem ) );

					IRPEvent theEvent = (IRPEvent) theInterfaceItem;

					String theEventName = theEvent.getName().replaceFirst( "req", "send_" );

					for (IRPActor theActor : theActors) {

						IRPModelElement theSend = _context.findElementWithMetaClassAndName( 
								"Reception", theEventName, theActor );

						if( theSend != null ){
							
							_context.debug( "Voila, found " + _context.elInfo(theSend) + " owned by "
									+ _context.elInfo( theActor ) );

							IRPLink existingLinkConnectingBlockToActor = _context
									.getExistingLinkBetweenBaseClassifiersOf( 
											theTestBlock, theActor, theBuildingBlock );

							if( existingLinkConnectingBlockToActor != null ){
								
								IRPPort theToPort = existingLinkConnectingBlockToActor.getToPort();

								theCode += "OPORT(" + theToPort.getName() + ")->GEN(";
								theCode += theSend.getName() + "(";
								
								try {
									IRPCollection theList = theMessage.getActualParameterList();

									if( theList.getCount() == 1 ){
										
										Object theParam = theList.getItem( 1 );
										String theValue = theParam.toString().replace( "value = ", "" );
																			
										_context.debug( "Parameter for value is " + theValue );
										
										if( theValue.matches( "\\d+" ) ){										
											theCode += theValue;
										} else {
											_context.warning( "Unable to parse argument parameter for " + _context.elInfo( theSend ) );
										}
									}
								} catch( Exception e ){
									_context.error( "Exception trying to get argument parameter for " + _context.elInfo( theSend ) );
								}
								
								theCode += "));\n";
								theCode += "sleep(4);\n";

							} else {
								_context.warning( "No connector found between " + _context.elInfo( theTestBlock ) + " and "
										+ _context.elInfo( theActor ) );
							}

						} else {
							@SuppressWarnings("unchecked")
							List<IRPClassifier> theBaseClassifiers = theActor.getBaseClassifiers().toList();

							for (IRPClassifier theBaseClassifier : theBaseClassifiers) {

								IRPModelElement theSendAgain = _context.findElementWithMetaClassAndName(
										"Reception", theEventName, theBaseClassifier);

								if (theSendAgain != null) {
									_context.debug("Voila, found " + _context.elInfo(theSendAgain)
											+ " owned by " + _context.elInfo(theBaseClassifier));

									IRPLink existingLinkConnectingBlockToActor = _context
											.getExistingLinkBetweenBaseClassifiersOf( 
													theTestBlock, theActor, theBuildingBlock );

									if (existingLinkConnectingBlockToActor != null) {
										IRPPort theToPort = existingLinkConnectingBlockToActor.getToPort();

										theCode += "OPORT(" + theToPort.getName() + ")->GEN(";
										theCode += theSendAgain.getName() + "(";
										theCode += "));\n";
										theCode += "sleep(4);\n";

									} else {
										_context.warning( "No connector found between " + _context.elInfo(theTestBlock)
												+ " and " + _context.elInfo( theActor ) );
									}
								}
							}
						}
					}
				}
			}

			theCode += "end_of_test();\n";

			theTC.setBody(theCode);
			theTC.highLightElement();
		}
	}
	
	public IRPOperation createTestCaseFor( 
			IRPClass theTestDriver ){

		IRPOperation theOp = null;

		if( _context.hasStereotypeCalled( "TestDriver", theTestDriver ) ){

			_context.debug( "createTestCaseFor was invoked for " + _context.elInfo( theTestDriver ) );

			String[] theSplitName = theTestDriver.getName().split("_");

			String thePrefix = theSplitName[0] + "_Test_";

			//_context.debug( "The prefix for TestCase was calculated as '" + thePrefix + "'" );

			int count = 0;
			boolean isUniqueNumber = false;
			String nameToTry = null;

			while (isUniqueNumber==false){
				count++;
				nameToTry = thePrefix + String.format("%03d", count);

				if (theTestDriver.findNestedElement(nameToTry, "Operation") == null){
					isUniqueNumber = true;
				}
			}

			if (isUniqueNumber){
				theOp = theTestDriver.addOperation(nameToTry);
				theOp.highLightElement();
				theOp.changeTo("Test Case");

				IRPState theState = _context.getStateCalled(
						"Ready", 
						theTestDriver.getStatechart(), 
						theTestDriver );

				String theEventName = "ev" + nameToTry;

				IRPEventReception theEventReception = theTestDriver.addReception( theEventName );

				if (theEventReception != null){
					IRPEvent theEvent = theEventReception.getEvent();

					_context.debug( "The state called " + theState.getFullPathName() + 
							" is owned by " + theState.getOwner().getFullPathName() );
					
					IRPTransition theTransition = theState.addInternalTransition( theEvent );
					theTransition.setItsAction( theOp.getName() + "();");
				}
			}		

		} else {
			UserInterfaceHelper.showWarningDialog(
					"This operation only works if you right-click a «TestDriver» block");	    
		}

		return theOp;
	}
}

/**
 * Copyright (C) 2017-2021  MBSE Training and Consulting Limited (www.executablembse.com)

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
