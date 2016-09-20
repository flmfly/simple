package simple.core.service;

import java.lang.reflect.Field;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import simple.config.annotation.ImportExtentionCheck;
import simple.config.annotation.ImportNotFindCheck;
import simple.config.annotation.RepresentationField;
import simple.config.annotation.ImportNotFindCheck.CheckType;
import simple.config.annotation.support.ImportExtentionCheck.CheckResult;
import simple.core.util.ReflectUtils;

@Service
public class ImportExtentionCheckService extends HandlerService {

	public CheckResult handle(Object before, Object after) {
		Class clazz = after.getClass();
		CheckResult checkResult = new CheckResult();
		ImportExtentionCheck extentionCheck = (ImportExtentionCheck) clazz
				.getAnnotation(ImportExtentionCheck.class);
		if (extentionCheck != null) {
			try {
				simple.config.annotation.support.ImportExtentionCheck handler = (simple.config.annotation.support.ImportExtentionCheck) super
						.fetchHandler(extentionCheck.value());
				CheckResult r = handler.handle(before, after);
				if (r.getCheckType() != CheckType.none) {
					checkResult.addMsg(r.getMsg());
					checkResult.setCheckType(r.getCheckType());
				}
			} catch (Exception e) {
				checkResult.setCheckType(CheckType.err);
				checkResult.addMsg(e.getMessage());
			}
		}
		Field[] fields = ReflectUtils.getDeclaredFields(clazz);
		if (fields != null && fields.length > 0) {
			for (Field field : fields) {
				ImportNotFindCheck check = field
						.getAnnotation(ImportNotFindCheck.class);
				if (check != null) {
					if (check.value() == CheckType.none) {
						continue;
					}
					try {
						simple.config.annotation.support.ImportExtentionCheck handler = new ImportNotFindCheckHander(
								field);
						CheckResult r = handler.handle(before, after);
						if(!r.getMsg().isEmpty()){
							if (r.getCheckType() == CheckType.none) {
								r.setCheckType(check.value());
							}
							checkResult.addMsg(r.getMsg());
						}
						
						if (r.getCheckType() == CheckType.err
								|| checkResult.getCheckType() == CheckType.none) {
							checkResult.setCheckType(r.getCheckType());
						}
					} catch (Exception e) {
						checkResult.setCheckType(CheckType.err);
						checkResult.addMsg(e.getMessage());
					}
				}
			}
		}
		return checkResult;
	}

	private class ImportNotFindCheckHander implements
			simple.config.annotation.support.ImportExtentionCheck {
		private final Field field;

		public ImportNotFindCheckHander(Field field) {
			this.field = field;
			this.field.setAccessible(true);
		}

		@Override
		public CheckResult handle(Object before, Object after) {
			CheckResult checkResult = new CheckResult();
			if (field != null) {
				RepresentationField representationField = field
						.getAnnotation(RepresentationField.class);
				String title = null;
				if (representationField != null) {
					title = representationField.title();
				}
				if (StringUtils.isEmpty(title)) {
					title = field.getName();
				}
				try {
					Object beforeObj = field.get(before);
					Object afterObj = field.get(after);
					if (beforeObj != null && afterObj == null) {
						checkResult.addMsg(title + "不存在！");
					}
				} catch (Exception e) {
					checkResult.setCheckType(CheckType.err);
					checkResult.addMsg(e.getMessage());
				}
			}

			return checkResult;
		}

	}

}
