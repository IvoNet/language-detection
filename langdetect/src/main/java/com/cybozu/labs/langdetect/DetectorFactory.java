package com.cybozu.labs.langdetect;

import com.cybozu.labs.langdetect.util.LangProfile;
import com.rmtheis.langdetect.profile.AR;
import com.rmtheis.langdetect.profile.BG;
import com.rmtheis.langdetect.profile.CA;
import com.rmtheis.langdetect.profile.CS;
import com.rmtheis.langdetect.profile.DA;
import com.rmtheis.langdetect.profile.DE;
import com.rmtheis.langdetect.profile.EL;
import com.rmtheis.langdetect.profile.EN;
import com.rmtheis.langdetect.profile.ES;
import com.rmtheis.langdetect.profile.ET;
import com.rmtheis.langdetect.profile.FI;
import com.rmtheis.langdetect.profile.FR;
import com.rmtheis.langdetect.profile.GL;
import com.rmtheis.langdetect.profile.HE;
import com.rmtheis.langdetect.profile.HI;
import com.rmtheis.langdetect.profile.HR;
import com.rmtheis.langdetect.profile.HT;
import com.rmtheis.langdetect.profile.HU;
import com.rmtheis.langdetect.profile.ID;
import com.rmtheis.langdetect.profile.IT;
import com.rmtheis.langdetect.profile.JA;
import com.rmtheis.langdetect.profile.LT;
import com.rmtheis.langdetect.profile.LV;
import com.rmtheis.langdetect.profile.MK;
import com.rmtheis.langdetect.profile.MT;
import com.rmtheis.langdetect.profile.NE;
import com.rmtheis.langdetect.profile.NL;
import com.rmtheis.langdetect.profile.NO;
import com.rmtheis.langdetect.profile.PL;
import com.rmtheis.langdetect.profile.PT;
import com.rmtheis.langdetect.profile.RO;
import com.rmtheis.langdetect.profile.RU;
import com.rmtheis.langdetect.profile.SV;
import com.rmtheis.langdetect.profile.TA;
import com.rmtheis.langdetect.profile.TR;
import com.rmtheis.langdetect.profile.UK;
import com.rmtheis.langdetect.profile.VI;
import com.rmtheis.langdetect.profile.ZHCN;
import com.rmtheis.langdetect.profile.ZHTW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Language Detector Factory Class
 *
 * This class manages an initialization and constructions of {@link Detector}. 
 *
 * Before using language detection library, 
 * load profiles with {@link DetectorFactory#loadProfile(java.util.List)} method
 * and set initialization parameters.
 *
 * When the language detection,
 * construct Detector instance via {@link DetectorFactory#create()}.
 * See also {@link Detector}'s sample code.
 *
 * <ul>
 * <li>4x faster improvement based on Elmer Garduno's code. Thanks!</li>
 * </ul>
 *
 * @see Detector
 * @author Nakatani Shuyo
 * @author ivonet
 */
public class DetectorFactory {
    public static final HashMap<String, double[]> wordLangProbMap;
    public static final ArrayList<String> langlist;
    private static final List<LangProfile> profilelist;
    public Long seed = null;

    static {
        wordLangProbMap = new HashMap<String, double[]>();
        langlist = new ArrayList<String>();
        profilelist = Arrays.asList(
//                (new AF()).getLangProfile(), // Afrikaans
//                (new SQ()).getLangProfile(), // Albanian
                (new AR()).getLangProfile(), // Arabic
//                (new AN()).getLangProfile(), // Aragonese
//                (new AST()).getLangProfile(), // Asturian
//                (new EU()).getLangProfile(), // Basque
//                (new BE()).getLangProfile(), // Belarusian
//                (new BN()).getLangProfile(), // Bengali
//                (new BR()).getLangProfile(), // Breton
                (new BG()).getLangProfile(), // Bulgarian
                (new CA()).getLangProfile(), // Catalan
                (new ZHCN()).getLangProfile(), // Chinese (Simplified)
                (new ZHTW()).getLangProfile(), // Chinese (Traditional)
                (new HR()).getLangProfile(), // Croatian
                (new CS()).getLangProfile(), // Czech
                (new DA()).getLangProfile(), // Danish
                (new NL()).getLangProfile(), // Dutch
                (new EN()).getLangProfile(), // English
                (new ET()).getLangProfile(), // Estonian
                (new FI()).getLangProfile(), // Finnish
                (new FR()).getLangProfile(), // French
                (new GL()).getLangProfile(), // Galician
                (new DE()).getLangProfile(), // German
                (new EL()).getLangProfile(), // Greek
//                (new GU()).getLangProfile(), // Gujarati
                (new HT()).getLangProfile(), // Haitian
                (new HE()).getLangProfile(), // Hebrew
                (new HI()).getLangProfile(), // Hindi
                (new HU()).getLangProfile(), // Hungarian
//                (new IS()).getLangProfile(), // Icelandic
                (new ID()).getLangProfile(), // Indonesian
//                (new GA()).getLangProfile(), // Irish
                (new IT()).getLangProfile(), // Italian
                (new JA()).getLangProfile(), // Japanese
//                (new KN()).getLangProfile(), // Kannada
//                (new KN()).getLangProfile(), // Kannada
//                (new KO()).getLangProfile(), // Korean
                (new LV()).getLangProfile(), // Latvian
                (new LT()).getLangProfile(), // Lithuanian
                (new MK()).getLangProfile(), // Macedonian
//                (new MS()).getLangProfile(), // Malay
//                (new ML()).getLangProfile(), // Malayalam
                (new MT()).getLangProfile(), // Maltese
//                (new MR()).getLangProfile(), // Marathi
                (new NE()).getLangProfile(), // Nepali
                (new NO()).getLangProfile(), // Norwegian
//                (new OC()).getLangProfile(), // Occitan
//                (new PA()).getLangProfile(), // Panjabi
//                (new FA()).getLangProfile(), // Persian
                (new PL()).getLangProfile(), // Polish
                (new PT()).getLangProfile(), // Portuguese
                (new RO()).getLangProfile(), // Romanian
                (new RU()).getLangProfile(), // Russian
//                (new SR()).getLangProfile(), // Serbian
//                (new SK()).getLangProfile(), // Slovak
//                (new SL()).getLangProfile(), // Slovene
//                (new SO()).getLangProfile(), // Somali
                (new ES()).getLangProfile(), // Spanish
//                (new SW()).getLangProfile(), // Swahili
                (new SV()).getLangProfile(), // Swedish
//                (new TL()).getLangProfile(), // Tagalog
                (new TA()).getLangProfile(), // Tamil
//                (new TE()).getLangProfile(), // Telugu
//                (new TH()).getLangProfile(), // Thai
                (new TR()).getLangProfile(), // Turkish
                (new UK()).getLangProfile(), // Ukrainian
//                (new UR()).getLangProfile(), // Urdu
                (new VI()).getLangProfile() // Vietnamese
//                (new CY()).getLangProfile() // Welsh
//                (new YI()).getLangProfile() // Yiddish

        );
    }

    private DetectorFactory() {
    }

    private static final DetectorFactory instance_ = new DetectorFactory();

    public static void loadProfile(final List<LangProfile> profiles) {
        int index = 0;
        final int langsize = profiles.size();
        for (final LangProfile profile : profiles) {
            addProfile(profile, index, langsize);
            ++index;
      }

    }

    static /* package scope */ void addProfile(final LangProfile profile, final int index, final int langsize) {
        final String lang = profile.name;
        langlist.add(lang);
        for (final String word : profile.freq
                                        .keySet()) {
            if (!wordLangProbMap.containsKey(word)) {
                wordLangProbMap.put(word, new double[langsize]);
            }
            final int length = word.length();
            if ((length >= 1) && (length <= 3)) {
                final double prob = profile.freq
                                           .get(word)
                                           .doubleValue() / profile.n_words[length - 1];
                wordLangProbMap.get(word)[index] = prob;
            }
        }
    }

    /**
     * Clear loaded language profiles (reinitialization to be available)
     */
    public static void clear() {
        langlist.clear();
        wordLangProbMap.clear();
    }

    /**
     * Construct Detector instance
     *
     * @return Detector instance
     * @throws LangDetectException
     */
    public static Detector create() {
        return createDetector();
    }

    /**
     * Construct Detector instance with smoothing parameter 
     *
     * @param alpha smoothing parameter (default value = 0.5)
     * @return Detector instance
     * @throws LangDetectException
     */
    public static Detector create(final double alpha) throws LangDetectException {
        final Detector detector = createDetector();
        detector.setAlpha(alpha);
        return detector;
    }

    private static Detector createDetector() {
        final Detector detector = new Detector(instance_);
        if (wordLangProbMap.isEmpty()) {
            loadProfile(profilelist);
        }
        return detector;
    }

    public static void setSeed(final long seed) {
        instance_.seed = seed;
    }

    public static List<String> getLangList() {
        return Collections.unmodifiableList(langlist);
    }
}
