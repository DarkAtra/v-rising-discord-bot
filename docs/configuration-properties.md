---
title: Configuration Properties
nav_order: 6
---

# Configuration Properties

All properties listed in the following section can be set via environment variables, as command-line arguments or in properties files.
For example, the `bot.discord-bot-token` property can be set using:

* Environment: `BOT_DISCORD_BOT_TOKEN=example`
* Command-line argument: `--bot.discord-bot-token=example`
* In `application.yml`: `bot.discord-bot-token: example`

### `bot.discord-bot-token`

The token that the bot uses to authenticate to discord.
You can find this in the [discord developer portal](https://discord.com/developers/applications).

<div class="inline-table">

| Type   | Default value |
|--------|---------------|
| String | `null`        |

</div>

### `bot.database-path`

The path to the database file. The bot will attempt to create this file if it does not exist.
All data stored in the database is encrypted using the `bot.database-username` and `bot.database-password` properties.
Should be overwritten when running inside a docker container.

<div class="inline-table">

| Type | Default value |
|------|---------------|
| Path | `./bot.db`    |

</div>

### `bot.database-username`

The username for the database.

<div class="inline-table">

| Type   | Default value          |
|--------|------------------------|
| String | `v-rising-discord-bot` |

</div>

### `bot.database-password`

The password for the database. It is recommended to use a strong password.

<div class="inline-table">

| Type   | Default value |
|--------|---------------|
| String | `null`        |

</div>

### `bot.update-delay`

The delay between update attempts for the status embed, player activity feed, pvp kill feed and leaderboards.
Cannot be less than 5 seconds.

<div class="inline-table">

| Type     | Default value |
|----------|---------------|
| Duration | `1m`          |

</div>

### `bot.max-failed-attempts`

Defines after how many attempts the bot will automatically set the status for the status embed to `INACTIVE`.
The status embed is no longer updated if it is in status `INACTIVE`.
Use `0` to disable this functionality.

<div class="inline-table">

| Type | Default value |
|------|---------------|
| Int  | `0`           |

</div>

### `bot.max-failed-api-attempts`

Defines after how many attempts the bot will automatically set the status for the failing bot companion feature to `INACTIVE`.
The player activity feed, pvp kill feed and leaderboards are no longer updated if they are in status `INACTIVE`.
Use `0` to disable this functionality.

<div class="inline-table">

| Type | Default value |
|------|---------------|
| Int  | `0`           |

</div>

### `bot.max-recent-errors`

The maximum number of error messages to keep for debugging issues.
This limit applies to the following commands:

* `/get-player-activity-feed-details`
* `/get-pvp-kill-feed-details`
* `/get-status-monitor-details`

Use `0` if you don't want to persist any error messages for debugging purposes.

<div class="inline-table">

| Type | Default value |
|------|---------------|
| Int  | `5`           |

</div>

### `bot.max-characters-per-error`

The character limit for each error message that is persisted for debugging purposes.
This limit applies to the following commands:

* `/get-player-activity-feed-details`
* `/get-pvp-kill-feed-details`
* `/get-status-monitor-details`

<div class="inline-table">

| Type | Default value |
|------|---------------|
| Int  | `200`         |

</div>

### `bot.allow-local-address-ranges`

Whether addresses from reserved ip ranges are permitted when adding or updating servers.
It's recommended to set this to `false` if you attempt to provide this bot as a service for others.

<div class="inline-table">

| Type    | Default value |
|---------|---------------|
| Boolean | `true`        |

</div>

### `bot.admin-user-ids`

A list of admin user ids.
Admins are allowed to send direct messages to the bot to issue commands.
Commands are no longer guild specific in this context.

For example, if an admin uses the `/list-servers` command in a DM, the bot responds
with a list of all server status monitors and not only the ones for a specific discord guild.

<div class="inline-table">

| Type        | Default value |
|-------------|---------------|
| Set<String> | `[]`          |

</div>

### `bot.cleanup-job-enabled`

Whether the bot should automatically delete `INACTIVE` server monitors once a day.
The cleanup job will run at midnight UTC and deletes all server that have been in status `INACTIVE` for more than 7 days.

<div class="inline-table">

| Type    | Default value |
|---------|---------------|
| Boolean | `false`       |

</div>

### `bot.database-backup-job-enabled`

Whether the bot should automatically create a database backup once a day.
The backup job will run at `23:45` UTC.

<div class="inline-table">

| Type    | Default value |
|---------|---------------|
| Boolean | `false`       |

</div>

### `bot.database-backup-directory`

Which directory to store database backups in.
Should be overwritten when running inside a docker container.

<div class="inline-table">

| Type | Default value         |
|------|-----------------------|
| Path | `./database-backups/` |

</div>

### `bot.database-backup-max-files`

The maximum amount of recent backups to keep.

<div class="inline-table">

| Type | Default value |
|------|---------------|
| Int  | `10`          |

</div>
