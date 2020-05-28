CLEAN:

$ rm *.class
$ rmdir /s /q PeerProtocol
$ rmdir /s /q Storage

BUILD:

$ javac *.java
$ rmiregistry &

INITIATOR NODE:

$ java -Djavax.net.ssl.keyStore=keystore -Djavax.net.ssl.keyStorePassword=sdis1920 -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=sdis1920 Peer <access_point> 127.0.0.1 <port>

NODE

$ java -Djavax.net.ssl.keyStore=keystore -Djavax.net.ssl.keyStorePassword=sdis1920 -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=sdis1920 Peer <access_point> 127.0.0.1 <port> 127.0.0.1 <join_port>

TESTAPP:

$ java TestApp <access_point> <protocol> <operand_1>*

T3G22

Gustavo Nunes Ribeiro de Magalhães - up201705072@fe.up.pt
Nuno Miguel Teixeira Cardoso - up201706162@fe.up.pt
João Francisco de Pinho Brandão - up201705573@fe.up.pt
Tiago Gonçalves da Silva - up201705985@fe.up.pt

