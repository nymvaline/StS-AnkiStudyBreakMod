package ankiStudyBreak.cards;
import basemod.AutoAdd;
import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import ankiStudyBreak.AnkiStudyBreak;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static ankiStudyBreak.AnkiStudyBreak.makeCardPath;

@AutoAdd.Ignore
public class YesStudied extends CustomCard{
    public static final Logger logger = LogManager.getLogger("YesStudied");

    public static final String ID = AnkiStudyBreak.makeID(YesStudied.class.getSimpleName());//"YesStudied";
    private static final CardStrings cardStrings = CardCrawlGame.languagePack.getCardStrings(ID);
    public static final String IMG = makeCardPath("YesStudiedAnki.png");
    public static final String NAME = cardStrings.NAME;
    public static final String DESCRIPTION = cardStrings.DESCRIPTION;
    // STAT DECLARATION

    private static final CardRarity RARITY = CardRarity.COMMON;
    private static final CardTarget TARGET = CardTarget.NONE;
    private static final CardType TYPE = CardType.STATUS;
    public static final CardColor COLOR = CardColor.COLORLESS;

    private static final int COST = -2;

    public YesStudied() {
        this(0, AnkiStudyBreak.numCardsToStudy);
    }
    public YesStudied(int upgrades, int remaining) {
        super(ID, NAME, IMG, COST, DESCRIPTION, TYPE, COLOR, RARITY, TARGET);
        baseMagicNumber = magicNumber = remaining;
        this.timesUpgraded = upgrades;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
    }

    public void upgrade() {
    }

    public AbstractCard makeCopy() {
        return new YesStudied(timesUpgraded, baseMagicNumber);
    }
}
