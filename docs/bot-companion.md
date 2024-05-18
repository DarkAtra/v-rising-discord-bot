---
title: Bot companion integration
nav_order: 5
---

# [v-rising-discord-bot-companion](https://github.com/DarkAtra/v-rising-discord-bot-companion) Integration

{: .warning }
> Please note, that modding support for 1.0 is still in its early stages and things are expected to break! Use this mod in production at your own risk.

The bot companion is a server-side mod that allows the bot to fetch additional data about players, such as the gear level.
It does this by exposing [additional http endpoints](#endpoints) on the servers api port.

{: .warning }
> I highly recommend not exposing the api port to the internet in an unprotected manner.

## Setup Guide

1. [Install BepInEx](https://github.com/decaprime/VRising-Modding/releases/tag/1.690.2) on your V Rising server.
2. Download the [v-rising-discord-bot-companion.dll](https://github.com/DarkAtra/v-rising-discord-bot-companion/releases/tag/v0.5.2)
    * **or** clone [this repo](https://github.com/DarkAtra/v-rising-discord-bot-companion) and build it via `dotnet build`. This
      requires [dotnet 6.0](https://dotnet.microsoft.com/en-us/download/dotnet/6.0).
3. Move the `v-rising-discord-bot-companion.dll` to the BepInExp plugin folder. You will also need
   the [Bloodstone.dll](https://github.com/decaprime/Bloodstone/releases/tag/v0.2.1).
4. Enable the servers api port by adding the following to your `ServerHostSettings.json`:
   ```
   "API": {
     "Enabled": true,
     "BindPort": 25570
   }
   ```
   **It is not recommended to expose the api port to the internet in an unprotected manner.** Consider protecting the api port using a reverse proxy that
   supports basic authentication or by using a firewall rule.
5. Start the V Rising server and test if the mod works as expect by running the following command in your
   terminal: `curl http://localhost:25570/v-rising-discord-bot/characters`. Expect status code `200` as soon as the server has fully started.

### Connecting the bot companion to the discord bot

1. Make sure you're using the latest version of the discord bot: https://github.com/DarkAtra/v-rising-discord-bot/releases.
    * **Tip**: You can also find the current docker image here: https://github.com/DarkAtra/v-rising-discord-bot/pkgs/container/v-rising-discord-bot
2. Use the `/update-server` command to update the status monitor and set both `server-api-hostname` and `server-api-port`.
   If the V Rising server and the discord bot are hosted on the same machine, set `server-api-hostname` to `localhost` and `server-api-port`
   to `25570` if you used the above `ServerHostSettings.json`.
3. You should see the gear level for each player in the status embed the next time it is updated.

### Enabling the activity or kill feed

1. Use the `/update-server` command to update the status monitor and set `player-activity-feed-channel-id` to the id of the discord channel you want the
   activity feed to appear in. You can do the same for the kill feed by setting `pvp-kill-feed-channel-id`.
2. The bot will now post a message whenever a player joins or leaves the server or whenever someone was killed in a PvP battle. It looks something like this:
   <img alt="Companion Preview" src="assets/companion-preview.png" width="400"/>

## Endpoints

### `/v-rising-discord-bot/characters`

Returns information about all characters that exist on the server.

#### Example Response

```http
HTTP/1.1 200 OK
Transfer-Encoding: chunked
Content-Type: application/json

[
  {
    "name": "Atra",
    "gearLevel": 83,
    "clan": "Test",
    "killedVBloods": [
      "FOREST_WOLF",
      "BANDIT_STONEBREAKER"
    ]
  },
  {
    "name": "Socium",
    "gearLevel": 84,
    "killedVBloods": []
  }
]
```

### `/v-rising-discord-bot/player-activities`

Returns a list of connect and disconnect events for the last 10 minutes.

Note that this is not persistent across server restarts.

#### Example Response

```http
HTTP/1.1 200 OK
Transfer-Encoding: chunked
Content-Type: application/json

[
  {
    "type": "CONNECTED",
    "playerName": "Atra",
    "occurred": "2023-01-01T00:00:00Z"
  },
  {
    "type": "DISCONNECTED",
    "playerName": "Atra",
    "occurred": "2023-01-01T01:00:00Z"
  }
]
```

### `/v-rising-discord-bot/pvp-kills`

Returns the most recent pvp kills.

Note that this is not persistent across server restarts.

#### Example Response

```http
HTTP/1.1 200 OK
Transfer-Encoding: chunked
Content-Type: application/json

[
  {
    "killer": {
      "name": "Atra",
      "gearLevel": 71
    },
    "victim": {
      "name": "Testi",
      "gearLevel": 11
    },
    "occurred": "2023-01-01T00:00:00Z"
  }
]
```
