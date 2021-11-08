package designsynthesisplugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JScrollPane;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSEBasePanel;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class AutoConnectFlowPortsPanel extends ExecutableMBSEBasePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1814353152453871188L;
	
	protected AutoConnectFlowPortsMap _radioButtonMap = new AutoConnectFlowPortsMap();
	protected IRPSysMLPort _publishingPort;
	protected IRPAttribute _publishingAttribute;
	protected IRPInstance _publishingPart;

	public static void main(String[] args) {
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		ExecutableMBSE_Context theContext = new ExecutableMBSE_Context( theRhpApp.getApplicationConnectionString() );
		IRPModelElement theSelectedEl = theContext.getSelectedElement( false );
		
		if( theSelectedEl instanceof IRPAttribute ){
			launchThePanel( (IRPAttribute) theSelectedEl, theContext );
		}
	}
	
	public static void launchThePanel(
			IRPAttribute theAttribute,
			ExecutableMBSE_Context context ){

		javax.swing.SwingUtilities.invokeLater( new Runnable() {

			@Override
			public void run() {

				JFrame.setDefaultLookAndFeelDecorated( true );

				UserInterfaceHelper.setLookAndFeel();

				JFrame frame = new JFrame(
						"Auto-connect to " + context.elInfo( theAttribute ) );

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				// Cannot pass any Rhapsody elements or context in this
				AutoConnectFlowPortsPanel thePanel = 
						new AutoConnectFlowPortsPanel( 
								context.get_rhpAppID(),
								theAttribute.getGUID() );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}

	@SuppressWarnings("unchecked")
	public AutoConnectFlowPortsPanel(
			String theAppID,
			String thePublishingAttributeGUID ){

		super( theAppID );

		_publishingAttribute = 
				(IRPAttribute) _context.get_rhpPrj().findElementByGUID( thePublishingAttributeGUID );
		
		_context.get_selectedContext().setContextTo( _publishingAttribute );
		
		IRPClass theBuildingBlock = 
				_context.get_selectedContext().getBuildingBlock();

		if( theBuildingBlock == null ){

			buildUnableToRunDialog( 
					"Sorry, this helper is unable to run this command because \n" +
							"there was no execution context or block found in the model. \n " +
					"You need to add the relevant package structure first." );

		} else {

			_publishingPort = _context.getExistingFlowPort( _publishingAttribute );

			_publishingPart = getPartMatchingAttributesOwnerUnder( 
					theBuildingBlock, _publishingAttribute.getOwner() );
			
			if( _publishingPort != null && _context.hasStereotypeCalled( "publish", _publishingAttribute ) ){


			}

			List<IRPInstance> theCandidateParts =
					theBuildingBlock.getNestedElementsByMetaClass( "Part", 0 ).toList();

			// Add radio buttons for all the parts that are not test drivers
			for( IRPInstance theCandidatePart : theCandidateParts ){	

				_context.debug( "theCandidatePart is " + _context.elInfo( theCandidatePart ) );
				
				IRPClassifier theOtherClass = theCandidatePart.getOtherClass();

				if( theOtherClass instanceof IRPClass &&
						!_context.hasStereotypeCalled( "TestDriver", theOtherClass ) &&
						!theCandidatePart.equals( _publishingPart ) ){

					AutoConnectFlowPortsInfo theFlowPortInfo = 
							new AutoConnectFlowPortsInfo( 
									_publishingAttribute, 
									_publishingPart, 
									theCandidatePart,
									_context );

					_radioButtonMap.put( 
							theCandidatePart, 
							theFlowPortInfo );
				}
			}

			setLayout( new BorderLayout( 10, 10 ) );
			setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

			Box theBox = Box.createVerticalBox();

			if( _radioButtonMap.isEmpty() ){

				JLabel theLabel = new JLabel( "There are no other parts" );
				theLabel.setAlignmentX( Component.LEFT_ALIGNMENT );
				theBox.add( theLabel );

			} else {

				JPanel theRadioButtonTable = createMakeChoicesPanel( _radioButtonMap );
				theRadioButtonTable.setAlignmentX( Component.LEFT_ALIGNMENT );

				JScrollPane theScrollPane = new JScrollPane( theRadioButtonTable );

				if( _radioButtonMap.size() > 10 ){
					theScrollPane.setPreferredSize( new Dimension( 450, 311 ) );				
				}

				String theIntroMsg = 
						"The part called " + _publishingPart.getName() + ":" + _publishingPart.getOtherClass().getName() +
						" has a «publish» attribute called '" + _publishingAttribute.getName() + "'";

				theBox.add( new JLabel( theIntroMsg ) );
				theBox.add( new JLabel( "   " ) );
				theBox.add( new JLabel( "Do you want to auto-connect to attribute(s) in the following and set them to «subscribe»:\n") );
				theBox.add( new JLabel( "   " ) );

				theBox.add( theScrollPane );

				add( theBox, BorderLayout.CENTER );
				add( createOKCancelPanel(), BorderLayout.PAGE_END );
			}
		}
	}

	private IRPInstance getPartMatchingAttributesOwnerUnder(
			IRPClass theBuildingBlock,
			IRPModelElement typedByClassifier ){

		@SuppressWarnings("unchecked")
		List<IRPInstance> theParts = 
		theBuildingBlock.getNestedElementsByMetaClass( "Part", 0 ).toList();

		List<IRPInstance> theMatchingParts = new ArrayList<IRPInstance>();

		for( IRPInstance thePart : theParts ){	

			IRPClassifier theOtherClass = thePart.getOtherClass();

			if( theOtherClass instanceof IRPClass &&
					theOtherClass.equals( typedByClassifier ) ){

				theMatchingParts.add( thePart );
			}
		}

		IRPInstance theChosenPart = null;

		if( theMatchingParts.size() == 1 ){
			theChosenPart = theMatchingParts.get( 0 );
		}

		return theChosenPart;
	}

	private JPanel createMakeChoicesPanel(
			Map<IRPInstance, AutoConnectFlowPortsInfo> theButtonMap ){

		JPanel thePanel = new JPanel();

		GroupLayout theGroupLayout = new GroupLayout( thePanel );
		thePanel.setLayout( theGroupLayout );
		theGroupLayout.setAutoCreateGaps( true );

		SequentialGroup theHorizSequenceGroup = theGroupLayout.createSequentialGroup();
		SequentialGroup theVerticalSequenceGroup = theGroupLayout.createSequentialGroup();

		ParallelGroup theColumn1ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );
		ParallelGroup theColumn2ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );
		ParallelGroup theColumn3ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );

		theHorizSequenceGroup.addGroup( theColumn1ParallelGroup );
		theHorizSequenceGroup.addGroup( theColumn2ParallelGroup );
		theHorizSequenceGroup.addGroup( theColumn3ParallelGroup );

		for (Entry<IRPInstance, AutoConnectFlowPortsInfo> entry : theButtonMap.entrySet()){

			AutoConnectFlowPortsInfo theValue = entry.getValue();

			JLabel theName = new JLabel( theValue.getIDString() );//entry.getKey().getName() );
			theName.setMinimumSize( new Dimension( 150, 22 ) );
			theName.setBorder( BorderFactory.createEmptyBorder( 0, 10, 0, 10 ) );

			theColumn1ParallelGroup.addComponent( theName );  
			theColumn2ParallelGroup.addComponent( theValue.getM_BindingChoiceComboBox() );    
			theColumn3ParallelGroup.addComponent( theValue.getM_ChosenNameTextField());        

			ParallelGroup theVertical1ParallelGroup = 
					theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE );

			theVertical1ParallelGroup.addComponent( theName );
			theVertical1ParallelGroup.addComponent( theValue.getM_BindingChoiceComboBox() );
			theVertical1ParallelGroup.addComponent( theValue.getM_ChosenNameTextField() );

			theVerticalSequenceGroup.addGroup( theVertical1ParallelGroup );		  
		}

		theGroupLayout.setHorizontalGroup( theHorizSequenceGroup );
		theGroupLayout.setVerticalGroup( theVerticalSequenceGroup );

		return thePanel;
	}

	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled ){

		boolean isValid = true;
		String errorMsg = "";

		for( Entry<IRPInstance, AutoConnectFlowPortsInfo> entry : _radioButtonMap.entrySet() ){

			AutoConnectFlowPortsInfo theValue = entry.getValue();

			if( theValue.isCreateNewSelected() ){

				String theChosenAttributeName = theValue.getM_ChosenNameTextField().getText();

				_context.debug( "Create new was selected for " + _context.elInfo( entry.getKey( ) ) +
						" with value " + theChosenAttributeName ); 

				boolean isLegalName = _context.isLegalName( theChosenAttributeName, theValue.getM_SubscribingBlock() );

				if( !isLegalName ){
					errorMsg += theChosenAttributeName + " is not a legal name for an executable attribute\n";
					isValid = false;

				} else if (!_context.isElementNameUnique(
						theChosenAttributeName, "Attribute", theValue.getM_SubscribingBlock(), 1) ){

					errorMsg += theChosenAttributeName + " is not unique in " + 
							_context.elInfo( theValue.getM_SubscribingBlock() ) + ", please choose again\n";

					isValid = false;
				}
			}
		}

		if( isMessageEnabled && !isValid && errorMsg != null ){

			UserInterfaceHelper.showWarningDialog( errorMsg );
		}

		return isValid;
	}

	@Override
	protected void performAction() {

		try {
			if( checkValidity( false ) ){

				for( Entry<IRPInstance, AutoConnectFlowPortsInfo> entry : _radioButtonMap.entrySet() ){

					AutoConnectFlowPortsInfo theTgtInfo = entry.getValue();
					theTgtInfo.performSelectedOperations();
				}

				//ConfirmDiagramUpdatePanel.launchThePanel( _radioButtonMap );

			} else {
				_context.error( "AutoConnectFlowPortsPanel.performAction, checkValidity returned false" );
			}	

		} catch( Exception e ){
			_context.error( "AutoConnectFlowPortsPanel.performAction, unhandled exception was detected, e=" + e.getMessage() );
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