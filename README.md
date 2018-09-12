# ibmwcm-rssfeed-validator
IBM WCM RSS feed validator

IBM WCM RSS feed validator tools helps to validate the RSS Feed xml format, image URLs, element names. This helps to minimize the failures and troubleshooting time when the feed is used on WCI. Tool can be either used as a standalone executable jar or it can also be imported to local eclipse IDE and executed from there.

Features
--------
1. Checks whether the XML is well formatted.
2. Validates the ibmwcm elements tags
3. Verifies whether the image URLs used in the feed is correct
4. Results are logged in an external log file for troubleshooting purposes.
5. ibmwcm elements are configurable in the .properties file
 

Steps to use the tool
---------------------
1. Prerequisite - Java 7 and above, Maven tool should be installed & configured in your local machine. 
2. Execute the following maven command "mvn clean install" to generate the .jar file.
3. From the target directory execute the below command
4. java -jar ibmwcm-rssfeed-validator-1.0-jar-with-dependencies.jar "<< URL of the hosted RSS Feed xml >>"
