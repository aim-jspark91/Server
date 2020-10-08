package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class BulletinBoardArea extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="factoryName", type="Key", dataType="String", initial="", history="")
	private String factoryName;
	
	@CTORMTemplate(seq = "2", name="no", type="Key", dataType="String", initial="", history="")
	private String no;
	
	@CTORMTemplate(seq = "3", name="areaName", type="Column", dataType="String", initial="", history="")
	private String areaName;
	
	public BulletinBoardArea()
	{
		
	}

	public BulletinBoardArea(String factoryName, String no)
	{
		setFactoryName(factoryName);
		setNo(no);
	}

	public String getFactoryName() {
		return factoryName;
	}

	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}

	public String getNo() {
		return no;
	}

	public void setNo(String no) {
		this.no = no;
	}

	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}
	
}
