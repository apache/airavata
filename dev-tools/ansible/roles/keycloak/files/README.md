## Generating Keystore for SSL certificates

**Note:** will only work on Mac and Linux

### Files Needed:
1. SSL certificate for the domain (ex: .cer or .crt or .pem)
2. Private key used to acquire that SSL Certificate, i.e. key used to create certificate request (.key).

### Process:
1. Place both file in airavata/dev-tools/roles/keycloak/files
2. The first step is to convert them into a single PKCS12 file using the following command, You will be asked for various passwords (the password to access the key (if set) and then the password for the PKCS12 file being created): 
``` 
openssl pkcs12 -export -in host.crt -inkey host.key > host.p12
```
3. Then import the PKCS12 file into a keystore using the command: 
``` 
keytool -importkeystore -srckeystore host.p12 -destkeystore keycloak.jks -srcstoretype pkcs12 
```

###Sample output:
```$shell
$ openssl pkcs12 -export -in host.crt -inkey host.key > host.p12
Enter pass phrase for host.key:
Enter Export Password:
Verifying - Enter Export Password:
```
```
$ keytool -importkeystore -srckeystore host.p12 -destkeystore host.jks
-srcstoretype pkcs12
Enter destination keystore password:  
Re-enter new password: 
Enter source keystore password:  
Entry for alias 1 successfully imported.
Import command completed:  1 entries successfully imported, 0 entries failed
or cancelled
```