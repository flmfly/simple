package simple.core.service;

import org.springframework.stereotype.Service;

import simple.config.annotation.SMSSupport;
import simple.core.exception.SMSSendFailedException;
import simple.core.util.MD5Utils;

@Service("CoreMessageService")
public class MessageService extends HandlerService {

	public String operateSMS(String domainName, Request request)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, SMSSendFailedException {
		SMSSupport smsSupport = super.annotionService.getSMSSupport(domainName);
		return ((SMSSender) super.fetchHandler(Class.forName(smsSupport
				.sender()))).send(request.mobile,
				MD5Utils.convertMD5(request.message));
	}

	public class Request {
		public String mobile;
		public String message;
	}
}
