all: pom.xml \
	src/main/java/sh/blockparty/template/spigotplugin/SpigotPlugin.java\
	src/main/java/sh/blockparty/template/spigotplugin/MpxCommand.java
	mvn package

install: all
	cp target/Minecraft_SLP-0.1-SNAPSHOT.jar ../plugins
