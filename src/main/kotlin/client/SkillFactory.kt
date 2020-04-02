package client

import cn.hutool.core.io.FileUtil
import cn.hutool.core.io.resource.ClassPathResource
import cn.hutool.core.io.resource.NoResourceException
import com.fasterxml.jackson.core.type.TypeReference
import constants.skills.*
import provider.MapleData
import provider.MapleDataProviderFactory
import provider.MapleDataTool
import server.MapleStatEffect
import server.life.Element
import tools.DeflaterUtils
import tools.JacksonUtil
import java.io.File
import java.util.*

object SkillFactory {
    private val skills: MutableMap<Int, Skill> = HashMap()

    fun getSkill(id: Int): Skill? {
        return if (skills.isNotEmpty()) {
            skills[Integer.valueOf(id)]
        } else null
    }

    fun loadAllSkills() {
        try {
            val resource = ClassPathResource("wz/Skill.wz")
            val unzipString = DeflaterUtils.unzipString(FileUtil.readString(resource.file, "utf-8"))
            val map = JacksonUtil.json2Bean<Map<Int, Skill>>(unzipString, object : TypeReference<Map<Int, Skill>>() {})
            skills.putAll(map)
println(skills)
        } catch (e: NoResourceException) {
            val datasource = MapleDataProviderFactory.getDataProvider(MapleDataProviderFactory.fileInWZPath("Skill.wz"))
            val root = datasource.root
            var skillid: Int
            for (topDir in root.files) { // Loop thru jobs
                if (topDir.name.length <= 8) {
                    for (data in datasource.getData(topDir.name)) { // Loop thru each jobs
                        if (data.name == "skill") {
                            for (data2 in data) { // Loop thru each jobs
                                if (data2 != null) {
                                    skillid = data2.name.toInt()
                                    skills[skillid] = loadFromData(skillid, data2)
                                }
                            }
                        }
                    }
                }
            }
            Thread.currentThread().contextClassLoader.getResource("wz")?.path?.let {
                if (FileUtil.isDirEmpty(File(it))) {
                    FileUtil.mkdir(File(it))
                }
                FileUtil.writeString(DeflaterUtils.zipString(JacksonUtil.bean2Json(skills)), "${it}/Skill.wz", "UTF-8")
            }


        }
    }

    private fun loadFromData(id: Int, data: MapleData): Skill {
        val ret = Skill(id)
        var isBuff = false
        val skillType = MapleDataTool.getInt("skillType", data, -1)
        val elem = MapleDataTool.getString("elemAttr", data, null)
        if (elem != null) {
            ret.element = Element.getFromChar(elem[0])
        } else {
            ret.element = Element.NEUTRAL
        }
        val effect = data.getChildByPath("effect")
        if (skillType != -1) {
            if (skillType == 2) {
                isBuff = true
            }
        } else {
            val action_ = data.getChildByPath("action")
            var action = false
            if (action_ == null) {
                if (data.getChildByPath("prepare/action") != null) {
                    action = true
                } else {
                    when (id) {
                        Gunslinger.INVISIBLE_SHOT, Corsair.HYPNOTIZE -> action = true
                    }
                }
            } else {
                action = true
            }
            ret.action = action
            val hit = data.getChildByPath("hit")
            val ball = data.getChildByPath("ball")
            isBuff = effect != null && hit == null && ball == null
            isBuff = isBuff or (action_ != null && MapleDataTool.getString("0", action_, "") == "alert2")
            when (id) {
                Hero.RUSH, Paladin.RUSH, DarkKnight.RUSH, DragonKnight.SACRIFICE, FPMage.EXPLOSION, FPMage.POISON_MIST, Cleric.HEAL, Ranger.MORTAL_BLOW, Sniper.MORTAL_BLOW, Assassin.DRAIN, Hermit.SHADOW_WEB, Bandit.STEAL, Shadower.SMOKE_SCREEN, SuperGM.HEAL_PLUS_DISPEL, Hero.MONSTER_MAGNET, Paladin.MONSTER_MAGNET, DarkKnight.MONSTER_MAGNET, Evan.ICE_BREATH, Evan.FIRE_BREATH, Gunslinger.RECOIL_SHOT, Marauder.ENERGY_DRAIN, BlazeWizard.FLAME_GEAR, NightWalker.SHADOW_WEB, NightWalker.POISON_BOMB, NightWalker.VAMPIRE, ChiefBandit.CHAKRA, Aran.COMBAT_STEP, Evan.RECOVERY_AURA -> isBuff = false
                Beginner.RECOVERY, Beginner.NIMBLE_FEET, Beginner.MONSTER_RIDER, Beginner.ECHO_OF_HERO, Beginner.MAP_CHAIR, Warrior.IRON_BODY, Fighter.AXE_BOOSTER, Fighter.POWER_GUARD, Fighter.RAGE, Fighter.SWORD_BOOSTER, Crusader.ARMOR_CRASH, Crusader.COMBO, Hero.ENRAGE, Hero.HEROS_WILL, Hero.MAPLE_WARRIOR, Hero.STANCE, Page.BW_BOOSTER, Page.POWER_GUARD, Page.SWORD_BOOSTER, Page.THREATEN, WhiteKnight.BW_FIRE_CHARGE, WhiteKnight.BW_ICE_CHARGE, WhiteKnight.BW_LIT_CHARGE, WhiteKnight.MAGIC_CRASH, WhiteKnight.SWORD_FIRE_CHARGE, WhiteKnight.SWORD_ICE_CHARGE, WhiteKnight.SWORD_LIT_CHARGE, Paladin.BW_HOLY_CHARGE, Paladin.HEROS_WILL, Paladin.MAPLE_WARRIOR, Paladin.STANCE, Paladin.SWORD_HOLY_CHARGE, Spearman.HYPER_BODY, Spearman.IRON_WILL, Spearman.POLEARM_BOOSTER, Spearman.SPEAR_BOOSTER, DragonKnight.DRAGON_BLOOD, DragonKnight.POWER_CRASH, DarkKnight.AURA_OF_BEHOLDER, DarkKnight.BEHOLDER, DarkKnight.HEROS_WILL, DarkKnight.HEX_OF_BEHOLDER, DarkKnight.MAPLE_WARRIOR, DarkKnight.STANCE, Magician.MAGIC_GUARD, Magician.MAGIC_ARMOR, FPWizard.MEDITATION, FPWizard.SLOW, FPMage.SEAL, FPMage.SPELL_BOOSTER, FPArchMage.HEROS_WILL, FPArchMage.INFINITY, FPArchMage.MANA_REFLECTION, FPArchMage.MAPLE_WARRIOR, ILWizard.MEDITATION, ILMage.SEAL, ILWizard.SLOW, ILMage.SPELL_BOOSTER, ILArchMage.HEROS_WILL, ILArchMage.INFINITY, ILArchMage.MANA_REFLECTION, ILArchMage.MAPLE_WARRIOR, Cleric.INVINCIBLE, Cleric.BLESS, Priest.DISPEL, Priest.DOOM, Priest.HOLY_SYMBOL, Priest.MYSTIC_DOOR, Bishop.HEROS_WILL, Bishop.HOLY_SHIELD, Bishop.INFINITY, Bishop.MANA_REFLECTION, Bishop.MAPLE_WARRIOR, Archer.FOCUS, Hunter.BOW_BOOSTER, Hunter.SOUL_ARROW, Ranger.PUPPET, Bowmaster.CONCENTRATE, Bowmaster.HEROS_WILL, Bowmaster.MAPLE_WARRIOR, Bowmaster.SHARP_EYES, Crossbowman.CROSSBOW_BOOSTER, Crossbowman.SOUL_ARROW, Sniper.PUPPET, Marksman.BLIND, Marksman.HEROS_WILL, Marksman.MAPLE_WARRIOR, Marksman.SHARP_EYES, Rogue.DARK_SIGHT, Assassin.CLAW_BOOSTER, Assassin.HASTE, Hermit.MESO_UP, Hermit.SHADOW_PARTNER, NightLord.HEROS_WILL, NightLord.MAPLE_WARRIOR, NightLord.NINJA_AMBUSH, NightLord.SHADOW_STARS, Bandit.DAGGER_BOOSTER, Bandit.HASTE, ChiefBandit.MESO_GUARD, ChiefBandit.PICKPOCKET, Shadower.HEROS_WILL, Shadower.MAPLE_WARRIOR, Shadower.NINJA_AMBUSH, Pirate.DASH, Marauder.TRANSFORMATION, Buccaneer.SUPER_TRANSFORMATION, Corsair.BATTLE_SHIP, GM.HIDE, SuperGM.HASTE, SuperGM.HOLY_SYMBOL, SuperGM.BLESS, SuperGM.HIDE, SuperGM.HYPER_BODY, Noblesse.BLESSING_OF_THE_FAIRY, Noblesse.ECHO_OF_HERO, Noblesse.MONSTER_RIDER, Noblesse.NIMBLE_FEET, Noblesse.RECOVERY, Noblesse.MAP_CHAIR, DawnWarrior.COMBO, DawnWarrior.FINAL_ATTACK, DawnWarrior.IRON_BODY, DawnWarrior.RAGE, DawnWarrior.SOUL, DawnWarrior.SOUL_CHARGE, DawnWarrior.SWORD_BOOSTER, BlazeWizard.ELEMENTAL_RESET, BlazeWizard.FLAME, BlazeWizard.IFRIT, BlazeWizard.MAGIC_ARMOR, BlazeWizard.MAGIC_GUARD, BlazeWizard.MEDITATION, BlazeWizard.SEAL, BlazeWizard.SLOW, BlazeWizard.SPELL_BOOSTER, WindArcher.BOW_BOOSTER, WindArcher.EAGLE_EYE, WindArcher.FINAL_ATTACK, WindArcher.FOCUS, WindArcher.PUPPET, WindArcher.SOUL_ARROW, WindArcher.STORM, WindArcher.WIND_WALK, NightWalker.CLAW_BOOSTER, NightWalker.DARKNESS, NightWalker.DARK_SIGHT, NightWalker.HASTE, NightWalker.SHADOW_PARTNER, ThunderBreaker.DASH, ThunderBreaker.ENERGY_CHARGE, ThunderBreaker.ENERGY_DRAIN, ThunderBreaker.KNUCKLER_BOOSTER, ThunderBreaker.LIGHTNING, ThunderBreaker.SPARK, ThunderBreaker.LIGHTNING_CHARGE, ThunderBreaker.SPEED_INFUSION, ThunderBreaker.TRANSFORMATION, Legend.BLESSING_OF_THE_FAIRY, Legend.AGILE_BODY, Legend.ECHO_OF_HERO, Legend.RECOVERY, Legend.MONSTER_RIDER, Legend.MAP_CHAIR, Aran.MAPLE_WARRIOR, Aran.HEROS_WILL, Aran.POLEARM_BOOSTER, Aran.COMBO_DRAIN, Aran.SNOW_CHARGE, Aran.BODY_PRESSURE, Aran.SMART_KNOCKBACK, Aran.COMBO_BARRIER, Aran.COMBO_ABILITY, Evan.BLESSING_OF_THE_FAIRY, Evan.RECOVERY, Evan.NIMBLE_FEET, Evan.HEROS_WILL, Evan.ECHO_OF_HERO, Evan.MAGIC_BOOSTER, Evan.MAGIC_GUARD, Evan.ELEMENTAL_RESET, Evan.MAPLE_WARRIOR, Evan.MAGIC_RESISTANCE, Evan.MAGIC_SHIELD, Evan.SLOW -> isBuff = true
            }
        }
        for (level in data.getChildByPath("level")) {
            ret.addLevelEffect(MapleStatEffect.loadSkillEffectFromData(level, id, isBuff))
        }
        ret.animationTime = 0
        if (effect != null) {
            for (effectEntry in effect) {
                ret.incAnimationTime(MapleDataTool.getIntConvert("delay", effectEntry, 0))
            }
        }
        return ret
    }
}