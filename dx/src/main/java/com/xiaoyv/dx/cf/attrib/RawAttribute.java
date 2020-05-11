

package com.xiaoyv.dx.cf.attrib;

import com.xiaoyv.dx.rop.cst.ConstantPool;
import com.xiaoyv.dx.util.ByteArray;

/**
 * Raw attribute, for holding onto attributes that are unrecognized.
 */
public final class RawAttribute extends BaseAttribute {
    /**
     * {@code non-null;} attribute data
     */
    private final ByteArray data;

    /**
     * {@code null-ok;} constant pool to use for resolution of cpis in {@link
     * #data}
     */
    private final ConstantPool pool;

    /**
     * Constructs an instance.
     *
     * @param name {@code non-null;} attribute name
     * @param data {@code non-null;} attribute data
     * @param pool {@code null-ok;} constant pool to use for cpi resolution
     */
    public RawAttribute(String name, ByteArray data, ConstantPool pool) {
        super(name);

        if (data == null) {
            throw new NullPointerException("data == null");
        }

        this.data = data;
        this.pool = pool;
    }

    /**
     * Constructs an instance from a sub-array of a {@link ByteArray}.
     *
     * @param name   {@code non-null;} attribute name
     * @param data   {@code non-null;} array containing the attribute data
     * @param offset offset in {@code data} to the attribute data
     * @param length length of the attribute data, in bytes
     * @param pool   {@code null-ok;} constant pool to use for cpi resolution
     */
    public RawAttribute(String name, ByteArray data, int offset,
                        int length, ConstantPool pool) {
        this(name, data.slice(offset, offset + length), pool);
    }

    /**
     * Get the raw data of the attribute.
     *
     * @return {@code non-null;} the data
     */
    public ByteArray getData() {
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public int byteLength() {
        return data.size() + 6;
    }

    /**
     * Gets the constant pool to use for cpi resolution, if any. It
     * presumably came from the class file that this attribute came
     * from.
     *
     * @return {@code null-ok;} the constant pool
     */
    public ConstantPool getPool() {
        return pool;
    }
}
