---
title: Bot companion integration
nav_order: 6
---

# [v-rising-discord-bot-companion](https://github.com/DarkAtra/v-rising-discord-bot-companion) Integration

The bot companion is a server-side mod that allows the bot to fetch additional data about players, such as the gear level.
It does this by exposing [additional http endpoints](#endpoints) on the servers api port.

{: .warning }
> I highly recommend not exposing the api port to the internet in an unprotected manner. The setup guide below explains how the API port can be secured using
> Basic Authentication.

{: .warning }
> It is **not** possible to use the bot-companion with GPortal hosted server as they don't allow exposing the API port to the internet.

## Setup Guide

1. [Install BepInEx](https://github.com/decaprime/VRising-Modding/releases) on your v rising server.
2. Download the [v-rising-discord-bot-companion.dll](https://github.com/DarkAtra/v-rising-discord-bot-companion/releases)
    * **or** clone [this repo](https://github.com/DarkAtra/v-rising-discord-bot-companion) and build it via `dotnet build`. This
      requires [dotnet 6.0](https://dotnet.microsoft.com/en-us/download/dotnet/6.0).
3. Move the `v-rising-discord-bot-companion.dll` to the BepInExp plugin folder. You will also need
   the [Bloodstone.dll](https://github.com/decaprime/Bloodstone/releases).
4. Enable the servers api port by adding the following to your `ServerHostSettings.json`:
   ```
   "API": {
     "Enabled": true,
     "BindPort": 25570
   }
   ```
   **It is not recommended to expose the api port to the internet in an unprotected manner.** More on that in the next few steps.
5. Start the v rising server and test if the mod works as expect by running the following command in your
   terminal: `curl -v http://localhost:25570/v-rising-discord-bot/characters`. Expect status code `200 OK` as soon as the server has fully started.
6. Secure the API port using [Basic Authentication](https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication). Navigate to the `BepInEx/config` folder
   and open `v-rising-discord-bot-companion.cfg`. The file should look something like this:
   ```
   ## Settings file was created by plugin v-rising-discord-bot-companion v0.6.0
   ## Plugin GUID: v-rising-discord-bot-companion

   [Authentication]

   ## A list of comma separated username:password entries that are allowed to query the HTTP API.
   # Setting type: String
   # Default value:
   BasicAuthUsers =
   ```
   Put the desired username and password after `BasicAuthUsers = `. For example the following would configure a user `apirising` with `e15kAly03I2K271b` as its
   password:
   ```
   ## Settings file was created by plugin v-rising-discord-bot-companion v0.6.0
   ## Plugin GUID: v-rising-discord-bot-companion

   [Authentication]

   ## A list of comma separated username:password entries that are allowed to query the HTTP API.
   # Setting type: String
   # Default value:
   BasicAuthUsers = apirising:e15kAly03I2K271b
   ```
   Please choose a password that you have never used before, ensure that it does not contain `,` and try to make it at least 16 characters long.
7. Restart your server and confirm that the API responds with status code `403 Forbidden` when you
   execute `curl -v http://localhost:25570/v-rising-discord-bot/characters`. Next confirm that a request with proper authentication succeeds with
   status code `200 OK`. For the above example the command looks like
   this: `curl -v -u apirising:e15kAly03I2K271b http://localhost:25570/v-rising-discord-bot/characters`.

### Connecting the bot companion to the discord bot

1. Make sure you're using [the latest version](https://github.com/DarkAtra/v-rising-discord-bot/releases) of the discord bot.
    * **Tip**: You can also find the current docker image [here](https://github.com/DarkAtra/v-rising-discord-bot/pkgs/container/v-rising-discord-bot)
2. Use the `/update-server` command to update the server and specify a `server-api-hostname`, `server-api-port`, `server-api-username`
   and `server-api-password`. If the v rising server and the discord bot are hosted on the same machine, set `server-api-hostname` to `localhost`
   and `server-api-port` to `25570` if you used the `ServerHostSettings.json` from above.
3. You should see the gear level for each player in the status embed the next time it is updated.

### Enabling the activity or kill feed

1. Use the `/configure-player-activity-feed` or `/configure-pvp-kill-feed` command and set `channel-id`
   to the id of the discord channel you want the player activity feed or pvp kill feed to appear in.
2. The bot will now post a message whenever a player joins or leaves the server or whenever someone was killed in a PvP battle.
   It looks something like this:

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
