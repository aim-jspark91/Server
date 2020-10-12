package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class RecipeRelationLastActiveVer extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="parentMachine", type="Key", dataType="String", initial="", history="")
	private String parentMachine;
	
	@CTORMTemplate(seq = "2", name="parentMachineRecipe", type="Key", dataType="String", initial="", history="")
	private String parentMachineRecipe;
	
	@CTORMTemplate(seq = "3", name="childMachine", type="Key", dataType="String", initial="", history="")
	private String childMachine;
	
	@CTORMTemplate(seq = "4", name="childMachineRecipe", type="Key", dataType="String", initial="", history="")
	private String childMachineRecipe;
	
	@CTORMTemplate(seq = "5", name = "lastActiveTimeKey", type = "Column", dataType = "String", initial = "", history = "")
	private String lastActiveTimeKey;
	

	//instantiation
	public RecipeRelationLastActiveVer()
	{
		
	}

	public RecipeRelationLastActiveVer(String parentMachine, String parentMachineRecipe,
			String childMachine, String childMachineRecipe) {
		super();
		this.parentMachine = parentMachine;
		this.parentMachineRecipe = parentMachineRecipe;
		this.childMachine = childMachine;
		this.childMachineRecipe = childMachineRecipe;
	}

	public String getParentMachine() {
		return parentMachine;
	}

	public void setParentMachine(String parentMachine) {
		this.parentMachine = parentMachine;
	}

	public String getParentMachineRecipe() {
		return parentMachineRecipe;
	}

	public void setParentMachineRecipe(String parentMachineRecipe) {
		this.parentMachineRecipe = parentMachineRecipe;
	}

	public String getChildMachine() {
		return childMachine;
	}

	public void setChildMachine(String childMachine) {
		this.childMachine = childMachine;
	}

	public String getChildMachineRecipe() {
		return childMachineRecipe;
	}

	public void setChildMachineRecipe(String childMachineRecipe) {
		this.childMachineRecipe = childMachineRecipe;
	}

	public String getLastActiveTimeKey() {
		return lastActiveTimeKey;
	}

	public void setLastActiveTimeKey(String lastActiveTimeKey) {
		this.lastActiveTimeKey = lastActiveTimeKey;
	}

	
	
}
