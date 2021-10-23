package com.mbsetraining.sysmlhelper.usecasepackage;

import java.util.ArrayList;
import java.util.List;

import com.mbsetraining.sysmlhelper.executablembse.ExecutableMBSE_Context;
import com.telelogic.rhapsody.core.*;

public class CreateActorPkg {

	protected IRPPackage _actorPkg;
	protected List<IRPActor> _actors = new ArrayList<IRPActor>();
	protected List<IRPPackage> _actorPkgs = new ArrayList<IRPPackage>();
	protected ExecutableMBSE_Context _context;

	public enum CreateActorPkgOption {
		DoNothing,
		CreateNew,
		CreateNewButEmpty,
		UseExisting,
		InstantiateFromExisting,
	}

	public List<IRPPackage> getActorPkgs(){
		return _actorPkgs;
	}

	public List<IRPActor> getActors(){
		return _actors;
	}

	public CreateActorPkg(
			ExecutableMBSE_Context context ){

		_context = context;
	}

	@SuppressWarnings("unchecked")
	public IRPPackage useExisting(
			IRPPackage theExistingActorPkg ){
		
		_actorPkg = theExistingActorPkg;
		_actorPkgs.add( _actorPkg );

		_actors = theExistingActorPkg.getActors().toList();
		
		return _actorPkg;
	}
	
	public IRPPackage createNew(
			IRPPackage theActorPkgOwner,
			String theActorPkgName,
			IRPPackage theUseCasePkg ){

		_actorPkg = createActorPackage( 
				theActorPkgOwner, 
				theActorPkgName, 
				theUseCasePkg );

		_actorPkgs.add( _actorPkg );

		_actors = addDefaultActorsForUseCaseDiagramTo( 
				_actorPkg,
				false );

		return _actorPkg;
	}

	public IRPPackage createNewButEmpty(
			IRPPackage theActorPkgOwner,
			String theActorPkgName,
			IRPPackage theUseCasePkg ){

		_actorPkg = createActorPackage( 
				theActorPkgOwner, 
				theActorPkgName, 
				theUseCasePkg );

		_actorPkgs.add( _actorPkg );

		return _actorPkg;
	}

	@SuppressWarnings("unchecked")
	public void useExisting(
			List<IRPPackage> theOptionalExistingActorPkgs,
			IRPPackage theUseCasePkg ){

		for( IRPPackage theExistingPkg : theOptionalExistingActorPkgs ){

			_actorPkgs.add( theExistingPkg );

			_actors.addAll(
					theExistingPkg.getNestedElementsByMetaClass(
							"Actor", 1).toList() );

			theUseCasePkg.addDependencyTo( theExistingPkg );
		}	
	}

	public IRPPackage instantiateFromExisting(
			IRPPackage theActorPkgOwner,
			String theActorPkgName,
			IRPPackage theUseCasePkg,
			IRPPackage theExistingActorPkg,
			String thePrefixIfInstantiating ){

		_actorPkg = createActorPackage( 
				theActorPkgOwner, 
				theActorPkgName, 
				theUseCasePkg );

		_actorPkgs.add( _actorPkg );

		@SuppressWarnings("unchecked")
		List<IRPActor> theCandidateActors = 
		theExistingActorPkg.getNestedElementsByMetaClass(
				"Actor", 1).toList();

		for( IRPActor theCandidateActor : theCandidateActors ){
			
			IRPActor theActor = _actorPkg.addActor( 
					theCandidateActor.getName() + "_" + 
							thePrefixIfInstantiating );
			
			theActor.addGeneralization( theCandidateActor );
			_actors.add( theActor );
		}

		//theUseCasePkg.addDependencyTo( theExistingPkg );

		return _actorPkg;
	}

	private IRPPackage createActorPackage(
			IRPPackage underThePackage,
			String withTheName,
			IRPPackage andDependencyTo ){

		IRPPackage theActorPkg = underThePackage.addNestedPackage( withTheName );
		theActorPkg.changeTo( _context.REQTS_ANALYSIS_ACTOR_PACKAGE );
		_context.setSavedInSeparateDirectoryIfAppropriateFor( theActorPkg );
		andDependencyTo.addDependencyTo( theActorPkg );

		return theActorPkg;
	}

	private List<IRPActor> addDefaultActorsForUseCaseDiagramTo(
			IRPPackage thePackage,
			boolean isSkipIfExisting ){

		List<IRPActor> theActors = new ArrayList<>();

		List<String> theActorNames = 
				_context.getDefaultActorsForUseCaseDiagram(
						thePackage );

		for( String theActorName : theActorNames ){

			if( isSkipIfExisting ){
				IRPModelElement theExistingActorEl =
						thePackage.getProject().findAllByName( 
								theActorName, "Actor" );

				if( theExistingActorEl == null ){
					IRPActor theActor = thePackage.addActor( theActorName );
					theActors.add( theActor );
				} else {
					theActors.add( (IRPActor) theExistingActorEl );
				}
			} else {
				IRPActor theActor = thePackage.addActor( theActorName );
				theActors.add( theActor );
			}
		}

		return theActors;
	}
}

/**
 * Copyright (C) 2018-2021  MBSE Training and Consulting Limited (www.executablembse.com)

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