package com.mbsetraining.sysmlhelper.autorealizewithcopy;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.border.EmptyBorder;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSEBasePanel;
import com.mbsetraining.sysmlhelper.executablembse.SelectedElementContext;
import com.mbsetraining.sysmlhelper.sequencediagram.HighlightModelElementButton;
import com.mbsetraining.sysmlhelper.sequencediagram.InterfaceInfoList;
import com.mbsetraining.sysmlhelper.sequencediagram.MessageInfo;
import com.mbsetraining.sysmlhelper.sequencediagram.MessageInfoList;
import com.telelogic.rhapsody.core.*;

public class AutoRealizeWithCopyPanel extends ExecutableMBSEBasePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private IRPSequenceDiagram _diagram;
	private MessageInfoList _messageInfoList;
	private InterfaceInfoList _interfaceInfoList;
	private List<IRPClassifier> _classifiersNotInScope;

	public static void main(String[] args) {
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		
		IRPModelElement theSelectedEl = theRhpApp.getSelectedElement();
					
		launchThePanel( theRhpApp.getApplicationConnectionString(), theSelectedEl.getGUID() );
	}
	
	public static void launchThePanel(
			String theAppID,
			String theSelectedGUID ){

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame( "Update interfaces based on sequence diagram" );

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				AutoRealizeWithCopyPanel thePanel = 
						new AutoRealizeWithCopyPanel(
								theAppID,
								theSelectedGUID );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}

	public AutoRealizeWithCopyPanel( 
			String theAppID,
			String theSelectedGUID ){

		super( theAppID );

		IRPModelElement theSelectedEl = _context.get_rhpPrj().findElementByGUID( theSelectedGUID );
		
		_context.debug( "AutoRealizeWithCopyPanel was invoked for " + _context.elInfo( theSelectedEl ) );
		
		_context.get_selectedContext().setContextTo( theSelectedEl );
		
		IRPGraphElement theSelectedGraphEl = _context.get_selectedContext().getSelectedGraphEl();

		_interfaceInfoList = new InterfaceInfoList( _context );
		
		if( theSelectedEl instanceof IRPSequenceDiagram ){
			
			_diagram = (IRPSequenceDiagram) theSelectedEl;
			_messageInfoList = new MessageInfoList( _diagram, _interfaceInfoList, _context );
			_classifiersNotInScope = getClassifiersNotInScope( _diagram );

		} else if( theSelectedEl instanceof IRPMessage ) {
			
			IRPMessage theMessage = (IRPMessage) theSelectedEl;
			_diagram = (IRPSequenceDiagram) theSelectedGraphEl.getDiagram();
			_messageInfoList = new MessageInfoList( theMessage, _diagram, _interfaceInfoList, _context );
			_classifiersNotInScope = getClassifiersNotInScope( _diagram );

		} else {
			_context.error( "UpdateInferfacesBasedOnSequenceDiagramPanel is not supported for " + _context.elInfo( theSelectedEl ) );
		}
		
		setLayout( new BorderLayout() );
		setBorder( new EmptyBorder(0, 10, 10, 10) );

		String introText = 
				"This helper will realize events and operations on " + 
						_context.elInfo( _diagram ) + " weith copy.";

		JPanel theStartPanel = new JPanel();

		theStartPanel.setLayout( new BoxLayout( theStartPanel, BoxLayout.PAGE_AXIS ) );
		//theStartPanel.add( new JLabel( " " ) );
		
		theStartPanel.add( createPanelWithTextCentered( introText ) );

		add( theStartPanel, BorderLayout.PAGE_START );
		
		if( !_messageInfoList.isEmpty() ){
			
			JPanel theMainPanel = createContent();
			theMainPanel.setAlignmentX( Component.LEFT_ALIGNMENT );
			add( theMainPanel, BorderLayout.CENTER );

		} else {
			add( createPanelWithTextCentered( 
					_messageInfoList.get_upToDateCount() + 
					" messages were checked. No action necessary." ), 
					BorderLayout.CENTER );
		}
		
		add( createOKCancelPanel(), BorderLayout.PAGE_END );		
	}

	private JPanel createContent(){

		JPanel thePanel = new JPanel();

		GroupLayout theGroupLayout = new GroupLayout( thePanel );
		thePanel.setLayout( theGroupLayout );
		theGroupLayout.setAutoCreateGaps( true );

		SequentialGroup theHorizSequenceGroup = theGroupLayout.createSequentialGroup();
		SequentialGroup theVerticalSequenceGroup = theGroupLayout.createSequentialGroup();

		ParallelGroup theColumn1ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );
		ParallelGroup theColumn2ParallelGroup = theGroupLayout.createParallelGroup( GroupLayout.Alignment.LEADING );

		theHorizSequenceGroup.addGroup( theColumn1ParallelGroup );
		theHorizSequenceGroup.addGroup( theColumn2ParallelGroup );

		for( MessageInfo messageInfo : _messageInfoList ){
			
			JButton comp1 = new HighlightModelElementButton( messageInfo.getName(), messageInfo.get_message() );
			
			JTextField comp2 = new JTextField( messageInfo.getActionDescription() );
			comp2.setEditable( false );

			theColumn1ParallelGroup.addComponent( comp1 ); 
			theColumn2ParallelGroup.addComponent( comp2 );   

			ParallelGroup theVertical1ParallelGroup = 
					theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE );

			ParallelGroup theVertical2ParallelGroup = 
					theGroupLayout.createParallelGroup( GroupLayout.Alignment.BASELINE );

			theVertical2ParallelGroup.addComponent( comp1 );
			theVertical2ParallelGroup.addComponent( comp2 );

			theVerticalSequenceGroup.addGroup( theVertical1ParallelGroup );		
			theVerticalSequenceGroup.addGroup( theVertical2ParallelGroup );		
		}
		
		theGroupLayout.setHorizontalGroup( theHorizSequenceGroup );
		theGroupLayout.setVerticalGroup( theVerticalSequenceGroup );

		return thePanel;
	}
	
	private List<IRPClassifier> getClassifiersNotInScope(
			IRPSequenceDiagram theSD ){
		
		List<IRPClassifier> theClassifiersNotInScope = new ArrayList<IRPClassifier>();
		
		@SuppressWarnings("unchecked")
		List<IRPModelElement> theElements = theSD.getElementsInDiagram().toList();
		
		IRPPackage thePackageForBlocks = _context.get_selectedContext().getPackageForBlocks();
		
		if( thePackageForBlocks == null ){
			_context.error( "Unable to determine thePackageForBlocks for selected context" );
		} else {
			_context.debug( "thePackageForBlocks is " + _context.elInfo( thePackageForBlocks ) );
			
			for (IRPModelElement theElement : theElements) {
				
				if( theElement instanceof IRPClassifierRole ){
					
					IRPClassifierRole theClassifierRole = (IRPClassifierRole)theElement;
					IRPClassifier theClassifier = theClassifierRole.getFormalClassifier();
					
					if( theClassifier instanceof IRPClass ){					
						
						SelectedElementContext theClassContext = new SelectedElementContext( _context );
						theClassContext.setContextTo( theClassifier );
						IRPPackage thePackageForBlocksForClass = theClassContext.getPackageForBlocks();

						if( thePackageForBlocksForClass instanceof IRPPackage ){
							
							if( thePackageForBlocksForClass.equals( thePackageForBlocks ) ){
							
								_context.debug( _context.elInfo( theElement ) + " is in the same package" );
							} else {
								_context.debug( _context.elInfo( theElement ) + " is in a different package" );
								theClassifiersNotInScope.add( theClassifier );							
							}
						}
					}
				}
			}
		}
		
		return theClassifiersNotInScope;
		
	}
			

	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled ){
		
		boolean isValid = true;
		
		String theMsg = "";
		
		boolean isDiagramOwner = _diagram.getOwner() != null;
		
		if( !isDiagramOwner ){
			theMsg += _context.elInfo( _diagram ) + " does not appear to be saved. " + 
					"Please save the diagram and try again. ";
			isValid = false;
		}
		
		if( isMessageEnabled && !isValid ){
			UserInterfaceHelper.showWarningDialog( theMsg );
		}
		
		return isValid;
	}

	@Override
	protected void performAction() {

		if( checkValidity( false ) ){
			
			for( MessageInfo messageInfo : _messageInfoList ){
				
				if( messageInfo.get_isRealizationNeeded() ){
					
					_context.debug( "Realize " + messageInfo.getName() );					
				}
			}
		}
	}
}

/**
 * Copyright (C) 2021  MBSE Training and Consulting Limited (www.executablembse.com)

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