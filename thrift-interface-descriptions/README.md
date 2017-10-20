1. https://github.com/satyamsah/airavata/wiki (Links to an external site.)Links to an external site.  link for wiki :

 I am working on developing a new feature inside airavata that is is to develop a new  go client. It will give new capability inside airavata to server new users who want to interact with airavata using an application written in go .

 

 

2. pull request for airavata core repo -  (https://github.com/apache/airavata (Links to an external site.)Links to an external site. ):

pull request number(119): https://github.com/apache/airavata/pull/119 (Links to an external site.)Links to an external site.


3. Jira story:

https://issues.apache.org/jira/browse/AIRAVATA-2522 (Links to an external site.)Links to an external site.




### To Do : GO client to get all the project under airavata

##### the main task is to replicate the task of Marcus: https://github.com/machristie/airavata-python3-client
1) clone the current forked airavata repository by:

 `git clone https://github.com/satyamsah/airavata`

2) install 'go' by following the link : https://tecadmin.net/install-go-on-ubuntu/
   1) lets say that the 'gopath' is '$HOME/Projects/Proj1/' and  make sure to note down the 'gopath' directory you set

3) `cd ~`

4)  Installing thrift repo. Install thrift by  'cloning' and 'following the README' in 
 the following repo "https://github.com/apache/thrift" : 

    1)  `git clone https://github.com/apache/thrift`
    2)   execute the steps to install thrift and follow readme.

 
4) `cd $GOPATH`
5) create a directory named `src` under which is inside gopath:

`mkdir src`

6) change directory to 'src' and 'thrift-lib'. This will store all the native thrift go stubs which  are required to run go program:

`cd src`

`mkdir -p git.apache.org/thrift.git/lib/go/thrift`

`cd git.apache.org/thrift.git/lib/go/thrift`

6) copy all the go stubs located inside '~/thrift/lib/go/thrift'(Thrift-directory) to 'src' subfolder of 'thrift-lib'( gopath--src directory)

`cp -r ~/thrift/lib/go/thrift/* .`

7) Airavata-repo: go to 'airavata/tools/go-tools':

`cd ~/airavata/tools/go-tools`

8) type to generate new folder 'go-gen' : 

#`thrift -r --gen go airavata-apis/airavata_api.thrift`

9) copy all the go stubs generated in the 'go-gen' folder to 'src' folder of go-path:

`cp -r gen-go/ $GOPATH/src/ `


11) `chmod 777 build.sh` 

12) If you don't have testdrive dev  credentials , contact me. I will share my credentials.:

   `./build.sh <your-testdrive-dev-username> <your-testdrive-dev-password>`

13) You will get the list of projects in json format. Copy the json output

14) Use any of the json formatter to see the result.
