package simple.base.vo;

public class User {

	private Long userId;

	private Long employeeId;

	private String userName;

	private String employeeNo;

	private String employeeName;

	private String employeeMobile;

	private String position;

	private String account;

	private String password;

	private String verifyCode;

	private String smsVerifyCode;

	private String newPassword;

	private String password1;

	private String email;

	private boolean needVerify;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getVerifyCode() {
		return verifyCode;
	}

	public void setVerifyCode(String verifyCode) {
		this.verifyCode = verifyCode;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getPassword1() {
		return password1;
	}

	public void setPassword1(String password1) {
		this.password1 = password1;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isNeedVerify() {
		return needVerify;
	}

	public void setNeedVerify(boolean needVerify) {
		this.needVerify = needVerify;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	// public String getTel() {
	// return tel;
	// }
	//
	// public void setTel(String tel) {
	// this.tel = tel;
	// }
	//
	// public String getCarNo() {
	// return carNo;
	// }
	//
	// public void setCarNo(String carNo) {
	// this.carNo = carNo;
	// }
	//
	// public String getDriverCode() {
	// return driverCode;
	// }
	//
	// public void setDriverCode(String driverCode) {
	// this.driverCode = driverCode;
	// }
	//
	// public String getCarrierCode() {
	// return carrierCode;
	// }
	//
	// public void setCarrierCode(String carrierCode) {
	// this.carrierCode = carrierCode;
	// }

	// public List<Menu> getMenus() {
	// return menus;
	// }
	//
	// public void setMenus(List<Menu> menus) {
	// this.menus = menus;
	// }

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public void clearPassword() {
		this.password = null;
		this.password1 = null;
		this.newPassword = null;
	}

	public Long getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(Long employeeId) {
		this.employeeId = employeeId;
	}

	public String getEmployeeNo() {
		return employeeNo;
	}

	public void setEmployeeNo(String employeeNo) {
		this.employeeNo = employeeNo;
	}

	public String getEmployeeName() {
		return employeeName;
	}

	public void setEmployeeName(String employeeName) {
		this.employeeName = employeeName;
	}

	public String getEmployeeMobile() {
		return employeeMobile;
	}

	public void setEmployeeMobile(String employeeMobile) {
		this.employeeMobile = employeeMobile;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getSmsVerifyCode() {
		return smsVerifyCode;
	}

	public void setSmsVerifyCode(String smsVerifyCode) {
		this.smsVerifyCode = smsVerifyCode;
	}

	// public class Menu {
	// private String menuCode;
	//
	// private String menuName;
	//
	// public String getMenuCode() {
	// return menuCode;
	// }
	//
	// public void setMenuCode(String menuCode) {
	// this.menuCode = menuCode;
	// }
	//
	// public String getMenuName() {
	// return menuName;
	// }
	//
	// public void setMenuName(String menuName) {
	// this.menuName = menuName;
	// }
	// }

}
