package nachos.kv;

public class UnitTestChecker {
	public String className;
	public String methodName;
	public String testNum;
	
	public UnitTestChecker(String className, String methodName, Integer testNum) {
		this.className = className;
		this.methodName = methodName;
		this.testNum = "Test" + testNum.toString();
	}
	
	public void assertTrue(boolean statement) {
		if (statement == true)
			System.out.println(this.pass());
		else
			System.out.println(this.fail());
	}
	
	public String pass() {
		return this.className + "/" + this.methodName + "/" + this.testNum + " ---> Pass";
	}
	
	public String fail() {
		return this.className + "/" + this.methodName + "/" + this.testNum + " ---> Fail";
	}
}
