package com.blzsaa;

import org.assertj.core.util.Strings;

import java.text.MessageFormat;

public class ImdbListHelper {
    public static final String HEADER =
            "Position,Const,Created,Modified,Description,Title,URL,Title Type,IMDb Rating,Runtime (mins),Year,Genres,Num Votes,Release Date,Directors";
    public static final String TITLE1 = "TITLE1";
    public static final String TITLE2 = "TITLE2";
    public static final String CSV =
            Strings.join(HEADER, rowWithTitle(TITLE1), rowWithTitle(TITLE2)).with(System.lineSeparator());

    public static String rowWithTitle(String title) {
        return MessageFormat.format(
                "1,tt1612774,2017-12-28,2017-12-28,,{0},http://www.imdb.com/title/tt1612774/,movie,5.8,82,2010,\"Comedy, Horror\",28813,2010-05-15,Quentin Dupieux",
                title);
    }
}
