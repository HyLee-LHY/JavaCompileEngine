

package com.xiaoyv.dx.rop.cst;

/**
 * Constants which are literal 64-bit values of some sort.
 */
public abstract class CstLiteral64
        extends CstLiteralBits {
    /**
     * the value as {@code long} bits
     */
    private final long bits;

    /**
     * Constructs an instance.
     *
     * @param bits the value as {@code long} bits
     */
    /*package*/ CstLiteral64(long bits) {
        this.bits = bits;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(Object other) {
        return (other != null) &&
                (getClass() == other.getClass()) &&
                bits == ((CstLiteral64) other).bits;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode() {
        return (int) bits ^ (int) (bits >> 32);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int compareTo0(Constant other) {
        long otherBits = ((CstLiteral64) other).bits;

        if (bits < otherBits) {
            return -1;
        } else if (bits > otherBits) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isCategory2() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean fitsInInt() {
        return (int) bits == bits;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getIntBits() {
        return (int) bits;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final long getLongBits() {
        return bits;
    }
}
