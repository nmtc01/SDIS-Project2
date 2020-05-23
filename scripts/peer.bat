cd ../src

echo off
set access_point=%1
set port=%2
set join_port=%3

java -Djavax.net.ssl.keyStore=keystore -Djavax.net.ssl.keyStorePassword=sdis1920 -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=sdis1920 Peer %access_point% 127.0.0.1 %port% 127.0.0.1 %join_port%

cd ../scripts