package kr.co.aim.messolution.dispatch.management.data;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "CT", divider="_")
public class MaxQTime_SubOperationList extends UdfAccessor {
	@CTORMTemplate(seq = "1", name="RelationId", type="Key", dataType="String", initial="", history="")
	private String RelationId;
	
	@CTORMTemplate(seq = "2", name="ProcessOperationName", type="Key", dataType="String", initial="", history="")
	private String ProcessOperationName;

	public String getRelationId() {
		return RelationId;
	}

	public void setRelationId(String relationId) {
		RelationId = relationId;
	}

	public String getProcessOperationName() {
		return ProcessOperationName;
	}

	public void setProcessOperationName(String processOperationName) {
		ProcessOperationName = processOperationName;
	}
	
	
}
