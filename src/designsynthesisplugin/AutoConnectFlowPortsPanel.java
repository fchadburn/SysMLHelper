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

import com.mbsetraining.sysmlhelper.common.ConfigurationSettings;
import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSEBasePanel;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class AutoConnectFlowPortsPanel extends ExecutableMBSEBasePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private AutoConnectFlowPortsMap m_RadioButtonMap = 
			new AutoConnectFlowPortsMap();

	private IRPAttribute m_PublishingAttribute = null;
	private IRPInstance m_PublishingPart = null;
	
	public static void launchThePanel(
			final IRPAttribute theAttribute,
			final ExecutableMBSE_Context context ){
		
			IRPClass theBuildingBlock = 
					_context.getBuildingBlock( theAttribute );
			
			if( theBuildingBlock != null ){
				
				final IRPSysMLPort thePort = _context.getExistingFlowPort( theAttribute );
				
				if( _context.hasStereotypeCalled( "publish", theAttribute ) &&
					thePort != null ){
					
					@SuppressWarnings("unchecked")
					List<IRPInstance> theParts =
					    theBuildingBlock.getNestedElementsByMetaClass( 
					    		"Part", 0 ).toList();
					
					List<IRPInstance> theMatchingParts = new ArrayList<IRPInstance>();
					
					for( IRPInstance thePart : theParts ){	

						IRPClassifier theOtherClass = thePart.getOtherClass();

						if( theOtherClass instanceof IRPClass &&
							theOtherClass.equals( theAttribute.getOwner() ) ){
							theMatchingParts.add( thePart );
						}
					}
					
					IRPInstance theChosenPart = null;
					
					if( theMatchingParts.size() == 1 ){
						theChosenPart = theMatchingParts.get( 0 );
					}
					
					if( theChosenPart != null ){
						
						final IRPInstance thePart = theChosenPart;
						
						javax.swing.SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								
								JFrame.setDefaultLookAndFeelDecorated( true );

								JFrame frame = new JFrame("Auto-connect to " + Logger.elInfo( theAttribute ));
								
								frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

								AutoConnectFlowPortsPanel thePanel = 
										new AutoConnectFlowPortsPanel( 
												theAppID,
												theAttribute, 
												thePort,
												thePart );

								frame.setContentPane( thePanel );
								frame.pack();
								frame.setLocationRelativeTo( null );
								frame.setVisible( true );
							}
						});
					
					}
				}
				
			}
	//	}
	}
	
	@SuppressWarnings("unchecked")
	public AutoConnectFlowPortsPanel(
			String theAppID,
			String thePublishingAttributeGUID,
			String thePublishingFlowPortGUID,
			IRPInstance thePublishingPartGUID ){
		
		super( theAppID );
		
		IRPAttribute thePublishingAttribute = (IRPAttribute) _context.get_rhpPrj().findElementByGUID(thePublishingAttributeGUID);
		IRPSysMLPort thePublishingFlowPort = (IRPSysMLPort) _context.get_rhpPrj().findElementByGUID(thePublishingFlowPortGUID);
		IRPInstance thePublishingPart = (IRPInstance) _context.get_rhpPrj().findElementByGUID(thePublishingFlowPortGUID);
		
		m_PublishingAttribute = thePublishingAttribute;
		m_PublishingPart = thePublishingPart;
		
		IRPClassifier theAssemblyBlock = 
				(IRPClassifier) thePublishingPart.getOwner();
		
		List<IRPInstance> theParts =
		    theAssemblyBlock.getNestedElementsByMetaClass( "Part", 0 ).toList();
		
		for( IRPInstance thePart : theParts ){	

			IRPClassifier theOtherClass = thePart.getOtherClass();

			if( theOtherClass instanceof IRPClass &&
				!_context.hasStereotypeCalled( "TestDriver", thePart ) &&
				!thePart.equals( thePublishingPart ) ){

				AutoConnectFlowPortsInfo theFlowPortInfo = 
						new AutoConnectFlowPortsInfo( 
								thePublishingAttribute, 
								thePublishingPart, 
								thePart,
								_context );
				
				m_RadioButtonMap.put( 
						thePart, 
						theFlowPortInfo );
			}
		}
		
		setLayout( new BorderLayout( 10, 10 ) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		
		Box theBox = Box.createVerticalBox();

		if( m_RadioButtonMap.isEmpty() ){
			
			JLabel theLabel = new JLabel("There are no other parts");
			theLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			theBox.add( theLabel );
			
		} else {
			
			JPanel theRadioButtonTable = createMakeChoicesPanel( m_RadioButtonMap );
			theRadioButtonTable.setAlignmentX( Component.LEFT_ALIGNMENT );
			
			JScrollPane theScrollPane = new JScrollPane( theRadioButtonTable );
			
			if( m_RadioButtonMap.size() > 10 ){
				theScrollPane.setPreferredSize( new Dimension( 450, 311 ) );				
			}
			
			String theIntroMsg = 
					"The part called " + m_PublishingPart.getName() + ":" + m_PublishingPart.getOtherClass().getName() +
					" has a �publish� attribute called '" + m_PublishingAttribute.getName() + "'";
			
			theBox.add( new JLabel( theIntroMsg ) );
			theBox.add( new JLabel( "   " ) );
			theBox.add( new JLabel( "Do you want to auto-connect to attribute(s) in the following and set them to �subscribe�:\n") );
			theBox.add( new JLabel( "   " ) );

			theBox.add( theScrollPane );
		}

		add( theBox, BorderLayout.CENTER );
		add( createOKCancelPanel(), BorderLayout.PAGE_END );
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
				
		for( Entry<IRPInstance, AutoConnectFlowPortsInfo> entry : m_RadioButtonMap.entrySet() ){
		    
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
														
				for( Entry<IRPInstance, AutoConnectFlowPortsInfo> entry : m_RadioButtonMap.entrySet() ){
					
					AutoConnectFlowPortsInfo theTgtInfo = entry.getValue();
					theTgtInfo.performSelectedOperations();
				}
				
				ConfirmDiagramUpdatePanel.launchThePanel( m_RadioButtonMap );
				
			} else {
				_context.error("Error in CreateNewActorPanel.performAction, checkValidity returned false");
			}	
			
		} catch( Exception e ){
			_context.error("Error in CopyActivityDiagramsPanel.performAction, unhandled exception was detected");
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