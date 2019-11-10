package com.vladsch.flexmark.util.sequence;

import com.vladsch.flexmark.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A CharSequence that references original char sequence with offsets into original preserved
 * <p>
 * a subSequence() returns a sub-sequence from the original base sequence with corresponding offsets
 */
@SuppressWarnings("SameParameterValue")
public interface BasedSequence extends IRichSequence<BasedSequence> {
    BasedSequence NULL = new EmptyBasedSequence();
    BasedSequence EMPTY = new EmptyBasedSequence();
    BasedSequence EOL = CharSubSequence.of(IRichSequence.EOL);
    BasedSequence SPACE = CharSubSequence.of(IRichSequence.SPACE);
    List<BasedSequence> EMPTY_LIST = new ArrayList<>();
    BasedSequence[] EMPTY_ARRAY = new BasedSequence[0];
    BasedSequence[] EMPTY_SEGMENTS = new BasedSequence[0];
    BasedSequence LINE_SEP = CharSubSequence.of(IRichSequence.LINE_SEP);

    @NotNull
    static BasedSequence of(@NotNull CharSequence charSequence) {
        return BasedSequenceImpl.create(charSequence, 0, charSequence.length());
    }

    @NotNull
    static BasedSequence of(@NotNull CharSequence charSequence, int startIndex) {
        return BasedSequenceImpl.create(charSequence, startIndex, charSequence.length());
    }

    @NotNull
    static BasedSequence of(@NotNull CharSequence charSequence, int startIndex, int endIndex) {
        return BasedSequenceImpl.create(charSequence, startIndex, endIndex);
    }

    @NotNull
    static BasedSequence ofSpaces(int count) {
        return of(RepeatedSequence.ofSpaces(count));
    }

    @NotNull
    static BasedSequence repeatOf(char c, int count) {
        return of(RepeatedSequence.repeatOf(String.valueOf(c), 0, count));
    }

    @NotNull
    static BasedSequence repeatOf(@NotNull CharSequence chars, int count) {
        return of(RepeatedSequence.repeatOf(chars, 0, chars.length() * count));
    }

    @NotNull
    static BasedSequence repeatOf(@NotNull CharSequence chars, int startIndex, int endIndex) {
        return of(RepeatedSequence.repeatOf(chars, startIndex, endIndex));
    }

    @NotNull
    static BasedSequenceBuilder builder(@NotNull BasedSequence sequence) {
        return sequence.getBuilder();
    }

    /**
     * Get the underlying object on which this sequence contents are based
     *
     * @return underlying object containing original text
     */
    @NotNull
    Object getBase();

    /**
     * Get the base sequence for the text
     *
     * @return base sequence
     */
    @NotNull
    BasedSequence getBaseSequence();

    /**
     * Get the start offset of this sequence into {@link #getBaseSequence()} and {@link #getBase()} original text source.
     *
     * @return start offset in original text
     */
    int getStartOffset();

    /**
     * Get the end offset of this sequence into {@link #getBaseSequence()} and {@link #getBase()} original text source.
     *
     * @return end offset in original text
     */
    int getEndOffset();

    /**
     * Get the offset of index in this sequence mapped to offset into {@link #getBaseSequence()} and {@link #getBase()} original text source.
     *
     * @param index index for which to get the offset in original source
     * @return offset of index of this sequence in original text
     */
    int getIndexOffset(int index);

    /**
     * Get the range of indices that map into {@link #getBaseSequence()} with startOffset and endOffset
     *
     * @param startOffset start offset into base sequence
     * @param endOffset   end offset into base sequence
     * @return range into this sequence that spans start and end offset.
     */
    @NotNull
    Range getIndexRange(int startOffset, int endOffset);

    /**
     * Get the range of this sequence in original {@link #getBaseSequence()} and {@link #getBase()} original text source.
     *
     * @return Range of start offset and end offset
     */
    @NotNull
    Range getSourceRange();

    /**
     * Get a portion of the original sequence that this sequence is based on
     *
     * @param startIndex offset from 0 of original sequence
     * @param endIndex   offset from 0 of original sequence
     * @return based sequence whose contents reflect the selected portion
     */
    @NotNull
    BasedSequence baseSubSequence(int startIndex, int endIndex);

    /**
     * Get a portion of the original sequence that this sequence is based on
     *
     * @param startIndex offset from 0 of original sequence
     * @return based sequence from startIndex to the endIndex
     */
    @NotNull
    BasedSequence baseSubSequence(int startIndex);

    /**
     * Safe, if index out of range returns '\0'
     *
     * @param index index in string
     * @return character or '\0' if index out of sequence range
     */
    char safeCharAt(int index);

    /**
     * Safe, if index out of range but based sequence has characters will return those, else returns '\0'
     * <p>
     * Allows peeking into preceding/following characters to the ones contained in this sequence
     *
     * @param index index in string
     * @return character or '\0' if index out of base sequence
     */
    char safeBaseCharAt(int index);

    /**
     * Get empty prefix to this sequence
     *
     * @return same as subSequence(0,0)
     */
    @NotNull
    BasedSequence getEmptyPrefix();

    /**
     * Get empty suffix to this sequence
     *
     * @return same as subSequence(length())
     */
    @NotNull
    BasedSequence getEmptySuffix();

    /**
     * Get the unescaped string of this sequence content
     *
     * @return unescaped text
     */
    @NotNull
    String unescape();

    /**
     * Get the unescaped string of this sequence content without unescaping entities
     *
     * @return unescaped text
     */
    @NotNull
    String unescapeNoEntities();

    /**
     * Get the unescaped string of this sequence content
     *
     * @param textMapper replaced text mapper which will be uses to map unescaped index to original source index
     * @return unescaped text in based sequence
     */
    @NotNull
    BasedSequence unescape(@NotNull ReplacedTextMapper textMapper);

    /**
     * replace any \r\n and \r by \n
     *
     * @param textMapper replaced text mapper which will be uses to map unescaped index to original source index
     * @return based sequence with only \n for line separators
     */
    @NotNull
    BasedSequence normalizeEOL(@NotNull ReplacedTextMapper textMapper);

    /**
     * replace any \r\n and \r by \n, append terminating EOL if one is not present
     *
     * @param textMapper replaced text mapper which will be uses to map unescaped index to original source index
     * @return based sequence with only \n for line separators and terminated by \n
     */
    @NotNull
    BasedSequence normalizeEndWithEOL(@NotNull ReplacedTextMapper textMapper);

    /**
     * Test if the given sequence is a continuation of this sequence in original source text
     *
     * @param other sequence to test
     * @return true if the given sequence is a continuation of this one in the original text
     */
    boolean isContinuedBy(@NotNull BasedSequence other);

    /**
     * Test if this sequence is a continuation of the given sequence in original source text
     *
     * @param other sequence to test
     * @return true if this sequence is a continuation of the given sequence in original source text
     */
    boolean isContinuationOf(@NotNull BasedSequence other);

    /**
     * Splice the given sequence to the end of this one and return a BasedSequence of the result.
     * Does not copy anything, creates a new based sequence of the original text but one that spans
     * characters of this sequence and other
     *
     * @param other sequence to append to end of this one
     * @return based sequence that contains the span from start of this sequence and end of other
     *     <p>
     *     assertion will fail if the other sequence is not a continuation of this one
     */
    @NotNull
    BasedSequence spliceAtEnd(@NotNull BasedSequence other);

    /**
     * start/end offset based containment, not textual
     *
     * @param other based sequence from the same base
     * @return true if other is contained in this
     */
    boolean containsAllOf(@NotNull BasedSequence other);

    /**
     * start/end offset based containment, not textual
     *
     * @param other based sequence from the same base
     * @return true if other is contained in this
     */
    boolean containsSomeOf(@NotNull BasedSequence other);

    /**
     * Get the prefix part of this from other, start/end offset based containment, not textual
     *
     * @param other based sequence from the same base
     * @return prefix part of this as compared to other, start/end offset based, not content
     */
    @NotNull
    BasedSequence prefixOf(@NotNull BasedSequence other);

    /**
     * Get the suffix part of this from other, start/end offset based containment, not textual
     *
     * @param other based sequence from the same base
     * @return suffix part of this as compared to other, start/end offset based, not content
     */
    @NotNull
    BasedSequence suffixOf(@NotNull BasedSequence other);

    /**
     * start/end offset based intersection, not textual
     *
     * @param other based sequence from the same parent
     * @return sequence which is the intersection of the range of this and other
     */
    @NotNull
    BasedSequence intersect(@NotNull BasedSequence other);

    /**
     * Extend this based sequence to include characters from underlying based sequence
     *
     * @param charSet  set of characters to include
     * @param maxCount maximum extra characters to include
     * @return sequence which
     */
    @NotNull
    BasedSequence extendByAny(@NotNull CharSequence charSet, int maxCount);

    @NotNull
    BasedSequence extendByAny(@NotNull CharSequence charSet);

    @NotNull
    BasedSequence extendByOneOfAny(@NotNull CharSequence charSet);

    /**
     * Extend this based sequence to include up to the next character from underlying based sequence
     *
     * @param charSet  set of characters to include
     * @param maxCount maximum extra characters to include
     * @return sequence which
     */
    @NotNull
    BasedSequence extendToAny(@NotNull CharSequence charSet, int maxCount);
    @NotNull
    BasedSequence extendToAny(@NotNull CharSequence charSet);

    /**
     * Extend in contained based sequence
     *
     * @param eolChars   characters to consider as EOL, note {@link #eolStartLength(int)} {@link #eolEndLength(int)} should report length of EOL found if length > 1
     * @param includeEol if to include the eol in the string
     * @return resulting sequence after extension. If already spanning the line then this sequence is returned.
     *     if the last character of this sequence are found in eolChars then no extension will be performed since it already includes the line end
     */
    @NotNull BasedSequence extendToEndOfLine(@NotNull CharSequence eolChars, boolean includeEol);
    @NotNull BasedSequence extendToEndOfLine(@NotNull CharSequence eolChars);
    @NotNull BasedSequence extendToEndOfLine(boolean includeEol);
    @NotNull BasedSequence extendToEndOfLine();

    /**
     * Extend in contained based sequence
     *
     * @param eolChars   characters to consider as EOL, note {@link #eolStartLength(int)} {@link #eolEndLength(int)} should report length of EOL found if length > 1
     * @param includeEol if to include the eol in the string
     * @return resulting sequence after extension. If already spanning the line then this sequence is returned.
     *     if the first character of this sequence are found in eolChars then no extension will be performed since it already includes the line end
     */
    @NotNull BasedSequence extendToStartOfLine(@NotNull CharSequence eolChars, boolean includeEol);
    @NotNull BasedSequence extendToStartOfLine(@NotNull CharSequence eolChars);
    @NotNull BasedSequence extendToStartOfLine(boolean includeEol);
    @NotNull BasedSequence extendToStartOfLine();

    /**
     * Extend this based sequence to include characters from underlying based sequence
     * taking tab expansion to 4th spaces into account
     *
     * @param maxColumns maximum columns to include, default {@link Integer#MAX_VALUE}
     * @return sequence which
     */
    @NotNull BasedSequence prefixWithIndent(int maxColumns);
    @NotNull BasedSequence prefixWithIndent();

    /*
      These are convenience methods returning coordinates in Base Sequence of this sequence
     */
    @NotNull Pair<Integer, Integer> baseLineColumnAtIndex(int index);
    @NotNull Range baseLineRangeAtIndex(int index);
    int baseEndOfLine(int index);
    int baseEndOfLineAnyEOL(int index);
    int baseStartOfLine(int index);
    int baseStartOfLineAnyEOL(int index);
    int baseColumnAtIndex(int index);

    @NotNull Pair<Integer, Integer> baseLineColumnAtStart();
    @NotNull Pair<Integer, Integer> baseLineColumnAtEnd();
    int baseEndOfLine();
    int baseEndOfLineAnyEOL();
    int baseStartOfLine();
    int baseStartOfLineAnyEOL();
    @NotNull Range baseLineRangeAtStart();
    @NotNull Range baseLineRangeAtEnd();
    int baseColumnAtEnd();
    int baseColumnAtStart();

    /**
     * Track given index in this sequence with respect to base offset
     *
     * @param index            offset within this sequence to track
     *                         ie. between its start and end offsets
     * @param trackerDirection direction of interest, specifically if this sequence is the
     *                         result of typing the direction is {@link TrackerDirection#RIGHT},
     *                         result of backspacing direction is {@link TrackerDirection#LEFT},
     *                         otherwise it is {@link TrackerDirection#NONE}
     * @return based sequence which tracks offset through editing modifications
     */
    @NotNull
    BasedSequence trackIndex(int index, @NotNull TrackerDirection trackerDirection);

    /**
     * Get the tracked index after editing and chopping manipulation of an offset tracking sequence
     *
     * @return best guess at the tracked index based on the contents of this sequence,
     *     0 to length() or -1 if offset is
     *     no longer part of this sequence
     */
    int getTrackedIndex();

    /**
     * Get tracked offset computed from the tracked index if this sequence is placed at startOffset
     * in the destination
     *
     * @param startOffset new start offset at destination for this sequence
     * @param maxOffset   max offset at destination
     * @return best guess at the tracked index based on the contents of this sequence,
     *     0 to maxOffset or -1 if offset is
     *     no longer part of this sequence
     */
    int getTrackedOffset(int startOffset, int maxOffset);

    class EmptyBasedSequence extends BasedSequenceImpl {
        @Override
        public int length() {
            return 0;
        }

        @Override
        public char charAt(int index) {
            throw new StringIndexOutOfBoundsException("String index: " + index + " out of range: 0, " + length());
        }

        @Override
        public int getIndexOffset(int index) {
            if (index == 0) return 0;
            throw new StringIndexOutOfBoundsException("String index: " + index + " out of range: 0, " + length());
        }

        @NotNull
        @Override
        public BasedSequence subSequence(int i, int i1) {
            if (i == 0 && i1 == 0) return this;
            throw new StringIndexOutOfBoundsException("EMPTY subSequence(" + i + "," + i1 + ") only subSequence(0, 0) is allowed");
        }

        @NotNull
        @Override
        public BasedSequence baseSubSequence(int startIndex, int endIndex) {
            return subSequence(startIndex, endIndex);
        }

        @NotNull
        @Override
        public BasedSequence getBaseSequence() {
            return BasedSequence.NULL;
        }

        @NotNull
        @Override
        public BasedSequence getBase() {
            return BasedSequence.NULL;
        }

        @Override
        public int getStartOffset() {
            return 0;
        }

        @Override
        public int getEndOffset() {
            return 0;
        }

        @NotNull
        @Override
        public Range getSourceRange() {
            return Range.NULL;
        }

        @NotNull
        @Override
        public String toString() {
            return "";
        }
    }
}
