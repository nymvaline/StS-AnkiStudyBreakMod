package ankiStudyBreak;

import basemod.*;
import basemod.interfaces.*;
import basemod.patches.com.megacrit.cardcrawl.helpers.input.ScrollInputProcessor.TextInput;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.google.gson.Gson;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.localization.*;
import ankiStudyBreak.cards.YesStudied;
import ankiStudyBreak.util.AnkiConnectSetUrl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ankiStudyBreak.util.IDCheck;
import ankiStudyBreak.util.TextureLoader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

/*
 * With that out of the way:
 * Welcome to this super over-commented Slay the Spire modding base.
 * Use it to make your own mod of any type. - If you want to add any standard in-game content (character,
 * cards, relics), this is a good starting point.
 * It features 1 character with a minimal set of things: 1 card of each type, 1 debuff, couple of relics, etc.
 * If you're new to modding, you basically *need* the BaseMod wiki for whatever you wish to add
 * https://github.com/daviscook477/BaseMod/wiki - work your way through with this base.
 * Feel free to use this in any way you like, of course. MIT licence applies. Happy modding!
 *
 * And pls. Read the comments.
 */

@SpireInitializer
public class AnkiStudyBreak implements
        EditCardsSubscriber,
        EditStringsSubscriber,
        PostInitializeSubscriber {
    public static final Logger logger = LogManager.getLogger(AnkiStudyBreak.class.getName());
    private static String modID;

    private static int randStudyInterval = 1; // This is accessed by calcStudyInterval.
    public static int studyIntervalPos = 0;

    // Mod-settings settings.
    public static Properties ankiStudyBreakDefaultSettings = new Properties();
    public static final String ANKICONNECT_URL_SETTINGS = "ankiconnectUrl";
    public static String ankiConnectUrlString = "http://127.0.0.1:8765";

    public static boolean currentlyTypingAnkiConnectUrl = false;
    public static final String NUM_CARDS_TO_STUDY_SETTINGS = "numCardsToStudy";
    public static int numCardsToStudy = 10;

    public static int studyInterval = 1; // do a study break every this many rooms
    public static final String STUDY_INTERVAL_SETTINGS = "studyInterval";

    public static boolean useRandStudyInterval = false;
    public static final String USE_RAND_STUDY_INTERVAL_SETTINGS = "useRandStudyInterval";

    //This is for the in-game mod settings panel.
    private static final String MODNAME = "Anki Study Break";
    private static final String AUTHOR = "nymvaline"; // And pretty soon - You!
    private static final String DESCRIPTION = "Pause at the beginning of every room to study.";
    
    // =============== INPUT TEXTURE LOCATION =================

    //Mod Badge - A small icon that appears in the mod settings menu next to your mod.
    public static final String BADGE_IMAGE = "ankiStudyBreakResources/images/Badge.png";

    // =============== MAKE IMAGE PATHS =====================
    
    public static String makeCardPath(String resourcePath) {
        return getModID() + "Resources/images/cards/" + resourcePath;
    }

    // =============== /MAKE IMAGE PATHS/ =================
    
    // =============== /INPUT TEXTURE LOCATION/ =================
    
    
    // =============== SUBSCRIBE, INITIALIZE =================
    
    public AnkiStudyBreak() {
        logger.info("Subscribe to BaseMod hooks");
        
        BaseMod.subscribe(this);
      
        setModID("ankiStudyBreak");

        logger.info("Done subscribing");

        logger.info("Adding mod settings");
        // This loads the mod settings.
        // The actual mod Button is added below in receivePostInitialize()
        // These are default values that _should_ be overwritten by the saved config.
        ankiStudyBreakDefaultSettings.setProperty(ANKICONNECT_URL_SETTINGS, "http://127.0.0.1:8765");
        ankiStudyBreakDefaultSettings.setProperty(NUM_CARDS_TO_STUDY_SETTINGS, "10");
        ankiStudyBreakDefaultSettings.setProperty(STUDY_INTERVAL_SETTINGS, "1");
        ankiStudyBreakDefaultSettings.setProperty(USE_RAND_STUDY_INTERVAL_SETTINGS, "false");
        try {
            SpireConfig config = new SpireConfig("ankiStudyBreak", "ankiStudyBreakConfig", ankiStudyBreakDefaultSettings);
            // the "fileName" parameter is the name of the file MTS will create where it will save our setting.
            config.load(); // Load the setting and set the boolean to equal it
            ankiConnectUrlString = config.getString(ANKICONNECT_URL_SETTINGS);
            numCardsToStudy = config.getInt(NUM_CARDS_TO_STUDY_SETTINGS);
            studyInterval = config.getInt(STUDY_INTERVAL_SETTINGS);
            useRandStudyInterval = config.getBool(USE_RAND_STUDY_INTERVAL_SETTINGS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // These settings are not loaded from config, but need to be adjusted on launching the game.
        if(numCardsToStudy == 0) {
            numCardsToStudy = 10;
        }
        randStudyInterval = 1; // resets to 1 on re-launching the game, because we don't save this.

        logger.info("Done adding mod settings");
        
    }
    
    // ====== BEGIN NO EDIT AREA ======
    // I did remove the angry comments in here. From the DefaultMod. I don't understand this part of the code.
    
    public static void setModID(String ID) {
        Gson coolG = new Gson();
        InputStream in = AnkiStudyBreak.class.getResourceAsStream("/IDCheckStrings.json");
        IDCheck EXCEPTION_STRINGS = coolG.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), IDCheck.class); // OR THIS, DON'T EDIT IT
        logger.info("You are attempting to set your mod ID as: " + ID);
        if (ID.equals(EXCEPTION_STRINGS.DEFAULTID)) {
            throw new RuntimeException(EXCEPTION_STRINGS.EXCEPTION);
        } else if (ID.equals(EXCEPTION_STRINGS.DEVID)) {
            modID = EXCEPTION_STRINGS.DEFAULTID;
        } else {
            modID = ID;
        }
        logger.info("Success! ID is " + modID);
    }
    
    public static String getModID() { // NO
        return modID;
    }
    
    private static void pathCheck() {
        Gson coolG = new Gson();
        InputStream in = AnkiStudyBreak.class.getResourceAsStream("/IDCheckStrings.json");
        IDCheck EXCEPTION_STRINGS = coolG.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), IDCheck.class);
        String packageName = AnkiStudyBreak.class.getPackage().getName();
        FileHandle resourcePathExists = Gdx.files.internal(getModID() + "Resources");
        if (!modID.equals(EXCEPTION_STRINGS.DEVID)) {
            if (!packageName.equals(getModID())) {
                throw new RuntimeException(EXCEPTION_STRINGS.PACKAGE_EXCEPTION + getModID());
            }
            if (!resourcePathExists.exists()) {
                throw new RuntimeException(EXCEPTION_STRINGS.RESOURCE_FOLDER_EXCEPTION + getModID() + "Resources");
            }
        }
    }
    
    // End of no-edit zone.
    
    
    public static void initialize() {
        logger.info("=================================== Initializing Mod. Hi. =========================");
        AnkiStudyBreak  ankiStudyBreak = new AnkiStudyBreak();
        logger.info("========================= / Mod Initialized. Hello World./ =========================");
    }


    // =============== POST-INITIALIZE =================
    
    @Override
    public void receivePostInitialize() {
        logger.info("Loading badge image and mod options");
        
        // Load the Mod Badge
        Texture badgeTexture = TextureLoader.getTexture(BADGE_IMAGE);
        
        // Create the Mod Menu
        ModPanel settingsPanel = new ModPanel();
        try {
            //SpireConfig config = new SpireConfig("ankiStudyBreak", "ankiStudyBreakConfig", ankiStudyBreakDefaultSettings);
            ModMinMaxSlider numCardsToStudySlider = new ModMinMaxSlider("Anki Cards per floor",
                350.0f,
                700.0f,
                0,
                100,
                numCardsToStudy,
                "%.0f",
                settingsPanel,
                (slider) -> {
                    numCardsToStudy = Math.round(slider.getValue());
                    try {
                        // And based on that boolean, set the settings and save them
                        SpireConfig config = new SpireConfig("ankiStudyBreak", "ankiStudyBreakConfig", ankiStudyBreakDefaultSettings);
                        config.setInt(NUM_CARDS_TO_STUDY_SETTINGS, numCardsToStudy);
                        config.save();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            ModMinMaxSlider studyIntervalSlider = new ModMinMaxSlider("Number of floors between breaks",
                    350.0f,
                    600.0f,
                    1,
                    16,
                    studyInterval,
                    "%.0f",
                    settingsPanel,
                    (slider) -> {
                        studyInterval = Math.round(slider.getValue());
                        try {
                            // And based on that boolean, set the settings and save them
                            SpireConfig config = new SpireConfig("ankiStudyBreak", "ankiStudyBreakConfig", ankiStudyBreakDefaultSettings);
                            config.setInt(STUDY_INTERVAL_SETTINGS, studyInterval);
                            config.save();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        randStudyInterval = 1;
                    }
            );

            ModLabeledToggleButton useRandStudyIntervalButton = new ModLabeledToggleButton(
                    "Randomize number of floors between breaks, using slider value as max.",
                    800.0f,
                    600.0f,
                    Settings.CREAM_COLOR,
                    FontHelper.charDescFont, // Position (trial and error it), color, font
                    useRandStudyInterval, //Boolean it uses
                    settingsPanel, // The mod panel in which this button will be in
                    (label) -> {},
                    (button) -> {
                        useRandStudyInterval = button.enabled;
                        try {
                            // And based on that boolean, set the settings and save them
                            SpireConfig config = new SpireConfig("ankiStudyBreak", "ankiStudyBreakConfig", ankiStudyBreakDefaultSettings);
                            config.setBool(USE_RAND_STUDY_INTERVAL_SETTINGS, useRandStudyInterval);
                            config.save();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if(button.enabled) {
                            randStudyInterval = 1;
                        }
                    }
            );

            AnkiConnectSetUrl urlReceiver = new AnkiConnectSetUrl();
            ModLabeledToggleButton setAnkiConnectUrl = new ModLabeledToggleButton(
                    "URL is only accurate if toggle is off. Current URL: " + ankiConnectUrlString,
                    350,
                    450,
                    Settings.CREAM_COLOR,
                    FontHelper.charDescFont, // Position (trial and error it), color, font
                    currentlyTypingAnkiConnectUrl, // Boolean it uses
                    settingsPanel, // The mod panel in which this button will be in
                    (label) -> {
                        label.text = "ADVANCED OPTION: Check to change AnkiConnect URL. \n MAKE SURE THIS IS UNCHECKED when you finish typing.\n Current URL: " + ankiConnectUrlString;
                    },
                    (button) -> {
                        if(button.enabled) {
                            ankiConnectUrlString = "";
                            TextInput.startTextReceiver(urlReceiver);
                        }
                        else {
                            TextInput.stopTextReceiver(urlReceiver);
                            try {
                                // And based on that boolean, set the settings and save them
                                SpireConfig config = new SpireConfig("ankiStudyBreak", "ankiStudyBreakConfig", ankiStudyBreakDefaultSettings);
                                config.setString(ANKICONNECT_URL_SETTINGS, ankiConnectUrlString);
                                config.save();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        logger.info("URL: " + ankiConnectUrlString);
                    }); // The actual button:);

            ModLabeledButton resetButton = new ModLabeledButton("Reset Study Break mod settings.",
                    350.0f, 200.0f, Settings.CREAM_COLOR, Settings.CREAM_COLOR,
                    settingsPanel, // The mod panel in which this button will be in
                    (button) -> { // The actual button:

                        numCardsToStudySlider.setValue(10);
                        numCardsToStudy = 10;
                        studyIntervalSlider.setValue(1);
                        studyInterval = 1;
                        randStudyInterval = 1;
                        useRandStudyInterval = false;
                        if(setAnkiConnectUrl.toggle.enabled) {
                            setAnkiConnectUrl.toggle.toggle();
                        }
                        ankiConnectUrlString = "http://127.0.0.1:8765";
                        try {
                            // And based on that boolean, set the settings and save them
                            SpireConfig config = new SpireConfig("ankiStudyBreak", "ankiStudyBreakConfig", ankiStudyBreakDefaultSettings);
                            config.setString(ANKICONNECT_URL_SETTINGS, ankiConnectUrlString);
                            config.setInt(NUM_CARDS_TO_STUDY_SETTINGS, numCardsToStudy);
                            config.setInt(STUDY_INTERVAL_SETTINGS, studyInterval);
                            config.setBool(USE_RAND_STUDY_INTERVAL_SETTINGS, useRandStudyInterval);
                            config.save();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        logger.info("Anki Study Break options were reset");
                    });
            settingsPanel.addUIElement(setAnkiConnectUrl); // Add the button to the settings panel. Button is a go.
            settingsPanel.addUIElement(numCardsToStudySlider);
            settingsPanel.addUIElement(studyIntervalSlider);
            settingsPanel.addUIElement(useRandStudyIntervalButton);
            settingsPanel.addUIElement(resetButton);

        } catch (Exception e) {
            e.printStackTrace();
        }



        BaseMod.registerModBadge(badgeTexture, MODNAME, AUTHOR, DESCRIPTION, settingsPanel);

        

        logger.info("Done loading badge Image and mod options");
    }
    
    // =============== / POST-INITIALIZE/ =================


    // ================ ADD CARDS ===================
    
    @Override
    public void receiveEditCards() {
        //Ignore this
        pathCheck();
        logger.info("Adding cards");
        // This method automatically adds any cards so you don't have to manually load them 1 by 1
        // For more specific info, including how to exclude cards from being added:
        // https://github.com/daviscook477/BaseMod/wiki/AutoAdd

        // The ID for this function isn't actually your modid as used for prefixes/by the getModID() method.
        // It's the mod id you give MTS in ModTheSpire.json - by default your artifact ID in your pom.xml

        new AutoAdd("AnkiStudyBreak") // ${project.artifactId}
            .packageFilter(YesStudied.class) // filters to any class in the same package as AbstractDefaultCard, nested packages included
            .setDefaultSeen(true)
            .cards();

        logger.info("Done adding cards!");
    }
    
    // ================ /ADD CARDS/ ===================
    
    
    // ================ LOAD THE TEXT ===================
    
    @Override
    public void receiveEditStrings() {
        logger.info("Beginning to edit strings for mod with ID: " + getModID());

        String language = getLanguage();
        logger.info("Getting strings for language: " + language);
        // CardStrings
        BaseMod.loadCustomStringsFile(CardStrings.class,
                getModID() + "Resources/localization/" + language + "/AnkiStudyBreak-Card-Strings.json");
        
        logger.info("Done editing strings");
    }
    
    // ================ /LOAD THE TEXT/ ===================

    // this adds "ModName:" before the ID of any card/relic/power etc.
    // in order to avoid conflicts if any other mod uses the same ID.
    public static String makeID(String idText) {
        return getModID() + ":" + idText;
    }

    private String getLanguage() {
        String supportedLanguages[] = {"eng", "zhs"};
        String language = Settings.language.toString().toLowerCase();
        if (!Arrays.asList(supportedLanguages).contains(language)) {
            language = "eng";
        }
        return language;
    }

    // ================ /RANDOM STUDY INTERVAL/ ===================
    public static int calcStudyInterval() {
        if(useRandStudyInterval) {
            return randStudyInterval;
        }
        return studyInterval;
    }
    public static void randomizeStudyInterval() {
        Random random = new Random();
        randStudyInterval = random.nextInt(studyInterval)+1;
    }
}
