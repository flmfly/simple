package simple.notice;

import java.util.Date;

/**
 */
public class NoticeMessage {


	private final Date start;

	private Date end;

	private String name;

	private long consumeTimeMillis;

	/**总数.*/
	private int total;

	private int processed;

	private int success;

	private int failure;


	private StringBuilder failureMessage = new StringBuilder();

	
	/**是否完成.*/
	private boolean done = false;
	
	public NoticeMessage() {
		start = new Date();
	}

	public boolean hasError() {
		return failure > 0;
	}

	public void plusSuccess() {
		success++;
		plusProcessed();
	}

	public void plusFailure() {
		failure++;
		plusProcessed();
	}

	private void plusProcessed() {
		processed++;
	}

	public StringBuilder appendFailureMessage(String message) {
		return failureMessage.append(message);
	}

	public long getConsumeTimeMillis() {
		return consumeTimeMillis;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
		consumeTimeMillis = (end.getTime() - start.getTime());
	}

	public Date getStart() {
		return start;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getProcessed() {
		return processed;
	}

	public void setProcessed(int processed) {
		this.processed = processed;
	}

	public int getSuccess() {
		return success;
	}

	public int getFailure() {
		return failure;
	}

	public String getFailureMessage() {
		return failureMessage.toString();
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

}
