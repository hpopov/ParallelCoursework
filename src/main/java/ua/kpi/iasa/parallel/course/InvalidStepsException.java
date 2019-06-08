package ua.kpi.iasa.parallel.course;

public class InvalidStepsException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7235682873302635929L;

	private final String exceptionHeader;
	private final String exceptionContent;
	
	public InvalidStepsException(String exceptionHeader, String exceptionContent) {
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
