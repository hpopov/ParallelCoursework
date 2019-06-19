package ua.kpi.iasa.parallel.course;

@SuppressWarnings("serial")
public class AlertableException extends Exception {


	private final String exceptionHeader;
	private final String exceptionContent;
	
	public AlertableException(String exceptionHeader, String exceptionContent) {
		super();
		this.exceptionHeader = exceptionHeader;
		this.exceptionContent = exceptionContent;
	}
	
	public String getExceptionHeader() {
		return exceptionHeader;
	}

	public String getExceptionContent() {
		return exceptionContent;
	}
}
