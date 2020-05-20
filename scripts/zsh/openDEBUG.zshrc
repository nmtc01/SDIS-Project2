#!/usr/bin/env zsh

killall -9 rmiregistry
cd ..
cd ..
cd out/production/sdis1920-t3g22/
rmiregistry &

#Chord initiator
osascript -e 'tell app "Terminal"
   do script "cd Desktop/SDIS/sdis1920-t3g22/out/production/sdis1920-t3g22/
java -Djavax.net.ssl.keyStore=keystore -Djavax.net.ssl.keyStorePassword=sdis1920 -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=sdis1920 Peer 1 192.168.1.15 8000 0"
end tell'

sleep 1

osascript -e 'tell app "Terminal"
   do script "cd Desktop/SDIS/sdis1920-t3g22/out/production/sdis1920-t3g22/
java -Djavax.net.ssl.keyStore=keystore -Djavax.net.ssl.keyStorePassword=sdis1920 -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=sdis1920 Peer 2 192.168.1.15 8001 192.168.1.15 8000 5"
end tell'

sleep 1

osascript -e 'tell app "Terminal"
   do script "cd Desktop/SDIS/sdis1920-t3g22/out/production/sdis1920-t3g22/
java -Djavax.net.ssl.keyStore=keystore -Djavax.net.ssl.keyStorePassword=sdis1920 -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=sdis1920 Peer 2 192.168.1.15 8002 192.168.1.15 8001 10"
end tell'

sleep 1

osascript -e 'tell app "Terminal"
   do script "cd Desktop/SDIS/sdis1920-t3g22/out/production/sdis1920-t3g22/
java -Djavax.net.ssl.keyStore=keystore -Djavax.net.ssl.keyStorePassword=sdis1920 -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=sdis1920 Peer 2 192.168.1.15 8003 192.168.1.15 8002 15"
end tell'



# 192.168.1.15

# 84.90.128.213

# 127.0.0.1