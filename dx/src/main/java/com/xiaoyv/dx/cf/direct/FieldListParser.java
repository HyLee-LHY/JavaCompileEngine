

package com.xiaoyv.dx.cf.direct;

import com.xiaoyv.dx.cf.iface.AttributeList;
import com.xiaoyv.dx.cf.iface.Member;
import com.xiaoyv.dx.cf.iface.StdField;
import com.xiaoyv.dx.cf.iface.StdFieldList;
import com.xiaoyv.dx.rop.code.AccessFlags;
import com.xiaoyv.dx.rop.cst.CstNat;
import com.xiaoyv.dx.rop.cst.CstType;

/**
 * Parser for lists of fields in a class file.
 */
final /*package*/ class FieldListParser extends MemberListParser {
    /**
     * {@code non-null;} list in progress
     */
    private final StdFieldList fields;

    /**
     * Constructs an instance.
     *
     * @param cf               {@code non-null;} the class file to parse from
     * @param definer          {@code non-null;} class being defined
     * @param offset           offset in {@code bytes} to the start of the list
     * @param attributeFactory {@code non-null;} attribute factory to use
     */
    public FieldListParser(DirectClassFile cf, CstType definer, int offset,
                           AttributeFactory attributeFactory) {
        super(cf, definer, offset, attributeFactory);
        fields = new StdFieldList(getCount());
    }

    /**
     * Gets the parsed list.
     *
     * @return {@code non-null;} the parsed list
     */
    public StdFieldList getList() {
        parseIfNecessary();
        return fields;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String humanName() {
        return "field";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String humanAccessFlags(int accessFlags) {
        return AccessFlags.fieldString(accessFlags);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getAttributeContext() {
        return AttributeFactory.CTX_FIELD;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Member set(int n, int accessFlags, CstNat nat,
                         AttributeList attributes) {
        StdField field =
                new StdField(getDefiner(), accessFlags, nat, attributes);

        fields.set(n, field);
        return field;
    }
}
