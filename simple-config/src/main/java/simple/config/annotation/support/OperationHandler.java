package simple.config.annotation.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface OperationHandler {

	public boolean disabled(final Object domain);

	public OperationResult handle(final Map<String, Object> parameters,
			final List<Object> domains);

	public class OperationException extends RuntimeException {
		/**
		 * 
		 */
		private static final long serialVersionUID = 569469280022526321L;
		private final List<String> messages;

		public OperationException(List<String> messages) {
			super(join(messages));
			this.messages = messages;
		}

		public List<String> getMessages() {
			return messages;
		}

		private static String join(List<String> messages) {
			StringBuffer err = new StringBuffer();
			if (messages != null) {
				for (String msg : messages) {
					err.append(msg).append("\r\n");
				}
			}
			return err.toString();
		}
	}

	class OperationResult {
		private boolean success = false;

		private List<String> errorMessages = new ArrayList<String>();

		public boolean isSuccess() {
			return success;
		}

		public void setSuccess(boolean success) {
			this.success = success;
		}

		public List<String> getErrorMessages() {
			return errorMessages;
		}

		public void setErrorMessages(List<String> errorMessages) {
			this.errorMessages.addAll(errorMessages);
		}

		public void addErrorMessage(String errorMessage) {
			this.errorMessages.add(errorMessage);
		}

	}
}
