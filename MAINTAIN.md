Project Maintenance
=================

Publishing to Maven Central Repository:

1. Create a Jira account by signing up at: https://issues.sonatype.org/secure/Signup!default.jspa
2. Create a new project ticket at: https://issues.sonatype.org/secure/CreateIssue.jspa?issuetype=21&pid=10134 (make sure the Issue Type is *New Project*)
3. Generate a PGP Signature and distribute it to one or more public key servers. To generate a PGP Signature:
   ```
   $ gpg2 --gen-key

   $ gpg: key YOUR_KEY_ID marked as ultimately trusted
   ```
   To distribute:
   ```
   $ gpg2 --keyserver SERVER_NAME --send-keys YOUR_KEY_ID
   ```

4. Find your ~.m2 folder (this folder is hidden on some systems)
5. Update or create your settings.xml to contain your Jira and public key information:
```xml
<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>jira_username</username>
      <password>jira_password</password>
    </server>
  </servers>
  <profiles>
    <profile>
      <id>ossrh</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <properties>
        <gpg.executable>gpg2</gpg.executable>
        <gpg.passphrase>your_key_passphrase</gpg.passphrase>
      </properties>
    </profile>
  </profiles>
</settings>
```
6. Run Maven from the project directory
```
$ mvn clean deploy
```
7. Login to nexus repository manager using your Jira account created in step 1 (https://s01.oss.sonatype.org/#welcome)
8. Click on *Staging Repositories* on the left sidebar, your staging repository should be available
9. Click on your staging repository and ensure it contains the correct contents, click *Close*, include a relevant description
10. Wait a few seconds for Sonatype to buffer
11. Click *Release*, include a relevant description
12. Comment your Jira ticket to sync Maven Central with your Group ID

Congratulations! You have released to Maven Central Repository. The search query should sync within a few hours to a day.