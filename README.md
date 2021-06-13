# overview

This POC shares the idea that how to have dashboard/profile concept at application level, that is displaying data specfic to logged-in user.

![com-6ix-auth0-user-prov](https://static.swimlanes.io/63527387cb9a7fa40dd6a0eea010698d.png)

# pre-requisites
java jdk 1.7 or above (to download refer to- http://www.oracle.com/technetwork/java/javase/downloads/index.html)

# create jar
downlod/checkout source code into some directory. for example- com-6ix-auth0-user-prov
open terminal, change directory to com-6ix-auth0-user-prov

	gradlew build

after successful build, go to com-6ix-auth0-user-prov/build/libs and copy com-6ix-auth0-user-prov-1.0.jar to directory from where you want to run java component.

#  run com-6ix-auth0-user-prov

     java -jar com-6ix-auth0-user-prov-1.0.jar 

