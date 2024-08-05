# AscensionChat

AscensionChat is a Discord integrated chat bot for Project Ascension that allows users to easily communicate between Discord and Ascension WoW.

**It does NOT support WoW Classic or Retail servers.**

Currently supported versions are:
  - Vanilla
  - The Burning Crusade
  - Wrath of the Lich King
  - Cataclysm (4.3.4 build 15595)
  - Mists of Pandaria (5.4.8 build 18414)


## ✨ Features

- **Clientless**: Does not need the WoW Client to be open to run
- **Seamless Chat Integration**: Integrates channels between WoW and Discord
  - Guild chat, Officer chat, Local chat, Emotes, Custom Channels _(Local chat, Emotes, and Custom Channels are disabled by default)_
  - In-game links _(items, spells, etc.)_ are displayed as links to the Ascension Database _(db.ascension.gg)_
  - Customisable message format
- **Smart Tagging**:
  - Tag players on Discord from WoW using `@and_part_or_all_of_their_name`
  - Tag `@here`, `@everyone`, and `"@Even Roles With Spaces"` _(include quotes around them)_
- **Custom Commands**:
  - Check who is online in your guild with `?who` (also displays level and current area)
  - Invite a player to your guild with `?ginvite` (character bot uses must have Guild Invite perms)
  - Kick a player from your guild with `?gkick` (character bot uses bot must have Guild Kick perms)
  - Query other players in the world
- **Highly Portable**:
  - Runs as a Java program and works on **Windows**, **Mac**, and **Linux**

## 📖 How it Works

The bot uses Discord's API to log into your Discord server. It then uses supplied information to log into Ascension as a WoW character. Once it logs into WoW and sees the configured channels, it relays messages to your Discord and WoW channels respectively.

### Disclaimer: Do not use this bot on an account with existing characters!
- Even though this bot does not do anything malicious, some servers may not like a bot connecting, and GMs may ban the **_account_**! Make a new account just for the bot. **_YOU HAVE BEEN WARNED!_**

## 📸 Example Images

<details>
<summary><b>Discord Example</b> <i>(before fixing achievement whitespace)</i></summary>
<img src="images/example1.png"/>
</details>

<details>
<summary><b>Talking in Guild Chat</b></summary>
<img src="images/example2.png"/>
</details>

## 🛠️ Setup Discord Bot

1. Create a Discord Bot on your Discord account:

   - Go to <https://discordapp.com/developers/applications/>
   - Sign into your Discord account if necessary, and click `Create an application`
   - Change the application name to something meaningful like "WoW Chat"
   - On the left, click the `Bot` tab
   - Add a Bot
   - Disable the `Public Bot` option
   - Enable `PRESENCE INTENT`, `SERVER MEMBERS INTENT` and `MESSAGE CONTENT INTENT` under `Privileged Gateway Intents`. **This is important! Without it, your bot will not work!**
   - Remember where `TOKEN` is, or copy it to notepad. We will need this for the config.
     - _(Can't see `Copy`? Click `Reset Token` then `Copy`)_

2. Invite your bot to Discord:
   - Open a new browser tab/window and paste: <https://discordapp.com/oauth2/authorize?client_id=YOUR_CLIENT_ID&scope=bot> _(you could do this in notepad if you want)_
     - Don't press enter yet!
   - In a different tab, go back to the `OAuth2` page of your new bot/application
   - Copy the `Client ID`
   - Replace **`YOUR_CLIENT_ID`** in the URL above with the `Client ID` you just copied
   - Assign the bot the necessary roles/permissions in your Discord Server for it to view/enter your desired channels

## 🔧 Configuration

   - **Discord** section:
     - `token`: Enter your bot's `TOKEN` we noted earlier in the `Bot` tab of the Discord Developer Portal. This is what AscensionChat will use to login to Discord.
     - `enable_dot_commands`: If set to `1`, it will not format outgoing messages starting with `.`, enabling you to send things like `.s in` to the server directly. If set to `0`, it will format these messages like regular messages.
     - `dot_commands_whitelist`: If empty, it will allow or disallow dot commands based on the `enable_dot_commands` setting above. If any command is listed here, the bot will **ONLY** allow _those specific commands_ to be sent in-game.
     - `enable_invite_command`: If set to `1`, it will allow the use of `?invite charname` anywhere `?who` is allowed. If set to `0`, it disables the command.
     - `banned_invite_list`: A list of character names that cannot be used with `?invite`
     - `enable_commands_channels`: A list of channels for which to allow commands. If not specified or empty, the bot will allow commands from all channels.
   - **WoW** section:
     - `platform`: Leave as `Mac` unless your target server has Warden _(anticheat)_ disabled AND it is blocking/has disabled Mac logins. In this case, put `Windows`.
       - **NOTE:** For Ascension, I cannot get past character selection if `Mac` is set on MacOS. Having `platform=Windows` seems to work without issue.
     - `locale`: Optionally specify a locale if you want to join locale-specific global channels. **enUS** is the default locale.
     - `enable_server_motd`: Set to `0` to ignore sending the server's MotD. Set to `1` to send the server's MotD as a SYSTEM message.
     - `account`: Replace `REPLACE-ME` with the bot's Ascension account name.
     - `password`: Replace `REPLACE-ME` with the bot's WoW Ascension password.
     - `character`: Replace `REPLACE-ME` with the bot's character name, as it is shown in the character list.
   - **Guild** section:
     - This section sets up guild notifications on Discord.
     - For each notification—`online`, `offline`, `joined`, `left`, `motd`, `achievement`—specify:
       - `enabled`: `0` to not display in Discord, `1` to display in Discord
       - `format`: How should the message be displayed?
       - `channel`: Enter the `Channel name` **OR** `ID` of where you want the message displayed, instead of the default `guildrelay-chat` channel.
   - **Chat** section:
     - This section sets up the channel relays between Discord and Ascension. You can have an unlimited number of channel relays.
     - `direction`: How do you want to relay each channel? Put:
       `wow_to_discord`, `discord_to_wow`, or `both`.
     - **wow** section:
       - In `type`, put one of: `Say`, `Guild`, `Officer`, `Emote`, `Yell`, `System`, `Whisper`, `Channel`. This is the type of chat the Bot will read for this section.
         - If you put `type=Channel`, you must also provide a `channel=yourChannelName` value.
       - In `format`, put how you want to display the message. Supported replaceable values are `%time`, `%user`, `%message`, and `%channel` if the above `type` is `Channel`.
       - `filters`: See filters section. If a channel configuration has this section, it will override the global filters and use these instead for this channel.
         - If this is in the `wow` section, it will filter `Discord->WoW` messages.
       - Optionally in `id`, specify the `channel ID` if your server has a non-standard global channel.
     - **Discord** section:
       - `channel`: The Discord channel **name** OR **ID** where to display the message. **It is advised to use the channel ID here instead of the name, so the bot does not stop working when the channel name is changed.**
         - To see channels' IDs, you must enable Developer mode in Discord under User Settings -> Appearance -> Advanced.
       - `format`: Same options as in the **wow** section above.
       - `filters`: See filters section. If a channel configuration has this section, it will override the global filters and use these instead for this channel.
         - If this is in the `discord` section, it will filter `WoW->Discord` messages.
   - **Filters** section:
     - This section specifies filters for chat messages to be ignored by the bot. It works for both directions: Discord to WoW and WoW to Discord. It can be overridden in each specific channel configuration as stated above.
     - `enabled`: `0` to globally disable all filters, `1` to enable them.
     - `patterns`: List of Java Regex match patterns. If the incoming message matches any one of the patterns and filters are enabled, it will be ignored.
       - When ignored, the message will not be relayed; however, it will be logged into the bot's command line output prepended with the word FILTERED.

## 🚀 Running AscensionChat

1. Download the [**latest**](https://github.com/NotYourAverageGamer/AscensionChat/releases/latest) ready-made binary from the GitHub [releases](https://github.com/NotYourAverageGamer/AscensionChat/releases)

   - **Make sure you have a Java Runtime Environment (JRE) 1.8 or higher installed on your system!**

     - Check the version of your Java installation with

       ```bash
       java -version
       ```

- **Windows**: [Configure `ascensionchat.conf`](https://github.com/NotYourAverageGamer/AscensionChat/edit/master/README.md#-configure-ascensionchatconf) and run `run.bat`
- **Mac/Linux**: [Configure `ascensionchat.conf`](https://github.com/NotYourAverageGamer/AscensionChat/edit/master/README.md#-configure-ascensionchatconf) and run `run.sh`

## ⬆️ Updating

#### Before updating your `ascension.conf` file, save a copy of your current `.conf` file. This will allow you to easily transfer your login details and any custom formatting to the new config, making the update process smoother.

- Download the [**latest**](https://github.com/NotYourAverageGamer/AscensionChat/releases/latest) `ascensionchat.jar` and replace the one in your current `ascensionchat` folder. Alternatively, download the [**latest**](https://github.com/NotYourAverageGamer/AscensionChat/releases/latest) `ascensionchat.zip` file; but be careful not to replace your `ascensionchat.conf`!

## 🚧 Compiling AscensionChat from source

- WoW Chat/AscensionChat is written in Scala and compiles to a Java executable using [**Maven**](https://maven.apache.org).
- It requires [**Java JDK 21**](https://adoptium.net/en-GB/installation/), [**Scala 2.12.19**](https://www.scala-lang.org/download/2.12.19.html) and [**Maven**](https://maven.apache.org).

1. Run `mvn clean package` which will produce a file in the `/target` folder called `ascensionchat-*.zip`
2. Run `unzip ascensionchat-*.zip`, edit the configuration file, and execute `run.bat` for Windows or `run.sh` for Linux/MacOS. (Edit the name of the config file in `run.*` if you supply your own config with a different name)
   - If no config file is supplied, the bot will try to use `ascensionchat.conf`

## 🧪 Tested OS's

#### This project has been manually compiled and run on the following Operating Systems (bare-metal, no VM's/Containers)

- 🍎 **MacOS**

  - **MacOS Ventura** 13.6.7 (13" Early '15 MacBook Air, Intel, OpenCore Legacy Patcher)
    - [SDKMAN!](https://sdkman.io/) one-line install from their site (MacOS/Linux only)
    - Scala (2.12.19) installed with SDKMAN!

- 🐧 **Linux**

  - **Arch** (rolling, x86_64, Linux 6.6.37-1-lts)
    - Java (21.0.3-tem), Scala (2.12.19) and Maven (3.9.8) installed with [SDKMAN!](https://sdkman.io/)
  - **Mint** Virginia 21.3 (x86_64, Linux 5.15.0-113-generic)
    - Scala (2.12.19) and Java (21.0.3-tem) installed with [SDKMAN!](https://sdkman.io/)
    - Maven from the `apt` repository

- 🏁 **Windows**

  - **Windows 10 Pro** (22H2, 19045.4529)
    - You will want to use `Windows Terminal` (_which you might not have by default_), because the default `Windows Console Host` does not support the ANSI Escape Sequences used in this project. This results in the Escape Codes showing in the terminal and no colour, making terminal output harder to read. Fortunately, this is pretty straightforward to setup. See [**HERE**](https://learn.microsoft.com/en-us/windows/terminal/install) for steps to setup/install.
  - **Windows 11 Pro** (23H2, 22631.3737)
  - **WSL - Ubuntu 22.04** (on Win11-Pro)

## 🙏 Acknowledgements

Thank you to the following people/projects for helping make this project possible

- <https://github.com/fjaros/wowchat>
- <https://github.com/Szyler/AscensionChat>
- <https://github.com/xanthics/AscensionChat>
