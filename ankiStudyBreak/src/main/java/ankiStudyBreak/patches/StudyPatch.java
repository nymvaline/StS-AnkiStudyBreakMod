package ankiStudyBreak.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.*;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;
import ankiStudyBreak.AnkiStudyBreak;
import ankiStudyBreak.cards.YesStudied;
import com.megacrit.cardcrawl.cards.AbstractCard;
import ankiStudyBreak.util.AnkiConnectRequest;

@SpirePatch(    // "Use the @SpirePatch annotation on the patch class."
        clz = AbstractDungeon.class, // This is the class where the method we will be patching is. In our case - Abstract Dungeon
        method = "nextRoomTransition", // This is the name of the method we will be patching.
        paramtypez = {
            SaveFile.class
        }
)

// This one brings up the initial screen, with the special card.
// StudyCheckPatch triggers when the special card is selected, and will continue to trigger when it's selected.
public class StudyPatch {
    public static void Postfix(AbstractDungeon __instance, SaveFile saveFile) {
        if(AnkiStudyBreak.studyIntervalPos >= AnkiStudyBreak.studyInterval) {
            AnkiStudyBreak.studyIntervalPos = 0;
        }
        if(AnkiStudyBreak.studyIntervalPos == 0) {
            boolean isLoadingPostCombatSave = CardCrawlGame.loadingSave && saveFile != null && saveFile.post_combat;
            if (__instance.nextRoom != null && !isLoadingPostCombatSave && AnkiStudyBreak.numCardsToStudy>0) {
                int baseline = AnkiConnectRequest.getCardsStudied();
                if((__instance.nextRoom.room instanceof EventRoom)
                       || (__instance.nextRoom.room instanceof RestRoom)
                       || (__instance.nextRoom.room instanceof MonsterRoom)
                        || (__instance.nextRoom.room instanceof ShopRoom)
                        || (__instance.nextRoom.room instanceof TreasureRoom)) {
                    CardGroup group = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
                   AbstractCard card = new YesStudied(baseline, AnkiStudyBreak.numCardsToStudy);
                   group.addToBottom(card);
                  int count = 0;
                   __instance.gridSelectScreen.open(group, 1, "", false,
                           false,
                          false,
                          false);
                }
            }
        }
        else {
            AnkiStudyBreak.studyIntervalPos ++;
        }
    }
}
