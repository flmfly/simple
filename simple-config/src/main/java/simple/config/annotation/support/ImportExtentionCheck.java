package simple.config.annotation.support;

import java.util.ArrayList;
import java.util.List;

import simple.config.annotation.ImportNotFindCheck;
import simple.config.annotation.ImportNotFindCheck.CheckType;

public interface ImportExtentionCheck {
	public CheckResult handle(Object before, Object after);

	public static class CheckResult {
		private final List<String> msg = new ArrayList<String>();
		private ImportNotFindCheck.CheckType checkType = CheckType.none;

		public void addMsg(String msg) {
			this.msg.add(msg);
		}

		public void addMsg(List<String> msg) {
			this.msg.addAll(msg);
		}

		public List<String> getMsg() {
			return msg;
		}

		public ImportNotFindCheck.CheckType getCheckType() {
			return checkType;
		}

		public void setCheckType(ImportNotFindCheck.CheckType checkType) {
			if (checkType == null) {
				this.checkType = CheckType.none;
			} else {
				this.checkType = checkType;
			}

		}
	}

}
