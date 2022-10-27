package com.mbsetraining.sysmlhelper.viewviewpoint;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.mbsetraining.sysmlhelper.common.UserInterfaceHelper;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSEBasePanel;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class AddToViewPanel extends ExecutableMBSEBasePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6999678549678497401L;
	private ViewLinkInfo _smartLinkInfo;
	private JCheckBox _populateOnDiagramCheckBox; 

	private List<IRPModelElement> _startLinkEls;
	private List<IRPGraphElement> _startLinkGraphEls;
	private List<IRPModelElement> _endLinkEls;
	private List<IRPGraphElement> _endLinkGraphEls;
	
	private IRPStereotype _viewStereotype;
	
	public static void main(String[] args) {
	
		IRPApplication theRhpApp = RhapsodyAppServer.getActiveRhapsodyApplication();
		String theAppID = theRhpApp.getApplicationConnectionString(); 
		
		ExecutableMBSE_Context theContext = new ExecutableMBSE_Context( theAppID );
		launchThePanel( theAppID, theContext );
	}
	
	// Cannot pass Rhapsody elements to separate thread hence need to send GUID strings instead
	public static void launchThePanel(
			final String theAppID,
			ExecutableMBSE_Context theContext ){

		List<String> theExcludedNames = new ArrayList<>();
		theExcludedNames.add( theContext.VIEW_TBD );

		List<IRPModelElement> theCandidateViews = theContext.getElementsInProjectThatMatch( 
				"Package", "View", theExcludedNames );

		if( theCandidateViews.isEmpty() ) {

			UserInterfaceHelper.showInformationDialog( 
					"There are no Views in the project. Create a " + theContext.VIEW_AND_VIEWPOINT_PACKAGE + 
					" first, and then \n" + "use the helper menu " + 
					"to create a named View structure before running this command.");
		} else {

			IRPModelElement theChosenView = UserInterfaceHelper.
					launchDialogToSelectElement( theCandidateViews, "Choose the view", true );

			if( theChosenView != null ) {

				List<String> theSelectedElGUIDs = new ArrayList<>();
				theSelectedElGUIDs.add( theChosenView.getGUID() );

				javax.swing.SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {

						JFrame.setDefaultLookAndFeelDecorated( true );

						JFrame frame = new JFrame( "Add to a view" );

						frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

						AddToViewPanel thePanel = 
								new AddToViewPanel( 
										theAppID,
										theSelectedElGUIDs );

						frame.setContentPane( thePanel );
						frame.pack();
						frame.setLocationRelativeTo( null );
						frame.setVisible( true );
					}
				});
			}
		}
	}
	
	public AddToViewPanel(
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
				
				if( theStartLinkGUIDs.size() == 1 ) {
										
					String theStereotypeName = theStartLinkEl.getName().replaceFirst( _context.VIEW_PREFIX, "" ); 
					
					_viewStereotype = _context.getExistingStereotype( theStereotypeName, theStartLinkEl.getProject() );
					
					if( _viewStereotype == null ) {
						_context.warning( "Unable to find a view stereotype with the name " + theStereotypeName );
					}
				}
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
			_smartLinkInfo = new ViewLinkInfo(
					_startLinkEls, _startLinkGraphEls, _endLinkEls, _endLinkGraphEls, _context );

			setLayout( new BorderLayout(10,10) );
			setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

			add( new JLabel( _smartLinkInfo.getAddToViewDescriptionHTML() ), BorderLayout.PAGE_START );

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
					
					if( _startLinkEls.size() == 1 && 
							_viewStereotype != null ) {
						
						IRPModelElement theView = _startLinkEls.get( 0 );
						
						@SuppressWarnings("unchecked")
						List<IRPDependency> theDependencies = theView.getDependencies().toList();
						
						for( IRPDependency theDependency : theDependencies ){
							
							if( !_context.hasStereotypeCalled( 
									_viewStereotype.getName(), theDependency ) ) {
								
								theDependency.addSpecificStereotype( _viewStereotype );
							};
						}
					}
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
 * Copyright (C) 2022  MBSE Training and Consulting Limited (www.executablembse.com)

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
