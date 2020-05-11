

package com.xiaoyv.dx.rop.cst;

import com.xiaoyv.dx.rop.type.Type;

/**
 * Constants of type {@code boolean}.
 */
public final class CstBoolean
        extends CstLiteral32 {
    /**
     * {@code non-null;} instance representing {@code false}
     */
    public static final CstBoolean VALUE_FALSE = new CstBoolean(false);

    /**
     * {@code non-null;} instance representing {@code true}
     */
    public static final CstBoolean VALUE_TRUE = new CstBoolean(true);

    /**
     * Makes an instance for the given value. This will return an
     * already-allocated instance.
     *
     * @param value the {@code boolean} value
     * @return {@code non-null;} the appropriate instance
     */
    public static CstBoolean make(boolean value) {
        return value ? VALUE_TRUE : VALUE_FALSE;
    }

    /**
     * Makes an instance for the given {@code int} value. This
     * will return an already-allocated instance.
     *
     * @param value must be either {@code 0} or {@code 1}
     * @return {@code non-null;} the appropriate instance
     */
    public static CstBoolean make(int value) {
        if (value == 0) {
            return VALUE_FALSE;
        } else if (value == 1) {
            return VALUE_TRUE;
        } else {
            throw new IllegalArgumentException("bogus value: " + value);
        }
    }

    /**
     * Constructs an instance. This constructor is private; use {@link #make}.
     *
     * @param value the {@code boolean} value
     */
    private CstBoolean(boolean value) {
        super(value ? 1 : 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getValue() ? "boolean{true}" : "boolean{false}";
    }

    /**
     * {@inheritDoc}
     */
    public Type getType() {
        return Type.BOOLEAN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String typeName() {
        return "boolean";
    }

    /**
     * {@inheritDoc}
     */
    public String toHuman() {
        return getValue() ? "true" : "false";
    }

    /**
     * Gets the {@code boolean} value.
     *
     * @return the value
     */
    public boolean getValue() {
        return (getIntBits() == 0) ? false : true;
    }
}
