# Конфигурация

В первую очередь необходимо обновить файл `application.properties` в папке `resources`, где необходимо указать
информацию о базах данных и о боте.</br>

Также существует файл `app.properties`, который будет генерироваться в папке с .jar файлом. Данные, которые должны там
храниться, а именно названия полей и значения по умолчанию, находятся в классе `core.config.ServerProperties`.

# Ответы

### Хранение

Тексты ответов не должны хранится в коде напрямую. Его следует хранить в специальных XML файлах. Они хранятся в
папке `dictionary` в директории с .jar файлом.</br>
Каждый файл должен называться следующим образом: `dictionary.langcode.xml`, где `langcode` – некоторый код языка,
например `ru` или `en`.</br>
Правила хранения данных подробно описаны на русском в файле `dictionary.ru.xml` и на английском в
файле `dictionary.en.xml`.</br>

### Получение данных

Для получения данных следует использовать класс `DictProvider`. Чтобы получить необходимый ответ, нужно указать
требуемый язык, а также уникальный ключ. Для каждого ключа из стандартного .xml файла генерируется константа в
классе `Replies` и `Words`.

```kotlin
val greetingReply = dictProvider.reply()
    .language(user.languageCode)
    .key(Replies.START)
    .get()
```

### Аргументы

В случае, если в ответе должны присутствовать аргументы, для них генерируется специальный класс-Builder.

```kotlin
val langArgs = SelectLangArgumentsBuilder()
    .supportedLangs(getSupportedLangsText())
    .build()
val langReply = dictProvider.reply()
    .language(user.languageCode)
    .key(Replies.SELECT_LANG)
    .args(*langArgs)
    .get()
```

# Обработчики

Взаимодействие с Telegram-ботом происходит в формате обычного диалога в Telegram чате, поэтому сервер постоянно будет
обрабатывать сообщения, присылаемые пользователем.</br>
За обработку таких сообщений отвечает обработчик (`Handler`). Его цель – в ответ на некоторое сообщение вернуть
результат, который можно будет отправить пользователю.</br>

## Создание

Для создания обработчика необходимо лишь создать класс, наследующийся от одного из абстрактных обработчиков, и пометить
его аннотацией `@Service`, а также передать соответствующие аргументы.

## Типы обработчиков

Подразумевается, что пользователь может прислать два типа сообщений: команду, начинающуюся с символа `/`, или же любой
другой контент, будь то текст, изображение, и так далее.</br>

### Команда

Команда может сразу выполнить необходимое действие. Команду обрабатывает `CommandHandler`.</br>
Пример команды – `/start`. В ответ пользователь получает лишь приветственное сообщение, дальнейшие действия не
нужны.</br>

```kotlin
@Service
class StartHandler : CommandHandler(COMMAND) {

    companion object {
        const val COMMAND = "start"
    }

    override fun safeHandle(user: User, message: Message): SendMessage {
        // ...
    }
}
```

### Активность

Активность необходима, чтобы, когда пользователь прислал некоторое сообщение, можно было однозначно установить, какие
данные пришли и продолжить работу. Сообщения обрабатывает `ActivityMessageHandler`.

```kotlin
@Service
class LanguageHandler : ActivityMessageHandler(ACTIVITY) {

    companion object {
        private val ACTIVITY = Activity.SELECT_LANGUAGE
    }

    override fun safeHandle(user: User, message: Message): SendMessage {
        // ...
    }
}
```

### Триггер

Команда также может выступать в роли **триггера**, в таком случае в результате выполнения она лишь присвоит пользователю
некоторую активность и попросит прислать некоторые данные для продолжения работы. Для их обработки существует
класс `AbstractTrigger`.</br>
Пример триггера – команда `/lang`. В ответ пользователь получает сообщение с возможными языками, на этом действие не
закончилось, от пользователя ожидается следующее сообщение, поэтому ему присваивается соответствующая активность
– `SELECT_LANGUAGE`

```kotlin
@Service
class LanguageTrigger(
    userRepository: UserRepository
) : AbstractTrigger(
    userRepository,
    cause = COMMAND,
    result = ACTIVITY
) {

    companion object {
        private const val COMMAND = "lang"
        private val ACTIVITY = Activity.SELECT_LANGUAGE
    }

    override fun afterHandle(user: User): SendMessage {
        // ...
    }
}
```

### Определенный контент

Помимо команд можно отлавливать сообщения, удовлетворяющие некоторому условию. Их обрабатывает `ConditionMessageHandler`
.

```kotlin
@Service
class LocationHandler : ConditionMessageHandler(condition) {

    companion object {
        val condition: (User, Message) -> Boolean = { _, message ->
            message.location != null
        }
    }

    override fun saveHandle(user: User, message: Message): SendMessage {
        // ...
    }
}
```
