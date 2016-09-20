package simple.core.orm.hibernate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.ClassWriter;
import org.springframework.asm.FieldVisitor;
import org.springframework.asm.Opcodes;
import org.springframework.asm.Type;
import org.springframework.core.InfrastructureProxy;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;

public class ComboSessionFactoryBean extends LocalSessionFactoryBean {

	private SessionFactoryProxy sessionFactoryProxy;

	private String[] packagesToScan;

	public void setPackagesToScan(String... packagesToScan) {
		this.packagesToScan = packagesToScan;
	}

	protected SessionFactory buildSessionFactory(LocalSessionFactoryBuilder sfb) {
		this.sessionFactoryProxy = new SessionFactoryProxy((DataSource) sfb
				.getProperties().get(Environment.DATASOURCE),
				super.getHibernateProperties(), this.packagesToScan);
		return (SessionFactory) Proxy.newProxyInstance(getClass()
				.getClassLoader(), new Class[] { SessionFactory.class,
				SessionFactoryImplementor.class, InfrastructureProxy.class },
				this.sessionFactoryProxy);
	}

	public void rebuildSessionFactory() {
		this.sessionFactoryProxy.buildSessionFactory();
	}

	class SessionFactoryProxy implements InvocationHandler, InfrastructureProxy {

		private SessionFactory sessionFactory;

		private ByteArrayClassLoader classLoader;

		private DataSource dataSource;

		private Properties hibernateProperties;

		private String[] packagesToScan;

		private Set<Class<?>> annotatedClasses = new HashSet<Class<?>>();

		public SessionFactoryProxy(DataSource dataSource,
				Properties hibernateProperties, String[] packagesToScan) {
			this.dataSource = dataSource;
			this.hibernateProperties = hibernateProperties;
			this.packagesToScan = packagesToScan;

			this.buildSessionFactory();
		}

		void buildSessionFactory() {
			if (null != sessionFactory && !sessionFactory.isClosed()) {
				sessionFactory.close();
			}
			this.annotatedClasses.clear();
			this.classLoader = null;
			this.classLoader = new ByteArrayClassLoader(Thread.currentThread()
					.getContextClassLoader());

			try {
				

//				Class<?> clazz = classLoader.loadFromByteArray(name,
//						cw.toByteArray());
//
//				this.annotatedClasses.add(clazz);
			} catch (Exception e) {
				e.printStackTrace();
			}

			LocalSessionFactoryBuilder sfb = new LocalSessionFactoryBuilder(
					this.dataSource, classLoader);
			if (this.packagesToScan != null) {
				sfb.scanPackages(this.packagesToScan);
			}

			if (this.hibernateProperties != null) {
				sfb.addProperties(this.hibernateProperties);
			}

			if (this.annotatedClasses != null) {
				sfb.addAnnotatedClasses(this.annotatedClasses
						.toArray(new Class<?>[this.annotatedClasses.size()]));
			}

			this.sessionFactory = sfb.buildSessionFactory();
		}

		@Override
		public Object getWrappedObject() {
			return this.sessionFactory;
		}

		@Override
		public Object invoke(Object arg0, Method arg1, Object[] arg2)
				throws Throwable {
			if ("getWrappedObject".equals(arg1.getName())) {
				return this.getWrappedObject();
			}
			return arg1.invoke(sessionFactory, arg2);
		}

	}

	class ByteArrayClassLoader extends ClassLoader {
		public ByteArrayClassLoader(ClassLoader parent) {
			super(parent);
		}

		public Class<?> loadFromByteArray(String name, byte[] b) {
			return super.defineClass(name, b, 0, b.length);
		}
	}
}
