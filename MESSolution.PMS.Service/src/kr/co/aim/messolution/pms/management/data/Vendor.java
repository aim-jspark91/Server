package kr.co.aim.messolution.pms.management.data;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "PMS", divider="_")
public class Vendor extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="vendorID", type="Key", dataType="String", initial="", history="")
	private String vendorID;

	@CTORMTemplate(seq = "2", name="vendorName", type="Column", dataType="String", initial="", history="")
	private String vendorName;
	
	@CTORMTemplate(seq = "3", name="telephone", type="Column", dataType="String", initial="", history="")
	private String telephone;
	
	@CTORMTemplate(seq = "4", name="mobile", type="Column", dataType="String", initial="", history="")
	private String mobile;
		
	public Vendor()
	{
		
	}
	
	public Vendor(String vendorID)
	{
		setVendorID(vendorID);
	}
	
	public String getVendorID() {
		return vendorID;
	}

	public void setVendorID(String vendorID) {
		this.vendorID = vendorID;
	}

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
}
