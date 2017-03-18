package com.wanxg.selfprj.findclass;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author WanX
 * @since 18/03/2017
 * 
 */
public class ClassFinder {

	static Logger logger = LoggerFactory.getLogger(ClassFinder.class);

	static SortedMap<String, String> report = new TreeMap<String, String>() {
		private static final long serialVersionUID = 1L;

		public String put(String key, String value) {
			// if(this.size()==50) {
			// logger.info("There are already more than 50 entries found.
			// Searching stopping");
			// return null;
			// }
			return super.put(key, value);
		}
	};
	static final String usage = "\nUsage: findclass [PATH] (-c CLASS_NAME | -p PACKAGE_NAME) \n" + "\n"
			+ "Argument list:\n\n" + "\t" + "PATH              " + "\t"
			+ "Optional argument. It can be the path of a jar file or a directory.\n" + "\t" + "                  "
			+ "\t" + "If it is not provided, the current path '.' is used.\n" + "\t" + "-c CLASS_NAME     " + "\t"
			+ "Class name that will be searched, OR \n" + "\t" + "-p PACKAGE_NAME   " + "\t"
			+ "Package name that will be searched.\n";

	public static void main(String[] args) {

		String path = null;
		String option = null;
		String target = null;

		logger.info("Starting ......");

		if (args.length < 2) {
			System.out.println(usage);
			return;
		}

		if (args.length == 2) {
			if (!(args[0].trim().equalsIgnoreCase("-p") || args[0].trim().equalsIgnoreCase("-c"))) {
				System.out.println(usage);
				return;
			} else {
				path = ".";
				option = args[0];
				target = args[1];
			}
		}

		else if (args.length > 2) {
			path = args[0];
			if (!(args[1].trim().equalsIgnoreCase("-p") || args[1].trim().equalsIgnoreCase("-c"))) {
				System.out.println(usage);
				return;
			} else {
				option = args[1];
				target = args[2];
			}
		}
		StringBuilder sb = new StringBuilder();
		Arrays.asList(args).forEach(arg -> sb.append(arg + " "));

		logger.info("Argument list: " + sb);
		logger.info("path: " + path);
		logger.info("option: " + option);
		logger.info("target: " + target);

		File file = new File(path);

		if (file.isFile()) {
			logger.info("Provided path refers to a file: " + file.getPath());
			searchInsideAJar(target, file, option);
		}

		else if (file.isDirectory()) {
			logger.info("Provided path refers to a directory: " + file.getPath());
			searchInsideADirectory(target, file, option);
		}

		logger.info(String.valueOf(report.size()) + " Result(s) found.");

		if (!report.isEmpty()) {
			if (option.equalsIgnoreCase("-c"))
				report.forEach((key, value) -> System.out.println(key + " -> " + value));
			else if (option.equalsIgnoreCase("-p"))
				report.forEach((key, value) -> System.out.println(value + ".* -> " + key));
			System.out.println("\n");
			System.out.println(report.size() + " Result(s) found.");
		}
	}

	private static void searchInsideADirectory(String target, File file, String option) {

		logger.info("Processing in a directory " + file);

		if (!file.isDirectory()) {
			logger.error("Error: the provided path is not a directory.");
			return;
		}

		Arrays.asList(file.listFiles(aFile -> {
			if (aFile.getName().endsWith(".jar") || aFile.isDirectory())
				return true;
			else
				return false;
		})).forEach(aFile -> {
			if (aFile.isFile())
				searchInsideAJar(target, aFile, option);
			else if (aFile.isDirectory())
				searchInsideADirectory(target, aFile, option);
		});
	}

	private static void searchInsideAJar(String target, File file, String option) {

		logger.info("Processing in a jar " + file);

		if (!file.getName().endsWith(".jar")) {
			logger.info(file + " is not a jar.");
			return;
		}

		JarFile jarFile;

		try {
			jarFile = new JarFile(file);

			jarFile.stream().filter(entry -> {

				if (!entry.isDirectory() && entry.getName().endsWith(".class"))
					return true;
				else
					return false;
			}).forEach(entry -> {

				if (option.equalsIgnoreCase("-c")) {
					String[] entryParts = entry.getName().split("/");
					if (target.endsWith(".class") && target.equals(entryParts[entryParts.length - 1]))

						report.put(entry.getName().replace("/", "."), file.getAbsolutePath());

					else if (entryParts[entryParts.length - 1]
							.substring(0, entryParts[entryParts.length - 1].indexOf(".")).equals(target))
						report.put(entry.getName().replace("/", "."), file.getAbsolutePath());
				}

				else if (option.equalsIgnoreCase("-p")) {
					if (entry.getName().startsWith(target.replace(".", "/"))) {
						report.put(file.getAbsolutePath(), target);
					}
				}
			});

		} catch (IOException e) {
			logger.error("IOException" + e.getMessage());
		}
	}
}
