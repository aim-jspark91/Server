package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class FirstGlassLogDetail extends UdfAccessor {
	@CTORMTemplate(seq = "1", name="dataType", type="Key", dataType="String", initial="", history="")
	private String dataType;
	
	@CTORMTemplate(seq = "2", name="dataId", type="Key", dataType="String", initial="", history="")
	private String dataId;
	
	@CTORMTemplate(seq = "3", name="key", type="Key", dataType="String", initial="", history="")
	private String key;
	
	@CTORMTemplate(seq = "4", name="value", type="Column", dataType="String", initial="", history="")
	private String value;

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
