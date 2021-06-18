package requirementsanalysisplugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mbsetraining.sysmlhelper.common.ConfigurationSettings;
import com.mbsetraining.sysmlhelper.common.GraphElInfo;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSEBasePanel;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.mbsetraining.sysmlhelper.smartlink.SmartLinkInfo;
import com.telelogic.rhapsody.core.*;

import functionalanalysisplugin.RequirementSelectionPanel;

public class RollUpTraceabilityToTheTransitionPanel extends ExecutableMBSEBasePanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected RequirementSelectionPanel _requirementsPanel = null;
	protected IRPTransition _transition = null;
	protected IRPGraphElement _transitionGE = null;
	protected IRPStatechartDiagram _statechartDiagram = null;
	protected Set<IRPRequirement> _reqtsForTable;
	protected JCheckBox _removeFromViewCheckBox;
	
	public static void launchThePanel(
			final IRPGraphElement theTransitionGraphEl,
			ConfigurationSettings theContext ){
	
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				
				JFrame.setDefaultLookAndFeelDecorated( true );
				
				JFrame frame = new JFrame(
						"Populate requirements on " + 
								theContext.elInfo( theTransitionGraphEl.getModelObject() ) );
				
				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
				
				RollUpTraceabilityToTheTransitionPanel thePanel = 
						new RollUpTraceabilityToTheTransitionPanel( theContext.get_rhpAppID() );

				frame.setContentPane( thePanel );
				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}
	
	public RollUpTraceabilityToTheTransitionPanel(
			String theAppID ){
		
		super( theAppID );
		
		IRPGraphElement theGraphEl = _context.getSelectedGraphEl();
		IRPModelElement theModelObject = theGraphEl.getModelObject();
		
		if( theModelObject instanceof IRPTransition ){
			
			_transition = (IRPTransition)theModelObject;
			_transitionGE = theGraphEl;
			_statechartDiagram = (IRPStatechartDiagram) _transitionGE.getDiagram();
			
			_reqtsForTable = getRequirementsRelatedTo( _transition );
			
			_requirementsPanel = new RequirementSelectionPanel( 
					"Requirements related to trigger/guard/actions are:",
					_reqtsForTable, 
					_reqtsForTable,
					_context );
			
			setLayout( new BorderLayout(10,10) );
			setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
			
			JPanel thePageStartPanel = new JPanel();
			thePageStartPanel.setLayout( new BoxLayout( thePageStartPanel, BoxLayout.X_AXIS ) );
			
			if( _reqtsForTable.isEmpty() ){
				
				JLabel theLabel = new JLabel( "There are no requirements to roll up" );
				theLabel.setAlignmentX( Component.LEFT_ALIGNMENT );
				thePageStartPanel.add( theLabel );
				
			} else {
				
				JButton theSelectAllButton = new JButton( "Select All" );
				theSelectAllButton.setPreferredSize( new Dimension( 75,25 ) );

				theSelectAllButton.addActionListener( new ActionListener() {
					
					@Override
					public void actionPerformed( ActionEvent e ) {
						
						try {
							_requirementsPanel.selectedRequirementsIn( _reqtsForTable );
														
						} catch (Exception e2) {
							
							_context.error( "Error, unhandled exception in PopulateRelatedRequirementsPanel " + 
									"constructor on Select All button action listener" );
						}
					}
				});
				
				JButton theDeselectAllButton = new JButton( "De-select All" );
				theDeselectAllButton.setPreferredSize( new Dimension( 75,25 ) );

				theDeselectAllButton.addActionListener( new ActionListener() {
					
					@Override
					public void actionPerformed( ActionEvent e ) {
						
						try {
							_requirementsPanel.deselectedRequirementsIn( _reqtsForTable );
														
						} catch (Exception e2) {
							
							_context.error( "Error, unhandled exception in PopulateRelatedRequirementsPanel " + 
									"constructor on De-select All button action listener" );
						}
					}
				});
						
				thePageStartPanel.add( theSelectAllButton );
				thePageStartPanel.add( new JLabel("  ") );
				thePageStartPanel.add( theDeselectAllButton );
				
				add( _requirementsPanel, BorderLayout.WEST );
			}
			
			add( thePageStartPanel, BorderLayout.PAGE_START );
			add( createOKCancelPanel(), BorderLayout.PAGE_END );
		}
	}
	

	
	public Set<IRPRequirement> getRequirementsRelatedTo(
			IRPTransition theTransition ){
		
		Set<IRPRequirement> theDependsOns = new HashSet<>();
		
		IRPStereotype theDependencyStereotype = 
				((ExecutableMBSE_Context) _context).getStereotypeToUseForFunctions( theTransition );
				
		IRPModelElement theOwner = 
				_context.findOwningClassIfOneExistsFor( 
						theTransition );
		
		// Look for matches related to the trigger

		IRPTrigger theTrigger = theTransition.getItsTrigger();
		
		if( theTrigger != null ){
			
			IRPInterfaceItem theInterfaceItem = theTrigger.getItsOperation();
			
			if( theInterfaceItem != null ){
				
				theDependsOns.addAll(
						_context.getRequirementsThatTraceFromWithStereotype(
								theInterfaceItem, 
								theDependencyStereotype.getName() ) );
			}	
		}
		
		// Look for matches related to the guard

		IRPGuard theGuard = theTransition.getItsGuard();
										
		if( theGuard != null ){
			
			String theBody = theGuard.getBody();
			
			if( theBody != null && !theBody.isEmpty() ){
				
				theDependsOns.addAll(
						getReqtsThatTraceFromRelatedToElsMentionedIn(
								theBody,
								(IRPClassifier) theOwner,
								theDependencyStereotype ) );
			}
		}
		
		// Look for matches related to the actions

		IRPAction theAction = theTransition.getItsAction();
		
		if( theAction != null ){
			
			String theBody = theAction.getBody();
			
			if( theBody != null && !theBody.isEmpty() ){
				
				theDependsOns.addAll(
						getReqtsThatTraceFromRelatedToElsMentionedIn(
								theBody,
								(IRPClassifier) theOwner,
								theDependencyStereotype ) );
			}
		}
		
		return theDependsOns;
	}

	private Set<IRPRequirement> getReqtsThatTraceFromRelatedToElsMentionedIn(
			String theText,
			IRPClassifier relatedToTheOwner, 
			IRPStereotype theDependencyStereotype ){
		
		Set<IRPRequirement> theReqts = new HashSet<>();
		
		Set<IRPOperation> theOps = extractOperationsMentionedIn( 
				theText, 
				relatedToTheOwner );
		
		for( IRPOperation theOp : theOps ){
			
			theReqts.addAll(
					_context.getRequirementsThatTraceFromWithStereotype(
							theOp, 
							theDependencyStereotype.getName() ) );							
		}
		
		Set<IRPAttribute> theAttributes = extractAttributesMentionedIn( 
				theText, 
				relatedToTheOwner );
		
		for( IRPAttribute theAttribute : theAttributes ){
			
			theReqts.addAll(
					_context.getRequirementsThatTraceFromWithStereotype(
							theAttribute, 
							theDependencyStereotype.getName() ) );							
		}
		
		return theReqts;
	}
	
	public Set<IRPOperation> extractOperationsMentionedIn( 
			String theText, 
			IRPClassifier ownedByClassifier ){
		
		Set<IRPOperation> theOperations = new HashSet<>();
		
		@SuppressWarnings("unchecked")
		List<IRPOperation> theClassifiersOps = 
				ownedByClassifier.getOperations().toList();
		
		for( IRPOperation theClassifiersOp : theClassifiersOps ){
			
			if( theText.contains( theClassifiersOp.getName() + "(") ){
				_context.debug( _context.elInfo( theClassifiersOp ) + " match found" );
				theOperations.add( theClassifiersOp );
			}		
		}
		
		return theOperations;
	}
	
	public Set<IRPAttribute> extractAttributesMentionedIn( 
			String theText, 
			IRPClassifier ownedByClassifier ){
		
		Set<IRPAttribute> theAttributes = new HashSet<>();
		
		@SuppressWarnings("unchecked")
		List<IRPAttribute> theOwnedAttributes = 
				ownedByClassifier.getAttributes().toList();
		
		for( IRPAttribute theOwnedAttribute : theOwnedAttributes ){
			
			if( theText.contains( theOwnedAttribute.getName() ) ){
				_context.debug( _context.elInfo( theOwnedAttribute ) + " match found" );
				theAttributes.add( theOwnedAttribute );
			}		
		}
		
		return theAttributes;
	}
	
	protected boolean checkValidity(
			boolean isMessageEnabled ){
		
		return true;
	}

	protected void performAction() {
		
		// do silent check first
		if( checkValidity( false ) ){
			
			List<IRPRequirement> theReqts = _requirementsPanel.getSelectedRequirementsList();
			
			if( theReqts.isEmpty() ){
				
				_context.info( "Doing nothing as there are no requirements" );
				
			} else {
			
				List<IRPModelElement> theStartLinkEls = new ArrayList<>();
				theStartLinkEls.add( _transition );
				
				List<IRPGraphElement> theStartLinkGraphEls = new ArrayList<>();
				theStartLinkGraphEls.add( _transitionGE );
				
				int x = new GraphElInfo (_transitionGE, _context ).getMidX();
				int y = new GraphElInfo (_transitionGE, _context ).getMidY();

				for( IRPRequirement theReqt : theReqts ){
					
					List<IRPModelElement> theEndLinkEls = new ArrayList<>();
					theEndLinkEls.add( theReqt );
					
					@SuppressWarnings("unchecked")
					List<IRPGraphElement> theReqtGEs = 
							_statechartDiagram.getCorrespondingGraphicElements( theReqt ).toList();
					
					if( theReqtGEs.isEmpty() ){
						
						IRPGraphNode theGraphNode = _statechartDiagram.addNewNodeForElement(
								theReqt, x+100, y+70, 300, 100 );
						
						x = x + 30;
						y = y + 30;
						
						theReqtGEs.add( theGraphNode );
					}
					
					SmartLinkInfo theSmartLinkInfo = 
							new SmartLinkInfo(
									theStartLinkEls, 
									theStartLinkGraphEls, 
									theEndLinkEls, 
									theReqtGEs, 
									_context );
					
					theSmartLinkInfo.createDependencies( true );
				}
			}
			
		} else {
			_context.error( "Error in PopulateRelatedRequirementsPanel.performAction, checkValidity returned false" );
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