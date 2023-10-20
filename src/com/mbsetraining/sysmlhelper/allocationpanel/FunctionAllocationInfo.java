package com.mbsetraining.sysmlhelper.allocationpanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;

import com.mbsetraining.sysmlhelper.common.RhapsodyComboBox;
import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class FunctionAllocationInfo {

	enum AllocationStatus_t {
		UNDECIDED,
		NOT_ALLOCATED,
		ALLOCATED,
		MULTIPLE_ALLOCATION_ERROR
	}

	protected ExecutableMBSE_Context _context;
	protected IRPInstance _usageToAllocate;
	protected List<IRPModelElement> _allocationChoices;
	protected RhapsodyComboBox _allocationChoicesComboBox;
	protected JLabel _nameLabel;
	protected JLabel _statusLabel;
	protected List<IRPModelElement> _existingBlockAllocationsUsingDependencies;
	protected List<IRPModelElement> _existingBlockAllocationsUsingParts;
	protected List<IRPInstance> _usagesInTargetArchitecture;

	public FunctionAllocationInfo(
			IRPInstance usageToAllocate,
			ExecutableMBSE_Context context ) {

		_context = context;
		_usageToAllocate = usageToAllocate;
	}
	
	public void buildContentFor(
			List<IRPModelElement> allocationChoices ){
		
		_allocationChoices = allocationChoices;
		_allocationChoicesComboBox = new RhapsodyComboBox( _allocationChoices, false );
		
		determineExistingAllocationsUsingDependencies();
		determineExistingAllocationsUsingParts();
		
		_allocationChoicesComboBox.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ){
				_statusLabel.setText( getStatusString() );
			}
		});
		
		_statusLabel = new JLabel( getStatusString() );
		
		_statusLabel.addMouseListener( new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {

				_context.debug( "Highlighting " + _context.elInfo(_usageToAllocate) + " on the diagram." );
				_usageToAllocate.highLightElement();
			}
		});
		
		_nameLabel = new JLabel( getUsageName() );
		
		_nameLabel.addMouseListener( new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {

				_context.debug( "Highlighting " + _context.elInfo(_usageToAllocate) + " on the diagram." );
				_context.get_rhpApp().highLightElement(_usageToAllocate);
			}
		});
	}
				
	public String getUsageName() {
		return _usageToAllocate.getName() + ":" + _usageToAllocate.getOtherClass().getName();
	}
	
	private void determineExistingAllocationsUsingDependencies() {
		
		_existingBlockAllocationsUsingDependencies = new ArrayList<>();

		List<IRPModelElement> existingAllocatedToEls = new ArrayList<>( _context.
				getElementsThatHaveStereotypedDependenciesFrom( _usageToAllocate, "Allocation" ) );

		for( IRPModelElement existingAllocatedToEl : existingAllocatedToEls ){

			if( existingAllocatedToEl instanceof IRPClassifier ) {

				_context.info( _context.elInfo( _usageToAllocate ) + " has allocate to " + _context.elInfo( existingAllocatedToEl ) );

				if( _allocationChoices.contains( existingAllocatedToEl ) ) {
					
					_context.info( "Existing allocation of " + _context.elInfo( _usageToAllocate ) + " using a usage found for " + _context.elInfo( existingAllocatedToEl ) );

					_existingBlockAllocationsUsingDependencies.add( existingAllocatedToEl );
					_allocationChoicesComboBox.setSelectedRhapsodyItem( existingAllocatedToEl );
				}
			}
		}
	}
	
	private void determineExistingAllocationsUsingParts() {
		
		_existingBlockAllocationsUsingParts = new ArrayList<>();
		
		// These will be function blocks
		for( IRPModelElement allocationChoice : _allocationChoices ){
			
			@SuppressWarnings("unchecked")
			List<IRPInstance> theParts = allocationChoice.getNestedElementsByMetaClass( "Part", 0 ).toList();

			for( IRPInstance thePart : theParts ){

				if( thePart.isTypelessObject() == 0 ) {

					IRPModelElement theCandidate = thePart.getOtherClass();

					if( theCandidate.equals( _usageToAllocate.getOtherClass() ) ) {
						_existingBlockAllocationsUsingParts.add( allocationChoice );
						_usagesInTargetArchitecture.add( thePart );
						_allocationChoicesComboBox.setSelectedRhapsodyItem( allocationChoice );
					}
				}
			}
		}
	}

	public String getStatusString() {

		String theMsg = "";
		
		IRPModelElement theChosenEl = _allocationChoicesComboBox.getSelectedRhapsodyItem();
		
		int dependencyCount = _existingBlockAllocationsUsingDependencies.size();
		int partCount = _existingBlockAllocationsUsingParts.size();

		if( theChosenEl == null ) {
			
			if( dependencyCount > 0 && partCount > 0 ) {
				theMsg += "There are " + dependencyCount + " dependencies and " + partCount + " parts to clean up";
			} else if( dependencyCount > 0 ) {
				theMsg += "There are " + dependencyCount + " dependencies to clean up";
			}
			
		} else {
			
			if( _existingBlockAllocationsUsingParts.contains(theChosenEl)) {
				theMsg += "Already allocated.";
			} else {
				theMsg += "New usage needed";
			}
		}


		return theMsg;
	}

	public void performAllocationOfUsages() {
		
		IRPModelElement theChosenEl = _allocationChoicesComboBox.getSelectedRhapsodyItem();

		_context.info( "performAction invoked for " + _context.elInfo( _usageToAllocate ) + " with selection of " + _context.elInfo( theChosenEl ) );

		for (IRPModelElement existingAllocation : _existingBlockAllocationsUsingParts) {
			_context.info( _context.elInfo( existingAllocation ) + " is an existing allocation using parts" );

		}
		
		for (IRPModelElement existingAllocation : _existingBlockAllocationsUsingDependencies) {
			_context.info( _context.elInfo( existingAllocation ) + " is an existing allocation using dependencies" );

		}

		if( theChosenEl instanceof IRPClassifier ) {
			
			if( _existingBlockAllocationsUsingParts.contains( theChosenEl ) ){
				_context.info( "No action necessary as " + _context.elInfo( theChosenEl ) + 
						" already has a usage typed by " + _context.elInfo( _usageToAllocate.getOtherClass() ) );
			} else {
				
				IRPClassifier allocatedSubsystem = (IRPClassifier) theChosenEl;
				
				IRPInstance thePart = (IRPInstance) allocatedSubsystem.addNewAggr( "Part", "" ); 
				thePart.setOtherClass( _usageToAllocate.getOtherClass() );
				thePart.changeTo( _usageToAllocate.getUserDefinedMetaClass() );
				
				_usagesInTargetArchitecture.add( thePart );
				
				_context.info( "Created " + _context.elInfo( thePart ) + " under " + _context.elInfo( allocatedSubsystem ) );
			}
		}
	}
}

/**
 * Copyright (C) 2023  MBSE Training and Consulting Limited (www.executablembse.com)

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
