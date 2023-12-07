package fr.univ_lyon1.info.m1.elizagpt.model;

import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Logic to process a message (and probably reply to it).
 */

public class MessageProcessor {
    private final Random random = new Random();
    private MessageList messageList = null;

    private MessageList filterMessageList = null;

    private FilterSubstring filterSubstring = new FilterSubstring();
    private FilterRegex filterRegex = new FilterRegex();


    private Map<String, Object> dataApplication = new HashMap<>();

    private MessagePattern messagePattern = new MessagePattern();


    /**
     * Constructor of MessageProcessor.
     *
     * @param msgList
     */
    public MessageProcessor(final MessageList msgList) {
        messageList = msgList;
    }

    /**
     * Normlize the text: remove extra spaces, add a final dot if missing.
     *
     * @param text
     * @return normalized text.
     */
    public Message normalize(final String text) {
        return new Message(text.replaceAll("\\s+", " ")
                .replaceAll("^\\s+", "")
                .replaceAll("\\s+$", "")
                .replaceAll("[^\\.!?:]$", "$0."), null, -1);
    }

    private void searchText(final Message text) {

        Pattern pattern;
        Matcher matcher;
        pattern = Pattern.compile(text.getMessage(), Pattern.CASE_INSENSITIVE);

        List<Integer> toDelete = new ArrayList<>();
        for (Message message : messageList.pullAllMessage()) {
            matcher = pattern.matcher(text.getMessage());
            if (!matcher.matches()) {
                // Can delete it right now, we're iterating over the list.
                toDelete.add(message.getId());
            }
        }

    }

    private String getUncleanAnswer(final Message normalizedText) {
        final Object answerTmp = messagePattern.getAnswer(normalizedText.getMessage());
        String toCleanAnswer = null;
        if (answerTmp instanceof String[]) {
            toCleanAnswer = pickRandom((String[]) answerTmp);
        } else if (answerTmp instanceof Map) {
            if (dataApplication.get("name") != null) {
                toCleanAnswer = ((Map<String, String>) answerTmp).get("hasName");
            } else {
                toCleanAnswer = ((Map<String, String>) answerTmp).get("hasNoName");
            }
        } else if (answerTmp instanceof String) {
            toCleanAnswer = (String) answerTmp;
        }
        return toCleanAnswer;
    }

    private String getCleanAnswer(final String toCleanAnswer) {
        String cleanAnswer;
        System.out.println(toCleanAnswer);
        if (toCleanAnswer.contains("%g")) {
            if (toCleanAnswer.contains("Bonjour")) {  //matcher.group(1) c'est le nom
                dataApplication.put("name", messagePattern.getMatcher().group(1));
            }
            cleanAnswer = toCleanAnswer.replace("%g", messagePattern.getMatcher().group(1));
        } else if (toCleanAnswer.contains("%je")) {
            cleanAnswer = toCleanAnswer.replace("%je",
                    firstToSecondPerson(messagePattern.getMatcher().group(1)));
        } else if (toCleanAnswer.contains("%n")) {
            cleanAnswer = toCleanAnswer.replace("%n", (String) dataApplication.get("name"));
        } else {
            cleanAnswer = toCleanAnswer;
        }
        return cleanAnswer;
    }

    /**
     * Traite le message envoyé par l'utilisateur.
     *
     * @param normalizedText
     */
    public void easyAnswer(final Message normalizedText) {

        messageList.add(normalizedText.getMessage(), false);

        String toCleanAnswer = getUncleanAnswer(normalizedText);

        String cleanAnswer = getCleanAnswer(toCleanAnswer);

        messageList.add(cleanAnswer, true);

//        // First, try to answer specifically to what the user said
//        pattern = Pattern.compile(".*Je m'appelle (.*)\\.", Pattern.CASE_INSENSITIVE);
//        matcher = pattern.matcher(normalizedText.getMessage());
//        if (matcher.matches()) {
//            name = matcher.group(1);
//            final String answer = "Bonjour " + matcher.group(1) + ".";
//            messageList.add(answer, true);
//
//            return;
//        }
//        pattern = Pattern.compile("Quel est mon nom \\?", Pattern.CASE_INSENSITIVE);
//        matcher = pattern.matcher(normalizedText.getMessage());
//        if (matcher.matches()) {
//            if (name != null) {
//                final String answer = "Votre nom est " + name + ".";
//                messageList.add(answer, true);
//            } else {
//                final String answer = "Je ne connais pas votre nom.";
//                messageList.add(answer, true);
//            }
//            return;
//        }
//        pattern = Pattern.compile("Qui est le plus (.*) \\?", Pattern.CASE_INSENSITIVE);
//        matcher = pattern.matcher(normalizedText.getMessage());
//        if (matcher.matches()) {
//            final String answer = "Le plus " + matcher.group(1)
//                    + " est bien sûr votre enseignant de MIF01 !";
//            messageList.add(
//                    answer,
//                    true
//            );
//            return;
//        }
//        pattern = Pattern.compile("(Je .*)\\.", Pattern.CASE_INSENSITIVE);
//        matcher = pattern.matcher(normalizedText.getMessage());
//        if (matcher.matches()) {
//            final String startQuestion = pickRandom(new String[]{
//                    "Pourquoi dites-vous que ",
//                    "Pourquoi pensez-vous que ",
//                    "Êtes-vous sûr que ",
//            });
//            final String answer = startQuestion + firstToSecondPerson(matcher.group(1)) + " ?";
//            messageList.add(
//                    answer,
//                    true
//            );
//            return;
//        }
//        pattern = Pattern.compile("(.*)\\?", Pattern.CASE_INSENSITIVE);
//        matcher = pattern.matcher(normalizedText.getMessage());
//        if (matcher.matches()) {
//            final String startQuestion = pickRandom(new String[]{
//                    "Je vous renvoie la question ",
//                    "Ici, c'est moi qui pose les\n" + "questions. ",
//            });
//            messageList.add((startQuestion), true);
//            return;
//        }
//        // Nothing clever to say, answer randomly
//        if (random.nextBoolean()) {
//            final String answer = "Il faut beau aujourd'hui, vous ne trouvez pas ?";
//            messageList.add(answer, true);
//            return;
//        }
//        if (random.nextBoolean()) {
//            final String answer = "Je ne comprends pas.";
//            messageList.add(answer, true);
//            return;
//        }
//        if (random.nextBoolean()) {
//            final String answer = "Hmmm, hmm ...";
//            messageList.add(answer, true);
//            return;
//        }
//        // Default answer
//        if (name != null) {
//            final String answer = "Qu'est-ce qui vous fait dire cela, " + name + " ?";
//            messageList.add(answer, true);
//        } else {
//            final String answer = "Qu'est-ce qui vous fait dire cela ?";
//            messageList.add(
//                    answer,
//                    true
//            );
//        }
    }

    /**
     * Apply the right way to filter.
     */
    public void doFilterAnswer(final String searchText) {
        filterMessageList = new MessageList(messageList);
        filterRegex.doFilter(searchText, messageList);
        messageList.notifyObservers();
    }

    /**
     * Undo the current filter.
     */
    public void undoFilterMessageList() {
        if (filterMessageList != null) {
            messageList.removeAll();
            for (Message msg : filterMessageList.pullAllMessage()) {
                messageList.add(msg.getMessage(), msg.getIsFromEliza());
            }
        }
        System.out.println(messageList.get(1).getMessage());
        messageList.notifyObservers();
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

    /**
     * Pick an element randomly in the array.
     */
    public <T> T pickRandom(final T[] array) {
        return array[random.nextInt(array.length)];
    }
}
