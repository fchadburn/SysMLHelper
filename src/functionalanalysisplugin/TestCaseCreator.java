package functionalanalysisplugin;

import java.util.List;

import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class TestCaseCreator {
	
	ExecutableMBSE_Context _context;
	
	public TestCaseCreator(
			ExecutableMBSE_Context context ) {
		
		_context = context;
	}
	
	public void createTestCaseFor( 
			IRPSequenceDiagram theSD ){

		_context.debug("createTestCaseFor invoked for " + _context.elInfo( theSD ) );

		IRPCollaboration theLogicalCollab = theSD.getLogicalCollaboration();

		@SuppressWarnings("unchecked")
		List<IRPMessage> theMessages = theLogicalCollab.getMessages().toList();

		IRPClass theBuildingBlock = 
				FunctionalAnalysisSettings.getBuildingBlock( theSD );

		if( theBuildingBlock != null ){

			IRPClass theTestBlock = 
					FunctionalAnalysisSettings.getTestBlock( theBuildingBlock );
			
			IRPOperation theTC = _context.createTestCaseFor( theTestBlock );

			String theCode = 
					"comment(\"\");\n" +
							"start_of_test();\n";

			List<IRPActor> theActors =
					FunctionalAnalysisSettings.getActors( theBuildingBlock );

			for (IRPMessage theMessage : theMessages) {

				IRPModelElement theSource = theMessage.getSource();
				IRPInterfaceItem theInterfaceItem = theMessage.getFormalInterfaceItem();

				if (theInterfaceItem instanceof IRPEvent) {
					_context.debug( _context.elInfo( theMessage ) + " was found with source = " + _context.elInfo(theSource)
							+ ", and theInterfaceItem=" + _context.elInfo(theInterfaceItem));

					IRPEvent theEvent = (IRPEvent) theInterfaceItem;

					String theEventName = theEvent.getName().replaceFirst("req", "send_");

					for (IRPActor theActor : theActors) {

						IRPModelElement theSend = _context.findElementWithMetaClassAndName("Reception",
								theEventName, theActor);

						if (theSend != null) {
							_context.debug("Voila, found " + _context.elInfo(theSend) + " owned by "
									+ _context.elInfo(theActor));

							IRPLink existingLinkConnectingBlockToActor = ActorMappingInfo
									.getExistingLinkBetweenBaseClassifiersOf(theTestBlock, theActor);

							if (existingLinkConnectingBlockToActor != null) {
								IRPPort theToPort = existingLinkConnectingBlockToActor.getToPort();

								theCode += "OPORT(" + theToPort.getName() + ")->GEN(";
								theCode += theSend.getName() + "(";
								//theCode += theMessage.
								theCode += "));\n";
								theCode += "sleep(4);\n";

							} else {
								_context.warning("No connector found between " + _context.elInfo(theTestBlock) + " and "
										+ _context.elInfo(theActor));
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

									IRPLink existingLinkConnectingBlockToActor = ActorMappingInfo
											.getExistingLinkBetweenBaseClassifiersOf(theTestBlock, theActor);

									if (existingLinkConnectingBlockToActor != null) {
										IRPPort theToPort = existingLinkConnectingBlockToActor.getToPort();

										theCode += "OPORT(" + theToPort.getName() + ")->GEN(";
										theCode += theSendAgain.getName() + "(";
										theCode += "));\n";
										theCode += "sleep(4);\n";

									} else {
										_context.warning("No connector found between " + _context.elInfo(theTestBlock)
												+ " and " + _context.elInfo(theActor));
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
