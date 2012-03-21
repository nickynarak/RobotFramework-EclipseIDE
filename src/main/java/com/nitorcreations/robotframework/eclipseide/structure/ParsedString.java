/**
 * Copyright 2012 Nitor Creations Oy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nitorcreations.robotframework.eclipseide.structure;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nitorcreations.robotframework.eclipseide.internal.rules.RFTArgumentUtils;
import com.nitorcreations.robotframework.eclipseide.structure.api.IParsedKeywordString;

/**
 * An immutable implementation of all the I*String interfaces in the
 * ...builder.info package.
 * 
 * @author xkr47
 */
public class ParsedString implements IParsedKeywordString {

    private static String[] STRIPPABLE_PREFIXES = { "given ", "when ", "then ", "and " };

    private final String value;
    private final int argCharPos;
    private ArgumentType type = ArgumentType.IGNORED;

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{[^}]+\\}");
    private Pattern valueAsKeywordPattern;
    private boolean checkedForInlineVariables;

    public enum ArgumentType {
        IGNORED, COMMENT, TABLE, SETTING_KEY, VARIABLE_KEY, NEW_TESTCASE, NEW_KEYWORD, SETTING_VAL, SETTING_FILE, SETTING_FILE_WITH_NAME_KEY, SETTING_FILE_ARG, SETTING_FILE_WITH_NAME_VALUE, VARIABLE_VAL, KEYWORD_LVALUE, FOR_PART, KEYWORD_CALL, KEYWORD_ARG,
    }

    public ParsedString(String value, int argCharPos) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        if (argCharPos < 0) {
            throw new IllegalArgumentException("argCharPos < 0");
        }
        this.value = value;
        this.argCharPos = argCharPos;
    }

    @Override
    public String getValue() {
        return value;
    }

    public boolean isEmpty() {
        return value.isEmpty() || value.equals("\\");
    }

    @Override
    public int getArgCharPos() {
        return argCharPos;
    }

    @Override
    public int getArgEndCharPos() {
        return argCharPos + value.length();
    }

    @Override
    public String getAlternateValue() {
        String lcValue = value.toLowerCase();
        for (String strippablePrefix : STRIPPABLE_PREFIXES) {
            if (lcValue.startsWith(strippablePrefix)) {
                return value.substring(strippablePrefix.length());
            }
        }
        return null;
    }

    public boolean matchesKeywordCall(String keywordCall) {
        if (!checkedForInlineVariables) {
            Matcher m = VARIABLE_PATTERN.matcher(value);
            if (m.find()) {
                StringBuffer valueAsKeywordPatternSb = new StringBuffer();
                int lastEnd = 0;
                do {
                    int start = m.start();
                    valueAsKeywordPatternSb.append(Pattern.quote(value.substring(lastEnd, start)));
                    valueAsKeywordPatternSb.append(".*");
                    lastEnd = m.end();
                } while (m.find());
                valueAsKeywordPatternSb.append(Pattern.quote(value.substring(lastEnd)));
                valueAsKeywordPattern = Pattern.compile(valueAsKeywordPatternSb.toString());
            }
            checkedForInlineVariables = true;
        }
        if (valueAsKeywordPattern != null) {
            return valueAsKeywordPattern.matcher(keywordCall).matches();
        } else {
            return value.equals(keywordCall);
        }
    }

    public ArgumentType getType() {
        return type;
    }

    public void setType(ArgumentType type) {
        assert type != null;
        this.type = type;
    }

    @Override
    public String toString() {
        return '"' + value + "\" (" + type + ')';
    }

    @Override
    public String getDebugString() {
        return toString() + " @" + argCharPos + "-" + (getArgEndCharPos() - 1);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return value.equals(obj);
    }

    public DynamicParsedString splitRegularArgument() {
        // TODO implement
        return new DynamicParsedString(value, argCharPos, null);
    }

    public String getUnescapedValue() {
        return RFTArgumentUtils.unescapeArgument(value, 0, value.length());
    }

}
