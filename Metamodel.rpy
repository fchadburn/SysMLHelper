I-Logix-RPY-Archive version 8.14.0 C++ 9810313
{ IProject 
	- _id = GUID a1bdb178-451f-4e7d-95ae-1d2a90f6f654;
	- _myState = 8192;
	- _properties = { IPropertyContainer 
		- Subjects = { IRPYRawContainer 
			- size = 3;
			- value = 
			{ IPropertySubject 
				- _Name = "Browser";
				- Metaclasses = { IRPYRawContainer 
					- size = 1;
					- value = 
					{ IPropertyMetaclass 
						- _Name = "Settings";
						- Properties = { IRPYRawContainer 
							- size = 1;
							- value = 
							{ IProperty 
								- _Name = "ShowOrder";
								- _Value = "True";
								- _Type = Bool;
							}
						}
					}
				}
			}
			{ IPropertySubject 
				- _Name = "CPP_CG";
				- Metaclasses = { IRPYRawContainer 
					- size = 1;
					- value = 
					{ IPropertyMetaclass 
						- _Name = "Configuration";
						- Properties = { IRPYRawContainer 
							- size = 1;
							- value = 
							{ IProperty 
								- _Name = "Environment";
								- _Value = "Cygwin";
								- _Type = Enum;
								- _ExtraTypeInfo = "MSVC,MSVCDLL,MSVCStandardLibrary,VxWorks,VxWorks6diab,VxWorks6gnu,VxWorks6diab_RTP,VxWorks6gnu_RTP,Solaris2,Cygwin,MicrosoftWinCE600,OseSfk,Linux,Solaris2GNU,QNXNeutrinoGCC, QNXNeutrinoMomentics,NucleusPLUS-PPC,WorkbenchManaged,WorkbenchManaged653,WorkbenchManaged_RTP";
							}
						}
					}
				}
			}
			{ IPropertySubject 
				- _Name = "General";
				- Metaclasses = { IRPYRawContainer 
					- size = 1;
					- value = 
					{ IPropertyMetaclass 
						- _Name = "Model";
						- Properties = { IRPYRawContainer 
							- size = 1;
							- value = 
							{ IProperty 
								- _Name = "RenameUnusedFiles";
								- _Value = "True";
								- _Type = Bool;
							}
						}
					}
				}
			}
		}
	}
	- _name = "Metamodel";
	- _modifiedTimeWeak = 4.29.2021::11:40:23;
	- _lastID = 10006;
	- _UserColors = { IRPYRawContainer 
		- size = 16;
		- value = 16777215; 16777215; 16777215; 16777215; 16777215; 16777215; 16777215; 16777215; 16777215; 16777215; 16777215; 16777215; 16777215; 16777215; 16777215; 16777215; 
	}
	- _defaultSubsystem = { ISubsystemHandle 
		- _m2Class = "ISubsystem";
		- _id = GUID c4407196-88cd-4c1d-a928-4e9df0746722;
	}
	- _component = { IHandle 
		- _m2Class = "IComponent";
		- _id = GUID ac5be808-8ba4-4b6d-b9cb-345bd0fe0eff;
	}
	- Multiplicities = { IRPYRawContainer 
		- size = 4;
		- value = 
		{ IMultiplicityItem 
			- _name = "1";
			- _count = -1;
		}
		{ IMultiplicityItem 
			- _name = "*";
			- _count = -1;
		}
		{ IMultiplicityItem 
			- _name = "0,1";
			- _count = -1;
		}
		{ IMultiplicityItem 
			- _name = "1..*";
			- _count = -1;
		}
	}
	- Subsystems = { IRPYRawContainer 
		- size = 5;
		- value = 
		{ IProfile 
			- fileName = "MetamodelProfile";
			- _id = GUID d2ddb420-4d4b-4832-bf58-e291ae395548;
		}
		{ ISubsystem 
			- _id = GUID c4407196-88cd-4c1d-a928-4e9df0746722;
			- _myState = 8192;
			- _name = "NotUsedPkg";
			- _modifiedTimeWeak = 4.4.2020::10:44:39;
			- weakCGTime = 4.4.2020::10:44:39;
			- strongCGTime = 4.4.2020::10:44:32;
			- _defaultComposite = GUID c6d756e8-e384-403c-b1ec-cc2c0444720f;
			- _eventsBaseID = -1;
			- Classes = { IRPYRawContainer 
				- size = 1;
				- value = 
				{ IClass 
					- _id = GUID c6d756e8-e384-403c-b1ec-cc2c0444720f;
					- _myState = 40960;
					- _name = "TopLevel";
					- _modifiedTimeWeak = 1.2.1990::0:0:0;
					- weakCGTime = 1.2.1990::0:0:0;
					- strongCGTime = 1.2.1990::0:0:0;
					- _multiplicity = "";
					- _itsStateChart = { IHandle 
						- _m2Class = "";
					}
					- _classModifier = Unspecified;
				}
			}
			- _configurationRelatedTime = 1.2.1990::0:0:0;
		}
		{ ISubsystem 
			- fileName = "Base_MetaModelPkg";
			- _id = GUID 66ce32f5-6908-43e4-bcd2-5191392e805b;
		}
		{ ISubsystem 
			- _id = GUID 452616f3-bfae-409f-a859-c0df3f0206e8;
			- _myState = 8192;
			- _name = "BasePkg";
			- Stereotypes = { IRPYRawContainer 
				- size = 1;
				- value = 
				{ IHandle 
					- _m2Class = "IStereotype";
					- _subsystem = "MetamodelProfile";
					- _name = "Metamodel Package";
					- _id = GUID 07e3765a-4e90-466c-91d7-8783e5435485;
				}
			}
			- _modifiedTimeWeak = 7.6.2020::9:0:41;
			- _lastID = 8;
			- Declaratives = { IRPYRawContainer 
				- size = 2;
				- value = 
				{ IDiagram 
					- _id = GUID f494f629-abf6-42e6-8613-382aa7e967c4;
					- _myState = 8192;
					- _properties = { IPropertyContainer 
						- Subjects = { IRPYRawContainer 
							- size = 1;
							- value = 
							{ IPropertySubject 
								- _Name = "Format";
								- Metaclasses = { IRPYRawContainer 
									- size = 4;
									- value = 
									{ IPropertyMetaclass 
										- _Name = "Association";
										- Properties = { IRPYRawContainer 
											- size = 4;
											- value = 
											{ IProperty 
												- _Name = "Font.Font";
												- _Value = "Tahoma";
												- _Type = String;
											}
											{ IProperty 
												- _Name = "Font.Size";
												- _Value = "8";
												- _Type = Int;
											}
											{ IProperty 
												- _Name = "Line.LineColor";
												- _Value = "128,128,128";
												- _Type = Color;
											}
											{ IProperty 
												- _Name = "Line.LineWidth";
												- _Value = "1";
												- _Type = Int;
											}
										}
									}
									{ IPropertyMetaclass 
										- _Name = "Class";
										- Properties = { IRPYRawContainer 
											- size = 8;
											- value = 
											{ IProperty 
												- _Name = "DefaultSize";
												- _Value = "0,34,84,148";
												- _Type = String;
											}
											{ IProperty 
												- _Name = "Fill.FillColor";
												- _Value = "255,255,255";
												- _Type = Color;
											}
											{ IProperty 
												- _Name = "Font.Font";
												- _Value = "Tahoma";
												- _Type = String;
											}
											{ IProperty 
												- _Name = "Font.Size";
												- _Value = "8";
												- _Type = Int;
											}
											{ IProperty 
												- _Name = "Font.Weight@Child.NameCompartment@Name";
												- _Value = "700";
												- _Type = Int;
											}
											{ IProperty 
												- _Name = "Line.LineColor";
												- _Value = "109,163,217";
												- _Type = Color;
											}
											{ IProperty 
												- _Name = "Line.LineStyle";
												- _Value = "0";
												- _Type = Int;
											}
											{ IProperty 
												- _Name = "Line.LineWidth";
												- _Value = "1";
												- _Type = Int;
											}
										}
									}
									{ IPropertyMetaclass 
										- _Name = "DiagramFrame";
										- Properties = { IRPYRawContainer 
											- size = 8;
											- value = 
											{ IProperty 
												- _Name = "DefaultSize";
												- _Value = "20,20,590,500";
												- _Type = String;
											}
											{ IProperty 
												- _Name = "Fill.FillColor";
												- _Value = "247,247,247";
												- _Type = Color;
											}
											{ IProperty 
												- _Name = "Fill.Transparent_Fill";
												- _Value = "0";
												- _Type = Int;
											}
											{ IProperty 
												- _Name = "Font.Font";
												- _Value = "Tahoma";
												- _Type = String;
											}
											{ IProperty 
												- _Name = "Font.Size";
												- _Value = "8";
												- _Type = Int;
											}
											{ IProperty 
												- _Name = "Font.Weight@Child.NameCompartment@Name";
												- _Value = "700";
												- _Type = Int;
											}
											{ IProperty 
												- _Name = "Line.LineColor";
												- _Value = "194,192,192";
												- _Type = Color;
											}
											{ IProperty 
												- _Name = "Line.LineWidth";
												- _Value = "1";
												- _Type = Int;
											}
										}
									}
									{ IPropertyMetaclass 
										- _Name = "Inheritance";
										- Properties = { IRPYRawContainer 
											- size = 4;
											- value = 
											{ IProperty 
												- _Name = "Font.Font";
												- _Value = "Tahoma";
												- _Type = String;
											}
											{ IProperty 
												- _Name = "Font.Size";
												- _Value = "8";
												- _Type = Int;
											}
											{ IProperty 
												- _Name = "Line.LineColor";
												- _Value = "128,128,128";
												- _Type = Color;
											}
											{ IProperty 
												- _Name = "Line.LineWidth";
												- _Value = "1";
												- _Type = Int;
											}
										}
									}
								}
							}
						}
					}
					- _name = "MMD - Add Timing diagram";
					- _modifiedTimeWeak = 1.2.1990::0:0:0;
					- _lastModifiedTime = "7.6.2020::8:59:53";
					- _graphicChart = { CGIClassChart 
						- _id = GUID 1c70c533-3c31-4f8a-a72b-b040e11a4c61;
						- m_type = 0;
						- m_pModelObject = { IHandle 
							- _m2Class = "IDiagram";
							- _id = GUID f494f629-abf6-42e6-8613-382aa7e967c4;
						}
						- m_pParent = ;
						- m_name = { CGIText 
							- m_str = "";
							- m_style = "Arial" 10 0 0 0 1 ;
							- m_color = { IColor 
								- m_fgColor = 0;
								- m_bgColor = 0;
								- m_bgFlag = 0;
							}
							- m_position = 1 0 0  ;
							- m_nIdent = 0;
							- m_bImplicitSetRectPoints = 0;
							- m_nOrientationCtrlPt = 8;
						}
						- m_drawBehavior = 0;
						- m_bIsPreferencesInitialized = 0;
						- elementList = 3;
						{ CGIClass 
							- _id = GUID e58b9a42-e30c-46f6-86a1-afb32e53c546;
							- m_type = 78;
							- m_pModelObject = { IHandle 
								- _m2Class = "IClass";
								- _id = GUID b2e7f70f-f540-4a1a-859f-fbb2d7dde341;
							}
							- m_pParent = ;
							- m_name = { CGIText 
								- m_str = "TopLevel";
								- m_style = "Arial" 10 0 0 0 1 ;
								- m_color = { IColor 
									- m_fgColor = 0;
									- m_bgColor = 0;
									- m_bgFlag = 0;
								}
								- m_position = 1 0 0  ;
								- m_nIdent = 0;
								- m_bImplicitSetRectPoints = 0;
								- m_nOrientationCtrlPt = 5;
							}
							- m_drawBehavior = 0;
							- m_bIsPreferencesInitialized = 0;
							- m_AdditionalLabel = { CGIText 
								- m_str = "";
								- m_style = "Arial" 10 0 0 0 1 ;
								- m_color = { IColor 
									- m_fgColor = 0;
									- m_bgColor = 0;
									- m_bgFlag = 0;
								}
								- m_position = 1 0 0  ;
								- m_nIdent = 0;
								- m_bImplicitSetRectPoints = 0;
								- m_nOrientationCtrlPt = 1;
							}
							- m_polygon = 0 ;
							- m_nNameFormat = 0;
							- m_nIsNameFormat = 0;
							- Attrs = { IRPYRawContainer 
								- size = 0;
							}
							- Operations = { IRPYRawContainer 
								- size = 0;
							}
						}
						{ CGIDiagramFrame 
							- _id = GUID d1454293-e12d-401d-a851-59d0844c8029;
							- m_type = 203;
							- m_pModelObject = { IHandle 
								- _m2Class = "";
							}
							- m_pParent = GUID e58b9a42-e30c-46f6-86a1-afb32e53c546;
							- m_name = { CGIText 
								- m_str = "";
								- m_style = "Arial" 10 0 0 0 1 ;
								- m_color = { IColor 
									- m_fgColor = 0;
									- m_bgColor = 0;
									- m_bgFlag = 0;
								}
								- m_position = 1 0 0  ;
								- m_nIdent = 0;
								- m_bImplicitSetRectPoints = 0;
								- m_nOrientationCtrlPt = 8;
							}
							- m_drawBehavior = 4096;
							- m_transform = 2.63889 0 0 3.63636 20 20 ;
							- m_bIsPreferencesInitialized = 1;
							- m_AdditionalLabel = { CGIText 
								- m_str = "";
								- m_style = "Arial" 10 0 0 0 1 ;
								- m_color = { IColor 
									- m_fgColor = 0;
									- m_bgColor = 0;
									- m_bgFlag = 0;
								}
								- m_position = 1 0 0  ;
								- m_nIdent = 0;
								- m_bImplicitSetRectPoints = 0;
								- m_nOrientationCtrlPt = 1;
							}
							- m_polygon = 4 0 0  0 132  216 132  216 0  ;
							- m_nNameFormat = 0;
							- m_nIsNameFormat = 0;
						}
						{ CGIClass 
							- _id = GUID 7a77b311-26cb-469f-8322-122ed3ba7240;
							- m_type = 87;
							- m_pModelObject = { IHandle 
								- _m2Class = "IClass";
								- _subsystem = "Base_MetaModelPkg";
								- _name = "Diagram";
								- _id = GUID d34f5f9c-66e5-4fa2-bb6a-440e3710b219;
							}
							- m_pParent = GUID e58b9a42-e30c-46f6-86a1-afb32e53c546;
							- m_name = { CGIText 
								- m_str = "Base_MetaModelPkg::Diagram";
								- m_style = "Arial" 10 0 0 0 1 ;
								- m_color = { IColor 
									- m_fgColor = 0;
									- m_bgColor = 0;
									- m_bgFlag = 0;
								}
								- m_position = 1 0 0  ;
								- m_nIdent = 0;
								- m_bImplicitSetRectPoints = 0;
								- m_nOrientationCtrlPt = 5;
							}
							- m_drawBehavior = 2056;
							- m_transform = 0.0906516 0 0 0.101604 315.819 112.572 ;
							- m_bIsPreferencesInitialized = 1;
							- m_AdditionalLabel = { CGIText 
								- m_str = "";
								- m_style = "Arial" 10 0 0 0 1 ;
								- m_color = { IColor 
									- m_fgColor = 0;
									- m_bgColor = 0;
									- m_bgFlag = 0;
								}
								- m_position = 1 0 0  ;
								- m_nIdent = 0;
								- m_bImplicitSetRectPoints = 0;
								- m_nOrientationCtrlPt = 1;
							}
							- m_polygon = 4 2 329  2 1451  1061 1451  1061 329  ;
							- m_nNameFormat = 0;
							- m_nIsNameFormat = 0;
							- frameset = "<frameset rows=50%,50%>
<frame name=AttributeListCompartment>
<frame name=OperationListCompartment>";
							- Compartments = { IRPYRawContainer 
								- size = 2;
								- value = 
								{ CGICompartment 
									- _id = GUID 4ad74648-7206-4574-b4f5-2a588968c357;
									- m_name = "Attribute";
									- m_displayOption = Explicit;
									- m_bShowInherited = 0;
									- m_bOrdered = 0;
									- Items = { IRPYRawContainer 
										- size = 0;
									}
								}
								{ CGICompartment 
									- _id = GUID d005bd54-3ae2-4cb8-89a4-d184b17ac359;
									- m_name = "Operation";
									- m_displayOption = Explicit;
									- m_bShowInherited = 0;
									- m_bOrdered = 0;
									- Items = { IRPYRawContainer 
										- size = 0;
									}
								}
							}
							- Attrs = { IRPYRawContainer 
								- size = 0;
							}
							- Operations = { IRPYRawContainer 
								- size = 0;
							}
						}
						
						- m_access = 'Z';
						- m_modified = 'N';
						- m_fileVersion = "";
						- m_nModifyDate = 0;
						- m_nCreateDate = 0;
						- m_creator = "";
						- m_bScaleWithZoom = 1;
						- m_arrowStyle = 'S';
						- m_pRoot = GUID e58b9a42-e30c-46f6-86a1-afb32e53c546;
						- m_currentLeftTop = 0 0 ;
						- m_currentRightBottom = 0 0 ;
						- m_bFreezeCompartmentContent = 0;
					}
					- _defaultSubsystem = { IHandle 
						- _m2Class = "ISubsystem";
						- _id = GUID 452616f3-bfae-409f-a859-c0df3f0206e8;
					}
				}
				{ IDiagram 
					- _id = GUID 28d34d6d-40fd-4f4c-8b14-87997f25d78e;
					- _myState = 10240;
					- _properties = { IPropertyContainer 
						- Subjects = { IRPYRawContainer 
							- size = 1;
							- value = 
							{ IPropertySubject 
								- _Name = "Format";
								- Metaclasses = { IRPYRawContainer 
									- size = 5;
									- value = 
									{ IPropertyMetaclass 
										- _Name = "Association";
										- Properties = { IRPYRawContainer 
											- size = 4;
											- value = 
											{ IProperty 
												- _Name = "Font.Font";
												- _Value = "Tahoma";
												- _Type = String;
											}
											{ IProperty 
												- _Name = "Font.Size";
												- _Value = "8";
												- _Type = Int;
											}
											{ IProperty 
												- _Name = "Line.LineColor";
												- _Value = "128,128,128";
												- _Type = Color;
											}
											{ IProperty 
												- _Name = "Line.LineWidth";
												- _Value = "1";
												- _Type = Int;
											}
										}
									}
									{ IPropertyMetaclass 
										- _Name = "Class";
										- Properties = { IRPYRawContainer 
											- size = 8;
											- value = 
											{ IProperty 
												- _Name = "DefaultSize";
												- _Value = "0,34,84,148";
												- _Type = String;
											}
											{ IProperty 
												- _Name = "Fill.FillColor";
												- _Value = "255,255,255";
												- _Type = Color;
											}
											{ IProperty 
												- _Name = "Font.Font";
												- _Value = "Tahoma";
												- _Type = String;
											}
											{ IProperty 
												- _Name = "Font.Size";
												- _Value = "8";
												- _Type = Int;
											}
											{ IProperty 
												- _Name = "Font.Weight@Child.NameCompartment@Name";
												- _Value = "700";
												- _Type = Int;
											}
											{ IProperty 
												- _Name = "Line.LineColor";
												- _Value = "109,163,217";
												- _Type = Color;
											}
											{ IProperty 
												- _Name = "Line.LineStyle";
												- _Value = "0";
												- _Type = Int;
											}
											{ IProperty 
												- _Name = "Line.LineWidth";
												- _Value = "1";
												- _Type = Int;
											}
										}
									}
									{ IPropertyMetaclass 
										- _Name = "Composition";
										- Properties = { IRPYRawContainer 
											- size = 4;
											- value = 
											{ IProperty 
												- _Name = "Font.Font";
												- _Value = "Tahoma";
												- _Type = String;
											}
											{ IProperty 
												- _Name = "Font.Size";
												- _Value = "8";
												- _Type = Int;
											}
											{ IProperty 
												- _Name = "Line.LineColor";
												- _Value = "128,128,128";
												- _Type = Color;
											}
											{ IProperty 
												- _Name = "Line.LineWidth";
												- _Value = "1";
												- _Type = Int;
											}
										}
									}
									{ IPropertyMetaclass 
										- _Name = "DiagramFrame";
										- Properties = { IRPYRawContainer 
											- size = 8;
											- value = 
											{ IProperty 
												- _Name = "DefaultSize";
												- _Value = "20,20,590,500";
												- _Type = String;
											}
											{ IProperty 
												- _Name = "Fill.FillColor";
												- _Value = "247,247,247";
												- _Type = Color;
											}
											{ IProperty 
												- _Name = "Fill.Transparent_Fill";
												- _Value = "0";
												- _Type = Int;
											}
											{ IProperty 
												- _Name = "Font.Font";
												- _Value = "Tahoma";
												- _Type = String;
											}
											{ IProperty 
												- _Name = "Font.Size";
												- _Value = "8";
												- _Type = Int;
											}
											{ IProperty 
												- _Name = "Font.Weight@Child.NameCompartment@Name";
												- _Value = "700";
												- _Type = Int;
											}
											{ IProperty 
												- _Name = "Line.LineColor";
												- _Value = "194,192,192";
												- _Type = Color;
											}
											{ IProperty 
												- _Name = "Line.LineWidth";
												- _Value = "1";
												- _Type = Int;
											}
										}
									}
									{ IPropertyMetaclass 
										- _Name = "Inheritance";
										- Properties = { IRPYRawContainer 
											- size = 4;
											- value = 
											{ IProperty 
												- _Name = "Font.Font";
												- _Value = "Tahoma";
												- _Type = String;
											}
											{ IProperty 
												- _Name = "Font.Size";
												- _Value = "8";
												- _Type = Int;
											}
											{ IProperty 
												- _Name = "Line.LineColor";
												- _Value = "128,128,128";
												- _Type = Color;
											}
											{ IProperty 
												- _Name = "Line.LineWidth";
												- _Value = "1";
												- _Type = Int;
											}
										}
									}
								}
							}
						}
					}
					- _name = "metamodel diagram_3";
					- Stereotypes = { IRPYRawContainer 
						- size = 1;
						- value = 
						{ IHandle 
							- _m2Class = "IStereotype";
							- _subsystem = "MetamodelProfile";
							- _name = "Metamodel Diagram";
							- _id = GUID 29aa39b2-51f3-46a3-a5e2-2a37448756dd;
						}
					}
					- _modifiedTimeWeak = 1.2.1990::0:0:0;
					- _lastModifiedTime = "7.6.2020::9:0:41";
					- _graphicChart = { CGIClassChart 
						- _id = GUID ea35eaf7-ada6-4257-9129-eb172f822f2f;
						- m_type = 0;
						- m_pModelObject = { IHandle 
							- _m2Class = "IDiagram";
							- _id = GUID 28d34d6d-40fd-4f4c-8b14-87997f25d78e;
						}
						- m_pParent = ;
						- m_name = { CGIText 
							- m_str = "";
							- m_style = "Arial" 10 0 0 0 1 ;
							- m_color = { IColor 
								- m_fgColor = 0;
								- m_bgColor = 0;
								- m_bgFlag = 0;
							}
							- m_position = 1 0 0  ;
							- m_nIdent = 0;
							- m_bImplicitSetRectPoints = 0;
							- m_nOrientationCtrlPt = 8;
						}
						- m_drawBehavior = 0;
						- m_bIsPreferencesInitialized = 0;
						- elementList = 11;
						{ CGIClass 
							- _id = GUID 52e66c73-3f11-4422-bf42-b41d0d008999;
							- m_type = 78;
							- m_pModelObject = { IHandle 
								- _m2Class = "IClass";
								- _id = GUID b2e7f70f-f540-4a1a-859f-fbb2d7dde341;
							}
							- m_pParent = ;
							- m_name = { CGIText 
								- m_str = "TopLevel";
								- m_style = "Arial" 10 0 0 0 1 ;
								- m_color = { IColor 
									- m_fgColor = 0;
									- m_bgColor = 0;
									- m_bgFlag = 0;
								}
								- m_position = 1 0 0  ;
								- m_nIdent = 0;
								- m_bImplicitSetRectPoints = 0;
								- m_nOrientationCtrlPt = 5;
							}
							- m_drawBehavior = 0;
							- m_bIsPreferencesInitialized = 0;
							- m_AdditionalLabel = { CGIText 
								- m_str = "";
								- m_style = "Arial" 10 0 0 0 1 ;
								- m_color = { IColor 
									- m_fgColor = 0;
									- m_bgColor = 0;
									- m_bgFlag = 0;
								}
								- m_position = 1 0 0  ;
								- m_nIdent = 0;
								- m_bImplicitSetRectPoints = 0;
								- m_nOrientationCtrlPt = 1;
							}
							- m_polygon = 0 ;
							- m_nNameFormat = 0;
							- m_nIsNameFormat = 0;
							- Attrs = { IRPYRawContainer 
								- size = 0;
							}
							- Operations = { IRPYRawContainer 
								- size = 0;
							}
						}
						{ CGIDiagramFrame 
							- _id = GUID 30d15013-7976-450c-b808-50504d7240da;
							- m_type = 203;
							- m_pModelObject = { IHandle 
								- _m2Class = "";
							}
							- m_pParent = GUID 52e66c73-3f11-4422-bf42-b41d0d008999;
							- m_name = { CGIText 
								- m_str = "";
								- m_style = "Arial" 10 0 0 0 1 ;
								- m_color = { IColor 
									- m_fgColor = 0;
									- m_bgColor = 0;
									- m_bgFlag = 0;
								}
								- m_position = 1 0 0  ;
								- m_nIdent = 0;
								- m_bImplicitSetRectPoints = 0;
								- m_nOrientationCtrlPt = 8;
							}
							- m_drawBehavior = 4096;
							- m_transform = 3.59722 0 0 3.63636 20 20 ;
							- m_bIsPreferencesInitialized = 1;
							- m_AdditionalLabel = { CGIText 
								- m_str = "";
								- m_style = "Arial" 10 0 0 0 1 ;
								- m_color = { IColor 
									- m_fgColor = 0;
									- m_bgColor = 0;
									- m_bgFlag = 0;
								}
								- m_position = 1 0 0  ;
								- m_nIdent = 0;
								- m_bImplicitSetRectPoints = 0;
								- m_nOrientationCtrlPt = 1;
							}
							- m_polygon = 4 0 0  0 132  216 132  216 0  ;
							- m_nNameFormat = 0;
							- m_nIsNameFormat = 0;
						}
						{ CGIClass 
							- _id = GUID a2569d4f-1004-459c-96e5-c5d1cb29d561;
							- m_type = 87;
							- m_pModelObject = { IHandle 
								- _m2Class = "IClass";
								- _subsystem = "Base_MetaModelPkg";
								- _name = "Diagram";
								- _id = GUID d34f5f9c-66e5-4fa2-bb6a-440e3710b219;
							}
							- m_pParent = GUID 52e66c73-3f11-4422-bf42-b41d0d008999;
							- m_name = { CGIText 
								- m_str = "Diagram";
								- m_style = "Arial" 10 0 0 0 1 ;
								- m_color = { IColor 
									- m_fgColor = 0;
									- m_bgColor = 0;
									- m_bgFlag = 0;
								}
								- m_position = 1 0 0  ;
								- m_nIdent = 0;
								- m_bImplicitSetRectPoints = 0;
								- m_nOrientationCtrlPt = 5;
							}
							- m_drawBehavior = 4104;
							- m_transform = 0.0906516 0 0 0.101604 310.819 92.5722 ;
							- m_bIsPreferencesInitialized = 1;
							- m_AdditionalLabel = { CGIText 
								- m_str = "";
								- m_style = "Arial" 10 0 0 0 1 ;
								- m_color = { IColor 
									- m_fgColor = 0;
									- m_bgColor = 0;
									- m_bgFlag = 0;
								}
								- m_position = 1 0 0  ;
								- m_nIdent = 0;
								- m_bImplicitSetRectPoints = 0;
								- m_nOrientationCtrlPt = 1;
							}
							- m_polygon = 4 2 329  2 1451  1061 1451  1061 329  ;
							- m_nNameFormat = 0;
							- m_nIsNameFormat = 0;
							- frameset = "<frameset rows=33%,33%,33%>
<frame name=AttributeListCompartment>
<frame name=OperationListCompartment>
<frame name=DescriptionCompartment>";
							- Compartments = { IRPYRawContainer 
								- size = 3;
								- value = 
								{ CGICompartment 
									- _id = GUID 86669554-a379-4979-be86-ed7ff20e4a21;
									- m_name = "Attribute";
									- m_displayOption = Explicit;
									- m_bShowInherited = 0;
									- m_bOrdered = 0;
									- Items = { IRPYRawContainer 
										- size = 0;
									}
								}
								{ CGICompartment 
									- _id = GUID d1d25bfa-6d4e-4b8d-a964-1e03209080f1;
									- m_name = "Operation";
									- m_displayOption = Explicit;
									- m_bShowInherited = 0;
									- m_bOrdered = 0;
									- Items = { IRPYRawContainer 
										- size = 0;
									}
								}
								{ CGICompartment 
									- _id = GUID c1cf3f0b-d866-4c94-bd2d-ffe847f0f46a;
									- m_name = "Description";
									- m_displayOption = All;
									- m_bShowInherited = 0;
									- m_bOrdered = 0;
								}
							}
							- Attrs = { IRPYRawContainer 
								- size = 0;
							}
							- Operations = { IRPYRawContainer 
								- size = 0;
							}
						}
						{ CGIClass 
							- _id = GUID 05e5cef6-960a-46a6-95ff-2c1c1b3a1c54;
							- _properties = { IPropertyContainer 
								- Subjects = { IRPYRawContainer 
									- size = 1;
									- value = 
									{ IPropertySubject 
										- _Name = "General";
										- Metaclasses = { IRPYRawContainer 
											- size = 1;
											- value = 
											{ IPropertyMetaclass 
												- _Name = "Graphics";
												- Properties = { IRPYRawContainer 
													- size = 1;
													- value = 
													{ IProperty 
														- _Name = "FitBoxToItsTextuals";
														- _Value = "False";
														- _Type = Bool;
													}
												}
											}
										}
									}
								}
							}
							- m_type = 87;
							- m_pModelObject = { IHandle 
								- _m2Class = "IClass";
								- _subsystem = "Base_MetaModelPkg";
								- _name = "TimingDiagram";
								- _id = GUID 604642f5-8d0b-4cff-9c48-632093024776;
							}
							- m_pParent = GUID 52e66c73-3f11-4422-bf42-b41d0d008999;
							- m_name = { CGIText 
								- m_str = "TimingDiagram";
								- m_style = "Arial" 10 0 0 0 1 ;
								- m_color = { IColor 
									- m_fgColor = 0;
									- m_bgColor = 0;
									- m_bgFlag = 0;
								}
								- m_position = 1 0 0  ;
								- m_nIdent = 0;
								- m_bImplicitSetRectPoints = 0;
								- m_nOrientationCtrlPt = 5;
							}
							- m_drawBehavior = 4104;
							- m_transform = 0.1322 0 0 0.101604 80.7356 206.572 ;
							- m_bIsPreferencesInitialized = 1;
							- m_AdditionalLabel = { CGIText 
								- m_str = "";
								- m_style = "Arial" 10 0 0 0 1 ;
								- m_color = { IColor 
									- m_fgColor = 0;
									- m_bgColor = 0;
									- m_bgFlag = 0;
								}
								- m_position = 1 0 0  ;
								- m_nIdent = 0;
								- m_bImplicitSetRectPoints = 0;
								- m_nOrientationCtrlPt = 1;
							}
							- m_polygon = 4 2 329  2 1451  1061 1451  1061 329  ;
							- m_nNameFormat = 0;
							- m_nIsNameFormat = 0;
							- frameset = "<frameset rows=33%,33%,33%>
<frame name=AttributeListCompartment>
<frame name=OperationListCompartment>
<frame name=DescriptionCompartment>";
							- Compartments = { IRPYRawContainer 
								- size = 3;
								- value = 
								{ CGICompartment 
									- _id = GUID e001aab7-3040-4a2c-9275-185d0732cc2d;
									- m_name = "Attribute";
									- m_displayOption = Explicit;
									- m_bShowInherited = 0;
									- m_bOrdered = 0;
									- Items = { IRPYRawContainer 
										- size = 0;
									}
								}
								{ CGICompartment 
									- _id = GUID 6996e1c2-a990-4872-8687-4a0879d14694;
									- m_name = "Operation";
									- m_displayOption = Explicit;
									- m_bShowInherited = 0;
									- m_bOrdered = 0;
									- Items = { IRPYRawContainer 
										- size = 0;
									}
								}
								{ CGICompartment 
									- _id = GUID 5f3551e0-60f9-482c-9cbd-abb27b22a599;
									- m_name = "Description";
									- m_displayOption = All;
									- m_bShowInherited = 0;
									- m_bOrdered = 0;
								}
							}
							- Attrs = { IRPYRawContainer 
								- size = 0;
							}
							- Operations = { IRPYRawContainer 
								- size = 0;
							}
						}
						{ CGIInheritance 
							- _id = GUID 3c2d58c2-0e6f-4a85-bad2-05d8287e0e11;
							- _properties = { IPropertyContainer 
								- Subjects = { IRPYRawContainer 
									- size = 1;
									- value = 
									{ IPropertySubject 
										- _Name = "General";
										- Metaclasses = { IRPYRawContainer 
											- size = 1;
											- value = 
											{ IPropertyMetaclass 
												- _Name = "Graphics";
												- Properties = { IRPYRawContainer 
													- size = 1;
													- value = 
													{ IProperty 
														- _Name = "TreeStyle";
														- _Value = "True";
														- _Type = Bool;
													}
												}
											}
										}
									}
								}
							}
							- m_type = 103;
							- m_pModelObject = { IHandle 
								- _m2Class = "IGeneralization";
								- _subsystem = "Base_MetaModelPkg";
								- _class = "TimingDiagram";
								- _name = "Diagram";
								- _id = GUID 14893459-f905-40c5-b3d8-2db083ef165b;
							}
							- m_pParent = ;
							- m_name = { CGIText 
								- m_str = "";
								- m_style = "Arial" 10 0 0 0 1 ;
								- m_color = { IColor 
									- m_fgColor = 0;
									- m_bgColor = 0;
									- m_bgFlag = 0;
								}
								- m_position = 1 0 0  ;
								- m_nIdent = 0;
								- m_bImplicitSetRectPoints = 0;
								- m_nOrientationCtrlPt = 5;
							}
							- m_drawBehavior = 4104;
							- m_bIsPreferencesInitialized = 1;
							- m_pSource = GUID 05e5cef6-960a-46a6-95ff-2c1c1b3a1c54;
							- m_sourceType = 'F';
							- m_pTarget = GUID a2569d4f-1004-459c-96e5-c5d1cb29d561;
							- m_targetType = 'T';
							- m_direction = ' ';
							- m_rpn = { CGIText 
								- m_str = "";
								- m_style = "Arial" 10 0 0 0 1 ;
								- m_color = { IColor 
									- m_fgColor = 0;
									- m_bgColor = 0;
									- m_bgFlag = 0;
								}
								- m_position = 1 0 0  ;
								- m_nIdent = 0;
								- m_bImplicitSetRectPoints = 0;
								- m_nOrientationCtrlPt = 1;
							}
							- m_arrow = 1 359 285  ;
							- m_anglePoint1 = 0 0 ;
							- m_anglePoint2 = 0 0 ;
							- m_line_style = 2;
							- m_SourcePort = 1061 772 ;
							- m_TargetPort = 531 1451 ;
							- m_ShowName = 0;
							- m_ShowStereotype = 1;
						}
						{ CGIClass 
							- _id = GUID c5d2dcc2-4d66-4138-9478-549f5b0da541;
							- _properties = { IPropertyContainer 
								- Subjects = { IRPYRawContainer 
									- size = 1;
									- value = 
									{ IPropertySubject 
										- _Name = "General";
										- Metaclasses = { IRPYRawContainer 
											- size = 1;
											- value = 
											{ IPropertyMetaclass 
												- _Name = "Graphics";
												- Properties = { IRPYRawContainer 
													- size = 1;
													- value = 
													{ IProperty 
														- _Name = "FitBoxToItsTextuals";
														- _Value = "False";
														- _Type = Bool;
													}
												}
											}
										}
									}
								}
							}
							- m_type = 87;
							- m_pModelObject = { IHandle 
								- _m2Class = "IClass";
								- _subsystem = "Base_MetaModelPkg";
								- _name = "SequenceDiagram";
								- _id = GUID 1870f282-13c3-4753-bf26-fafd222ca618;
							}
							- m_pParent = GUID 52e66c73-3f11-4422-bf42-b41d0d008999;
							- m_name = { CGIText 
								- m_str = "SequenceDiagram";
								- m_style = "Arial" 10 0 0 0 1 ;
								- m_color = { IColor 
									- m_fgColor = 0;
									- m_bgColor = 0;
									- m_bgFlag = 0;
								}
								- m_position = 1 0 0  ;
								- m_nIdent = 0;
								- m_bImplicitSetRectPoints = 0;
								- m_nOrientationCtrlPt = 5;
							}
							- m_drawBehavior = 4110;
							- m_transform = 0.146364 0 0 0.101604 400.707 297.572 ;
							- m_bIsPreferencesInitialized = 1;
							- m_AdditionalLabel = { CGIText 
								- m_str = "";
								- m_style = "Arial" 10 0 0 0 1 ;
								- m_color = { IColor 
									- m_fgColor = 0;
									- m_bgColor = 0;
									- m_bgFlag = 0;
								}
								- m_position = 1 0 0  ;
								- m_nIdent = 0;
								- m_bImplicitSetRectPoints = 0;
								- m_nOrientationCtrlPt = 1;
							}
							- m_polygon = 4 2 329  2 1451  1061 1451  1061 329  ;
							- m_nNameFormat = 0;
							- m_nIsNameFormat = 0;
							- frameset = "<frameset rows=100%>
<frame name=DescriptionCompartment>";
							- Compartments = { IRPYRawContainer 
								- size = 1;
								- value = 
								{ CGICompartment 
									- _id = GUID 79fd78f3-171f-4d52-97f6-52144ffdabcc;
									- m_name = "Description";
									- m_displayOption = All;
									- m_bShowInherited = 0;
									- m_bOrdered = 0;
								}
							}
							- Attrs = { IRPYRawContainer 
								- size = 0;
							}
							- Operations = { IRPYRawContainer 
								- size = 0;
							}
						}
						{ CGIInheritance 
							- _id = GUID cead1cdc-940d-4660-bd5d-c0ee924d0223;
							- _properties = { IPropertyContainer 
								- Subjects = { IRPYRawContainer 
									- size = 1;
									- value = 
									{ IPropertySubject 
										- _Name = "General";
										- Metaclasses = { IRPYRawContainer 
											- size = 1;
											- value = 
											{ IPropertyMetaclass 
												- _Name = "Graphics";
												- Properties = { IRPYRawContainer 
													- size = 1;
													- value = 
													{ IProperty 
														- _Name = "TreeStyle";
														- _Value = "True";
														- _Type = Bool;
													}
												}
											}
										}
									}
								}
							}
							- m_type = 103;
							- m_pModelObject = { IHandle 
								- _m2Class = "IGeneralization";
								- _subsystem = "Base_MetaModelPkg";
								- _class = "SequenceDiagram";
								- _name = "Diagram";
								- _id = GUID 3277e880-8a5f-4c23-8b28-d2a37b6d4835;
							}
							- m_pParent = ;
							- m_name = { CGIText 
								- m_str = "";
								- m_style = "Arial" 10 0 0 0 1 ;
								- m_color = { IColor 
									- m_fgColor = 0;
									- m_bgColor = 0;
									- m_bgFlag = 0;
								}
								- m_position = 1 0 0  ;
								- m_nIdent = 0;
								- m_bImplicitSetRectPoints = 0;
								- m_nOrientationCtrlPt = 7;
							}
							- m_drawBehavior = 4104;
							- m_bIsPreferencesInitialized = 1;
							- m_pSource = GUID c5d2dcc2-4d66-4138-9478-549f5b0da541;
							- m_sourceType = 'F';
							- m_pTarget = GUID a2569d4f-1004-459c-96e5-c5d1cb29d561;
							- m_targetType = 'T';
							- m_direction = ' ';
							- m_rpn = { CGIText 
								- m_str = "";
								- m_style = "Arial" 10 0 0 0 1 ;
								- m_color = { IColor 
									- m_fgColor = 0;
									- m_bgColor = 0;
									- m_bgFlag = 0;
								}
								- m_position = 1 0 0  ;
								- m_nIdent = 0;
								- m_bImplicitSetRectPoints = 0;
								- m_nOrientationCtrlPt = 7;
							}
							- m_arrow = 2 455 285  359 285  ;
							- m_anglePoint1 = 0 0 ;
							- m_anglePoint2 = 0 0 ;
							- m_line_style = 2;
							- m_SourcePort = 371 329 ;
							- m_TargetPort = 531 1451 ;
							- m_ShowName = 0;
							- m_ShowStereotype = 1;
						}
						{ CGIClass 
							- _id = GUID 1163c1b2-eb17-46c9-98a6-3c40731d6fdb;
							- _properties = { IPropertyContainer 
								- Subjects = { IRPYRawContainer 
									- size = 1;
									- value = 
									{ IPropertySubject 
										- _Name = "General";
										- Metaclasses = { IRPYRawContainer 
											- size = 1;
											- value = 
											{ IPropertyMetaclass 
												- _Name = "Graphics";
												- Properties = { IRPYRawContainer 
													- size = 1;
													- value = 
													{ IProperty 
														- _Name = "FitBoxToItsTextuals";
														- _Value = "False";
														- _Type = Bool;
													}
												}
											}
										}
									}
								}
							}
							- m_type = 87;
							- m_pModelObject = { IHandle 
								- _m2Class = "IClass";
								- _id = GUID 0fbbe8a1-c45d-4380-bdbf-f962ce64e3d6;
							}
							- m_pParent = GUID 52e66c73-3f11-4422-bf42-b41d0d008999;
							- m_name = { CGIText 
								- m_str = "StateInvariant";
								- m_style = "Arial" 10 0 0 0 1 ;
								- m_color = { IColor 
									- m_fgColor = 0;
									- m_bgColor = 0;
									- m_bgFlag = 0;
								}
								- m_position = 1 0 0  ;
								- m_nIdent = 0;
								- m_bImplicitSetRectPoints = 0;
								- m_nOrientationCtrlPt = 5;
							}
							- m_drawBehavior = 4110;
							- m_transform = 0.1322 0 0 0.101604 644.736 64.5722 ;
							- m_bIsPreferencesInitialized = 1;
							- m_AdditionalLabel = { CGIText 
								- m_str = "";
								- m_style = "Arial" 10 0 0 0 1 ;
								- m_color = { IColor 
									- m_fgColor = 0;
									- m_bgColor = 0;
									- m_bgFlag = 0;
								}
								- m_position = 1 0 0  ;
								- m_nIdent = 0;
								- m_bImplicitSetRectPoints = 0;
								- m_nOrientationCtrlPt = 1;
							}
							- m_polygon = 4 2 329  2 1451  1061 1451  1061 329  ;
							- m_nNameFormat = 0;
							- m_nIsNameFormat = 0;
							- frameset = "<frameset rows=100%>
<frame name=DescriptionCompartment>";
							- Compartments = { IRPYRawContainer 
								- size = 1;
								- value = 
								{ CGICompartment 
									- _id = GUID 336f4f88-420d-4bdd-9583-4f2e01911963;
									- m_name = "Description";
									- m_displayOption = All;
									- m_bShowInherited = 0;
									- m_bOrdered = 0;
								}
							}
							- Attrs = { IRPYRawContainer 
								- size = 0;
							}
							- Operations = { IRPYRawContainer 
								- size = 0;
							}
						}
						{ CGIClass 
							- _id = GUID f766ade7-1b48-4c2b-af9d-cf8b59721d42;
							- m_type = 87;
							- m_pModelObject = { IHandle 
								- _m2Class = "IClass";
								- _subsystem = "Base_MetaModelPkg";
								- _name = "ModelElement";
								- _id = GUID 3e418e73-25a6-4c58-863e-05da06118993;
							}
							- m_pParent = GUID 52e66c73-3f11-4422-bf42-b41d0d008999;
							- m_name = { CGIText 
								- m_str = "ModelElement";
								- m_style = "Arial" 10 0 0 0 1 ;
								- m_color = { IColor 
									- m_fgColor = 0;
									- m_bgColor = 0;
									- m_bgFlag = 0;
								}
								- m_position = 1 0 0  ;
								- m_nIdent = 0;
								- m_bImplicitSetRectPoints = 0;
								- m_nOrientationCtrlPt = 5;
							}
							- m_drawBehavior = 4110;
							- m_transform = 0.0906516 0 0 0.101604 690.819 278.572 ;
							- m_bIsPreferencesInitialized = 1;
							- m_AdditionalLabel = { CGIText 
								- m_str = "";
								- m_style = "Arial" 10 0 0 0 1 ;
								- m_color = { IColor 
									- m_fgColor = 0;
									- m_bgColor = 0;
									- m_bgFlag = 0;
								}
								- m_position = 1 0 0  ;
								- m_nIdent = 0;
								- m_bImplicitSetRectPoints = 0;
								- m_nOrientationCtrlPt = 1;
							}
							- m_polygon = 4 2 329  2 1451  1061 1451  1061 329  ;
							- m_nNameFormat = 0;
							- m_nIsNameFormat = 0;
							- frameset = "<frameset rows=100%>
<frame name=DescriptionCompartment>";
							- Compartments = { IRPYRawContainer 
								- size = 1;
								- value = 
								{ CGICompartment 
									- _id = GUID 2806a69a-95ce-4d89-9b73-71ddc8e41030;
									- m_name = "Description";
									- m_displayOption = All;
									- m_bShowInherited = 0;
									- m_bOrdered = 0;
								}
							}
							- Attrs = { IRPYRawContainer 
								- size = 0;
							}
							- Operations = { IRPYRawContainer 
								- size = 0;
							}
						}
						{ CGIInheritance 
							- _id = GUID 9eb619d9-c9e8-4801-896a-08fcf797e80f;
							- _properties = { IPropertyContainer 
								- Subjects = { IRPYRawContainer 
									- size = 1;
									- value = 
									{ IPropertySubject 
										- _Name = "General";
										- Metaclasses = { IRPYRawContainer 
											- size = 1;
											- value = 
											{ IPropertyMetaclass 
												- _Name = "Graphics";
												- Properties = { IRPYRawContainer 
													- size = 1;
													- value = 
													{ IProperty 
														- _Name = "TreeStyle";
														- _Value = "True";
														- _Type = Bool;
													}
												}
											}
										}
									}
								}
							}
							- m_type = 103;
							- m_pModelObject = { IHandle 
								- _m2Class = "IGeneralization";
								- _subsystem = "Base_MetaModelPkg";
								- _class = "Diagram";
								- _name = "ModelElement";
								- _id = GUID 72b9bc72-41cf-4482-89bd-9892e5c29ca3;
							}
							- m_pParent = ;
							- m_name = { CGIText 
								- m_str = "";
								- m_style = "Arial" 10 0 0 0 1 ;
								- m_color = { IColor 
									- m_fgColor = 0;
									- m_bgColor = 0;
									- m_bgFlag = 0;
								}
								- m_position = 1 0 0  ;
								- m_nIdent = 0;
								- m_bImplicitSetRectPoints = 0;
								- m_nOrientationCtrlPt = 7;
							}
							- m_drawBehavior = 4104;
							- m_bIsPreferencesInitialized = 0;
							- m_pSource = GUID a2569d4f-1004-459c-96e5-c5d1cb29d561;
							- m_sourceType = 'F';
							- m_pTarget = GUID f766ade7-1b48-4c2b-af9d-cf8b59721d42;
							- m_targetType = 'T';
							- m_direction = ' ';
							- m_rpn = { CGIText 
								- m_str = "";
								- m_style = "Arial" 10 0 0 0 1 ;
								- m_color = { IColor 
									- m_fgColor = 0;
									- m_bgColor = 0;
									- m_bgFlag = 0;
								}
								- m_position = 1 0 0  ;
								- m_nIdent = 0;
								- m_bImplicitSetRectPoints = 0;
								- m_nOrientationCtrlPt = 7;
							}
							- m_arrow = 2 359 276  744 276  ;
							- m_anglePoint1 = 0 0 ;
							- m_anglePoint2 = 0 0 ;
							- m_line_style = 2;
							- m_SourcePort = 531 1451 ;
							- m_TargetPort = 587 329 ;
							- m_ShowName = 0;
							- m_ShowStereotype = 1;
						}
						{ CGIInheritance 
							- _id = GUID d4240159-b0d5-4fd6-952d-51ca6f0e1129;
							- _properties = { IPropertyContainer 
								- Subjects = { IRPYRawContainer 
									- size = 1;
									- value = 
									{ IPropertySubject 
										- _Name = "General";
										- Metaclasses = { IRPYRawContainer 
											- size = 1;
											- value = 
											{ IPropertyMetaclass 
												- _Name = "Graphics";
												- Properties = { IRPYRawContainer 
													- size = 1;
													- value = 
													{ IProperty 
														- _Name = "TreeStyle";
														- _Value = "True";
														- _Type = Bool;
													}
												}
											}
										}
									}
								}
							}
							- m_type = 103;
							- m_pModelObject = { IHandle 
								- _m2Class = "IGeneralization";
								- _id = GUID aef4af18-005e-422e-a539-267d86331094;
							}
							- m_pParent = ;
							- m_name = { CGIText 
								- m_str = "";
								- m_style = "Arial" 10 0 0 0 1 ;
								- m_color = { IColor 
									- m_fgColor = 0;
									- m_bgColor = 0;
									- m_bgFlag = 0;
								}
								- m_position = 1 0 0  ;
								- m_nIdent = 0;
								- m_bImplicitSetRectPoints = 0;
								- m_nOrientationCtrlPt = 7;
							}
							- m_drawBehavior = 4104;
							- m_bIsPreferencesInitialized = 1;
							- m_pSource = GUID 1163c1b2-eb17-46c9-98a6-3c40731d6fdb;
							- m_sourceType = 'F';
							- m_pTarget = GUID f766ade7-1b48-4c2b-af9d-cf8b59721d42;
							- m_targetType = 'T';
							- m_direction = ' ';
							- m_rpn = { CGIText 
								- m_str = "";
								- m_style = "Arial" 10 0 0 0 1 ;
								- m_color = { IColor 
									- m_fgColor = 0;
									- m_bgColor = 0;
									- m_bgFlag = 0;
								}
								- m_position = 1 0 0  ;
								- m_nIdent = 0;
								- m_bImplicitSetRectPoints = 0;
								- m_nOrientationCtrlPt = 7;
							}
							- m_arrow = 2 722 276  744 276  ;
							- m_anglePoint1 = 0 0 ;
							- m_anglePoint2 = 0 0 ;
							- m_line_style = 2;
							- m_SourcePort = 584 1451 ;
							- m_TargetPort = 587 329 ;
							- m_ShowName = 0;
							- m_ShowStereotype = 1;
						}
						
						- m_access = 'Z';
						- m_modified = 'N';
						- m_fileVersion = "";
						- m_nModifyDate = 0;
						- m_nCreateDate = 0;
						- m_creator = "";
						- m_bScaleWithZoom = 1;
						- m_arrowStyle = 'S';
						- m_pRoot = GUID 52e66c73-3f11-4422-bf42-b41d0d008999;
						- m_currentLeftTop = 0 0 ;
						- m_currentRightBottom = 0 0 ;
						- m_bFreezeCompartmentContent = 0;
					}
					- _defaultSubsystem = { IHandle 
						- _m2Class = "ISubsystem";
						- _id = GUID 452616f3-bfae-409f-a859-c0df3f0206e8;
					}
				}
			}
			- weakCGTime = 7.6.2020::9:0:41;
			- strongCGTime = 7.3.2020::11:23:46;
			- _defaultComposite = GUID b2e7f70f-f540-4a1a-859f-fbb2d7dde341;
			- _eventsBaseID = -1;
			- Classes = { IRPYRawContainer 
				- size = 2;
				- value = 
				{ IClass 
					- _id = GUID b2e7f70f-f540-4a1a-859f-fbb2d7dde341;
					- _myState = 40960;
					- _name = "TopLevel";
					- _modifiedTimeWeak = 1.2.1990::0:0:0;
					- weakCGTime = 1.2.1990::0:0:0;
					- strongCGTime = 1.2.1990::0:0:0;
					- _multiplicity = "";
					- _itsStateChart = { IHandle 
						- _m2Class = "";
					}
					- _classModifier = Unspecified;
				}
				{ IClass 
					- _id = GUID 0fbbe8a1-c45d-4380-bdbf-f962ce64e3d6;
					- _myState = 8192;
					- _name = "StateInvariant";
					- Stereotypes = { IRPYRawContainer 
						- size = 1;
						- value = 
						{ IHandle 
							- _m2Class = "IStereotype";
							- _subsystem = "MetamodelProfile";
							- _name = "UML Element";
							- _id = GUID c546edaf-0940-491b-9aec-a3d87afcaf95;
						}
					}
					- _modifiedTimeWeak = 7.3.2020::14:52:51;
					- _theMainDiagram = { IHandle 
						- _m2Class = "IDiagram";
						- _id = GUID 28d34d6d-40fd-4f4c-8b14-87997f25d78e;
					}
					- weakCGTime = 7.3.2020::14:52:51;
					- strongCGTime = 7.3.2020::14:52:27;
					- Inheritances = { IRPYRawContainer 
						- size = 1;
						- value = 
						{ IGeneralization 
							- _id = GUID aef4af18-005e-422e-a539-267d86331094;
							- _modifiedTimeWeak = 1.2.1990::0:0:0;
							- _dependsOn = { INObjectHandle 
								- _m2Class = "IClass";
								- _subsystem = "Base_MetaModelPkg";
								- _name = "ModelElement";
								- _id = GUID 3e418e73-25a6-4c58-863e-05da06118993;
							}
							- _inheritanceType = iPublic;
							- _isVirtual = 0;
						}
					}
					- _multiplicity = "";
					- _itsStateChart = { IHandle 
						- _m2Class = "";
					}
					- _classModifier = Unspecified;
				}
			}
			- _configurationRelatedTime = 7.6.2020::9:0:41;
		}
		{ ISubsystem 
			- fileName = "TimingDiagramPkg";
			- _id = GUID f13cfe7b-f79f-445d-a867-2ea999723c42;
		}
	}
	- Diagrams = { IRPYRawContainer 
		- size = 0;
	}
	- Components = { IRPYRawContainer 
		- size = 1;
		- value = 
		{ IComponent 
			- _id = GUID ac5be808-8ba4-4b6d-b9cb-345bd0fe0eff;
			- _myState = 139264;
			- _name = "NotUsedCmp";
			- _modifiedTimeWeak = 4.4.2020::11:3:12;
			- m_buildType = Executable;
			- m_libraries = "";
			- m_additionalSources = "";
			- m_standardHeaders = "";
			- m_includePath = "";
			- m_initializationCode = "";
			- m_folder = { IFolder 
				- _id = GUID 64d58ea2-10f1-4bb3-8e55-eb9a4f5dfd31;
				- _myState = 73728;
				- _name = "Files";
				- _modifiedTimeWeak = 4.4.2020::7:25:24;
				- m_path = "";
				- Elements = { IRPYRawContainer 
					- size = 0;
				}
				- Files = { IRPYRawContainer 
					- size = 0;
				}
			}
			- m_configActive = { ICodeGenConfigInfoHandle 
				- _m2Class = "ICodeGenConfigInfo";
				- _id = GUID 20e98e1b-dd3b-462d-82bf-06b60ea7bfec;
			}
			- Configs = { IRPYRawContainer 
				- size = 1;
				- value = 
				{ ICodeGenConfigInfo 
					- _id = GUID 20e98e1b-dd3b-462d-82bf-06b60ea7bfec;
					- _myState = 8192;
					- _name = "NotUsedCfg";
					- _modifiedTimeWeak = 4.4.2020::11:3:12;
					- HyperLinks = { IRPYRawContainer 
						- size = 2;
						- value = 
						{ IMHyperLink 
							- _id = GUID ea0828e2-9341-40f7-b601-1733236e855d;
							- _modifiedTimeWeak = 4.4.2020::11:3:12;
							- _dependsOn = { INObjectHandle 
								- _m2Class = "";
							}
							- _linkDispName = "Edit Makefile";
							- _linkType = "FREE";
							- _linkTarget = "NotUsedCmp.mak";
							- isConfigurationFileHyperLink = 1;
						}
						{ IMHyperLink 
							- _id = GUID ab302dec-d228-4471-bfa1-02777a8245e7;
							- _modifiedTimeWeak = 4.4.2020::11:3:12;
							- _dependsOn = { INObjectHandle 
								- _m2Class = "";
							}
							- _linkDispName = "Edit Main File";
							- _linkType = "FREE";
							- _linkTarget = "MainNotUsedCmp";
							- isConfigurationFileHyperLink = 1;
						}
					}
					- _scopeType = Explicit;
					- _libraries = "";
					- _additionalSources = "";
					- _standardHeaders = "";
					- _includePath = "";
					- _targetMain = "";
					- _instrumentation = Animation;
					- _timeModel = RealTime;
					- m_generateActors = 1;
					- _statechartImplementation = Flat;
					- _initializationCode = "";
					- _checksList = 0 ;
					- ScopeElements = { IRPYRawContainer 
						- size = 0;
					}
					- InitialInstances = { IRPYRawContainer 
						- size = 0;
					}
					- _root = { IFolder 
						- _id = GUID 9e2f4714-17bb-44d2-a180-fc6c9b578ff3;
						- _myState = 8192;
						- _name = "NotUsedCfg";
						- _modifiedTimeWeak = 4.4.2020::7:25:47;
						- m_path = "";
						- Elements = { IRPYRawContainer 
							- size = 0;
						}
						- Files = { IRPYRawContainer 
							- size = 0;
						}
					}
					- weakCGTime = 4.4.2020::11:3:12;
					- strongCGTime = 4.4.2020::7:25:47;
					- AnimScopeElements = { IRPYRawContainer 
						- size = 0;
					}
					- m_allInAnimScope = 1;
					- m_generateUsecases = 0;
				}
			}
			- HandlesInMe = { IRPYRawContainer 
				- size = 3;
				- value = 
				{ IHandle 
					- _m2Class = "IProfile";
					- _name = "MetamodelProfile";
					- _id = GUID d2ddb420-4d4b-4832-bf58-e291ae395548;
				}
				{ IHandle 
					- _m2Class = "ISubsystem";
					- _name = "Base_MetaModelPkg";
					- _id = GUID 66ce32f5-6908-43e4-bcd2-5191392e805b;
				}
				{ IHandle 
					- _m2Class = "ISubsystem";
					- _name = "TimingDiagramPkg";
					- _id = GUID f13cfe7b-f79f-445d-a867-2ea999723c42;
				}
			}
			- VariationPoints = { IRPYRawContainer 
				- size = 0;
			}
			- SelectedVariants = { IRPYRawContainer 
				- size = 0;
			}
			- weakCGTime = 4.4.2020::11:3:12;
			- strongCGTime = 4.4.2020::7:25:37;
		}
	}
}

