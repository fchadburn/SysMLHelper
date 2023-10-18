package com.mbsetraining.sysmlhelper.functionallocationpanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
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

	//protected String _allocatedFromString;
	protected IRPInstance _usageToAllocate;
	protected List<IRPModelElement> _allocationChoices;
	protected RhapsodyComboBox _allocationChoicesComboBox;
	protected JLabel _statusLabel;
	protected List<IRPModelElement> _existingAllocationsUsingDependencies;
	protected List<IRPModelElement> _existingAllocationsUsingParts;

	public FunctionAllocationInfo(
			IRPInstance usageToAllocate,
			List<IRPModelElement> allocationChoices,
			ExecutableMBSE_Context context ) {

		_context = context;
		_usageToAllocate = usageToAllocate;

		if( _usageToAllocate.isTypelessObject() == 1 ) {
			_context.warning( "Found that " + _context.elInfo( _usageToAllocate ) + " is a typeless part" );
		}
		
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
	}
	
	public String getUsageName() {
		return _usageToAllocate.getName() + ":" + _usageToAllocate.getOtherClass().getName();
	}
	
	private void determineExistingAllocationsUsingDependencies() {
		
		_existingAllocationsUsingDependencies = new ArrayList<>();

		List<IRPModelElement> existingAllocatedToEls = new ArrayList<>( _context.
				getElementsThatHaveStereotypedDependenciesFrom( _usageToAllocate, "Allocation" ) );

		for( IRPModelElement existingAllocatedToEl : existingAllocatedToEls ){

			if( existingAllocatedToEl instanceof IRPClassifier ) {

				_context.info( _context.elInfo( _usageToAllocate ) + " has allocate to " + _context.elInfo( existingAllocatedToEl ) );

				if( _allocationChoices.contains( existingAllocatedToEl ) ) {
					
					_context.info( "Existing allocation of " + _context.elInfo( _usageToAllocate ) + " using a usage found for " + _context.elInfo( existingAllocatedToEl ) );

					_existingAllocationsUsingDependencies.add( existingAllocatedToEl );
					_allocationChoicesComboBox.setSelectedRhapsodyItem( existingAllocatedToEl );
				}
			}
		}
	}
	
	private void determineExistingAllocationsUsingParts() {
		
		_existingAllocationsUsingParts = new ArrayList<>();
		
		for( IRPModelElement allocationChoice : _allocationChoices ){
			
			@SuppressWarnings("unchecked")
			List<IRPInstance> theParts = allocationChoice.getNestedElementsByMetaClass( "Part", 0 ).toList();

			for( IRPInstance thePart : theParts ){

				if( thePart.isTypelessObject() == 0 ) {

					IRPModelElement theCandidate = thePart.getOtherClass();

					if( theCandidate.equals( _usageToAllocate.getOtherClass() ) ) {
						_existingAllocationsUsingParts.add( allocationChoice );
						_allocationChoicesComboBox.setSelectedRhapsodyItem( allocationChoice );
					}
				}
			}
		}
	}

	public String getStatusString() {

		String theMsg = "";
		
		IRPModelElement theChosenEl = _allocationChoicesComboBox.getSelectedRhapsodyItem();
		
		int dependencyCount = _existingAllocationsUsingDependencies.size();
		int partCount = _existingAllocationsUsingParts.size();

		if( theChosenEl == null ) {
			
			if( dependencyCount > 0 && partCount > 0 ) {
				theMsg += "There are " + dependencyCount + " dependencies and " + partCount + " parts to clean up";
			} else if( dependencyCount > 0 ) {
				theMsg += "There are " + dependencyCount + " dependencies to clean up";
			}
			
		} else {
			
			if( _existingAllocationsUsingParts.contains(theChosenEl)) {
				theMsg += "Already allocated.";
			} else {
				theMsg += "New usage needed";
			}
		}


		return theMsg;
	}

	public void performAction() {
		
		IRPModelElement theChosenEl = _allocationChoicesComboBox.getSelectedRhapsodyItem();

		_context.info( "performAction invoked for " + _context.elInfo( _usageToAllocate ) + " with selection of " + _context.elInfo( theChosenEl ) );

		for (IRPModelElement existingAllocation : _existingAllocationsUsingParts) {
			_context.info( _context.elInfo( existingAllocation ) + " is an existing allocation using parts" );

		}
		
		for (IRPModelElement existingAllocation : _existingAllocationsUsingDependencies) {
			_context.info( _context.elInfo( existingAllocation ) + " is an existing allocation using dependencies" );

		}

		if( theChosenEl instanceof IRPClassifier ) {
			
			if( _existingAllocationsUsingParts.contains( theChosenEl ) ){
				_context.info( "No action necessary as " + _context.elInfo( theChosenEl ) + 
						" already has a usage typed by " + _context.elInfo( _usageToAllocate.getOtherClass() ) );
			} else {
				
				IRPClassifier allocatedSubsystem = (IRPClassifier) theChosenEl;
				
				IRPInstance thePart = (IRPInstance) allocatedSubsystem.addNewAggr( "Part", "" ); 
				thePart.setOtherClass( _usageToAllocate.getOtherClass() );
				thePart.changeTo( _usageToAllocate.getUserDefinedMetaClass() );
				
				_context.info( "Created " + _context.elInfo( thePart ) + " under " + _context.elInfo( allocatedSubsystem ) );
			}
		}

		/*
		switch( _allocationStatus ) {
		
		case UNDECIDED:
			_context.info( "No choice" );
			break;
		
		case NOT_ALLOCATED:
			_context.info( "Skipping" );
			break;
		
		case ALLOCATION_DEPENDENCY_BUT_NO_PART:
			
			_context.info( "Create part" );
			
			IRPModelElement theSelectedEl = _allocationChoicesComboBox.getSelectedRhapsodyItem();
			
			if( theSelectedEl instanceof IRPClassifier ) {
				
				IRPClassifier allocatedSubsystem = (IRPClassifier) theSelectedEl;
				
				IRPInstance thePart = (IRPInstance) allocatedSubsystem.addNewAggr( "Part", "" ); 
				thePart.setOtherClass( _usageToAllocate.getOtherClass() );
				thePart.changeTo( _usageToAllocate.getUserDefinedMetaClass() );
				
				_context.info( "Created " + _context.elInfo( thePart ) + " under " + _context.elInfo( allocatedSubsystem ) );
			}
			
			break;
		
		case ALREADY_ALLOCATED_WITH_PART:
			_context.info( "Skipping" );
			break;
		
		case MULTIPLE_ALLOCATION_ERROR:
			_context.info( "Skipping" );
			break;
		}*/

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
