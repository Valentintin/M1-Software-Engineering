package fr.univ_lyon1.info.m1.elizagpt.model;

//import javafx.scene.control.Label;
import fr.univ_lyon1.info.m1.elizagpt.data.Data;
import fr.univ_lyon1.info.m1.elizagpt.observer.ProcessorObserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Logic to process a message (and probably reply to it).
 */

public class MessageProcessor {
    private final Random random = new Random();
    private final List<Data> dataList = new ArrayList<>(); // Liste de message
    private ProcessorObserver observer = null;
    private String name = null; //variable tmp pour stocker le nom.

    /**
     * Créé un nouveau observer.
     *
     * @param newObserver
     */
    public void attachObserver(final ProcessorObserver newObserver) {
        observer = newObserver;
    }

    /**
     * Met a jour l'observer.
     *
     */
    public void notifyObservers() {
        observer.processorUpdated();
    }

    /**
     * constructor of processor
     */
    public void beginConversation() {
        dataList.add(new Data("Bonjour", true));
        notifyObservers();
    }

    /**
     * Normlize the text: remove extra spaces, add a final dot if missing.
     *
     * @param text
     * @return normalized text.
     */
    public String normalize(final String text) {
        return text.replaceAll("\\s+", " ")
                .replaceAll("^\\s+", "")
                .replaceAll("\\s+$", "")
                .replaceAll("[^\\.!?:]$", "$0.");
    }

    /**
     * Recupère la dernière réponse du robot.
     *
     * @return réponse du robot.
     */
    public String lastResponse() {
        return dataList.get(dataList.size() - 1).getMessage();
    }

    /**
     * Traite le message envoyé par l'utilisateur.
     * @param normalizedText
     */
    public void easyAnswer(final String normalizedText) {

        dataList.add(new Data(normalizedText, false));

        Pattern pattern;
        Matcher matcher;

        // First, try to answer specifically to what the user said
        pattern = Pattern.compile(".*Je m'appelle (.*)\\.", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(normalizedText);
        if (matcher.matches()) {
            name = matcher.group(1);
            dataList.add(new Data(("Bonjour " + matcher.group(1) + "."), true));
            notifyObservers();

            return;
        }
        pattern = Pattern.compile("Quel est mon nom \\?", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(normalizedText);
        if (matcher.matches()) {
            if (name != null) {
                dataList.add(new Data(("Votre nom est " + name + "."), true));
                notifyObservers();
            } else {
                dataList.add(new Data(("Je ne connais pas votre nom."), true));
                notifyObservers();
            }
            return;
        }
        pattern = Pattern.compile("Qui est le plus (.*) \\?", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(normalizedText);
        if (matcher.matches()) {
            dataList.add(new Data(
                    ("Le plus " + matcher.group(1) + " est bien sûr votre enseignant de MIF01 !"),
                    true)
            );
            notifyObservers();
            return;
        }
        pattern = Pattern.compile("(Je .*)\\.", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(normalizedText);
        if (matcher.matches()) {
            final String startQuestion = pickRandom(new String[] {
                    "Pourquoi dites-vous que ",
                    "Pourquoi pensez-vous que ",
                    "Êtes-vous sûr que ",
            });
            dataList.add(new Data(
                    (startQuestion + firstToSecondPerson(matcher.group(1)) + " ?"),
                    true)
            );
            notifyObservers();
            return;
        }
        pattern = Pattern.compile("(.*)\\?", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(normalizedText);
        if (matcher.matches()) {
            final String startQuestion = pickRandom(new String[] {
                    "Je vous renvoie la question ",
                    "Ici, c'est moi qui pose les\n" +  "questions. ",
            });

            dataList.add(new Data((startQuestion), true));
            notifyObservers();
            return;
        }
        // Nothing clever to say, answer randomly
        if (random.nextBoolean()) {
            dataList.add(new Data(("Il faut beau aujourd'hui, vous ne trouvez pas ?"), true));
            notifyObservers();
            return;
        }
        if (random.nextBoolean()) {
            dataList.add(new Data(("Je ne comprends pas."), true));
            notifyObservers();
            return;
        }
        if (random.nextBoolean()) {
            dataList.add(new Data(("Hmmm, hmm ..."), true));
            notifyObservers();
            return;
        }
        // Default answer
        if (name != null) {
            dataList.add(new Data(("Qu'est-ce qui vous fait dire cela, " + name + " ?"), true));
            notifyObservers();
        } else {
            dataList.add(new Data(
                    ("Qu'est-ce qui vous fait dire cela ?"),
                    true)
            );
            notifyObservers();
        }
    }

    /**
     * Information about conjugation of a verb.
     */
    public static class Verb {
        private final String firstSingular;
        private final String secondPlural;

        public String getFirstSingular() {
            return firstSingular;
        }

        public String getSecondPlural() {
            return secondPlural;
        }

        Verb(final String firstSingular, final String secondPlural) {
            this.firstSingular = firstSingular;
            this.secondPlural = secondPlural;
        }
    }

    /**
     * List of 3rd group verbs and their correspondance from 1st person signular
     * (Je) to 2nd person plural (Vous).
     */
    protected static final List<Verb> VERBS = Arrays.asList(
            new Verb("suis", "êtes"),
            new Verb("vais", "allez"),
            new Verb("peux", "pouvez"),
            new Verb("dois", "devez"),
            new Verb("dis", "dites"),
            new Verb("ai", "avez"),
            new Verb("fais", "faites"),
            new Verb("sais", "savez"),
            new Verb("dois", "devez"));

    /**
     * Turn a 1st-person sentence (Je ...) into a plural 2nd person (Vous ...).
     * The result is not capitalized to allow forming a new sentence.
     *
     *
     *
     * @param text
     * @return The 2nd-person sentence.
     */
    public String firstToSecondPerson(final String text) {
        String processedText = text
                .replaceAll("[Jj]e ([a-z]*)e ", "vous $1ez ");
        for (Verb v : VERBS) {
            processedText = processedText.replaceAll(
                    "[Jj]e " + v.getFirstSingular(),
                    "vous " + v.getSecondPlural());
        }
        processedText = processedText
                .replaceAll("[Jj]e ([a-z]*)s ", "vous $1ssez ")
                .replace("mon ", "votre ")
                .replace("ma ", "votre ")
                .replace("mes ", "vos ")
                .replace("moi", "vous");
        return processedText;
    }

    /** Pick an element randomly in the array. */
    public <T> T pickRandom(final T[] array) {
        return array[random.nextInt(array.length)];
    }
}
