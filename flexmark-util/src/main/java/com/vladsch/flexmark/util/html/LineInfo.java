package com.vladsch.flexmark.util.html;

import com.vladsch.flexmark.util.collection.BitField;
import com.vladsch.flexmark.util.collection.BitFieldSet;
import org.jetbrains.annotations.NotNull;

/**
 * Line information in LineAppendable
 */
public class LineInfo {
    public enum Flags implements BitField {
        PREFORMATTED(2),
        BLANK_PREFIX,
        BLANK_TEXT,
        HAS_EOL,
        ;

        final int bits;

        Flags() { this(1); }

        Flags(int bits) { this.bits = bits; }

        @Override
        public int getBits() { return bits; }
    }

    public enum Preformatted {
        NONE,
        FIRST,
        BODY,
        LAST,
        ;

        final int mask;

        Preformatted() {
            this.mask = BitFieldSet.setBitField(0, Flags.PREFORMATTED, ordinal());
//            System.out.println(String.format("Preformatted.%s ordinal: %d mask: %s", this.name(), this.ordinal(), Long.toBinaryString(this.mask)));
        }

        @NotNull
        static Preformatted get(int flags) {
            int preformatted = flags & F_PREFORMATTED;

            if (preformatted == FIRST.mask) return FIRST;
            else if (preformatted == BODY.mask) return BODY;
            else if (preformatted == LAST.mask) return LAST;
            else return NONE;
        }
    }

    final public static Flags BLANK_PREFIX = Flags.BLANK_PREFIX;
    final public static Flags BLANK_TEXT = Flags.BLANK_TEXT;
    final public static Flags PREFORMATTED = Flags.PREFORMATTED;
    final public static Flags HAS_EOL = Flags.HAS_EOL;

    final public static int F_PREFORMATTED = BitFieldSet.intMask(Flags.PREFORMATTED);
    final public static int F_BLANK_PREFIX = BitFieldSet.intMask(Flags.BLANK_PREFIX);
    final public static int F_BLANK_TEXT = BitFieldSet.intMask(Flags.BLANK_TEXT);
    final public static int F_HAS_EOL = BitFieldSet.intMask(Flags.HAS_EOL);

    final public static LineInfo NULL = new LineInfo(-1, 0, 0, 0, 0, 0, 0, true, true, Preformatted.NONE);

    final public int index;             // line index
    final public int prefixLength;      // line's prefix length
    final public int textLength;        // line's text length
    final public int length;            // line's length (including EOL if any)
    final public int sumPrefixLength;   // total length of previous lines' prefixes
    final public int sumTextLength;     // total length of previous lines' text
    final public int sumLength;         // total length of previous lines
    final public int flags;

    private LineInfo(int index, int prefixLength, int textLength, int length, int sumPrefixLength, int sumTextLength, int sumLength, boolean isBlankPrefix, boolean isBlankText, @NotNull Preformatted preformatted) {
        this.index = index;
        this.prefixLength = prefixLength;
        this.textLength = textLength;
        this.length = length;
        this.sumPrefixLength = sumPrefixLength + prefixLength;
        this.sumTextLength = sumTextLength + textLength;
        this.sumLength = sumLength + length;
        this.flags = (isBlankPrefix || prefixLength == 0 ? F_BLANK_PREFIX : 0) | (isBlankText || textLength == 0 ? F_BLANK_TEXT : 0) | (preformatted.ordinal()) | (prefixLength + textLength < length ? F_HAS_EOL : 0);
    }

    public boolean isNull() {
        return this == NULL;
    }

    public boolean isNotNull() {
        return this != NULL;
    }

    public boolean isBlankPrefix() {
        return BitFieldSet.any(flags, F_BLANK_PREFIX);
    }

    public boolean isBlankText() {
        return BitFieldSet.any(flags, F_BLANK_TEXT);
    }

    public boolean isPreformatted() {
        return BitFieldSet.any(flags, F_PREFORMATTED);
    }

    @NotNull
    public Preformatted getPreformatted() {
        return Preformatted.get(flags);
    }

    /**
     * NOTE: a line which consists of any prefix and blank text is considered a blank line
     *
     * @return true if the line is a blank line
     */
    public boolean isBlankTextAndPrefix() {
        return BitFieldSet.all(flags, F_BLANK_PREFIX | F_BLANK_TEXT);
    }

    public boolean endsWithEOL() {
        return BitFieldSet.any(flags, F_HAS_EOL);
    }

    public int getTextStart() {
        return prefixLength;
    }

    public int getTextEnd() {
        return prefixLength + textLength;
    }

    @Override
    public String toString() {
        return "LineInfo{" +
                "i=" + index +
                ", pl=" + prefixLength +
                ", tl=" + textLength +
                ", l=" + length +
                ", sumPl=" + sumPrefixLength +
                ", sumTl=" + sumTextLength +
                ", sumL=" + sumLength +
                ", f=" + (isBlankPrefix() ? "bp " : "") + (isBlankText() ? "bt " : "") + (isPreformatted() ? "p " : "") +
                '}';
    }

    @NotNull
    public static LineInfo create(int prefixLength, int textLength, int length, boolean isBlankPrefix, boolean isBlankText, @NotNull Preformatted preformatted) {
        return new LineInfo(0, prefixLength, textLength, length, 0, 0, 0, isBlankPrefix, isBlankText, preformatted);
    }

    @NotNull
    public static LineInfo create(@NotNull LineInfo prevInfo, int prefixLength, int textLength, int length, boolean isBlankPrefix, boolean isBlankText, @NotNull Preformatted preformatted) {
        return new LineInfo(
                prevInfo.index + 1,
                prefixLength,
                textLength,
                length,
                prevInfo.sumPrefixLength,
                prevInfo.sumTextLength,
                prevInfo.sumLength,
                isBlankPrefix,
                isBlankText,
                preformatted
        );
    }

    @NotNull
    public static LineInfo create(@NotNull LineInfo prevInfo, @NotNull LineInfo info) {
        return new LineInfo(
                prevInfo.index + 1,
                info.prefixLength,
                info.textLength,
                info.length,
                prevInfo.sumPrefixLength,
                prevInfo.sumTextLength,
                prevInfo.sumLength,
                info.isBlankPrefix(),
                info.isBlankText(),
                info.getPreformatted()
        );
    }
}