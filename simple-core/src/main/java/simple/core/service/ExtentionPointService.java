package simple.core.service;

import org.springframework.stereotype.Service;

import simple.config.annotation.ExtentionPoint;

@Service
public class ExtentionPointService extends HandlerService {

	public void afterFetch(Object entity) {
		ExtentionPoint ep = this.annotionService.getExtentionPoint(entity
				.getClass().getSimpleName().toLowerCase());
		if (null != ep) {
			try {
				simple.config.annotation.support.ExtentionPoint handler = (simple.config.annotation.support.ExtentionPoint) super
						.fetchHandler(Class.forName(ep.value()));
				handler.afterFetch(entity);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void beforeSave(Object entity) {
		ExtentionPoint ep = this.annotionService.getExtentionPoint(entity
				.getClass().getSimpleName().toLowerCase());
		if (null != ep) {
			try {
				simple.config.annotation.support.ExtentionPoint handler = (simple.config.annotation.support.ExtentionPoint) super
						.fetchHandler(Class.forName(ep.value()));
				handler.beforeSave(entity);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void afterSave(Object entity) {
		ExtentionPoint ep = this.annotionService.getExtentionPoint(entity
				.getClass().getSimpleName().toLowerCase());
		if (null != ep) {
			try {
				simple.config.annotation.support.ExtentionPoint handler = (simple.config.annotation.support.ExtentionPoint) super
						.fetchHandler(Class.forName(ep.value()));
				handler.afterSave(entity);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void beforeDelete(Object entity) {
		ExtentionPoint ep = this.annotionService.getExtentionPoint(entity
				.getClass().getSimpleName().toLowerCase());
		if (null != ep) {
			try {
				simple.config.annotation.support.ExtentionPoint handler = (simple.config.annotation.support.ExtentionPoint) super
						.fetchHandler(Class.forName(ep.value()));
				handler.beforeDelete(entity);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void afterDelete(Object entity) {
		ExtentionPoint ep = this.annotionService.getExtentionPoint(entity
				.getClass().getSimpleName().toLowerCase());
		if (null != ep) {
			try {
				simple.config.annotation.support.ExtentionPoint handler = (simple.config.annotation.support.ExtentionPoint) super
						.fetchHandler(Class.forName(ep.value()));
				handler.afterDelete(entity);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
