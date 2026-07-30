# Remove Redundant PNG Icons

The project now uses `.webp` versions of the app icons in the `mipmap` folders. This task involves removing the old `.png` files to clean up the resource directories.

## User Review Required

> [!IMPORTANT]
> I will be deleting all `.png` files in the `mipmap-*` directories. `.webp` versions already exist in these folders, so the app's appearance will not be affected.

## Proposed Changes

### Resources

#### [DELETE] [ic_launcher.png](file:///E:/Learn_and_Build/FundFlow/app/src/main/res/mipmap-hdpi/ic_launcher.png)
#### [DELETE] [ic_launcher_foreground.png](file:///E:/Learn_and_Build/FundFlow/app/src/main/res/mipmap-hdpi/ic_launcher_foreground.png)
#### [DELETE] [ic_launcher_round.png](file:///E:/Learn_and_Build/FundFlow/app/src/main/res/mipmap-hdpi/ic_launcher_round.png)
#### [DELETE] [ic_launcher.png](file:///E:/Learn_and_Build/FundFlow/app/src/main/res/mipmap-ldpi/ic_launcher.png)
#### [DELETE] [ic_launcher_round.png](file:///E:/Learn_and_Build/FundFlow/app/src/main/res/mipmap-ldpi/ic_launcher_round.png)
#### [DELETE] [ic_launcher.png](file:///E:/Learn_and_Build/FundFlow/app/src/main/res/mipmap-mdpi/ic_launcher.png)
#### [DELETE] [ic_launcher_foreground.png](file:///E:/Learn_and_Build/FundFlow/app/src/main/res/mipmap-mdpi/ic_launcher_foreground.png)
#### [DELETE] [ic_launcher_round.png](file:///E:/Learn_and_Build/FundFlow/app/src/main/res/mipmap-mdpi/ic_launcher_round.png)
#### [DELETE] [ic_launcher.png](file:///E:/Learn_and_Build/FundFlow/app/src/main/res/mipmap-xhdpi/ic_launcher.png)
#### [DELETE] [ic_launcher_foreground.png](file:///E:/Learn_and_Build/FundFlow/app/src/main/res/mipmap-xhdpi/ic_launcher_foreground.png)
#### [DELETE] [ic_launcher_round.png](file:///E:/Learn_and_Build/FundFlow/app/src/main/res/mipmap-xhdpi/ic_launcher_round.png)
#### [DELETE] [ic_launcher.png](file:///E:/Learn_and_Build/FundFlow/app/src/main/res/mipmap-xxhdpi/ic_launcher.png)
#### [DELETE] [ic_launcher_foreground.png](file:///E:/Learn_and_Build/FundFlow/app/src/main/res/mipmap-xxhdpi/ic_launcher_foreground.png)
#### [DELETE] [ic_launcher_round.png](file:///E:/Learn_and_Build/FundFlow/app/src/main/res/mipmap-xxhdpi/ic_launcher_round.png)
#### [DELETE] [ic_launcher.png](file:///E:/Learn_and_Build/FundFlow/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png)
#### [DELETE] [ic_launcher_foreground.png](file:///E:/Learn_and_Build/FundFlow/app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.png)
#### [DELETE] [ic_launcher_round.png](file:///E:/Learn_and_Build/FundFlow/app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png)

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to ensure the project still builds correctly without the PNG files.

### Manual Verification
- Check the `mipmap` folders to ensure only `.webp` files remain.
