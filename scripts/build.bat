cd ../src
javac *.java

TASKKILL /IM rmiregistry.exe /F
start rmiregistry.exe

cd ../scripts