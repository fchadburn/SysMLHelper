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
	private static final long serialVersionUID = 1L;

	private SmartLinkInfo m_SmartLinkInfo;
	private JCheckBox m_PopulateOnDiagramCheckBox; 

	private List<IRPModelElement> m_StartLinkEls;
	private List<IRPGraphElement> m_StartLinkGraphEls;
	private List<IRPModelElement> m_EndLinkEls;
	private List<IRPGraphElement> m_EndLinkGraphEls;
	
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

		m_StartLinkEls = new ArrayList<IRPModelElement>();
		m_StartLinkGraphEls = new ArrayList<IRPGraphElement>();

		for( String theGUID : theStartLinkGUIDs ){	
			
			IRPModelElement theStartLinkEl = _context.get_rhpPrj().findElementByGUID( theGUID );
			
			if( theStartLinkEl == null ){
				_context.error( "Unable to find start link element with GUID " + theGUID );
			} else {
				m_StartLinkEls.add( theStartLinkEl );
			}
		}
		
		m_EndLinkEls = _context.getSelectedElements();
		m_EndLinkGraphEls = _context.getSelectedGraphElements();
		
		for( IRPGraphElement theEndGraphEl : m_EndLinkGraphEls ){
			
			IRPDiagram theDiagram = theEndGraphEl.getDiagram();
			
			for( IRPModelElement startLinkEl : m_StartLinkEls ){
				
				@SuppressWarnings("unchecked")
				List<IRPGraphElement> theGraphEls = 
						theDiagram.getCorrespondingGraphicElements( startLinkEl ).toList();
			
				for (IRPGraphElement theGraphEl : theGraphEls) {
					
					if( !m_StartLinkGraphEls.contains( theGraphEl ) ){
						m_StartLinkGraphEls.add( theGraphEl );
					}
				}
			}
		}
		
		for( IRPModelElement theStartLinkEl : m_StartLinkEls ){

			if( m_EndLinkEls.contains( theStartLinkEl ) ){
				isCyclical = true;
				break;
			}
		}

		m_PopulateOnDiagramCheckBox = new JCheckBox( "Populate on diagram?" );
		m_PopulateOnDiagramCheckBox.setSelected( false );
		m_PopulateOnDiagramCheckBox.setVisible( false );

		if( isCyclical ){

			add( new JLabel( "Unable to proceed as you've selected cyclical start and end elements" ), BorderLayout.PAGE_START );

		} else {
			m_SmartLinkInfo = new SmartLinkInfo(
					m_StartLinkEls, m_StartLinkGraphEls, m_EndLinkEls, m_EndLinkGraphEls, _context );

			setLayout( new BorderLayout(10,10) );
			setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

			add( new JLabel( m_SmartLinkInfo.getDescriptionHTML() ), BorderLayout.PAGE_START );

			if( m_SmartLinkInfo.getIsPopulatePossible() ){

				m_PopulateOnDiagramCheckBox = new JCheckBox( "Populate on diagram?" );
				m_PopulateOnDiagramCheckBox.setSelected( true );
				m_PopulateOnDiagramCheckBox.setVisible( true );

				add( m_PopulateOnDiagramCheckBox, BorderLayout.CENTER );
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

			m_StartLinkEls = theStartLinkEls;
			m_StartLinkGraphEls = theStartLinkGraphEls;

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

				if( m_SmartLinkInfo.getAreNewRelationsNeeded() || 
						m_SmartLinkInfo.getIsPopulatePossible() ){

					m_SmartLinkInfo.createDependencies( 
							m_PopulateOnDiagramCheckBox.isSelected() );
				}

			} else {
				_context.error( "Error in SmartLinkPanel.performAction, " +
						"checkValidity returned false" );
			}	
		} catch (Exception e) {
			_context.error( "Error, unhandled exception detected in SmartLinkPanel.performAction" );
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
