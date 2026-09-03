a very small and minimal PoC discord stack implementation written in java. uses [opencord](https://github.com/MateriiApps/OpenCord/) as blueprint.

## usage

1. clone this repo
2. create ```local.properties``` file with ```DISCORD_TOKEN=your_token``` inside of it
3. connect your device

on windows:

```batch
.\gradlew.bat installDebug
```

on linux/macos:

```shell
./gradlew installDebug
```

then open the app and look at logcat to see it working (or not)
