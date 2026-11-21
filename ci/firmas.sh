#!/usr/bin/env bash
# Firmas publicas y protegidas (solo nombres de metodos y campos, con javap) de las clases de Minecraft/Forge que
# se van a usar, para programar contra la API exacta de 1.20.1.
JAR=$(find ~/.gradle/caches/forge_gradle -name "forge-*_mapped_official_*.jar" ! -name "*sources*" 2>/dev/null | head -1)
[ -z "$JAR" ] && { echo "sin jar de minecraft"; exit 0; }
echo "jar: $JAR"
CP="$JAR"
for d in $(find ~/.gradle/caches/modules-2/files-2.1 -name "*.jar" 2>/dev/null | grep -E "eventbus|forgespi|fmlcore|javafmllanguage|joml|brigadier|datafixerupper|authlib|guava" ); do CP="$CP:$d"; done
while read -r c; do
  [ -z "$c" ] && continue
  case "$c" in \#*) continue;; esac
  echo "===== $c"
  javap -cp "$CP" -protected "$c" 2>&1 | head -1500
done < ci/firmas.txt
