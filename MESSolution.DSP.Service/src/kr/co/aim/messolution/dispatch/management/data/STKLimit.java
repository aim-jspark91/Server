package kr.co.aim.messolution.dispatch.management.data;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "CT", divider="_")
public class STKLimit extends UdfAccessor {
	@CTORMTemplate(seq = "1", name="MachineName", type="Key", dataType="String", initial="", history="")
	private String MachineName;
	
	@CTORMTemplate(seq = "2", name="PortName", type="Key", dataType="String", initial="", history="")
	private String PortName;
	
	@CTORMTemplate(seq = "3", name="StockerName", type="Key", dataType="String", initial="", history="")
	private String StockerName;

	public String getMachineName() {
		return MachineName;
	}

	public void setMachineName(String machineName) {
		MachineName = machineName;
	}

	public String getPortName() {
		return PortName;
	}

	public void setPortName(String portName) {
		PortName = portName;
	}

	public String getStockerName() {
		return StockerName;
	}

	public void setStockerName(String stockerName) {
		StockerName = stockerName;
	}
	
	
}
