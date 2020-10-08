package kr.co.aim.messolution.pms.management.data;
import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "PMS", divider="_")
public class UserGradeFunction extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="stateType", type="Key", dataType="String", initial="", history="")
	private String stateType;

	@CTORMTemplate(seq = "2", name="stateCode", type="Key", dataType="String", initial="", history="")
	private String stateCode;
	
	@CTORMTemplate(seq = "3", name="rank", type="Key", dataType="String", initial="", history="")
	private String rank;
		
	@CTORMTemplate(seq = "4", name="dept", type="Key", dataType="String", initial="", history="")
	private String dept;
	
	@CTORMTemplate(seq = "5", name="userID", type="Key", dataType="String", initial="", history="")
	private String userID;
		
	@CTORMTemplate(seq = "6", name="functionName", type="Key", dataType="String", initial="", history="")
	private String functionName;
	
	public UserGradeFunction()
	{
		
	}
	
	public UserGradeFunction(String stateType, String stateCode, String rank, String dept, String userID, String functionname)
	{
		this.setStateType(stateType);
		this.setStateCode(stateCode);
		this.setRank(rank);
		this.setDept(dept);
		this.setUserID(userID);
		this.setFunctionName(functionname);
	}
	
	public String getStateType() {
		return stateType;
	}

	public void setStateType(String stateType) {
		this.stateType = stateType;
	}

	public String getStateCode() {
		return stateCode;
	}

	public void setStateCode(String stateCode) {
		this.stateCode = stateCode;
	}

	public String getRank() {
		return rank;
	}

	public void setRank(String rank) {
		this.rank = rank;
	}

	public String getDept() {
		return dept;
	}

	public void setDept(String dept) {
		this.dept = dept;
	}
	
	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}
	
}
