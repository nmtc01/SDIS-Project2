TASKKILL /IM rmiregistry.exe /F

cd ../src
rm *.class
rmdir /s /q PeerProtocol
rmdir /s /q Storage

cd ../scripts