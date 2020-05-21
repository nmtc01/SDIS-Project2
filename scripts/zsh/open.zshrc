#!/usr/bin/env zsh

killall -9 rmiregistry
cd ..
cd ..
cd out/production/sdis1920-t3g22/
rmiregistry &

#Chord initiator
osascript -e 'tell app "Terminal"
   do script "cd Desktop/SDIS/sdis1920-t3g22/out/production/sdis1920-t3g22/
java -Djavax.net.ssl.keyStore=keystore -Djavax.net.ssl.keyStorePassword=sdis1920 -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=sdis1920 Peer 1 127.0.0.1 9000"
end tell'

sleep 2

osascript -e 'tell app "Terminal"
   do script "cd Desktop/SDIS/sdis1920-t3g22/out/production/sdis1920-t3g22/
java -Djavax.net.ssl.keyStore=keystore -Djavax.net.ssl.keyStorePassword=sdis1920 -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=sdis1920 Peer 2 127.0.0.1 9001 127.0.0.1 9000"
end tell'

sleep 2

osascript -e 'tell app "Terminal"
   do script "cd Desktop/SDIS/sdis1920-t3g22/out/production/sdis1920-t3g22/
java -Djavax.net.ssl.keyStore=keystore -Djavax.net.ssl.keyStorePassword=sdis1920 -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=sdis1920 Peer 3 127.0.0.1 9002 127.0.0.1 9001"
end tell'

sleep 2

osascript -e 'tell app "Terminal"
   do script "cd Desktop/SDIS/sdis1920-t3g22/out/production/sdis1920-t3g22/
java -Djavax.net.ssl.keyStore=keystore -Djavax.net.ssl.keyStorePassword=sdis1920 -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=sdis1920 Peer 4 127.0.0.1 9003 127.0.0.1 9002"
end tell'


# 192.168.1.15

# 84.90.128.213

# 127.0.0.1