# AscensionChat -- README

**This fork has been modified for use with the Ascension Project. Some changes will need to be made if you want to use this with other servers**

AscensionChat is a Discord integration chat bot for the **Ascension Project** forked from **WoWChat** by fjaros. My _(NotYourAverageGamer)_ fork also contains code sourced from **szyler** and **xan-asc**, thank you all!

**It does NOT support WoW Classic or Retail servers.**

New Requirement: Due to Discord changes, you must check `PRESENCE INTENT` and `SERVER MEMBERS INTENT` under `Privileged Gateway Intents` in the Discord developer portal.

Currently supported versions are:

- Ascension WoW 3.3.5

Features:

- **Clientless** (Does not need the WoW Client to be open to run)
- **Seamless Chat integration** of channels between WoW and Discord
  - Guild chat, Officer chat, Local chat, Emotes, Custom Channels. (I have disabled Local chat, Emotes and Custom Channels by default.)
  - In-game links (items, spells, etc) are displayed as links to the Ascension Database (db.ascension.gg)
  - Configurable message format
- **Smart Tagging**
  - Tag players on Discord from WoW using `@and_part_or_all_of_their_name`
  - You can also tag `@here` and `@everyone` and `"@Even Roles With Spaces"` (include quotes around them)
- **Custom commands**
  - Check who is online in your guild with `?who`
  - Query other players in the world
- **Highly Portable**
  - Runs as a Java program, and therefore works on **Windows**, **Mac**, and **Linux**

## How it works

The bot uses Discord's API to login to your Discord server. It then uses supplied information
to login to Ascension as a WoW character. Once it logs in to WoW and sees the configured channels,
it will relay messages to your Discord and WoW channels respectively.

### DO NOT, under any circumstances, use this bot on an account with existing characters!

Even though this bot does not do anything malicious, some servers may not like a bot connecting, and GMs may ban the **_account_**!
Make a new account for just the bot. **YOU HAVE BEEN WARNED!**

##### Example 1 (before fixing achievement whitespace):

![gd-echoes](https://raw.githubusercontent.com/fjaros/wowchat/master/images/example1.png)

##### Talking in Guild Chat:

![guild-chat-construct](https://raw.githubusercontent.com/fjaros/wowchat/master/images/example2.png)

## Setup & Prerequisites

1. First you will need to create a Discord Bot on your Discord account:
   - Go to https://discordapp.com/developers/applications/
   - Sign into your Discord account if necessary, and click `Create an application`
   - Change the application name to something meaningful like "WoW Chat"
   - On the left, click the `Bot` tab
   - Add a Bot
   - Disable the `Public Bot` option.
   - **Enable `PRESENCE INTENT` and `SERVER MEMBERS INTENT` under `Privileged Gateway Intents`. This is important! Without it, your bot will not work!**
   - Underneath where it says `TOKEN` click `Copy`. This is what Ascension Chat will use to log into Discord.
2. Configure WoW Chat by opening `ascensionchat.conf` in your favourite text editor.

   - You can also create your own file, using the supplied `ascensionchat.conf` as a template.
   - **`Discord`** section:
     - **`token`**: Paste the `Bot token` you copied just above.
     - **`enable_dot_commands`**: If set to `1`, it will not format outgoing messages starting with `.`, enabling you to send things like `.s in` to the server directly. If set to `0`, it will format these messages like regular messages.
     - **`dot_commands_whitelist`**: If empty, it will allow or disallow dot commands based on the **`enable_dot_commands`** setting above. If any command is listed here, the bot will **ONLY** allow _those specific comamnds_ to be sent in-game.
     - **`enable_invite_command`**: If set to `1`, it will allow the use of `?invite charname` anywhere `?who` is allowed. If set to `0`, it disables the command.
     - **`banned_invite_list`**: A list of character names that cannot be used with `?invite`
     - **`enable_commands_channels`**: A list of channels for which to allow commands. If not specified or empty, the bot will allow commands from all channels.
   - **`wow`** section:
     - **`platform`**: Leave as **Mac** unless your target server has Warden (anticheat) disabled AND it is blocking/has disabled Mac logins. In this case put **Windows**.
     - **`locale`**: Optionally specify a locale if you want to join locale-specific global channels. **enUS** is the default locale.
     - **`enable_server_motd`**: Set to **`0`** to ignore sending server's MotD. Set to **`1`** to send server's MotD as a SYSTEM message.
     - **`account`**: Replace `REPLACE-ME` with the bot's Ascension account name.
     - **`password`**: Replace `REPLACE-ME` with the bot's WoW Ascension password.
     - **`character`**: Replace `REPLACE-ME` with the bot's character name, as it is shown in the character list.
   - **`Guild`** section:
     - This section sets up guild notifications on Discord.
     - For each notification, **`online`**, **`offline`**, **`joined`**, **`left`**, **`motd`**, **`achievement`** specify:
       - **`enabled`**: **`0`** to not display in Discord, **`1`** to display in Discord
       - **`format`**: How should the message be displayed?
       - **`channel`**: Enter the `Channel name` OR `ID`of where you want the message displayed, instead of the default `guildrelay-chat` channel.
   - **`Chat`** section:
     - This section sets up the channel relays between Discord and Ascension. You can have an unlimited number of channel relays.
     - **`direction`**: How do you want to relay each channel? Put:
       **`wow_to_discord`**, **`discord_to_wow`**, or **`both`**.
     - **`wow`** section:
       - In **`type`** put one of: **Say**, **Guild**, **Officer**, **Emote**, **Yell**, **System**, **Whisper**, **Channel**. This is the type of chat the Bot will read for this section.
         - If you put **`type=Channel`**, you must also provide a **`channel=yourChannelName`** value.
       - In **`format`** put how you want to display the message. Supported replaceable values are, **`%time`**, **`%user`**, **`%message`** and **`%channel`**, if above **`type`** is **`Channel`**.
       - **`filters`**: See filters section. If a channel configuration has this section, it will override the global filters and use these instead for this channel.
         - If this is in the **`wow`** section, it will filter Discord->WoW messages.
       - Optionally in **`id`**, specify the `channel ID` if your server has a non-standard global channel.
     - **`Discord`** section:
       - **`channel`**: The Discord channel **name** OR **ID** where to display the message. **It is advised to use channel ID here instead of name, so the bot does not stop working when the channel name is changed.**
         - To see channels' IDs, you must enable Developer mode in Discord under User Settings -> Appearance -> Advanced.
       - **`format`**: Same options as in **wow** section above.
       - **`filters`**: See filters section. If a channel configuration has this section, it will override the global filters and use these instead for this channel.
         - If this is in the **`discord`** section, it will filter WoW->Discord messages.
   - In section **`filters`**:
     - This section specifies filters for chat messages to be ignored by the bot. It works for both directions, Discord to WoW and WoW to Discord. It can be overriden in each specific channel configuration as stated above.
     - **`enabled`**: **0** to globally disable all filters, **1** to enable them.
     - **`patterns`**: List of Java Regex match patterns. If the incoming messages matches any one of the patterns and filters are enabled, it will be ignored.
       - When ignored, the message will not be relayed; however it will be logged into the bot's command line output prepended with the word FILTERED.

3. Invite your bot to Discord
   - Go back to https://discordapp.com/developers/applications/ and click your new Bot application.
   - In browser enter: https://discordapp.com/oauth2/authorize?client_id=CLIENT_ID&scope=bot
     - Replace **`CLIENT_ID`** (the one in CAPS) in the URL with the token from the Discord applications page earlier.
   - Assign the bot the necessary Discord roles/permissions to view/enter your desired channels.

## Run

1. Download the latest ready-made binary from github releases: https://github.com/NotYourAverageGamer/AscensionChat/releases
   - **Make sure you have a Java Runtime Environment (JRE) 1.8 or higher installed on your system!**
   - **On Windows**: Edit `ascensionchat.conf` as above and run `run.bat`
   - **On Mac/Linux**: Edit `ascensionchat.conf` as above and run `run.sh`

OR to compile yourself:

1. WoW Chat/AscensionChat is written in Scala and compiles to a Java executable using [maven](https://maven.apache.org).
2. It requires **Java JDK 1.8+** and **Scala 2.12.19**.
3. Run `mvn clean package` which will produce a file in the target folder called `ascensionchat-24.6.1.zip`
4. unzip `ascensionchat-24.6.1.zip`, edit the configuration file and run `java -jar ascensionchat.jar <config file>`
   - If no config file is supplied, the bot will try to use `ascensionchat.conf`
