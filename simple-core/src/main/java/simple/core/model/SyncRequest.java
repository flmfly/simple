package simple.core.model;

import java.util.Date;
import java.util.Map;

public class SyncRequest {

	private Date lastUpdateTime;

	private String fingerprint;

	private Map<?, ?> by;

	private String func;

	public Date getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(Date lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public String getFingerprint() {
		return fingerprint;
	}

	public void setFingerprint(String fingerprint) {
		this.fingerprint = fingerprint;
	}

	public Map<?, ?> getBy() {
		return by;
	}

	public void setBy(Map<?, ?> by) {
		this.by = by;
	}

	public String getFunc() {
		return func;
	}

	public void setFunc(String func) {
		this.func = func;
	}

}
