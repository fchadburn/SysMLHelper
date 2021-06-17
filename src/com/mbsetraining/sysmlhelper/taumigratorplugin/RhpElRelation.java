package com.mbsetraining.sysmlhelper.taumigratorplugin;

public abstract class RhpElRelation extends RhpEl {

	public RhpElRelation(
			String theElementName, 
			String theElementType,
			String theElementGuid,
			TauMigrator_Context context ) throws Exception{
		
		super( theElementName, theElementType, theElementGuid, context );
	}

	public RhpElRelation(
			String theElementName, 
			String theElementType,
			String theElementGuid,
			RhpEl theParent,
			TauMigrator_Context context ) throws Exception{
		
		super( theElementName, theElementType, theElementGuid, theParent, context );
	}
}
