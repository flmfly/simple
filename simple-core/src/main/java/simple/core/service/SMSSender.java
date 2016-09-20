package simple.core.service;

import java.util.Set;

import simple.core.exception.SMSSendFailedException;

public interface SMSSender {

	public String send(String mobile, String message)
			throws SMSSendFailedException;

	public String bulkSend(Set<String> mobileSet, String message)
			throws SMSSendFailedException;

}
