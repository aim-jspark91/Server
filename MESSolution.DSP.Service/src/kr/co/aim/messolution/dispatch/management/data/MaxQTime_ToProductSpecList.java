package kr.co.aim.messolution.dispatch.management.data;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "CT", divider="_")
public class MaxQTime_ToProductSpecList extends UdfAccessor {
	@CTORMTemplate(seq = "1", name="RelationId", type="Key", dataType="String", initial="", history="")
	private String RelationId;
	
	@CTORMTemplate(seq = "2", name="ProductSpecName", type="Key", dataType="String", initial="", history="")
	private String ProductSpecName;

	public String getRelationId() {
		return RelationId;
	}

	public void setRelationId(String relationId) {
		RelationId = relationId;
	}

	public String getProductSpecName() {
		return ProductSpecName;
	}

	public void setProductSpecName(String productSpecName) {
		ProductSpecName = productSpecName;
	}
	
}
