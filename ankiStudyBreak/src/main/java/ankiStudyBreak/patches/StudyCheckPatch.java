package ankiStudyBreak.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import ankiStudyBreak.AnkiStudyBreak;
import ankiStudyBreak.cards.YesStudied;
import ankiStudyBreak.util.AnkiConnectRequest;

@SpirePatch(
        clz = AbstractRoom.class,
        method = "update",
        paramtypez = {
        }
)
public class StudyCheckPatch {
    public static void Postfix(AbstractRoom __instance) {
        if (!AbstractDungeon.isScreenUp && !AbstractDungeon.gridSelectScreen.selectedCards.isEmpty()) {
            AbstractCard c = (AbstractCard) AbstractDungeon.gridSelectScreen.selectedCards.get(0);
            if(c instanceof YesStudied) {
                int baseline = c.timesUpgraded;
                String body = "{\"action\": \"getNumCardsReviewedToday\", \"version\": 6}";
                StringBuilder result = new StringBuilder();
                int totalStudied = AnkiConnectRequest.getCardsStudied();
                if((totalStudied - baseline) < AnkiStudyBreak.numCardsToStudy ) {
                    CardGroup group = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
                    AbstractCard card = new YesStudied(baseline, AnkiStudyBreak.numCardsToStudy-(totalStudied-baseline));
                    group.addToBottom(card);
                    int count = 0;
                    AbstractDungeon.gridSelectScreen.open(group, 1, "", false,
                            false,
                            false,
                            false);
                }
                else {
                    AnkiStudyBreak.studyIntervalPos++;
                    if(AnkiStudyBreak.useRandStudyInterval) {
                        AnkiStudyBreak.randomizeStudyInterval();
                    }
                }
                AbstractDungeon.gridSelectScreen.selectedCards.clear();
            }
        }
    }
}
