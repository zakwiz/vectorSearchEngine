import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.nio.file.StandardOpenOption.READ;

public class Test {
    public static void main(String[] args) throws ClassNotFoundException, IOException{
        if (args.length != 1) {
            System.err.println("Incorrect arg format, arg format is: invertedIndexFile");
            System.exit(-1);
        }

        List<Document> documentCollection;
        Map<String, DictionaryEntry> dictionary;

        Path inputPath = Paths.get(args[0]).toAbsolutePath();
        try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(inputPath, READ)))) {
            documentCollection = (List<Document>)in.readObject();
            dictionary = (Map<String, DictionaryEntry>)in.readObject();
        }

        List<Integer> searchTimes = new ArrayList<>();

        System.out.print("Enter a search term (ZZEND to exit): ");
        Scanner scan = new Scanner(System.in);
        String input = scan.next();
        Stemmer stemmer = new Stemmer();
        stemmer.add(input.toLowerCase().toCharArray(), input.length());
        stemmer.stem();
        String stemmedInput = stemmer.toString();

        while (!input.equals("ZZEND")) {
            DictionaryEntry entry = dictionary.get(stemmedInput);

            if (entry != null) {
                long startTime = System.nanoTime();

                System.out.println("Document frequency: " + entry.getDocFrequency());
                Map<Integer, Posting> postingMap = entry.getPostings();

                for (Map.Entry<Integer, Posting> postingEntry : postingMap.entrySet()) {
                    Document document = documentCollection.get(postingEntry.getKey() - 1);
                    List<TermPosition> termPositions = postingEntry.getValue().getTermPositions();

                    System.out.println("Document: " + document.getId());
                    System.out.println(document.getTitle());
                    System.out.println("Term frequency: " + termPositions.size());
                    System.out.print("Indexes: ");

                    Object[] test = termPositions.stream().map(position -> (position.getSection().toString() + "." + position.getIndex())).toArray();
                    String[] termIndexes = Arrays.copyOf(termPositions.stream().map(position -> (position.getSection().toString() + "." + position.getIndex())).toArray(), termPositions.size(), String[].class);
                    for (int i = 0; i < termIndexes.length; i++) {
                        System.out.print(termIndexes[i]);

                        if (i < termIndexes.length - 1)
                            System.out.print(", ");
                        else
                            System.out.println();
                    }

                    TermPosition firstOccurrence = termPositions.get(0);
                    String sectionContent = null;

                    switch (firstOccurrence.getSection()) {
                        case TITLE:
                            sectionContent = document.getTitle();
                            break;
                        case ABSTRACT:
                            sectionContent = document.getAbstractText();
                            break;
                    }

                    int numWords = 0;
                    int summaryStartIndex = 0;
                    int summaryEndIndex = sectionContent.length() - 1;

                    if (firstOccurrence.getIndex() > 0) {
                        summaryStartIndex = firstOccurrence.getIndex() - 1;

                        while (summaryStartIndex > 0 && numWords < 5) {
                            summaryStartIndex--;

                            if (sectionContent.charAt(summaryStartIndex) == ' ' || summaryStartIndex == 0)
                                numWords++;
                        }
                    }

                    numWords -= 6;

                    if (firstOccurrence.getIndex() + input.length() < sectionContent.length() - 1) {
                        summaryEndIndex = firstOccurrence.getIndex() + 1;

                        while (summaryEndIndex < sectionContent.length() && numWords < 5) {
                            if (sectionContent.charAt(summaryEndIndex) == ' ' || summaryEndIndex + 1 == sectionContent.length())
                                numWords++;

                            summaryEndIndex++;
                        }
                    }

                    if (numWords < 5) {
                        while (summaryStartIndex > 0 && numWords < 5) {
                            summaryStartIndex--;

                            if (sectionContent.charAt(summaryStartIndex) == ' ')
                                numWords++;
                        }
                    }

                    System.out.println("Summary: " + (summaryStartIndex > 0 ? "..." : "") + sectionContent.substring(summaryStartIndex, summaryEndIndex).trim() + (summaryEndIndex < sectionContent.length() - 1 ? "..." : ""));
                    System.out.println();
                }

                long endTime = System.nanoTime();
                int elapsedTime = (int)(endTime - startTime);

                System.out.println("Search time: " + elapsedTime + " nanoseconds");
                System.out.println("----------------------------------------------------------------------");
                System.out.println();
                searchTimes.add(elapsedTime);
            } else
                System.out.println("Term not found");

            System.out.print("Enter another term (ZZEND to exit): ");
            input = scan.next();
            stemmer = new Stemmer();
            stemmer.add(input.toLowerCase().toCharArray(), input.length());
            stemmer.stem();
            stemmedInput = stemmer.toString();
        }

        if (searchTimes.size() > 0)
            System.out.println("Average search time: " + searchTimes.stream().mapToInt(Integer::intValue).average().getAsDouble() + " nanoseconds");
    }
}
