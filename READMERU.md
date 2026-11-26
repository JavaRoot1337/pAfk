# pAfk

АФК-зоны на базе WorldGuard с BossBar-таймером и наградами по будням/выходным для Paper и Folia `1.21–26.4`.

## Совместимость

| Параметр | Значение |
| --- | --- |
| Ядро | Paper, Folia (`folia-supported: true`) |
| Minecraft | `1.21 – 26.4` |
| Java | `21+` |
| Обязательно | `WorldGuard` |

## Сборка

```powershell
.\gradlew.bat build
```

Итоговый JAR: `build/libs/pAfk-1.0.0.jar`.

## Команды

| Команда | Действие | Право |
| --- | --- | --- |
| `/pafk` или `/pafk status` | Показать активные сессии и статус зон | `afkzone.admin` |
| `/pafk reload` | Перезагрузить конфиг и регионы WorldGuard | `afkzone.admin` |
| `/afkreload` | Быстрая перезагрузка конфига | `afkzone.admin` |

## Права

| Право | Назначение | По умолчанию |
| --- | --- | --- |
| `afkzone.admin` | Статус и перезагрузка конфига | op |
| `afkzone.premium` | Доступ в премиум АФК-зону | op |

## Конфигурация

`plugins/pAfk/config.yml`:

- `settings` — `world-name`, `region-name`, `premium-region-name`, `reward-interval-sec`, команды наград для будней и выходных (`%player%`)
- `bossbar` — цвета, `overlay`, заголовки и формат `%time%` (`%m%`, `%mm%`, `%s%`, `%ss%`)
- `messages` — заголовки зон, сообщения перезагрузки и отказа доступа
- `sound` — звук выдачи награды (`enabled`, `name`, `volume`, `pitch`)

[English documentation](README.md)
