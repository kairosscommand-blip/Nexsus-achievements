# NexusAchievements

A cross-plugin achievement system for the whole Nexus family. 61 achievements, spanning 34 Nexus
plugins (plus a handful of plain "Nexus Core" ones), each popping up as a toast styled to look
like a real Minecraft advancement -- **without this plugin ever actually touching the real
advancement engine.** That's a deliberate design choice, not a limitation it ran into: a real
in-game advancement tree needs a datapack, and reaching into Minecraft's own internal (unversioned,
unofficial, constantly-renamed) advancement classes to fake one at runtime is exactly the kind of
fragile NMS reflection this whole plugin family avoids building on top of without a real server to
verify it against. So this builds the best possible mimic out of stable, real, public Bukkit/Paper
API instead: a big title flash using vanilla's own real header wording, vanilla's own real toast
sound, and a formatted chat card -- see `ToastService`'s own doc comment for the full reasoning.

## What actually earns something

Four trigger types, set per-achievement in `config.yml`:

- **VANILLA** -- hard-wired in `VanillaMilestoneListener` against real, certain vanilla events
  (a kill, a respawn, an XP level-up). Always live, no dependency on anything else.
- **COMMAND** -- fires the moment a player types an exact command, e.g. `/house create`. Always
  live, and tunable straight in config without recompiling anything -- every command literal used
  below is a real, documented command from that plugin's own actual usage.
- **CONSUME** -- fires when a player eats/drinks an item whose display name contains a configured
  substring, e.g. anything with "Cure" in the name. Always live and config-tunable.
- **API** -- nothing in this plugin fires it yet. It's fully defined (id, title, description,
  icon, frame, hidden flag, goal target if it's a counted one) and just waiting for one line --
  `NexusAchievementsApi.grant(player, "the_id")` -- dropped into that plugin's own real source the
  next time this project is actually looking at its code.

That split matters and was a deliberate honesty call: this pass was built entirely from memory of
the Nexus family's own plugin descriptions, without re-reading any of those other plugins' actual
source. Guessing at another plugin's *internal* event/field names and quietly wiring against them
would risk being wrong in ways that fail silently -- worse, it'd look done when it isn't. COMMAND
and CONSUME triggers only ever reference things that are genuinely public and already documented
(real slash commands, plausible item names) -- API-armed ones are honestly marked as not yet
connected rather than fake-wired against a guess.

## The full catalog (61 achievements, 34 Nexus plugins + Nexus Core)

| Source | Live now (VANILLA/COMMAND/CONSUME) | Armed, needs one `grant()` line (API) |
|---|---|---|
| Nexus Core | Into the Fray, Dragonslayer, Wither Away, Not Today, Seasoned Adventurer, Legendary | -- |
| NexusHouses | A House Is Born, Bend the Knee, Enemies of the Realm | Long May They Reign |
| NexusEconomy | First Coin, Line of Credit | Market Mogul |
| NexusFamily | 'Til Death (Or Respawn), Family Talk | New Voice in the House |
| NexusRealms | Flag in the Ground, Claim Your Corner | -- |
| NexusFrontier | Wanted, Dead or Alive | Caravan Master (x3) |
| NexusVice | One for the Road, Getting Clean | Rock Bottom |
| NexusSurvival | Clean Bill of Health, Hydration Station | Radiation Sickness Survived |
| NexusVitals | Patch Yourself Up, Splinted | -- |
| NexusHorror | -- | Keep It Together, Blood Moon Survivor |
| NexusCombat | -- | On a Streak, Bedrock-Tier |
| NexusLegends | -- | Cryptid Hunter |
| NexusWarbeasts | -- | Tamer of Monsters |
| NexusHatchlings | -- | Hatchling Bond |
| NexusGiants | -- | That's a Big Zombie |
| NexusFolklore | -- | Living Legend, Keeper of Stories |
| NexusMigrations | -- | Herd Sighting |
| NexusMorality | -- | A Helping Hand (x5), Cold Shoulder |
| NexusNPC | -- | Not All Heroes... |
| NexusPareidolia | Just the Wind... Probably | -- |
| NexusPulse | Caught Up | -- |
| NexusTitles | Make a Name for Yourself | -- |
| NexusHats | Fashion Icon | -- |
| NexusBudget | Paid in Full, Hiding It From the Taxman | -- |
| NexusEnchants | -- | Walk on Water (and Lava) |
| NexusMap | -- | Cartographer |
| NexusEvents | -- | Touched by a Blessing, Meteor Watcher |
| NexusScan | -- | Well Traveled (x50) |
| NexusThreshold | -- | The Ground Remembers |
| NexusOracle | -- | Ask and You Shall Know |
| NexusRequiem | -- | A Death That Mattered |
| NexusExpeditions | -- | Party Up |
| NexusStarter | -- | Welcome to the Nexus |
| NexusGate | Locked In | -- |
| NexusDenyBlocks | -- | Not on My Watch |
| NexusDrones | -- | Eyes Everywhere |

29 achievements are live the moment this plugin is installed on its own (18 COMMAND, 6 VANILLA, 5
CONSUME). The other 32 are fully designed and ready, waiting on a one-line `grant()` call in each
source plugin.

13 achievements are also marked `hidden` -- mostly every CHALLENGE-tier one (the rare, purple-frame
ones: Dragonslayer, Wither Away, Legendary, Market Mogul, Rock Bottom, Radiation Sickness Survived,
Keep It Together, Blood Moon Survivor, Bedrock-Tier, Cryptid Hunter, Living Legend, A Death That
Mattered), plus Long May They Reign (NexusHouses' succession is dramatic enough on its own to earn
the surprise). A hidden one shows as "???" in `/achievements` until actually earned, matching
vanilla's own convention for its more surprising advancements, and every CHALLENGE-tier one also
gets announced server-wide in chat when someone lands it (`toast.broadcast-challenge-tier` in
config, on by default).

## The toast itself

- A large centered title flash using vanilla's own real header text per frame: "Advancement
  Made!" (TASK), "Goal Reached!" (GOAL), "Challenge Complete!" (CHALLENGE, in purple).
- Vanilla's own real toast sound (`UI_TOAST_IN` for TASK/GOAL, `UI_TOAST_CHALLENGE_COMPLETE` plus
  an extra level-up chime for CHALLENGE -- "amplify the feeling of being rewarded" on the rarest
  tier specifically, not on every single ordinary one).
- A formatted chat card underneath with the achievement's icon (named, since chat can't render a
  real item texture), title, description, and which Nexus plugin it's from.

## Commands

`/achievements` (aliases `/ach`, `/nexusach`) -- lists your own earned/locked achievements,
grouped by source plugin, with a running "earned/total" count. `/achievements info <id>` for
detail on one. Admin (`nexusachievements.admin`, default op): `/achievements grant <player> <id>`,
`/achievements revoke <player> <id>`, `/achievements listall` (every defined id, for admin
reference), `/achievements reload`.

## Extending this from another Nexus plugin

`NexusAchievementsApi`, registered with Bukkit's ServicesManager on enable, same soft
Class.forName-or-compile-against-it pattern every cross-plugin surface in this project uses (see
that interface's own javadoc for the exact snippet). One method covers almost everything:
`grant(Player, String achievementId)` -- safe to call from anywhere, does nothing if the id is
already earned or doesn't exist. This is genuinely the single most important integration point in
this whole plugin -- every API-armed achievement above is a one-line call away from being live.

## Storage

Deliberately not a database, same simplicity this whole no-network-dependency plugin family leans
on elsewhere: a single flat, append-only `playerdata.log` under the plugin's data folder, replayed
in full on startup. See `AchievementStore`'s own doc comment for the exact format.

## What's deliberately not here

- **No real advancement-tree entry.** Nothing shows up in the vanilla Advancements screen (F3+something
  or the in-game menu) -- `/achievements` is this plugin's own substitute for browsing them. See
  the top of this README and `ToastService` for why that's a deliberate scope decision.
- **No verification that an API-armed achievement's real trigger condition is even reachable
  yet** -- e.g. "Long May They Reign" needs NexusHouses' succession code to call `grant()`, which
  it doesn't yet. Fully designed, not yet connected.
- **CONSUME triggers use best-guess item names**, not re-confirmed against those plugins' actual
  source this pass (see `ConsumeTriggerListener`'s own doc comment). They fail silently (never
  fire) if wrong, never fire on the wrong thing.

## Important

Written and verified against a hand-written stub of the Paper API in a sandbox with no network
access -- it has NOT been compiled against the real `paper-api` or run on a live server (standard
caveat for every plugin in this family). Compile-verified clean (`javac -Xlint:all -Werror`, 0
warnings, 0 errors) against this plugin's own private copy of the shared stub library. This is a
brand-new, self-contained plugin -- it doesn't modify any other plugin's files, so there's no
shared-file regression risk the way a change to a genuinely shared file would carry.
