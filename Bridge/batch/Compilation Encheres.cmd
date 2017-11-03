@echo off
echo Compilation des encheres
cd ..
del enchere.bin
cd enchere
java -classpath ..\bin bridgeBid.Compiler $encheres.txt ..\enchere.bin >..\log\compil.log
if errorlevel 1 goto ERREUR
cd ..
echo Decompilation des encheres pour controle
java -classpath ./bin bridgeBid.Decompiler  enchere.bin  >log\decompilEnchere.txt
echo Recompilation des encheres decompilees pour controle
java -classpath ./bin bridgeBid.Compiler log\decompilEnchere.txt log\decompilEnchere.bin >log\decompil.log
rem copy enchere.bin .\src
rem copy enchere.bin .\bin
comp enchere.bin log\decompilEnchere.bin
rem notepad log\compil.log
goto :EOF
:ERREUR
echo Erreur de compilation
cd ..
notepad log\compil.log
pause