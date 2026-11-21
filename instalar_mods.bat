@echo off
chcp 65001 >nul
title Instalador de mods - Ciudadela Gotica
echo ==========================================================
echo   Instalador de mods para la Ciudadela Gotica (Forge 1.20.1)
echo ==========================================================
echo.
echo ANTES: en TLauncher instala y abre una vez la version "Forge 1.20.1".
echo.
pause
set "MC=%APPDATA%\.minecraft"
set "MODS=%MC%\mods"
if not exist "%MODS%" mkdir "%MODS%"
if exist "%MODS%\*.jar" (
  echo Moviendo tus mods antiguos a la carpeta mods_antiguos ...
  if not exist "%MC%\mods_antiguos" mkdir "%MC%\mods_antiguos"
  move /Y "%MODS%\*.jar" "%MC%\mods_antiguos\" >nul
)
echo.
echo [1/19] mcw-furniture-3.4.1-mc1.20.1forge.jar
powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol='Tls12'; Invoke-WebRequest -UseBasicParsing -Uri 'https://cdn.modrinth.com/data/dtWC90iB/versions/mvGf4LNK/mcw-furniture-3.4.1-mc1.20.1forge.jar' -OutFile '%MODS%\mcw-furniture-3.4.1-mc1.20.1forge.jar'"
echo [2/19] mcw-roofs-2.3.2-mc1.20.1forge.jar
powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol='Tls12'; Invoke-WebRequest -UseBasicParsing -Uri 'https://cdn.modrinth.com/data/B8jaH3P1/versions/31e80GhE/mcw-roofs-2.3.2-mc1.20.1forge.jar' -OutFile '%MODS%\mcw-roofs-2.3.2-mc1.20.1forge.jar'"
echo [3/19] mcw-mcwwindows-2.4.2-mc1.20.1forge.jar
powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol='Tls12'; Invoke-WebRequest -UseBasicParsing -Uri 'https://cdn.modrinth.com/data/C7I0BCni/versions/SSIlzrPf/mcw-mcwwindows-2.4.2-mc1.20.1forge.jar' -OutFile '%MODS%\mcw-mcwwindows-2.4.2-mc1.20.1forge.jar'"
echo [4/19] mcw-lights-1.1.5-mc1.20.1forge.jar
powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol='Tls12'; Invoke-WebRequest -UseBasicParsing -Uri 'https://cdn.modrinth.com/data/w4an97C2/versions/H1a9Tx4h/mcw-lights-1.1.5-mc1.20.1forge.jar' -OutFile '%MODS%\mcw-lights-1.1.5-mc1.20.1forge.jar'"
echo [5/19] mcw-mcwpaths-1.1.1-mc1.20.1forge.jar
powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol='Tls12'; Invoke-WebRequest -UseBasicParsing -Uri 'https://cdn.modrinth.com/data/VRLhWB91/versions/yHkcTSFb/mcw-mcwpaths-1.1.1-mc1.20.1forge.jar' -OutFile '%MODS%\mcw-mcwpaths-1.1.1-mc1.20.1forge.jar'"
echo [6/19] mcw-bridges-3.1.2-mc1.20.1forge.jar
powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol='Tls12'; Invoke-WebRequest -UseBasicParsing -Uri 'https://cdn.modrinth.com/data/GURcjz8O/versions/KImk0Oo1/mcw-bridges-3.1.2-mc1.20.1forge.jar' -OutFile '%MODS%\mcw-bridges-3.1.2-mc1.20.1forge.jar'"
echo [7/19] mcw-mcwfences-1.2.1-mc1.20.1forge.jar
powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol='Tls12'; Invoke-WebRequest -UseBasicParsing -Uri 'https://cdn.modrinth.com/data/GmwLse2I/versions/HnyfcyJ9/mcw-mcwfences-1.2.1-mc1.20.1forge.jar' -OutFile '%MODS%\mcw-mcwfences-1.2.1-mc1.20.1forge.jar'"
echo [8/19] mcw-doors-1.1.5-mc1.20.1forge.jar
powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol='Tls12'; Invoke-WebRequest -UseBasicParsing -Uri 'https://cdn.modrinth.com/data/kNxa8z3e/versions/n8BlIUm3/mcw-doors-1.1.5-mc1.20.1forge.jar' -OutFile '%MODS%\mcw-doors-1.1.5-mc1.20.1forge.jar'"
echo [9/19] mcw-trapdoors-1.1.5-mc1.20.1forge.jar
powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol='Tls12'; Invoke-WebRequest -UseBasicParsing -Uri 'https://cdn.modrinth.com/data/n2fvCDlM/versions/5B4awHaA/mcw-trapdoors-1.1.5-mc1.20.1forge.jar' -OutFile '%MODS%\mcw-trapdoors-1.1.5-mc1.20.1forge.jar'"
echo [10/19] mcw-mcwstairs-1.0.2-mc1.20.1forge.jar
powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol='Tls12'; Invoke-WebRequest -UseBasicParsing -Uri 'https://cdn.modrinth.com/data/iP3wH1ha/versions/wL67k7Aj/mcw-mcwstairs-1.0.2-mc1.20.1forge.jar' -OutFile '%MODS%\mcw-mcwstairs-1.0.2-mc1.20.1forge.jar'"
echo [11/19] handcrafted-forge-1.20.1-3.0.6.jar
powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol='Tls12'; Invoke-WebRequest -UseBasicParsing -Uri 'https://cdn.modrinth.com/data/pJmCFF0p/versions/N7wZwOFy/handcrafted-forge-1.20.1-3.0.6.jar' -OutFile '%MODS%\handcrafted-forge-1.20.1-3.0.6.jar'"
echo [12/19] resourcefullib-forge-1.20.1-2.1.29.jar
powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol='Tls12'; Invoke-WebRequest -UseBasicParsing -Uri 'https://cdn.modrinth.com/data/G1hIVOrD/versions/OhsHaCcW/resourcefullib-forge-1.20.1-2.1.29.jar' -OutFile '%MODS%\resourcefullib-forge-1.20.1-2.1.29.jar'"
echo [13/19] supplementaries-1.20-3.1.45-forge.jar
powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol='Tls12'; Invoke-WebRequest -UseBasicParsing -Uri 'https://cdn.modrinth.com/data/fFEIiSDQ/versions/geO7x6Vi/supplementaries-1.20-3.1.45-forge.jar' -OutFile '%MODS%\supplementaries-1.20-3.1.45-forge.jar'"
echo [14/19] moonlight-1.20-2.16.39-forge.jar
powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol='Tls12'; Invoke-WebRequest -UseBasicParsing -Uri 'https://cdn.modrinth.com/data/twkfQtEc/versions/rF6052yd/moonlight-1.20-2.16.39-forge.jar' -OutFile '%MODS%\moonlight-1.20-2.16.39-forge.jar'"
echo [15/19] another_furniture-forge-1.20.1-3.0.4.jar
powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol='Tls12'; Invoke-WebRequest -UseBasicParsing -Uri 'https://cdn.modrinth.com/data/ulloLmqG/versions/S9tNKT5R/another_furniture-forge-1.20.1-3.0.4.jar' -OutFile '%MODS%\another_furniture-forge-1.20.1-3.0.4.jar'"
echo [16/19] BiomesOPlenty-forge-1.20.1-19.0.0.96.jar
powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol='Tls12'; Invoke-WebRequest -UseBasicParsing -Uri 'https://cdn.modrinth.com/data/HXF82T3G/versions/jxUqRzSD/BiomesOPlenty-forge-1.20.1-19.0.0.96.jar' -OutFile '%MODS%\BiomesOPlenty-forge-1.20.1-19.0.0.96.jar'"
echo [17/19] TerraBlender-forge-1.20.1-3.0.1.11.jar
powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol='Tls12'; Invoke-WebRequest -UseBasicParsing -Uri 'https://cdn.modrinth.com/data/kkmrDlKT/versions/IfcgopfA/TerraBlender-forge-1.20.1-3.0.1.11.jar' -OutFile '%MODS%\TerraBlender-forge-1.20.1-3.0.1.11.jar'"
echo [18/19] GlitchCore-forge-1.20.1-0.0.1.1.jar
powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol='Tls12'; Invoke-WebRequest -UseBasicParsing -Uri 'https://cdn.modrinth.com/data/s3dmwKy5/versions/pYPZ5MNI/GlitchCore-forge-1.20.1-0.0.1.1.jar' -OutFile '%MODS%\GlitchCore-forge-1.20.1-0.0.1.1.jar'"
echo [19/19] ciudadela-0.1.0.jar
powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol='Tls12'; Invoke-WebRequest -UseBasicParsing -Uri 'https://github.com/ferranxd7/ciudadela-gotica/releases/latest/download/ciudadela-0.1.0.jar' -OutFile '%MODS%\ciudadela-0.1.0.jar'"
echo.
echo ==========================================================
echo   LISTO. Abre TLauncher, elige "Forge 1.20.1" y juega.
echo   Crea un MUNDO NUEVO y escribe /ciudadela ir
echo ==========================================================
pause
