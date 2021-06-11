WiFiStreamScreen Android app

## Инструкция к подключению:
1. Скачать zip архив проекта
2. Распаковать его в папку с вашим проектом
3. В файл setting.dradle добавить
```
include(":проект", ":data")
```
4. Для загрузки всех зависимостей в build.gradle всего проекта добавить:
```
allprojects {
    repositories {
        google()
        maven { url = uri("https://jitpack.io") }
        jcenter()
    }
}
```
## Как использовать:
 - Для изменения параметров и прочих настроек обмена визуальной информации
```
import com.sarbaevartur.wifistreamscreen.data.settings.Settings
```
