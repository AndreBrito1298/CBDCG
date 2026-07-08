package isel.pt.cbdcg.domain.game.character

val KnightBasicProps = PassiveProps(PassiveType.BATTLE_PASSIVE, true, "When damaged, gains +def for 2 turns.", KnightBasic)
val KnightRareProps = PassiveProps(PassiveType.BATTLE_PASSIVE, true, "When HP is lost, gains ++def for 2 turns.", KnightRare)
val KnightEpicProps = PassiveProps(PassiveType.BATTLE_PASSIVE, true, "When HP is lost, it takes max 2 dmg.", KnightEpic)

val MageBasicProps = PassiveProps(PassiveType.BATTLE_PASSIVE, false, "When attacking, gains +dmg.", MageBasic)
val MageRareProps = PassiveProps(PassiveType.BATTLE_PASSIVE, false, "When attacking, gains +dmg for 2 turns.", MageRare)
val MageEpicProps = PassiveProps(PassiveType.BATTLE_PASSIVE, false, "When attacking, gains ++dmg.", MageEpic)

val AssassinBasicProps = PassiveProps(PassiveType.BATTLE_PASSIVE, false, "On the first turn, gains +spe if highest SPD, otherwise gains +def.", AssassinBasic)
val AssassinRareProps = PassiveProps(PassiveType.BATTLE_PASSIVE, false, "On the first turn, gains +dmg if highest HP, otherwise gains +def.", AssassinRare)
val AssassinEpicProps = PassiveProps(PassiveType.BATTLE_PASSIVE, false, "When below max HP, gains +dmg+def+spe.", AssassinEpic)

val AlchemistBasicProps = PassiveProps(PassiveType.BATTLE_PASSIVE, false, "On the first turn, gains +dmg if highest HP, otherwise gains +def.", AlchemistBasic)
val AlchemistRareProps = PassiveProps(PassiveType.BATTLE_PASSIVE, true, "When damaged, permanently recovers hp cannot exceed max hp.", AlchemistRare)
val AlchemistEpicProps = PassiveProps(PassiveType.BATTLE_PASSIVE, true, "When damaged, permanently gains +def+spe+dmg+hp.", AlchemistEpic)

val PaladinBasicProps = PassiveProps(PassiveType.NEUTRAL_PASSIVE, false, "Converts total def from items into dmg.", PaladinBasic)
val PaladinRareProps = PassiveProps(PassiveType.NEUTRAL_PASSIVE, false, "Gains +hp, for each equipped item.", PaladinRare)
val PaladinEpicProps = PassiveProps(PassiveType.NEUTRAL_PASSIVE, false, "Gains +hp, for each equipped item.", PaladinEpic)

val ElfBasicProps = PassiveProps(PassiveType.BATTLE_PASSIVE, false, "On the first turn, gains +def if highest DMG, otherwise gains +spe.", ElfBasic)
val ElfRareProps = PassiveProps(PassiveType.BATTLE_PASSIVE, false, "When attacking, gains +spe for 2 turns.", ElfRare)
val ElfEpicProps = PassiveProps(PassiveType.BATTLE_PASSIVE, true, "On the first turn, when HP is lost, gains ++def for 2 turns.", ElfEpic)

val TaoistBasicProps = PassiveProps(PassiveType.NEUTRAL_PASSIVE, false, "Converts equipped items total spe into dmg.", TaoistBasic)
val TaoistRareProps = PassiveProps(PassiveType.NEUTRAL_PASSIVE, false, "When holding no items, gains +dmg+spe.", TaoistRare)
val TaoistEpicProps = PassiveProps(PassiveType.NEUTRAL_PASSIVE, false, "When holding no items, gains +def+dmg+spe.", TaoistEpic)

val ThiefBasicProps = PassiveProps(PassiveType.BATTLE_PASSIVE, true, "When undamaged, gains +spe for 1 turn.", ThiefBasic)
val ThiefRareProps = PassiveProps(PassiveType.BATTLE_PASSIVE, true, "When undamaged, gains +spe for 1 turn; otherwise gains ++def for 2 turns.", ThiefRare)
val ThiefEpicProps = PassiveProps(PassiveType.BATTLE_PASSIVE, true, "When undamaged, gains +spe for 1 turn; otherwise gains ++def for 2 turns. Additionally, when attacking, has a chance to steal an item from the target (and a higher chance to unequip one of the target's items).", ThiefEpic)

val WerewolfBasicProps = PassiveProps(PassiveType.NEUTRAL_PASSIVE, false, "Gains +dmg, for each point of dmg from equipped items.", WerewolfBasic)
val WerewolfRareProps = PassiveProps(PassiveType.NEUTRAL_PASSIVE, false, "Gains +dmg, for each equipped item.", WerewolfRare)
val WerewolfEpicProps = PassiveProps(PassiveType.NEUTRAL_PASSIVE, false, "When attacking, reduces the target's -def by the target's own base def.", WerewolfEpic)

val BerserkerBasicProps = PassiveProps(PassiveType.NEUTRAL_PASSIVE, false, "Converts items total dmg to def.", BerserkerBasic)
val BerserkerRareProps = PassiveProps(PassiveType.NEUTRAL_PASSIVE, false, "Gains +def, one point for each equipped item.", BerserkerRare)
val BerserkerEpicProps = PassiveProps(PassiveType.BATTLE_PASSIVE, true, "When attacking, reduces the target's -dmg based on the attack's HP per 2 dmg dealt.", BerserkerEpic)

val PriestRareProps = PassiveProps(PassiveType.BATTLE_PASSIVE, false, "Gains +hp+dmg+def+spe for 1 turn.", PriestRare)
val PriestEpicProps = PassiveProps(PassiveType.BATTLE_PASSIVE, true, "When attacked, reduces the attacker's dmg to 1 for 1 turn.", PriestEpic)

val NoPassiveProps = PassiveProps(PassiveType.NEUTRAL_PASSIVE, false, "She's just a chill guy :)", NoPassive)