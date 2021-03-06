package org.bbop.paint.model;

import org.apache.log4j.Logger;
import org.bbop.paint.LogAction;
import org.bbop.paint.LogAlert;
import org.bbop.paint.touchup.Constant;
import org.bbop.paint.touchup.Preferences;
import org.bbop.paint.util.FileUtil;
import owltools.gaf.Bioentity;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class History {

	private static final Logger logger = Logger.getLogger(History.class);

	private static List<String> notes = new ArrayList<>();

	public static final String MF_SECTION = "# molecular_function";
	public static final String CC_SECTION = "# cellular_component";
	public static final String BP_SECTION = "# biological_process";
	public static final String PRUNED_SECTION = "# Pruned";
	public static final String WARNING_SECTION = "# WARNINGS - THE FOLLOWING HAVE BEEN REMOVED FOR THE REASONS NOTED";
	private static final String DATE_PREFIX = "201";
	private static final String NOTES_SECTION = "# Notes";
	private static final String REF_SECTION = "# Reference";

	public static void write(String program_name, String family_name) {
		String family_dir = Preferences.inst().getTreedir() + family_name + '/';

		if (FileUtil.validPath(family_dir)) {
			String logFileName = family_dir + family_name + ".log";
			List<String> contents = new ArrayList<>();
			contents.add("# " + program_name + " Log Report for " + dateNow());
			LogAction.report(contents);
			LogAlert.report(contents);
			logNotes(contents);
			logBoilerplate(contents);
			try {
				FileUtil.writeFile(logFileName, contents);
			} catch (IOException e) {
				logger.error("Unable to log updates for " + family_name);
				logger.error(e.getMessage());
			}
		}
	}

	public static void importPrior(String family_name) {
		String family_dir = Preferences.inst().getTreedir() + family_name + '/';

		if (FileUtil.validPath(family_dir)) {
			String log_file = family_dir + family_name + ".txt";
			if (!FileUtil.validPath(log_file)) {
				log_file = family_dir + family_name + ".log";
			}
			if (FileUtil.validPath(log_file)) {
				List<String> log_content = FileUtil.readFile(log_file);
				if (log_content != null) {
					clearBoilerPlate(log_content);
					clearEditLog(log_content);
					for (int i = 0; i < log_content.size(); i++) {
						String line = log_content.get(i).trim();
						notes.add(line);
					}
				} else {
					logger.error("Couldn't read" + log_file);
				}
			} else {
				logger.error("Invalid path for log file " + log_file);
			}
		} else {
			logger.error("Invalid path for family directory " + family_dir);
		}
	}

	private static void logNotes(List<String> contents) {
		contents.add(NOTES_SECTION);
		contents.addAll(notes);
		contents.add("");
	}

	private static void logBoilerplate(List<String> contents) {
		contents.add(REF_SECTION);
		contents.add(Constant.GO_PUBLICATION);
		contents.add("");
	}

	private static String dateNow() {
		long timestamp = System.currentTimeMillis();
		/* Date appears to be fixed?? */
		Date when = new Date(timestamp);
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
		sdf.setTimeZone(TimeZone.getDefault()); // local time
		return sdf.format(when);
	}

	private static void clearBoilerPlate(List<String> log_content) {
		for (int i = log_content.size() - 1; i >= 0; i--) {
			String line = log_content.get(i).trim();
			if (line.length() == 0) {
				log_content.remove(i);
			} else if (line.startsWith(REF_SECTION) ||
					line.contains(Constant.GO_REF_TITLE) ||
					line.contains(Constant.GO_REF_SW)) {
				log_content.remove(i);
			}
		}
	}

	private static void clearEditLog(List<String> log_content) {
		for (int i = log_content.size() - 1; i >= 0; i--) {
			String line = log_content.get(i).trim();
			if (line.contains(NOTES_SECTION) && !line.startsWith(NOTES_SECTION)) {
				line = line.substring(line.indexOf(NOTES_SECTION));
				log_content.set(i, line);
			}
			if (line.startsWith(NOTES_SECTION) && line.length() > NOTES_SECTION.length()) {
				line = line.substring(NOTES_SECTION.length());
				log_content.set(i, line);
			}
			if (line.startsWith(NOTES_SECTION) ||
					line.startsWith(DATE_PREFIX) ||
					line.startsWith(MF_SECTION) ||
					line.startsWith(CC_SECTION) ||
					line.startsWith(BP_SECTION) ||
					line.startsWith(PRUNED_SECTION) ||
					line.startsWith(WARNING_SECTION) ||
					line.startsWith("##")) {
				log_content.remove(i);
			}
		}
	}

	public static String makeLabel(Bioentity node) {
		String species = Preferences.inst().getSpecies(node.getNcbiTaxonId());
		if (species == null) {
			species = node.getSpeciesLabel();
		}
		if (node.getParent() != null && (species.equals("LUCA") || species.equals("root"))) {
			// if this is not the root node, but the clade is unknown
			species = "node";
		}
		return (species + '_' + node.getDBID());
	}
}
