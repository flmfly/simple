package simple.core.service.listener;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

public abstract class DomainChangedListener {
	public static final int DOMAIN_DELETED = 0;
	public static final int DOMAIN_UPDATED = 1;

	private final BlockingQueue<ChangeInfo> dataQueue = new LinkedBlockingDeque<ChangeInfo>();

	private final ExecutorService exec = Executors.newFixedThreadPool(Runtime
			.getRuntime().availableProcessors() + 1);

	private Thread processThread;

	private void createAndStartProcessThread() {
		processThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!dataQueue.isEmpty()) {
					final ChangeInfo changeInfo = dataQueue.poll();
					if (null != changeInfo) {
						exec.execute(new Runnable() {
							@Override
							public void run() {
								onDomainChanged(changeInfo);
							}
						});
					}
				}
			}
		});
		processThread.setName("DomainChangedListener_"
				+ System.currentTimeMillis());
		processThread.start();
	}

	public void applyChange(int changedType, Object domain) {
		ChangeInfo changeInfo = new ChangeInfo();
		changeInfo.setType(changedType);
		changeInfo.setDomain(domain);
		try {
			dataQueue.put(changeInfo);
			if (null == processThread || !processThread.isAlive()) {
				createAndStartProcessThread();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public abstract void onDomainChanged(ChangeInfo changeInfo);

	public class ChangeInfo {
		private int type;

		private Object domain;

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public Object getDomain() {
			return domain;
		}

		public void setDomain(Object domain) {
			this.domain = domain;
		}

	}
}
