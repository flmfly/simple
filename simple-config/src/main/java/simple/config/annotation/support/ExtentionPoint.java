package simple.config.annotation.support;

public interface ExtentionPoint {

	public void beforeSave(Object entity);

	public void afterSave(Object entity);

	public void beforeFetch(Object entity);

	public void afterFetch(Object entity);

	public void beforeDelete(Object entity);

	public void afterDelete(Object entity);

}
