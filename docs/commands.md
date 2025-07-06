---
title: Discord Commands
nav_order: 4
---

# Discord Commands

Please note that all commands are [guild](https://discord.com/developers/docs/resources/guild) specific by default.

## Server

### `/list-servers`

Use this command to list all servers that you have previously added using the `/add-server` command.
Admins can use this command in DMs, see [Configuration Properties](configuration-properties.md) for details.

| Parameter | Description                        | Required | Default value |
|-----------|------------------------------------|----------|---------------|
| `page`    | The page to request. Zero indexed. | `false`  | `0`           |

### `/add-server`

Use this command to add a server. This is required in order to use any feature of the bot.

| Parameter              | Description                                                                                                                                                            | Required | Default value |
|------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|---------------|
| `server-hostname`      | The hostname of the server to add.                                                                                                                                     | `true`   | `null`        |
| `server-query-port`    | The query port of the server to add.                                                                                                                                   | `true`   | `null`        |
| `server-api-hostname`  | The hostname to use when querying the server's api. Only required if you're planing to use features of the [v-rising-discord-bot-companion](bot-companion.md).         | `false`  | `null`        |
| `server-api-port`      | The api port of the server. Only required if you're planing to use features of the [v-rising-discord-bot-companion](bot-companion.md).                                 | `false`  | `null`        |
| `server-api-username`  | The username used to authenticate to the api of the server. Only required if you're planing to use features of the [v-rising-discord-bot-companion](bot-companion.md). | `false`  | `null`        |
| `server-api-password`  | The password used to authenticate to the api of the server. Only required if you're planing to use features of the [v-rising-discord-bot-companion](bot-companion.md). | `false`  | `null`        |
| `use-secure-transport` | Whether api requests should use https or not.                                                                                                                          | `false`  | `false`       |

### `/update-server`

Use this command to update the given server.
Only parameters specified are updated, all other parameters remain unchanged.
Admins can use this command in DMs, see [Configuration Properties](configuration-properties.md) for details.

| Parameter              | Description                                                                                                                                                            | Required | Default value |
|------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|---------------|
| `server-id`            | The id of the server to update.                                                                                                                                        | `true`   | `null`        |
| `server-hostname`      | The hostname of the server to add.                                                                                                                                     | `true`   | `null`        |
| `server-query-port`    | The query port of the server to add.                                                                                                                                   | `true`   | `null`        |
| `server-api-hostname`  | The hostname to use when querying the server's api. Only required if you're planing to use features of the [v-rising-discord-bot-companion](bot-companion.md).         | `false`  | `null`        |
| `server-api-port`      | The api port of the server. Only required if you're planing to use features of the [v-rising-discord-bot-companion](bot-companion.md).                                 | `false`  | `null`        |
| `server-api-username`  | The username used to authenticate to the api of the server. Only required if you're planing to use features of the [v-rising-discord-bot-companion](bot-companion.md). | `false`  | `null`        |
| `server-api-password`  | The password used to authenticate to the api of the server. Only required if you're planing to use features of the [v-rising-discord-bot-companion](bot-companion.md). | `false`  | `null`        |
| `use-secure-transport` | Whether api requests should use https or not.                                                                                                                          | `false`  | `false`       |

### `/remove-server`

Use this command to remove the given server. This command can not be undone.
Admins can use this command in DMs, see [Configuration Properties](configuration-properties.md) for details.

| Parameter   | Description                     | Required | Default value |
|-------------|---------------------------------|----------|---------------|
| `server-id` | The id of the server to remove. | `true`   | `null`        |

### `/get-server-details`

Use this command to get all configuration details for the given server.
Admins can use this command in DMs, see [Configuration Properties](configuration-properties.md) for details.

| Parameter   | Description                              | Required | Default value |
|-------------|------------------------------------------|----------|---------------|
| `server-id` | The id of the server to get details for. | `true`   | `null`        |

## Status Embed

### `/configure-status-monitor`

Use this command to configure the status embed, aka. status monitor, for a given server.
Admins can use this command in DMs, see [Configuration Properties](configuration-properties.md) for details.

| Parameter                    | Description                                                                                                                                 | Required | Default value |
|------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|----------|---------------|
| `server-id`                  | The id of the server to configure the status embed for.                                                                                     | `true`   | `null`        |
| `channel-id`                 | The id of the channel to post the status embed in.                                                                                          | `true`   | `null`        |
| `status`                     | Determines if the status embed should be updated or not. Either `ACTIVE` or `INACTIVE`.                                                     | `false`  | `ACTIVE`      |
| `display-server-description` | Whether to display the v rising server description in the status embed.                                                                     | `false`  | `true`        |
| `display-player-gear-level`  | Whether to display each player's gear level. Only has an effect if the server has a `server-api-hostname` and `server-api-port` configured. | `false`  | `true`        |

### `/get-status-monitor-details`

Use this command to get all status monitor configuration details for the given server.
Admins can use this command in DMs, see [Configuration Properties](configuration-properties.md) for details.

| Parameter   | Description                                                               | Required | Default value |
|-------------|---------------------------------------------------------------------------|----------|---------------|
| `server-id` | The id of the server to get all status monitor configuration details for. | `true`   | `null`        |

## Player Activity Feed

### `/configure-player-activity-feed`

Use this command to configure the player activity feed for a given server.
Admins can use this command in DMs, see [Configuration Properties](configuration-properties.md) for details.

| Parameter    | Description                                                                                     | Required | Default value |
|--------------|-------------------------------------------------------------------------------------------------|----------|---------------|
| `server-id`  | The id of the server to configure the player activity feed for.                                 | `true`   | `null`        |
| `channel-id` | The id of the channel to post the player activity feed in.                                      | `true`   | `null`        |
| `status`     | Determines if the player activity feed should be updated or not. Either `ACTIVE` or `INACTIVE`. | `false`  | `ACTIVE`      |

### `/get-player-activity-feed-details`

Use this command to get all player activity feed configuration details for the given server.
Admins can use this command in DMs, see [Configuration Properties](configuration-properties.md) for details.

| Parameter   | Description                                                                     | Required | Default value |
|-------------|---------------------------------------------------------------------------------|----------|---------------|
| `server-id` | The id of the server to get all player activity feed configuration details for. | `true`   | `null`        |

## Pvp Kill Feed

### `/configure-pvp-kill-feed`

Use this command to configure the pvp kill feed for a given server.
Admins can use this command in DMs, see [Configuration Properties](configuration-properties.md) for details.

| Parameter    | Description                                                                              | Required | Default value |
|--------------|------------------------------------------------------------------------------------------|----------|---------------|
| `server-id`  | The id of the server to configure the pvp kill feed for.                                 | `true`   | `null`        |
| `channel-id` | The id of the channel to post the pvp kill feed in.                                      | `true`   | `null`        |
| `status`     | Determines if the pvp kill feed should be updated or not. Either `ACTIVE` or `INACTIVE`. | `false`  | `ACTIVE`      |

### `/get-pvp-kill-feed-details`

Use this command to get all pvp kill feed configuration details for the given server.
Admins can use this command in DMs, see [Configuration Properties](configuration-properties.md) for details.

| Parameter   | Description                                                              | Required | Default value |
|-------------|--------------------------------------------------------------------------|----------|---------------|
| `server-id` | The id of the server to get all pvp kill feed configuration details for. | `true`   | `null`        |

## Raid Feed

Requires at least bot companion version `0.9.0`.

### `/configure-raid-feed`

Use this command to configure the raid feed for a given server.
Admins can use this command in DMs, see [Configuration Properties](configuration-properties.md) for details.

| Parameter                   | Description                                                                          | Required | Default value |
|-----------------------------|--------------------------------------------------------------------------------------|----------|---------------|
| `server-id`                 | The id of the server to configure the raid feed for.                                 | `true`   | `null`        |
| `channel-id`                | The id of the channel to post the raid feed in.                                      | `true`   | `null`        |
| `status`                    | Determines if the raid feed should be updated or not. Either `ACTIVE` or `INACTIVE`. | `false`  | `ACTIVE`      |
| `display-player-gear-level` | Whether to display each player's gear level in the raid feed.                        | `false`  | `false`       |

### `/get-raid-feed-details`

Use this command to get all raid feed configuration details for the given server.
Admins can use this command in DMs, see [Configuration Properties](configuration-properties.md) for details.

| Parameter   | Description                                                          | Required | Default value |
|-------------|----------------------------------------------------------------------|----------|---------------|
| `server-id` | The id of the server to get all raid feed configuration details for. | `true`   | `null`        |

## VBlood Kill Feed

Requires at least bot companion version `0.8.0`.

### `/configure-vblood-kill-feed`

Use this command to configure the vblood kill feed for a given server.
Admins can use this command in DMs, see [Configuration Properties](configuration-properties.md) for details.

| Parameter    | Description                                                                                 | Required | Default value |
|--------------|---------------------------------------------------------------------------------------------|----------|---------------|
| `server-id`  | The id of the server to configure the vblood kill feed for.                                 | `true`   | `null`        |
| `channel-id` | The id of the channel to post the vblood kill feed in.                                      | `true`   | `null`        |
| `status`     | Determines if the vblood kill feed should be updated or not. Either `ACTIVE` or `INACTIVE`. | `false`  | `ACTIVE`      |

### `/get-vblood-kill-feed-details`

Use this command to get all vblood kill feed configuration details for the given server.
Admins can use this command in DMs, see [Configuration Properties](configuration-properties.md) for details.

| Parameter   | Description                                                                 | Required | Default value |
|-------------|-----------------------------------------------------------------------------|----------|---------------|
| `server-id` | The id of the server to get all vblood kill feed configuration details for. | `true`   | `null`        |
