package krasa.albion.utils;


public class ThreadDump {
	private final String myRawDump;
	private final StackTraceElement[] myEdtStack;

	ThreadDump(String rawDump, StackTraceElement[] edtStack) {
		myRawDump = rawDump;
		myEdtStack = edtStack;
	}

	public String getRawDump() {
		return myRawDump;
	}

	StackTraceElement[] getEDTStackTrace() {
		return myEdtStack;
	}
}
