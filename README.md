# ibmwcm-rssfeed-validator
IBM WCM RSS feed validator

IBM WCM RSS feed validator tools helps to validate the RSS Feed xml format, image URLs, element names, etc.,. This helps to minimize the failures and troubleshooting time when the feed is used on WCI. Tool can either be used as a standalone executable jar or imported to a local eclipse IDE.

Features
--------
1. Checks whether the XML is well formatted.
2. Validates the ibmwcm elements tags
3. Verifies whether the image URLs used in the feed are correct
4. Logs validation results in a log file for troubleshooting purposes.
5. Additional ibmwcm elements are configurable in the .properties file
6. Downloads the images from the RSS feed in to local folder
7. Validates the final migrated article http URLs
 

Steps to use the tool
---------------------
1. Prerequisite - Java 7 and above, Maven tool should be installed & configured in your local machine. 
2. Execute the following maven command "mvn clean install" to generate the .jar file.
3. From the target directory execute the below command
4. java -jar ibmwcm-rssfeed-validator-1.0-jar-with-dependencies.jar "<< URL of the hosted RSS Feed xml >>"
