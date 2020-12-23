/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package krasa.albion.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author yole
 */
public class ThreadDumper {
	private ThreadDumper() {
	}

	public static String dumpThreadsToString() {
		StringWriter writer = new StringWriter();
		dumpThreadsToFile(ManagementFactory.getThreadMXBean(), writer);
		return writer.toString();
	}

	public static String dumpEdtStackTrace(ThreadInfo[] threadInfos) {
		StringWriter writer = new StringWriter();
		if (threadInfos.length > 0) {
			StackTraceElement[] trace = threadInfos[0].getStackTrace();
			printStackTrace(writer, trace);
		}
		return writer.toString();
	}


	public static ThreadInfo[] getThreadInfos() {
		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		return sort(threadMXBean.dumpAllThreads(false, false));
	}


	public static ThreadDump getThreadDumpInfo(final ThreadMXBean threadMXBean) {
		StringWriter writer = new StringWriter();
		StackTraceElement[] edtStack = dumpThreadsToFile(threadMXBean, writer);
		return new ThreadDump(writer.toString(), edtStack);
	}


	private static StackTraceElement[] dumpThreadsToFile(ThreadMXBean threadMXBean, Writer f) {
		StackTraceElement[] edtStack = null;
		boolean dumpSuccessful = false;

		try {
			ThreadInfo[] threads = sort(threadMXBean.dumpAllThreads(false, false));
			edtStack = dumpThreadInfos(threads, f);
			dumpSuccessful = true;
		} catch (Exception ignored) {

		}

		if (!dumpSuccessful) {
			final long[] threadIds = threadMXBean.getAllThreadIds();
			final ThreadInfo[] threadInfo = sort(threadMXBean.getThreadInfo(threadIds, Integer.MAX_VALUE));
			edtStack = dumpThreadInfos(threadInfo, f);
		}

		return edtStack;
	}

	private static StackTraceElement[] dumpThreadInfos(ThreadInfo[] threadInfo, Writer f) {
		List<ThreadInfo> list = moveEdtToEnd(threadInfo);
		try {
			f.write("Generated: " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()) + "\n");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		StackTraceElement[] edtStack = null;
		for (ThreadInfo info : list) {
			if (info != null) {
				if (info.getThreadName().startsWith("AWT-EventQueue")) {
					edtStack = info.getStackTrace();
				}
				dumpThreadInfo(info, f);
			}
		}

		return edtStack;
	}


	private static List<ThreadInfo> moveEdtToEnd(ThreadInfo[] threadInfo) {
		List<ThreadInfo> arrayList = new ArrayList<>(Arrays.asList(threadInfo));
		for (int i = 0; i < arrayList.size(); i++) {
			ThreadInfo info = arrayList.get(i);
			if (info != null) {
				if (info.getThreadName().startsWith("JavaFX Application Thread")) {
					arrayList.remove(info);
					arrayList.add(info);
				}
			}
		}
		return arrayList;
	}


	private static ThreadInfo[] sort(ThreadInfo[] threads) {
		Arrays.sort(threads, new Comparator<ThreadInfo>() {
			@Override
			public int compare(ThreadInfo o1, ThreadInfo o2) {
				return o2.getStackTrace().length - o1.getStackTrace().length;
			}
		});

		return threads;
	}

	private static void dumpThreadInfo(ThreadInfo info, Writer f) {
		dumpCallStack(info, f, info.getStackTrace());
	}

	private static void dumpCallStack(ThreadInfo info, Writer f, StackTraceElement[] stackTraceElements) {
		try {
			StringBuilder sb = new StringBuilder("\"").append(info.getThreadName()).append("\"");
			sb.append(" prio=0 tid=0x0 nid=0x0 ").append(getReadableState(info.getThreadState())).append("\n");
			sb.append("     java.lang.Thread.State: ").append(info.getThreadState()).append("\n");
			if (info.getLockName() != null) {
				sb.append(" on ").append(info.getLockName());
			}
			if (info.getLockOwnerName() != null) {
				sb.append(" owned by \"").append(info.getLockOwnerName()).append("\" Id=").append(info.getLockOwnerId());
			}
			if (info.isSuspended()) {
				sb.append(" (suspended)");
			}
			if (info.isInNative()) {
				sb.append(" (in native)");
			}

			f.write(sb + "\n");
			printStackTrace(f, stackTraceElements);
			f.write("\n");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void printStackTrace(Writer f, StackTraceElement[] stackTraceElements) {
		try {
			for (StackTraceElement element : stackTraceElements) {
				f.write("\tat " + element.toString() + "\n");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String getReadableState(Thread.State state) {
		switch (state) {
			case BLOCKED:
				return "blocked";
			case TIMED_WAITING:
			case WAITING:
				return "waiting on condition";
			case RUNNABLE:
				return "runnable";
			case NEW:
				return "new";
			case TERMINATED:
				return "terminated";
		}
		return null;
	}
}
