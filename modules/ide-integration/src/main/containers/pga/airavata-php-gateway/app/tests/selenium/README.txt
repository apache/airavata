###########################################################################
################JAVA TEST SCRIPTS FOR AIRAVATA PHP REFERENCE GATEWAY################
###########################################################################



###############Prerequisites To Run The Selenium Java Scripts##############
1. Firefox V 31.0 or 32.0 is required for the scripts.
https://support.mozilla.org/en-US/kb/install-older-version-of-firefox

2. Copy all PHP-Reference-Gateway application input files from 
https://iu.box.com/s/9ztdby709kso8siachz16svn2y511nn7 

3. Clone the airavata-php-gateway; 
“git clone https://github.com/apache/airavata-php-gateway.git”

4. In the folder structure go to app/tests/selenium

5. Follow instructions below to modify test configurations in exp.properties
###########################################################################


###############Environments available for Running the Scripts##############
Selenium test are executed through PHP-Reference-Gateway by giving the URL
Current Production URLs
Base URL: http://testdrive.airavata.org/portal/pga/public/
Sub URL: home
###########################################################################


###################Script Alterations & Execution Steps###################
1. Create a user account manually in the gateway using  https://testdrive.airavata.org/portal/pga/public/create

2. Open exp.properties file and add the Base URL and sub URL(both given above)

3. Add properties given below to exp.properties;
		
		i.	Username
		ii.	password
		iii.	Project Name
		iV.	Project Description
		V.	Experiment Name Extension
		VI.	Email addresses (thees are required to receive job notifications from resources)
		VII.	Your own local path to application input files 
	
4. In the command line after cloning and changing the properties execute “mvn clean install” 
When the code is built existing RunAllTests will execute. This will create a project and launch all application experiments in the gateway.  

5. Manual Execution Steps 
		i.	CreateUserLogin.java - Already done above
		ii.	In UserLogin class change the username and password to your own username and password
		iii.	CreateModifySearchProject.java - Create A Project, Modify and Search for the Project
		iV.	Run Experiment creation scripts at your preffered sequence 
		OR
		V. 	Use RunAllTests and run all the applications at once. 
		VI.	If you want to do load testing use ExpLoadTest class
		VII.	At the end run the SearchProjectExp.java to view your experiments (For the ease you can create all your experiments under one Project)
		VIII.	UserLogout.java
###########################################################################


############################Further Information############################
1. To record new Selenium scripts use Selenium IDE. Download from http://www.seleniumhq.org/download/

2. Record using IDE and export to java to modify and run

3. For clarifications, comments please use dev@airavata.apache.org or project@scigap.org

4. For more information on Selenium go to http://www.seleniumhq.org/docs/
###########################################################################


