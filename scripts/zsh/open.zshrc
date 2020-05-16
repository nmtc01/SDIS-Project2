#!/usr/bin/env zsh

killall -9 rmiregistry
cd ..
cd ..
cd out/production/sdis1920-t3g22/
rmiregistry &

#Chord initiator
osascript -e 'tell app "Terminal"
   do script "cd Desktop/SDIS/sdis1920-t3g22/out/production/sdis1920-t3g22/
java Peer 1 1 10.227.161.149 8000"
end tell'

osascript -e 'tell app "Terminal"
   do script "cd Desktop/SDIS/sdis1920-t3g22/out/production/sdis1920-t3g22/
java Peer 1 1 10.227.161.150 8000  10.227.161.149 8000"
end tell'

# VER PROVAVELMENTE QUEREMOS COLOCAR ISTO AO ABRIR OS PEERS "-Djavax.net.ssl.keyStore=keystore" "-Djavax.net.ssl.keyStorePassword=sdis1822"


  #java "-Djavax.net.ssl.keyStore=keystore" "-Djavax.net.ssl.keyStorePassword=sdis1822" "-Djavax.net.ssl.trustStore=truststore" "-Djavax.net.ssl.trustStorePassword=sdis1822" Node 10.227.161.149 8000 10.227.161.149 8000