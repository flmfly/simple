package simple.scheduler.quartz.service;

import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.Email;
import org.springframework.context.ApplicationContext;

import simple.notice.NoticeCallback;
import simple.notice.NoticeMessage;


/**
 * 
 */
public class MailNoticeService extends NoticeCallback {
	

	public static final Log log = LogFactory.getLog(MailNoticeService.class);

	public static final String NEWLINE = "\r\n\r\n";
	
	private ApplicationContext applicationContext;
	
	public MailNoticeService(ApplicationContext applicationContext){
		this.applicationContext = applicationContext;
	}
//	private EmailService emailService = (EmailService) SpringContextHelper.getBean("emailService");
	 
	
	public static String formatConsumeTimeMillis(long millis) {  
	    long days = millis / (1000 * 60 * 60 * 24);  
	    long hours = (millis % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);  
	    long minutes = (millis % (1000 * 60 * 60)) / (1000 * 60);  
	    double seconds = (millis % (1000 * 60)) / 1000.0;  
	    return days + " dats " + hours + " hours " + minutes + " mins "  
	            + seconds + " seconds";
	}  
	
	@Override
	public void notify(NoticeMessage message) {
		try {
			Email email = applicationContext.getBean(EmailService.class).getMultiPartEmail();
			email.getMailSession().getProperties().setProperty("mail.smtp.sendpartial", "true");
			StringBuilder theme = new StringBuilder();
			theme.append("[系统任务调度");
			theme.append("<");
			theme.append(message.getName());
			theme.append(">");
			if(message.hasError()){
				theme.append("(异常)");
			}
			if (message.isDone()) {
					theme.append("(完成)");
			}else{
				theme.append("(超时)");
			}
			theme.append("通知]");
			
			email.setSubject(theme.toString());
			
			StringBuilder content = new StringBuilder();
			content.append("任务名称:").append(message.getName()).append(NEWLINE);
			content.append("开始时间:").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(message.getStart())).append(NEWLINE);
			content.append("总耗时:");
			content.append(formatConsumeTimeMillis(message.getConsumeTimeMillis())).append(NEWLINE);
			
			content.append("执行项总数:").append(message.getTotal()).append(NEWLINE);
			content.append("完成数:").append(message.getProcessed()).append(NEWLINE);
			content.append("成功数:").append(message.getSuccess()).append(NEWLINE);
			content.append("失败数:").append(message.getFailure()).append(NEWLINE);
			if(message.hasError()){
				content.append("异常消息:").append(message.getFailureMessage()).append(NEWLINE);
			}
			
			email.setMsg(content.toString());
			
			String[] toEmail = StringUtils.replace(super.getObserver(), " ", "").split(";");
			
			for (String addr : toEmail) {
				if(StringUtils.isBlank(addr)){
					continue;
				}
				email.addTo(addr);
			}
			email.send();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}
