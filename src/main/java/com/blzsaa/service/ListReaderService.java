package com.blzsaa.service;

import com.blzsaa.ImdbListComparatorException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ListReaderService {
    public List<String> readMovieTitlesFrom(MultipartFile[] uploadingFiles) {
        return Arrays.stream(uploadingFiles)
                .map(this::createReader)
                .map(this::getTitlesFromCsv)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private Reader createReader(MultipartFile uploadingFile) {
        try {
            return new InputStreamReader(uploadingFile.getInputStream());
        } catch (IOException e) {
            throw new ImdbListComparatorException(e);
        }
    }

    private List<String> getTitlesFromCsv(Reader reader) {
        List<String> titles = new ArrayList<>();
        try (ICsvMapReader mapReader = new CsvMapReader(reader, CsvPreference.STANDARD_PREFERENCE)) {

            // the header columns are used as the keys to the Map
            final String[] header = mapReader.getHeader(true);

            Map<String, String> customerMap;
            while ((customerMap = mapReader.read(header)) != null) {
                titles.add(customerMap.get("Title"));
            }
        } catch (IOException e) {
            throw new ImdbListComparatorException(e);
        }
        return titles;
    }
}
