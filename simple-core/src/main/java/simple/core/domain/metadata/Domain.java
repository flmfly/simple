package simple.core.domain.metadata;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.annotations.FromAnnotationsRuleModule;
import org.apache.commons.digester3.annotations.rules.BeanPropertySetter;
import org.apache.commons.digester3.annotations.rules.ObjectCreate;
import org.apache.commons.digester3.annotations.rules.SetNext;
import org.apache.commons.digester3.annotations.rules.SetProperty;
import org.apache.commons.digester3.binder.DigesterLoader;

@ObjectCreate(pattern = "domain")
public class Domain {

	@SetProperty(pattern = "domain")
	public String code;

	@SetProperty(pattern = "domain")
	public String name;

	@SetProperty(pattern = "domain")
	public String moudle;

	@BeanPropertySetter(pattern = "domain/desc")
	public String desc;

	@SetProperty(pattern = "domain")
	public String view;

	public List<Behaviour> behaviours = new ArrayList<Behaviour>();

	public List<Property> properties = new ArrayList<Property>();

	@SetNext
	public void addProperty(Property property) {
		this.properties.add(property);
	}

	// @SetNext
	public void addBehaviour(Behaviour behaviour) {
		this.behaviours.add(behaviour);
	}

	// /Users/Jeffrey/workspace/simple-test/src/test/resources/domain.xml

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getView() {
		return view;
	}

	public void setView(String view) {
		this.view = view;
	}

	public String getMoudle() {
		return moudle;
	}

	public void setMoudle(String moudle) {
		this.moudle = moudle;
	}

	public static void main(String[] args) {
		DigesterLoader loader = DigesterLoader
				.newLoader(new FromAnnotationsRuleModule() {

					@Override
					protected void configureRules() {
						bindRulesFrom(Domain.class);
					}

				});
		Digester digester = loader.newDigester();
		try {
			Domain domain = digester
					.parse(new File(
							"/Users/Jeffrey/workspace/simple-test/src/test/resources/domain.xml"));

			System.out.println(domain);
		} catch (Exception e) {
			// do something
			e.printStackTrace();
		}
	}

}
