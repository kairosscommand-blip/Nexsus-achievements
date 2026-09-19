# NexusAchievements changelog

## v1.0.0

Brand-new plugin -- a cross-plugin achievement system covering 34 Nexus plugins plus a handful of
plain vanilla "Nexus Core" milestones, 61 achievements total, popped up as a toast styled to look
like a real Minecraft advancement (real header wording per frame, real toast sound, a formatted
chat card) without ever touching the real advancement engine -- see README/`ToastService` for why
that was a deliberate call, not something this plugin fell short of.

Built entirely from memory of this project's own earlier plugin descriptions, without re-reading
any other plugin's actual source this pass -- deliberately, per how it was asked for. That shaped
the architecture directly: rather than guess at another plugin's internal event/field names and
risk silently wiring against the wrong thing, every achievement's trigger is one of four honest,
narrow types --

- `VANILLA` (6 achievements) -- hard-coded in `VanillaMilestoneListener` against real, certain
  vanilla events (a kill, a respawn, an XP level-up). Zero dependency on anything else in the
  family.
- `COMMAND` (18) -- config-driven, matches a player typing an exact real command this project has
  documented elsewhere (e.g. `/house create`, `/vice rehab`, `/bills pay`).
- `CONSUME` (5) -- config-driven, matches eating/drinking an item whose display name contains a
  configured substring (e.g. anything with "Cure" in it for NexusSurvival's disease cure). Best-
  guess item names, not re-confirmed against real source -- fails silently if wrong, documented in
  `ConsumeTriggerListener`.
- `API` (32) -- fully defined, not yet connected. `NexusAchievementsApi.grant(player, id)`,
  registered via ServicesManager same as every other cross-plugin surface in this project, is the
  one-line integration point each of these is waiting on in its real source plugin.

New shared-library surfaces added (first plugin in the family to need them): `PlayerItemConsumeEvent`,
`PlayerRespawnEvent`, `PlayerLevelChangeEvent` (all real, standard Bukkit events with no earlier
need here), plus `Player#sendTitle(...)` and `Player#getLevel()`, and `PluginManager#callEvent(Event)`
(all real, long-stable Bukkit/Paper API). `Material` extended with ~45 real, long-stable item-name
constants used purely as toast/GUI icons. `Sound` extended with `UI_TOAST_IN` and
`UI_TOAST_CHALLENGE_COMPLETE` (best recollection of the real toast sound names, same "not verified
against a real jar" caveat every Sound/Particle addition in this family carries).

Storage is a single flat, append-only `playerdata.log` (see `AchievementStore`), not a database --
matches the simplicity this whole no-network-dependency project leans on elsewhere.

Compile-verified clean (`javac -Xlint:all -Werror`, 0 warnings, 0 errors) against this plugin's own
private copy of the shared hand-written stub library (copied from NexusWildcard's, the most
recently updated one in the family at the time, then extended). This is a brand-new, fully
self-contained plugin -- it doesn't modify any other plugin's files or shared stub copy, so there's
no cross-plugin regression risk the way editing a genuinely shared file would carry. Not yet
compiled against the real `paper-api` or run on a live server (standard caveat for every plugin in
this family, restated in full in README).
