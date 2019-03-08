package com.liferay.clam.antivirus.custom;

import com.liferay.document.library.kernel.antivirus.AntivirusScannerException;
import com.liferay.document.library.kernel.antivirus.BaseFileAntivirusScanner;
import com.liferay.document.library.kernel.util.DLPreviewableProcessor;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.io.File;
import java.io.IOException;


/**
 * @author Michael Wall
 */
public class CustomClamAntivirusScannerImpl extends BaseFileAntivirusScanner{

	@Override
	public void scan(File file) throws AntivirusScannerException {
		if (_log.isDebugEnabled()) {
			_log.debug("Scan request for file: " + file.getAbsolutePath());
		}

		//Ensure same slashes for all paths for comparison purposes...
		String filePath = file.getAbsolutePath().replace("/", "\\");
		
		if (filePath.startsWith(PREVIEW_TMP_PATH)) {
			String fileName = file.getName();
				
			//Expected syntax for most files is fileId, version (e.g. 2.1), page number e.g. 12132.2.1-1.png (case insensitive)
			// But the very first file uses syntax fileId, version (e.g. 2.1) e.g. 12132.2.1.png (case insensitive)
			if (fileName.matches("(?i:[0-9]+.[0-9]+.[0-9]+-[0-9]+.png)") || fileName.matches("(?i:[0-9]+.[0-9]+.[0-9]+.png)")) {
				if (_log.isDebugEnabled()) {
					_log.debug("Skipping scan of document_preview file: " + file.getAbsolutePath());	
				}
				
				return;
			}
		} else if (filePath.startsWith(THUMBNAIL_TMP_PATH)) {
			String fileName = file.getName();
				
			//Expected syntax 12132.2.1.png (case insensitive)
			// fileId, version (e.g. 2.1)
			if (fileName.matches("(?i:[0-9]+.[0-9]+.[0-9]+.png)")) {
				if (_log.isDebugEnabled()) {
					_log.debug("Skipping scan of document_thumbnail folder file: " + file.getAbsolutePath());	
				}
				
				return;
			}
		}
		
		int exitValue = 0;

		try {
			exitValue = _execute("clamdscan", file);
		}
		catch (InterruptedException | IOException e) {
			if (_log.isDebugEnabled()) {
				_log.debug("Unable to successfully execute clamdscan", e);
			}

			exitValue = -1;
		}
		
		if (_log.isDebugEnabled()) {
			_log.debug("clamdscan file: " + file.getAbsolutePath() + ", exitValue: " + exitValue);
		}
		
		try {
			if ((exitValue != 0) && (exitValue != 1)) {
				exitValue = _execute("clamscan", file);
				
				if (_log.isDebugEnabled()) {
					_log.debug("clamscan file: " + file.getAbsolutePath() + ", exitValue: " + exitValue);
				}
			}

			if (exitValue == 1) {
				throw new AntivirusScannerException(
					"Virus detected in " + file.getAbsolutePath(),
					AntivirusScannerException.VIRUS_DETECTED);
			}
			else if (exitValue >= 2) {
				throw new AntivirusScannerException(
					AntivirusScannerException.PROCESS_FAILURE);
			}
		}
		catch (InterruptedException | IOException e) {
			if (_log.isDebugEnabled()) {
				_log.debug("Unable to successfully execute clamscan", e);
			}

			throw new AntivirusScannerException(
				AntivirusScannerException.PROCESS_FAILURE, e);
		}
	}

	private int _execute(String command, File file)
		throws InterruptedException, IOException {

		if (_log.isDebugEnabled()) {
			_log.debug("_execute command: " + command + ", file: " + file.getAbsolutePath());	
		}
		
		Process process = null;

		try {
			ProcessBuilder processBuilder = new ProcessBuilder(
				command, "--stdout", "--no-summary", file.getAbsolutePath());

			processBuilder.redirectErrorStream(true);

			process = processBuilder.start();

			process.waitFor();

			return process.exitValue();
		}
		finally {
			if (process != null) {
				process.destroy();
			}
		}
	}
	
	private static final String PREVIEW_TMP_PATH = DLPreviewableProcessor.PREVIEW_TMP_PATH.replace("/", "\\");
	
	private static final String THUMBNAIL_TMP_PATH = DLPreviewableProcessor.THUMBNAIL_TMP_PATH.replace("/", "\\");
	
	private static final Log _log = LogFactoryUtil.getLog(
			CustomClamAntivirusScannerImpl.class);

}