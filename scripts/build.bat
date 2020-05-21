mkdir ../out

cd ../src
javac -d ../out *.java

cd ../out
TASKKILL /IM rmiregistry.exe /F
start rmiregistry.exe

cd ../scripts