#!/usr/bin/env zsh

killall -9 rmiregistry
cd ..
cd ..
cd out/production/sdis1920-t3g22/
rmiregistry &

#Chord initiator
osascript -e 'tell app "Terminal"
   do script "cd Desktop/SDIS/sdis1920-t3g22/out/production/sdis1920-t3g22/
java -Djavax.net.ssl.keyStore=keystore -Djavax.net.ssl.keyStorePassword=sdis1920 -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=sdis1920 Peer 1 1 84.90.128.213 8000"
end tell'

osascript -e 'tell app "Terminal"
   do script "cd Desktop/SDIS/sdis1920-t3g22/out/production/sdis1920-t3g22/
java -Djavax.net.ssl.keyStore=keystore -Djavax.net.ssl.keyStorePassword=sdis1920 -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=sdis1920 Peer 1 1 84.90.128.213 8001  84.90.128.213 8000"
end tell'

# java -Djavax.net.ssl.keyStore=<filename1> -Djavax.net.ssl.keyStorePassword=<password1> \
 #    -Djavax.net.ssl.trustStore=<filename2> -Djavax.net.ssl.trustStorePassword=<password2>


# VER PROVAVELMENTE QUEREMOS COLOCAR ISTO AO ABRIR OS PEERS "-Djavax.net.ssl.keyStore=keystore" "-Djavax.net.ssl.keyStorePassword=sdis1822"


  #java "-Djavax.net.ssl.keyStore=keystore" "-Djavax.net.ssl.keyStorePassword=sdis1822" "-Djavax.net.ssl.trustStore=truststore" "-Djavax.net.ssl.trustStorePassword=sdis1822" Node 10.227.161.149 8000 10.227.161.149 8000