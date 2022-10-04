WiFiStreamScreen Android app

Приложение-библиотека позволяющая обмениваться визуальной информацией между устройствами в одной Wi-Fi сети.

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
 - Для использования параметров и прочих настроек технологии обмена визуальной информации
```
....
import com.sarbaevartur.wifistreamscreen.data.settings.Settings
```
 - Для изменения состояния сервера технологии обмена визуальной информации
```
import com.sarbaevartur.wifistreamscreen.data.state.AppStateMachine
```
 - Для начала обмена
```
....
import com.sarbaevartur.wifistreamscreen.service.helper.IntentAction
....
IntentAction.StartStream.sendToAppService(*context*)
....
```
 - Для остановки обмена
```
import com.sarbaevartur.wifistreamscreen.service.helper.IntentAction
....
IntentAction.StopStream.sendToAppService(*context*)
....
```
