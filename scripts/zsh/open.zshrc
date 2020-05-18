#!/usr/bin/env zsh

killall -9 rmiregistry
cd ..
cd ..
cd out/production/sdis1920-t3g22/
rmiregistry &

#Chord initiator
osascript -e 'tell app "Terminal"
   do script "cd Desktop/SDIS/sdis1920-t3g22/out/production/sdis1920-t3g22/
java -Djavax.net.ssl.keyStore=keystore -Djavax.net.ssl.keyStorePassword=sdis1920 -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=sdis1920 Peer 1 84.90.128.213 8000 0"
end tell'

osascript -e 'tell app "Terminal"
   do script "cd Desktop/SDIS/sdis1920-t3g22/out/production/sdis1920-t3g22/
java -Djavax.net.ssl.keyStore=keystore -Djavax.net.ssl.keyStorePassword=sdis1920 -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=sdis1920 Peer 2 84.90.128.213 8001 84.90.128.213 8000 4"
end tell'

osascript -e 'tell app "Terminal"
   do script "cd Desktop/SDIS/sdis1920-t3g22/out/production/sdis1920-t3g22/
java -Djavax.net.ssl.keyStore=keystore -Djavax.net.ssl.keyStorePassword=sdis1920 -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=sdis1920 Peer 3 84.90.128.213 8002 84.90.128.213 8000 10"
end tell'

osascript -e 'tell app "Terminal"
   do script "cd Desktop/SDIS/sdis1920-t3g22/out/production/sdis1920-t3g22/
java -Djavax.net.ssl.keyStore=keystore -Djavax.net.ssl.keyStorePassword=sdis1920 -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=sdis1920 Peer 4 84.90.128.213 8003 84.90.128.213 8000 13"
end tell'

# 192.168.1.15

# 84.90.128.213

# 127.0.0.1