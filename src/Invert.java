import static java.nio.file.StandardOpenOption.*;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Invert {
    public static void main(String[] args) throws java.io.IOException {
        if (args.length < 1 || args.length > 3) {
            System.err.println("Incorrect arg format, arg format is: docCollectionFile [removeStopWords] [useStemming]");
            System.exit(-1);
        }

        boolean removeStopWords = args.length == 1 || args[1].equals("true");
        boolean useStemming = args.length < 2 || args[2].equals("true");
        List<Document> documentCollection = new ArrayList<>();

        try (BufferedReader collectionReader = Files.newBufferedReader(Paths.get(args[0]).toAbsolutePath())) {
            String line;
            char currentSection = ' ';
            String partialString = null;
            List<String> partialArray = new ArrayList<>();
            while ((line = collectionReader.readLine()) != null) {
                if (documentCollection.size() == 2437)
                    System.out.println();
                if (line.charAt(0) == '.') {
                    if (currentSection != ' ') {
                        switch (currentSection) {
                            case 'T':
                                documentCollection.get(documentCollection.size() - 1).setTitle(partialString);
                                partialString = null;
                                break;
                            case 'B':
                                documentCollection.get(documentCollection.size() - 1).setPublicationDate(partialString);
                                partialString = null;
                                break;
                            case 'W':
                                documentCollection.get(documentCollection.size() - 1).setAbstractText(partialString);
                                partialString = null;
                                break;
                            case 'A':
                                documentCollection.get(documentCollection.size() - 1).setAuthorList(partialArray.toArray(new String[partialArray.size()]));
                                partialArray.clear();
                                break;
                        }
                    }

                    currentSection = line.charAt(1);

                    if (currentSection == 'I') {
                        documentCollection.add(new Document(Integer.parseInt(line.substring(3))));
                    }
                }
                else {
                    switch (currentSection) {
                        case 'T':
                        case 'B':
                        case 'W':
                            if (partialString != null) {
                                partialString = partialString.trim() + " " + line.trim();
                            }
                            else
                                partialString = line;
                            break;
                        case 'A':
                            partialArray.add(line);
                            break;
                    }
                }
            }

            List<String> stopWords = new ArrayList<>();
            if (removeStopWords) {
                Path stopWordsPath = Paths.get("stopwords.txt").toAbsolutePath();
                try (BufferedReader stopWordReader = Files.newBufferedReader(stopWordsPath)) {
                    String stopWord;
                    while ((stopWord = stopWordReader.readLine()) != null)
                        stopWords.add(stopWord);
                }
            }

            SortedMap<String, DictionaryEntry> dictionary = new TreeMap<>();

            for (Document document : documentCollection) {
                addDocumentSection(dictionary, document, TermPosition.Section.TITLE, stopWords, useStemming);
                addDocumentSection(dictionary, document, TermPosition.Section.ABSTRACT, stopWords, useStemming);
            }

            Path outputPath = Paths.get("./invertedIndex.txt");
            try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(outputPath, CREATE, WRITE)))) {
                out.writeObject(documentCollection);
                out.writeObject(dictionary);
            }
        }
    }

    private static void addDocumentSection(Map<String, DictionaryEntry> dictionary, Document document, TermPosition.Section section, List<String> stopWords, boolean useStemming) {
        String content = null;

        switch(section) {
            case TITLE:
                content = document.getTitle();
                break;
            case ABSTRACT:
                content = document.getAbstractText();
                break;
        }

        if (content != null) {
            content = content.toLowerCase();
            Pattern p = Pattern.compile("\\w[\\w-]+('\\w*)?");
            Matcher m = p.matcher(content);

            while (m.find())
            {
                String term = content.substring(m.start(), m.end());

                if (stopWords.indexOf(term) == -1) {
                    if (useStemming) {
                        Stemmer stemmer = new Stemmer();
                        stemmer.add(term.toCharArray(), term.length());
                        stemmer.stem();
                        term = stemmer.toString();
                    }

                    DictionaryEntry entry = dictionary.get(term);
                    TermPosition position = new TermPosition(section, m.start());

                    if (entry == null) {
                        entry = new DictionaryEntry();
                        dictionary.put(term, entry);
                    }

                    entry.addPosition(document.getId(), position);
                }
            }
        }
    }
}