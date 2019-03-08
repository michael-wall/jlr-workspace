

Jar: custom.clam.antivirus.scanner-1.0.0.jar
Source: custom-clam-antivirus-scanner_source.7z

Local developer environment deployment steps:

1. Stop Liferay DXP

2. Copy custom.clam.antivirus.scanner-1.0.0.jar to the [TOMCAT_ROOT]/lib/ext folder

3. Update property dl.store.antivirus.impl in portal-ext.properties to point to the new Scanner Implementation:

dl.store.antivirus.impl=com.liferay.clam.antivirus.custom.CustomClamAntivirusScannerImpl

4. Start Liferay DXP

5. Optionally set logging on class 'com.liferay.clam.antivirus.custom.CustomClamAntivirusScannerImpl' to DEBUG
