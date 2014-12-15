package commonlib;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

public class LogManager {
	private boolean defaultWriteToDisk = true;
	private String logDir = null; // Logging directory
	private String baseFileName = null; // Base file name
	private final ReentrantLock mutex = new ReentrantLock();

	public LogManager(String directory, String baseFileName) {
		this.logDir = directory;
		this.baseFileName = baseFileName;
	}

	public LogManager(String directory, String baseFileName,
			boolean defaultWriteToDisk) {
		this(directory, baseFileName);
		this.defaultWriteToDisk = defaultWriteToDisk;
	}

	private boolean createDir() {
		if (this.logDir == null || this.logDir.length() == 0)
			return false;

		// Create the folder
		File logDirectory = new File(this.logDir);
		if (!logDirectory.exists() && logDirectory.mkdir()) {
			System.out.println("Directory: " + this.logDir + " created");
		} else if (!logDirectory.exists()) {
			System.out.println("Fail to create directory " + this.logDir);
			return false;
		}

		return true;
	}
	
	// fileName is full path and fileName
	private boolean createFile(String fileName) {
		if (fileName == null || fileName.length() == 0)
			return false;

		// Create the folder
		File file = new File(fileName);
		try {
			if (!file.exists() && file.createNewFile()) {
				System.out.println("Directory: " + this.logDir + " created");
			} else if (!file.exists()) {
				System.out.println("Fail to create directory " + this.logDir);
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	// Write log with default writeToDisk value
	public boolean writeLog(String log) {
		final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		// 0 is getStackTrace, 1 is the writeLog function, 2 is the writeLog's caller
		String functionName = ste[2].getMethodName();
		String fileName = Thread.currentThread().getStackTrace()[2].getFileName();
		int lineNumber = Thread.currentThread().getStackTrace()[2].getLineNumber();

		return this.writeLog(fileName, lineNumber, functionName, log, this.defaultWriteToDisk);
	}

	public boolean writeLog(String fileName, int lineNumber, String functionName, String log) {
		return this.writeLog(fileName, lineNumber, functionName, log, this.defaultWriteToDisk);
	}
	
	public boolean writeLog(String log, boolean writeToDisk) {
		final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		// 0 is getStackTrace, 1 is the writeLog function, 2 is the writeLog's caller
		String functionName = ste[2].getMethodName();
		String fileName = Thread.currentThread().getStackTrace()[2].getFileName();
		int lineNumber = Thread.currentThread().getStackTrace()[2].getLineNumber();
		
		return this.writeLog(fileName, lineNumber, functionName, log, writeToDisk);
	}

	@SuppressWarnings("deprecation")
	public boolean writeLog(String fileName, int lineNumber, String functionName, String log, boolean writeToDisk) {
		if (this.logDir == null || this.baseFileName == null
				|| functionName == null || log == null)
			return false;
		
		this.mutex.lock();
		
		try {
			if (!this.createDir()) {
				return false;
			}
	
			Date currentDate = new Date();
			String logFileName = this.logDir + Globals.pathSeparator + this.baseFileName + "."
					+ Helper.getCurrentDate() + "-" + currentDate.getHours()
					+ ".log";
			File logFile = new File(logFileName);
			if (!logFile.exists() && this.createFile(logFileName)) {
				System.out.println("File " + logFileName + " created");
			} else if (!logFile.exists()) {
				System.out.println("File " + logFileName + " can't be created");
				return false;
			}
	
			String logLine = "[" + fileName + ":" + lineNumber + "] [" + Helper.getCurrentDate() + "] ["
					+ Helper.getCurrentTime() + "] [" + functionName + "]: " + log;
			
			System.out.println("Log: "+logLine);
			if (writeToDisk) {
				try {
					PrintWriter out = new PrintWriter(new BufferedWriter(
							new FileWriter(logFileName, true)));
					out.println(logLine);
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			}
	
			return true;
		} finally {
			this.mutex.unlock();
		}
	}

	public static void main(String[] args) {
//		LogManager logManager = new LogManager("C:\\Project\\log", "testlog");
//		logManager.writeLog("main", "testlog hahah lolol", true);
//		logManager.writeLog("main", "testlog hahah lolol 2123123123asdq", true);
//		logManager.writeLog("testlog hahah lolol 2123123123asdq");
	}
}
