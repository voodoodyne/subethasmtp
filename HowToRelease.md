# How to Release #

  * update the `version` element in pom.xml, commit
  * check with `ant junit`
  * check with `mvn package`
  * `ant dist`
  * tag version in subversion
  * upload zip file to downloads, label it with `OpSys-All`, `Type-Archive`, `Featured`
  * remove `Featured` label from the download of the previous version
  * update ChangeLog
  * upload to Maven repository
  * announce the new version on the mail list
  * update the `version` element in pom.xml to the next version snapshot, e.g. `3.1.8-SNAPSHOT`.