---
title: Discord Commands
nav_order: 4
---

# Discord Commands

Please note that all commands are [guild](https://discord.com/developers/docs/resources/guild) specific by default.
All optional command parameters can be reset by passing `~` as an argument.

### `/list-servers`

Lists all server status monitors.
Admins can use this command in DMs, see [Configuration Properties](configuration-properties.md) for details.

### `/add-server`

Adds a server to the status monitor.

| Parameter                         | Description                                                                                                                                                                                                                 | Required | Default value |
|-----------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|---------------|
| `server-hostname`                 | The hostname of the server to add a status monitor for.                                                                                                                                                                     | `true`   | `null`        |
| `server-query-port`               | The query port of the server to add a status monitor for.                                                                                                                                                                   | `true`   | `null`        |
| `server-api-hostname`             | The hostname to use when querying the server's api. Only required if you're planing to use the [v-rising-discord-bot-companion integration](bot-companion.md).                                                              | `false`  | `null`        |
| `server-api-port`                 | The api port of the server. Only required if you're planing to use the [v-rising-discord-bot-companion integration](bot-companion.md).                                                                                      | `false`  | `null`        |
| `server-api-username`             | The username used to authenticate to the api of the server. Only required if you're planing to use the [v-rising-discord-bot-companion integration](bot-companion.md).                                                      | `false`  | `null`        |
| `server-api-password`             | The password used to authenticate to the api of the server. Only required if you're planing to use the [v-rising-discord-bot-companion integration](bot-companion.md).                                                      | `false`  | `null`        |
| `embed-enabled`                   | Whether or not a discord status embed should be posted.                                                                                                                                                                     | `false`  | `true`        |
| `display-server-description`      | Whether or not to display the v rising server description on discord.                                                                                                                                                       | `false`  | `true`        |
| `display-player-gear-level`       | Whether or not to display each player's gear level. Only has an effect if `server-api-hostname` and `server-api-port` are set.                                                                                              | `false`  | `true`        |
| `player-activity-feed-channel-id` | The id of the channel to post the player activity feed in. Only has an effect if `server-api-hostname` and `server-api-port` are set.                                                                                       | `false`  | `null`        |
| `pvp-kill-feed-channel-id`        | The id of the channel to post the pvp kill feed in. Only has an effect if `server-api-hostname` and `server-api-port` are set. Requires at least version `0.4.0` of the [v-rising-discord-bot-companion](bot-companion.md). | `false`  | `null`        |

### `/update-server`

Updates the given server status monitor. Only parameters specified are updated, all other parameters remain unchanged.
Admins can use this command in DMs, see [Configuration Properties](configuration-properties.md) for details.

| Parameter                         | Description                                                                                                                                                                                                                 | Required | Default value |
|-----------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|---------------|
| `server-status-monitor-id`        | The id of the server status monitor to update.                                                                                                                                                                              | `true`   | `null`        |
| `server-hostname`                 | The hostname of the server to add a status monitor for.                                                                                                                                                                     | `false`  | `null`        |
| `server-query-port`               | The query port of the server to add a status monitor for.                                                                                                                                                                   | `false`  | `null`        |
| `server-api-hostname`             | The hostname to use when querying the server's api. Only required if you're planing to use the [v-rising-discord-bot-companion integration](bot-companion.md).                                                              | `false`  | `null`        |
| `server-api-port`                 | The api port of the server. Only required if you're planing to use the [v-rising-discord-bot-companion integration](bot-companion.md).                                                                                      | `false`  | `null`        |
| `server-api-username`             | The username used to authenticate to the api of the server. Only required if you're planing to use the [v-rising-discord-bot-companion integration](bot-companion.md).                                                      | `false`  | `null`        |
| `server-api-password`             | The password used to authenticate to the api of the server. Only required if you're planing to use the [v-rising-discord-bot-companion integration](bot-companion.md).                                                      | `false`  | `null`        |
| `status`                          | Determines if a server status monitor should be updated or not. Either `ACTIVE` or `INACTIVE`.                                                                                                                              | `false`  | `null`        |
| `embed-enabled`                   | Whether or not a discord status embed should be posted. Set this to false if you only want to use the activity or kill feed feature of the bot.                                                                             | `false`  | `true`        |
| `display-server-description`      | Whether or not to display the v rising server description on discord.                                                                                                                                                       | `false`  | `true`        |
| `display-player-gear-level`       | Whether or not to display each player's gear level. Only has an effect if `server-api-hostname` and `server-api-port` are set.                                                                                              | `false`  | `true`        |
| `player-activity-feed-channel-id` | The id of the channel to post the player activity feed in. Only has an effect if `server-api-hostname` and `server-api-port` are set.                                                                                       | `false`  | `null`        |
| `pvp-kill-feed-channel-id`        | The id of the channel to post the pvp kill feed in. Only has an effect if `server-api-hostname` and `server-api-port` are set. Requires at least version `0.4.0` of the [v-rising-discord-bot-companion](bot-companion.md). | `false`  | `null`        |

### `/remove-server`

Removes a server from the status monitor.
Admins can use this command in DMs, see [Configuration Properties](configuration-properties.md) for details.

| Parameter                  | Description                                    | Required | Default value |
|----------------------------|------------------------------------------------|----------|---------------|
| `server-status-monitor-id` | The id of the server status monitor to remove. | `true`   | `null`        |

### `/get-server-details`

Gets all the configuration details for the specified server.
Admins can use this command in DMs, see [Configuration Properties](configuration-properties.md) for details.

| Parameter                  | Description                                             | Required | Default value |
|----------------------------|---------------------------------------------------------|----------|---------------|
| `server-status-monitor-id` | The id of the server status monitor to get details for. | `true`   | `null`        |
