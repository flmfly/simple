package simple.notice;


/**
*
*/
public abstract class NoticeCallback {

	private  String observer;

	public String getObserver() {
		return observer;
	}


	public void setObserver(String observer) {
		this.observer = observer;
	}


	public abstract void notify(NoticeMessage message);
	
	
	
	
}
