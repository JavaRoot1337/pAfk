# pAfk

AFK reward zones with BossBar countdown and weekday/weekend payouts for Paper and Folia `1.21–26.4`.

## Compatibility

| Parameter | Value |
| --- | --- |
| Core | Paper, Folia (`folia-supported: true`) |
| Minecraft | `1.21 – 26.4` |
| Java | `21+` |
| Required | `WorldGuard` |

## Build

```powershell
.\gradlew.bat build
```

Output JAR: `build/libs/pAfk-1.0.0.jar`.

## Commands

| Command | Action | Permission |
| --- | --- | --- |
| `/pafk` or `/pafk status` | Show active sessions and region status | `afkzone.admin` |
| `/pafk reload` | Reload config and WorldGuard regions | `afkzone.admin` |
| `/afkreload` | Reload config shortcut | `afkzone.admin` |

## Permissions

| Permission | Purpose | Default |
| --- | --- | --- |
| `afkzone.admin` | Status and config reload | op |
| `afkzone.premium` | Access to premium AFK zone | op |

## Configuration

`plugins/pAfk/config.yml`:

- `settings` — `world-name`, `region-name`, `premium-region-name`, `reward-interval-sec`, weekday/weekend reward commands (`%player%`)
- `bossbar` — bar colors, `overlay`, titles, and `%time%` format (`%m%`, `%mm%`, `%s%`, `%ss%`)
- `messages` — zone titles, reload, and access messages
- `sound` — payout sound (`enabled`, `name`, `volume`, `pitch`)

[Русская документация](READMERU.md)
