package com.mbsetraining.sysmlhelper.smartlink;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSEBasePanel;
import com.telelogic.rhapsody.core.*;

public class EndlinkPanel extends ExecutableMBSEBasePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6999678549678497401L;
	private SmartLinkInfo _smartLinkInfo;
	private JCheckBox _populateOnDiagramCheckBox; 

	private List<IRPModelElement> _startLinkEls;
	private List<IRPGraphElement> _startLinkGraphEls;
	private List<IRPModelElement> _endLinkEls;
	private List<IRPGraphElement> _endLinkGraphEls;
	
	public static void launchThePanel(
			final String theAppID,
			final List<String> theStartLinkGUIDs ){

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame( "End link" );

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				EndlinkPanel thePanel = 
						new EndlinkPanel( 
								theAppID,
								theStartLinkGUIDs );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}
	
	public EndlinkPanel(
			String theAppID,
			List<String> theStartLinkGUIDs ){

		super( theAppID );

		boolean isCyclical = false;

		_startLinkEls = new ArrayList<IRPModelElement>();
		_startLinkGraphEls = new ArrayList<IRPGraphElement>();

		for( String theGUID : theStartLinkGUIDs ){	
			
			IRPModelElement theStartLinkEl = _context.get_rhpPrj().findElementByGUID( theGUID );
			
			if( theStartLinkEl == null ){
				_context.error( "Unable to find start link element with GUID " + theGUID );
			} else {
				_startLinkEls.add( theStartLinkEl );
			}
		}
		
		_endLinkEls = _context.getSelectedElements();
		_endLinkGraphEls = _context.getSelectedGraphElements();
		
		for( IRPGraphElement theEndGraphEl : _endLinkGraphEls ){
			
			IRPDiagram theDiagram = theEndGraphEl.getDiagram();
			
			for( IRPModelElement startLinkEl : _startLinkEls ){
				
				@SuppressWarnings("unchecked")
				List<IRPGraphElement> theGraphEls = 
						theDiagram.getCorrespondingGraphicElements( startLinkEl ).toList();
			
				for (IRPGraphElement theGraphEl : theGraphEls) {
					
					if( !_startLinkGraphEls.contains( theGraphEl ) ){
						_startLinkGraphEls.add( theGraphEl );
					}
				}
			}
		}
		
		for( IRPModelElement theStartLinkEl : _startLinkEls ){

			if( _endLinkEls.contains( theStartLinkEl ) ){
				isCyclical = true;
				break;
			}
		}

		_populateOnDiagramCheckBox = new JCheckBox( "Populate on diagram?" );
		_populateOnDiagramCheckBox.setSelected( false );
		_populateOnDiagramCheckBox.setVisible( false );

		if( isCyclical ){

			add( new JLabel( "Unable to proceed as you've selected cyclical start and end elements" ), BorderLayout.PAGE_START );

		} else {
			_smartLinkInfo = new SmartLinkInfo(
					_startLinkEls, _startLinkGraphEls, _endLinkEls, _endLinkGraphEls, _context );

			setLayout( new BorderLayout(10,10) );
			setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

			add( new JLabel( _smartLinkInfo.getDescriptionHTML() ), BorderLayout.PAGE_START );

			if( _smartLinkInfo.getIsPopulatePossible() ){

				_populateOnDiagramCheckBox = new JCheckBox( "Populate on diagram?" );
				_populateOnDiagramCheckBox.setSelected( true );
				_populateOnDiagramCheckBox.setVisible( true );

				add( _populateOnDiagramCheckBox, BorderLayout.CENTER );
			}
		}

		add( createOKCancelPanel(), BorderLayout.PAGE_END );
	}

	public void selectStartLinkEls(
			final List<IRPModelElement> theStartLinkEls,
			final List<IRPGraphElement> theStartLinkGraphEls ){

		if( !theStartLinkEls.isEmpty() ){

			_context.info( "The following " + theStartLinkEls.size() + 
					" elements were selected in Start Link command:" );

			for( IRPModelElement theStartLinkEl : theStartLinkEls ){
				_context.info( _context.elInfo( theStartLinkEl ) ); 
			}

			_startLinkEls = theStartLinkEls;
			_startLinkGraphEls = theStartLinkGraphEls;

		} else {
			_context.error( "Error in SmartLinkPanel.launchTheStartLinkPanel, " +
					"no elements were selected" );
		}
	}

	@Override
	protected boolean checkValidity(boolean isMessageEnabled) {

		String errorMsg = "";

		boolean isValid = true;

		if (isMessageEnabled && !isValid && errorMsg != null){
			UserInterfaceHelper.showWarningDialog( errorMsg );
		}

		return isValid;
	}

	@Override
	protected void performAction() {
		try {
			// do silent check first
			if( checkValidity( false ) ){

				if( _smartLinkInfo.getAreNewRelationsNeeded() || 
						_smartLinkInfo.getIsPopulatePossible() ){

					_smartLinkInfo.createDependencies( 
							_populateOnDiagramCheckBox.isSelected() );
				}
				
				bleedColourToEndLinkGraphEls();
				
			} else {
				_context.error( "Error in SmartLinkPanel.performAction, " +
						"checkValidity returned false" );
			}	
		} catch (Exception e) {
			_context.error( "Error, unhandled exception detected in SmartLinkPanel.performAction" );
		}	
	}
	
	public void bleedColourToEndLinkGraphEls(){
		
		String theForegroundColour = _context.getBleedForegroundColor();
		
		for( IRPGraphElement _endLinkGraphEl : _endLinkGraphEls){
			
			IRPModelElement theModelObject = _endLinkGraphEl.getModelObject();
			
			if( theModelObject instanceof IRPRequirement ){
				
				//_context.info( "Bleed colour to " + _context.elInfo( theModelObject ) );
				
				_context.bleedColorToGraphElsRelatedTo( 
						theModelObject, 
						theForegroundColour, 
						_endLinkGraphEl.getDiagram() );
			}
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
