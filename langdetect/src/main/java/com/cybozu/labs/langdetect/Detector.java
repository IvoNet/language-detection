package com.cybozu.labs.langdetect;

import com.cybozu.labs.langdetect.util.NGram;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * {@link Detector} class is to detect language from specified text. 
 * Its instance is able to be constructed via the factory class {@link DetectorFactory}.
 * <p>
 * After appending a target text to the {@link Detector} instance with {@link #append(Reader)} or {@link #append
 * (String)},
 * the detector provides the language detection results for target text via {@link #detect()} or {@link
 * #getProbabilities()}.
 * {@link #detect()} method returns a single language name which has the highest probability.
 * {@link #getProbabilities()} methods returns a list of multiple languages and their probabilities.
 * <p>  
 * The detector has some parameters for language detection.
 * See {@link #setAlpha(double)}, {@link #setMaxTextLength(int)} and {@link #setPriorMap(HashMap)}.
 *
 * <pre>
 * import java.util.ArrayList;
 * import com.cybozu.labs.langdetect.Detector;
 * import com.cybozu.labs.langdetect.DetectorFactory;
 * import com.cybozu.labs.langdetect.Language;
 *
 * class LangDetectSample {
 *     public void init(String profileDirectory) throws LangDetectException {
 *         DetectorFactory.loadProfile(profileDirectory);
 *     }
 *     public String detect(String text) throws LangDetectException {
 *         Detector detector = DetectorFactory.create();
 *         detector.append(text);
 *         return detector.detect();
 *     }
 *     public ArrayList<Language> detectLangs(String text) throws LangDetectException {
 *         Detector detector = DetectorFactory.create();
 *         detector.append(text);
 *         return detector.getProbabilities();
 *     }
 * }
 * </pre>
 *
 * <ul>
 * <li>4x faster improvement based on Elmer Garduno's code. Thanks!</li>
 * </ul>
 *
 * @author Nakatani Shuyo
 * @see DetectorFactory
 */
public class Detector {
    private static final double ALPHA_DEFAULT = 0.5;
    private static final double ALPHA_WIDTH = 0.05;

    private static final int ITERATION_LIMIT = 1000;
    private static final double PROB_THRESHOLD = 0.1;
    private static final double CONV_THRESHOLD = 0.99999;
    private static final int BASE_FREQ = 10000;
    private static final String UNKNOWN_LANG = "unknown";

    private static final Pattern URL_REGEX = Pattern.compile("https?://[-_.?&~;+=/#0-9A-Za-z]{1,2076}");
    private static final Pattern MAIL_REGEX = Pattern.compile(
            "[-_.0-9A-Za-z]{1,64}@[-_0-9A-Za-z]{1,255}[-_.0-9A-Za-z]{1,255}");

    private final HashMap<String, double[]> wordLangProbMap;
    private final ArrayList<String> langlist;

    private StringBuffer text;
    private double[] langprob;

    private double alpha = ALPHA_DEFAULT;
    private int max_text_length = 10000;
    private double[] priorMap;
    private boolean verbose;
    private Long seed;

    /**
     * Constructor.
     * Detector instance can be constructed via {@link DetectorFactory#create()}.
     * @param factory {@link DetectorFactory} instance (only DetectorFactory inside)
     */
    public Detector(final DetectorFactory factory) {
        this.wordLangProbMap = factory.wordLangProbMap;
        this.langlist = factory.langlist;
        this.text = new StringBuffer();
        this.seed = factory.seed;
    }

    /**
     * Set Verbose Mode(use for debug).
     */
    public void setVerbose() {
        this.verbose = true;
    }

    /**
     * Set smoothing parameter.
     * The default value is 0.5(i.e. Expected Likelihood Estimate).
     * @param alpha the smoothing parameter
     */
    public void setAlpha(final double alpha) {
        this.alpha = alpha;
    }

    /**
     * Set prior information about language probabilities.
     * @param priorMap the priorMap to set
     * @throws LangDetectException
     */
    public void setPriorMap(final HashMap<String, Double> priorMap) throws LangDetectException {
        this.priorMap = new double[this.langlist
                .size()];
        double sump = 0;
        for (int i = 0; i < this.priorMap.length; ++i) {
            final String lang = this.langlist
                    .get(i);
            if (priorMap.containsKey(lang)) {
                final double p = priorMap.get(lang);
                if (p < 0) {
                    throw new LangDetectException(ErrorCode.InitParamError, "Prior probability must be non-negative.");
                }
                this.priorMap[i] = p;
                sump += p;
            }
        }
        if (sump <= 0) {
            throw new LangDetectException(ErrorCode.InitParamError, "More one of prior probability must be non-zero.");
        }
        for (int i = 0; i < this.priorMap.length; ++i) {
            this.priorMap[i] /= sump;
        }
    }

    /**
     * Specify max size of target text to use for language detection.
     * The default value is 10000(10KB).
     * @param max_text_length the max_text_length to set
     */
    public void setMaxTextLength(final int max_text_length) {
        this.max_text_length = max_text_length;
    }


    /**
     * Append the target text for language detection.
     * This method read the text from specified input reader.
     * If the total size of target text exceeds the limit size specified by {@link Detector#setMaxTextLength(int)},
     * the rest is cut down.
     *
     * @param reader the input reader (BufferedReader as usual)
     * @throws IOException Can't read the reader.
     */
    public void append(final Reader reader) throws IOException {
        final char[] buf = new char[this.max_text_length / 2];
        while ((this.text
                        .length() < this.max_text_length) && reader.ready()) {
            final int length = reader.read(buf);
            append(new String(buf, 0, length));
        }
    }

    /**
     * Append the target text for language detection.
     * If the total size of target text exceeds the limit size specified by {@link Detector#setMaxTextLength(int)},
     * the rest is cut down.
     *
     * @param text the target text to append
     */
    public void append(String text) {
        text = URL_REGEX.matcher(text)
                        .replaceAll(" ");
        text = MAIL_REGEX.matcher(text)
                         .replaceAll(" ");
        text = NGram.normalize_vi(text);
        char pre = 0;
        for (int i = 0; (i < text.length()) && (i < this.max_text_length); ++i) {
            final char c = text.charAt(i);
            if ((c != ' ') || (pre != ' ')) {
                this.text
                        .append(c);
            }
            pre = c;
        }
    }

    /**
     * Cleaning text to detect
     * (eliminate URL, e-mail address and Latin sentence if it is not written in Latin alphabet)
     */
    private void cleaningText() {
        int latinCount = 0;
        int nonLatinCount = 0;
        for (int i = 0; i < this.text
                .length(); ++i) {
            final char c = this.text
                    .charAt(i);
            if ((c <= 'z') && (c >= 'A')) {
                ++latinCount;
            } else if ((c >= '\u0300') && (Character.UnicodeBlock
                                                    .of(c) != Character.UnicodeBlock.LATIN_EXTENDED_ADDITIONAL)) {
                ++nonLatinCount;
            }
        }
        if ((latinCount * 2) < nonLatinCount) {
            final StringBuffer textWithoutLatin = new StringBuffer();
            for (int i = 0; i < this.text
                    .length(); ++i) {
                final char c = this.text
                        .charAt(i);
                if ((c > 'z') || (c < 'A')) {
                    textWithoutLatin.append(c);
                }
            }
            this.text = textWithoutLatin;
        }

    }

    /**
     * Detect language of the target text and return the language name which has the highest probability.
     * @return detected language name which has most probability.
     * @throws LangDetectException
     *  code = ErrorCode.CantDetectError : Can't detect because of no valid features in text
     */
    public String detect() throws LangDetectException {
        final ArrayList<Language> probabilities = getProbabilities();
        if (!probabilities.isEmpty()) {
            return probabilities.get(0).lang;
        }
        return UNKNOWN_LANG;
    }

    public void clear() {
        this.text = new StringBuffer();
    }

    /**
     * Get language candidates which have high probabilities
     * @return possible languages list (whose probabilities are over PROB_THRESHOLD,
     * ordered by probabilities descendant
     * @throws LangDetectException
     *  code = ErrorCode.CantDetectError : Can't detect because of no valid features in text
     */
    public ArrayList<Language> getProbabilities() throws LangDetectException {
        if (this.langprob == null) {
            detectBlock();
        }

        return sortProbability(this.langprob);
    }

    /**
     * @throws LangDetectException
     *
     */
    private void detectBlock() throws LangDetectException {
        cleaningText();
        final ArrayList<String> ngrams = extractNGrams();
        if (ngrams.isEmpty()) {
            throw new LangDetectException(ErrorCode.CantDetectError, "no features in text");
        }

        this.langprob = new double[this.langlist
                .size()];

        final Random rand = new Random();
        if (this.seed != null) {
            rand.setSeed(this.seed);
        }
        final int n_trial = 7;
        for (int t = 0; t < n_trial; ++t) {
            final double[] prob = initProbability();
            final double alpha = this.alpha + (rand.nextGaussian() * ALPHA_WIDTH);

            int i = 0;
            while (true) {
                final int r = rand.nextInt(ngrams.size());
                updateLangProb(prob, ngrams.get(r), alpha);
                if ((i % 5) == 0) {
                    if ((normalizeProb(prob) > CONV_THRESHOLD) || (i >= ITERATION_LIMIT)) {
                        break;
                    }
                    if (this.verbose) {
                        System.out
                              .println("> " + sortProbability(prob));
                    }
                }
                ++i;
            }
            for (int j = 0; j < this.langprob.length; ++j) {
                this.langprob[j] += prob[j] / n_trial;
            }
            if (this.verbose) {
                System.out
                      .println("==> " + sortProbability(prob));
            }
        }
    }

    /**
     * Initialize the map of language probabilities.
     * If there is the specified prior map, use it as initial map.
     * @return initialized map of language probabilities
     */
    private double[] initProbability() {
        final double[] prob = new double[this.langlist
                .size()];
        if (this.priorMap != null) {
            System.arraycopy(this.priorMap, 0, prob, 0, prob.length);
        } else {
            for (int i = 0; i < prob.length; ++i) {
                prob[i] = 1.0 / this.langlist
                        .size();
            }
        }
        return prob;
    }

    /**
     * Extract n-grams from target text
     * @return n-grams list
     */
    private ArrayList<String> extractNGrams() {
        final ArrayList<String> list = new ArrayList<String>();
        final NGram ngram = new NGram();
        for (int i = 0; i < this.text
                .length(); ++i) {
            ngram.addChar(this.text
                                  .charAt(i));
            for (int n = 1; n <= NGram.N_GRAM; ++n) {
                final String w = ngram.get(n);
                if ((w != null) && this.wordLangProbMap
                        .containsKey(w)) {
                    list.add(w);
                }
            }
        }
        return list;
    }

    /**
     * update language probabilities with N-gram string(N=1,2,3)
     * @param word N-gram string
     */
    private boolean updateLangProb(final double[] prob, final String word, final double alpha) {
        if ((word == null) || !this.wordLangProbMap
                .containsKey(word)) {
            return false;
        }

        final double[] langProbMap = this.wordLangProbMap
                .get(word);
        if (this.verbose) {
            System.out
                  .println(word + "(" + unicodeEncode(word) + "):" + wordProbToString(langProbMap));
        }

        final double weight = alpha / BASE_FREQ;
        int i = 0;
        while (i < prob.length) {
            final double v = langProbMap[i];
            prob[i] *= weight + v;
            ++i;
        }
        return true;
    }

    private String wordProbToString(final double[] prob) {
        final Formatter formatter = new Formatter();
        for (int j = 0; j < prob.length; ++j) {
            final double p = prob[j];
            if (p >= 0.00001) {
                formatter.format(" %s:%.5f", this.langlist
                        .get(j), p);
            }
        }
        formatter.close();
        return formatter.toString();
    }

    /**
     * normalize probabilities and check convergence by the maximun probability
     * @return maximum of probabilities
     */
    private static double normalizeProb(final double[] prob) {
        double maxp = 0;
        double sump = 0;
        for (final double aProb : prob) {
            sump += aProb;
        }
        for (int i = 0; i < prob.length; ++i) {
            final double p = prob[i] / sump;
            if (maxp < p) {
                maxp = p;
            }
            prob[i] = p;
        }
        return maxp;
    }

    private ArrayList<Language> sortProbability(final double[] prob) {
        final ArrayList<Language> list = new ArrayList<Language>();
        for (int j = 0; j < prob.length; ++j) {
            final double p = prob[j];
            if (p > PROB_THRESHOLD) {
                for (int i = 0; i <= list.size(); ++i) {
                    if ((i == list.size()) || (list.get(i).prob < p)) {
                        list.add(i, new Language(this.langlist
                                                         .get(j), p));
                        break;
                    }
                }
            }
        }
        return list;
    }

    private static String unicodeEncode(final String word) {
        final StringBuilder buf = new StringBuilder();
        for (int i = 0; i < word.length(); ++i) {
            final char ch = word.charAt(i);
            if (ch >= '\u0080') {
                String st = Integer.toHexString(0x10000 + (int) ch);
                while (st.length() < 4) {
                    st = "0" + st;
                }
                buf.append("\\u")
                   .append(st.subSequence(1, 5));
            } else {
                buf.append(ch);
            }
        }
        return buf.toString();
    }

}
